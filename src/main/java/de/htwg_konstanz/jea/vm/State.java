package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode
public final class State {
	@Getter
	private final LocalVars localVars;
	@Getter
	private final OpStack opStack;
	@Getter
	private final Heap heap;

	public State(@NonNull LocalVars localVars, @NonNull OpStack opStack, @NonNull Heap heap) {
		this.localVars = localVars;
		this.opStack = opStack;
		this.heap = heap;
	}

	public State(Heap heap, Set<Integer> indexes, int maxLocals) {
		this.heap = heap;
		opStack = new OpStack();

		Slot[] vars = new Slot[maxLocals];

		// initialize local vars
		for (int i = 0; i < vars.length; i++) {
			vars[i] = DontCareSlot.NORMAL_SLOT;
		}

		for (Integer index : indexes)
			vars[index] = heap.getRefToPhantomObject(index);

		localVars = new LocalVars(vars);
	}

	/**
	 * Returns the corresponding ObjectNodes to the {@code objectNode} in this
	 * states Heap.
	 * 
	 * @param objectNode
	 *            the ObjectNode to search the corresponding ObjectNodes for
	 * @param consumeStack
	 *            how much elements the method containing the {@code objectNode}
	 *            consumes (to locate the corresponding argument on this states
	 *            OpStack)
	 * 
	 * @return a Set of ObjectNode representing the {@code objectNode} in this
	 *         states Heap
	 */
	private Set<ObjectNode> mapsToObjects(ObjectNode objectNode, int consumeStack) {
		Set<ObjectNode> result = new HashSet<>();

		// objectNode is global object

		if (objectNode.equals(GlobalObject.getInstance())) {
			result.add(objectNode);
			return result;
		}

		// objectNode is internal object

		if (objectNode instanceof InternalObject) {
			result.add(objectNode);
			return result;
		}

		// objectNode is a Parameter (phantomObject) -> get corresponding
		// arguments in this Heap

		PhantomObject phantom = (PhantomObject) objectNode;

		if (!phantom.isSubPhantom()) {
			for (ObjectNode mappingObj : heap.dereference((ReferenceNode) opStack
					.getArgumentAtIndex(phantom.getIndex(), consumeStack))) {
				result.add(mappingObj);
			}
			return result;
		}

		// objectNode is Child of Global Parameter

		if (phantom.getParent().isGlobal()) {
			result.add(GlobalObject.getInstance());
			return result;
		}

		// objectNode is Child of Parameter

		for (ObjectNode parent : mapsToObjects(phantom.getParent(), consumeStack)) {
			for (ObjectNode field : heap.getFieldOf(parent, phantom.getFieldName())) {
				result.add(field);
			}
		}

		return result;
	}

	/**
	 * If a parameter in {@code summary} is escaped the corresponding argument
	 * in this heap is published. If {@code summary} represents an alien method
	 * all reference parameters are published.
	 * 
	 * @param heap
	 *            the Heap to publish
	 * @param summary
	 *            the Heap with the side effects of the method to apply to the
	 *            {@code heap}
	 * @param consumeStack
	 *            how many parameters the method of {@code summary} has. To
	 *            locate the corresponding objects on the Stack
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	private Heap publishEscapedArgs(Heap heap, Heap summary, int consumeStack) {
		Heap result = heap;

		if (summary.isAlien()) {
			for (int i = 0; i < consumeStack; i++) {
				Slot arg = opStack.get(opStack.size() - consumeStack + i);
				if (arg instanceof ReferenceNode)
					result = result.publish((ReferenceNode) arg);
			}
			return result;
		}

		for (ObjectNode object : summary.getEscapedObjects()) {
			if (object instanceof PhantomObject) {
				PhantomObject phantom = (PhantomObject) object;
				if (!phantom.isSubPhantom()) {
					result = result.publish(opStack.getArgumentAtIndex(phantom.getIndex(),
							consumeStack));
				}
			}
		}
		return result;
	}

	/**
	 * Adds the fieldEdges from the summary and replaces the objects of the
	 * summary with the corresponding objects in this states heap.
	 * 
	 * @param heap
	 *            the Heap to transfer the edges to
	 * @param summary
	 *            the Heap to transfer the edges from
	 * @param consumeStack
	 *            how many parameters the method of {@code summary} has. To
	 *            locate the corresponding objects on the Stack
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	private Heap transferFieldEdges(Heap heap, Heap summary, int consumeStack) {
		Heap result = new Heap(heap);

		for (FieldEdge edge : summary.getFieldEdges()) {
			ObjectNode originOfEdge = summary.getArgEscapeObjects().getObjectNode(
					edge.getOriginId());
			ObjectNode destinationOfEdge = summary.getArgEscapeObjects().getObjectNode(
					edge.getDestinationId());

			for (ObjectNode origin : mapsToObjects(originOfEdge, consumeStack))
				for (ObjectNode destination : mapsToObjects(destinationOfEdge, consumeStack))
					result = result.addField(origin, edge.getFieldName(), destination);
		}

		return result;
	}

	/**
	 * Applies the MetohdSummary {@code summary} to this state, so the effect of
	 * the method to the heap and the stack are applied to this state. Publishes
	 * the arguments that escape in the method. Adds the objects that are linked
	 * to the arguments. Adds the fields of these objects.
	 * 
	 * @param summary
	 *            the Heap which represents the side effects of the method
	 * @param consumeStack
	 *            how much elements the method consumes from the stack
	 * @param produceStack
	 *            how much elements the method produces for the stack
	 * @param returnType
	 *            whether the return value is a reference or a basic Type
	 * @param position
	 *            the position of the instruction in the current method to
	 *            create a unique id for the return object
	 * @return the resulting State
	 */
	@CheckReturnValue
	public State applyMethodSummary(Heap summary, int consumeStack, int produceStack,
			org.apache.bcel.generic.Type returnType, int position) {

		OpStack resultOpStack = opStack;
		Heap resultHeap = heap;

		resultHeap = publishEscapedArgs(resultHeap, summary, consumeStack);

		resultHeap = resultHeap.transferInternalObjectsFrom(summary);
		resultHeap = transferFieldEdges(resultHeap, summary, consumeStack);

		resultOpStack = resultOpStack.pop(consumeStack);

		if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
			ReferenceNode resultRef = new ReferenceNode(position, Category.LOCAL);

			resultHeap = resultHeap.transferResultFrom(summary, resultRef);
			resultOpStack = resultOpStack.push(resultRef);
		} else
			resultOpStack = resultOpStack.push(DontCareSlot.values()[produceStack], produceStack);

		return new State(localVars, resultOpStack, resultHeap);

	}

	@Override
	public String toString() {
		return localVars + " || " + opStack + " || " + heap;
	}
}
