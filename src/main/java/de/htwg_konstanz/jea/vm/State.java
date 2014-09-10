package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

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

	private Set<String> mapsTo(String objectId, Heap summary, int consumeStack) {
		Set<String> result = new HashSet<>();

		// global object

		if (objectId.equals(GlobalObject.getInstance().getId())) {
			result.add(objectId);
			return result;
		}

		// internal object

		if (summary.getArgEscapeObjects().getObjectNode(objectId) instanceof InternalObject) {
			result.add(objectId);
			return result;
		}

		// phantomObject

		PhantomObject phantom = (PhantomObject) summary.getArgEscapeObjects().getObjectNode(
				objectId);

		if (phantom.getOrigin() == null) {
			for (ObjectNode mappingObj : heap.dereference((ReferenceNode) opStack
					.getArgumentAtIndex(phantom.getIndex(), consumeStack))) {
				result.add(mappingObj.getId());
			}
			return result;
		}

		if (phantom.getOrigin().isGlobal()) {
			result.add(GlobalObject.getInstance().getId());
			return result;
		}

		for (String mapsToId : mapsTo(phantom.getOrigin().getId(), summary, consumeStack)) {
			for (ObjectNode field : heap.getObjectNodes().getFieldOf(
					heap.getObjectNodes().getObjectNode(mapsToId), heap.getFieldEdges(),
					phantom.getField())) {
				result.add(field.getId());
			}
		}

		return result;
	}

	private Heap publishEscapedArgs(Heap summary, int consumeStack) {
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
				if (phantom.getOrigin() == null) {
					result = result.publish(opStack.getArgumentAtIndex(phantom.getIndex(),
							consumeStack));
				}
			}
		}
		return result;
	}

	// private Heap publishEscapedArgs(MethodSummary summary, OpStack
	// opStack,
	// Heap heap, int consumeStack) {
	// Heap result = heap;
	//
	// if (summary.isAlien()) {
	// for (int i = 0; i < consumeStack; i++) {
	// Slot arg = opStack.get(opStack.size() - consumeStack + i);
	// if (arg instanceof ReferenceNode)
	// result = result.publish((ReferenceNode) arg);
	// }
	// return result;
	// }
	//
	// for (ObjectNode object : summary.getEscapedObjects()) {
	// if (object instanceof PhantomObject) {
	// PhantomObject phantom = (PhantomObject) object;
	// if (phantom.getOrigin() == null) {
	// result = result.publish(opStack.getArgumentAtIndex(phantom.getIndex(),
	// consumeStack));
	// }
	// }
	// }
	// return result;
	// }

	private Heap transferInternalObjects(Heap heap, Heap summary) {
		Heap result = new Heap(heap);

		for (ObjectNode object : summary.getArgEscapeObjects())
			if (object instanceof InternalObject)
				result.getObjectNodes().add(((InternalObject) object).resetEscapeState());

		return result;
	}

	private Heap transferFieldEdges(Heap heap, Heap summary, int consumeStack) {
		Heap result = new Heap(heap);

		for (FieldEdge edge : summary.getFieldEdges()) {
			for (String originId : mapsTo(edge.getOriginId(), summary, consumeStack))
				for (String destinationId : mapsTo(edge.getDestinationId(), summary, consumeStack))
					result.getFieldEdges().add(
							new FieldEdge(originId, edge.getFieldName(), destinationId));
		}

		return result;
	}

	private Heap transferResult(Heap heap, Heap summary, ReferenceNode ref) {
		Heap result = new Heap(heap);

		result.getReferenceNodes().add(ref);

		if (summary.isAlien())
			result.getPointsToEdges().add(
					new Pair<ReferenceNode, String>(ref, GlobalObject.getInstance().getId()));
		else
			for (ObjectNode resultValue : summary.getResultValues())
				result.getPointsToEdges().add(
						new Pair<ReferenceNode, String>(ref, resultValue.getId()));

		return result;
	}

	public State applyMethodSummary(Heap summary, int consumeStack, int produceStack,
			org.apache.bcel.generic.Type returnType, int position) {

		OpStack resultOpStack;
		Heap resultHeap;

		resultHeap = publishEscapedArgs(summary, consumeStack);

		resultHeap = transferInternalObjects(resultHeap, summary);
		resultHeap = transferFieldEdges(resultHeap, summary, consumeStack);

		resultOpStack = opStack.pop(consumeStack);

		if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
			ReferenceNode ref = new ReferenceNode(position, Category.LOCAL);

			resultHeap = transferResult(resultHeap, summary, ref);
			resultOpStack = resultOpStack.push(ref);
		} else
			resultOpStack = resultOpStack.push(DontCareSlot.values()[produceStack], produceStack);

		return new State(localVars, resultOpStack, resultHeap);

	}

	// public State applyMethodSummary(MethodSummary summary, int consumeStack,
	// int produceStack,
	// org.apache.bcel.generic.Type returnType, int position) {
	//
	// OpStack opStack = this.opStack;
	// Heap heap = this.heap;
	//
	// heap = publishEscapedArgs(summary, opStack, heap, consumeStack);
	// heap = transferInternalObjects(heap, summary);
	// heap = transferFieldEdges(heap, summary, consumeStack);
	//
	// opStack = opStack.pop(consumeStack);
	//
	// if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
	// ReferenceNode ref = new ReferenceNode(position, Category.LOCAL);
	//
	// heap = transferResult(heap, summary, ref);
	// opStack = opStack.push(ref);
	// } else
	// opStack = opStack.push(DontCareSlot.values()[produceStack],
	// produceStack);
	//
	// return new State(localVars, opStack, heap);
	// }

	@Override
	public String toString() {
		return localVars + " || " + opStack + " || " + heap;
	}
}
