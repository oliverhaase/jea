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
	 * @param summary
	 *            the Heap to map from
	 * @param position
	 *            the position of the instruction in the current method to
	 *            create a unique ids
	 * 
	 * @return a Set of ObjectNode representing the {@code objectNode} in this
	 *         states Heap. If the Set is empty, there is no matching object,
	 *         which means that the paramter or the field of a parameter was
	 *         null
	 */
	private Set<ObjectNode> mapsToObjects(Heap resultHeap, OpStack resultOpStack,
			ObjectNode objectNode, int consumeStack, Heap summary, int position) {
		Set<ObjectNode> result = new HashSet<>();

		// objectNode is global object

		if (objectNode.equals(GlobalObject.getInstance())) {
			result.add(objectNode);
			return result;
		}

		// objectNode is internal object

		if (objectNode instanceof InternalObject) {
			result.add(resultHeap.getObjectNodes().getObjectNode(
					objectNode.getId() + "|" + position));
			return result;
		}

		// objectNode is a Parameter (phantomObject) -> get corresponding
		// arguments in this Heap

		PhantomObject phantom = (PhantomObject) objectNode;

		// objectNode is TopParameter
		if (!phantom.isSubPhantom()) {
			for (ObjectNode mappingObj : resultHeap.dereference((ReferenceNode) resultOpStack
					.getArgumentAtIndex(phantom.getIndex(), consumeStack))) {
				result.add(mappingObj);
			}
			return result;
		}

		// objectNode is Child of Parameter
		for (ObjectNode parent : mapsToObjects(resultHeap, resultOpStack, summary.getObjectNodes()
				.getObjectNode(phantom.getParent()), consumeStack, summary, position)) {
			for (ObjectNode field : resultHeap.getFieldOf(parent, phantom.getFieldName())) {
				result.add(field);
			}
			return result;
		}

		// objectNode is Child of Global Parameter
		if (resultHeap.getObjectNodes().getObjectNode(phantom.getParent()).isGlobal()) {
			result.add(GlobalObject.getInstance());
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
	 * @param position
	 *            the position of the instruction in the current method to
	 *            create a unique ids
	 * @param stack
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	private Heap transferFieldEdges(Heap heap, OpStack stack, Heap summary, int consumeStack,
			int position) {
		Heap result = new Heap(heap);

		for (FieldEdge edge : summary.getFieldEdges()) {
			ObjectNode originOfEdge = summary.getArgEscapeObjects().getObjectNode(
					edge.getOriginId());
			ObjectNode destinationOfEdge = summary.getArgEscapeObjects().getObjectNode(
					edge.getDestinationId());

			for (ObjectNode origin : mapsToObjects(heap, stack, originOfEdge, consumeStack,
					summary, position))
				for (ObjectNode destination : mapsToObjects(heap, stack, destinationOfEdge,
						consumeStack, summary, position))
					result = result.addField(origin, edge.getFieldName(), destination);
		}

		return result;
	}

	/**
	 * Applies the MetohdSummary {@code summary} to this state, so the effect of
	 * the method to the heap and the stack are applied to this state. Publishes
	 * the arguments that escape in the method. Adds the objects that are linked
	 * to the arguments. Adds the fields of these objects. Adds the local and
	 * escaped objects to the corresponding set in this heap.
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
	 *            create a unique ids
	 * @return the resulting State
	 */
	@CheckReturnValue
	public State applyMethodSummary(Heap summary, int consumeStack, int produceStack,
			org.apache.bcel.generic.Type returnType, int position) {

		OpStack resultOpStack = opStack;
		Heap resultHeap = heap;

		resultHeap = publishEscapedArgs(resultHeap, summary, consumeStack);

		resultHeap = resultHeap.transferInternalObjectsFrom(summary, position);
		resultHeap = transferFieldEdges(resultHeap, resultOpStack, summary, consumeStack, position);

		resultOpStack = resultOpStack.pop(consumeStack);

		if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
			ReferenceNode resultRef = new ReferenceNode(position, Category.LOCAL);

			resultHeap = resultHeap.transferResultFrom(summary, resultRef);
			resultOpStack = resultOpStack.push(resultRef);
		} else
			resultOpStack = resultOpStack.push(DontCareSlot.values()[produceStack], produceStack);

		 resultHeap.getEscapedObjects().addAll(summary.getEscapedObjects());
		 resultHeap.getLocalObjects().addAll(summary.getLocalObjects());

		return new State(localVars, resultOpStack, resultHeap);

	}

	@Override
	public String toString() {
		return localVars + " || " + opStack + " || " + heap;
	}
}
