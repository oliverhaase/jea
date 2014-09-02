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
	private final ConnectionGraph cg;

	public State(@NonNull LocalVars localVars, @NonNull OpStack opStack, @NonNull ConnectionGraph cg) {
		this.localVars = localVars;
		this.opStack = opStack;
		this.cg = cg;
	}

	public State(ConnectionGraph cg, Set<Integer> indexes, int maxLocals) {
		this.cg = cg;
		opStack = new OpStack();

		Slot[] vars = new Slot[maxLocals];

		// initialize local vars
		for (int i = 0; i < vars.length; i++) {
			vars[i] = DontCareSlot.NORMAL_SLOT;
		}

		for (Integer index : indexes)
			vars[index] = cg.getRefToPhantomObject(index);

		localVars = new LocalVars(vars);
	}

	private Set<String> mapsTo(String objectId, ConnectionGraph summary, int consumeStack) {
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
			for (ObjectNode mappingObj : cg.dereference((ReferenceNode) opStack.getArgumentAtIndex(
					phantom.getIndex(), consumeStack))) {
				result.add(mappingObj.getId());
			}
			return result;
		}

		if (phantom.getOrigin().isGlobal()) {
			result.add(GlobalObject.getInstance().getId());
			return result;
		}

		for (String mapsToId : mapsTo(phantom.getOrigin().getId(), summary, consumeStack)) {
			for (ObjectNode field : cg.getObjectNodes().getFieldOf(
					cg.getObjectNodes().getObjectNode(mapsToId), cg.getFieldEdges(),
					phantom.getField())) {
				result.add(field.getId());
			}
		}

		return result;
	}

	private ConnectionGraph publishEscapedArgs(ConnectionGraph summary, int consumeStack) {
		ConnectionGraph result = cg;

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

	// private ConnectionGraph publishEscapedArgs(MethodSummary summary, OpStack
	// opStack,
	// ConnectionGraph cg, int consumeStack) {
	// ConnectionGraph result = cg;
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

	private ConnectionGraph transferInternalObjects(ConnectionGraph cg, ConnectionGraph summary) {
		ConnectionGraph result = new ConnectionGraph(cg);

		for (ObjectNode object : summary.getArgEscapeObjects())
			if (object instanceof InternalObject)
				result.getObjectNodes().add(((InternalObject) object).resetEscapeState());

		return result;
	}

	private ConnectionGraph transferFieldEdges(ConnectionGraph cg, ConnectionGraph summary,
			int consumeStack) {
		ConnectionGraph result = new ConnectionGraph(cg);

		for (FieldEdge edge : summary.getFieldEdges()) {
			for (String originId : mapsTo(edge.getOriginId(), summary, consumeStack))
				for (String destinationId : mapsTo(edge.getDestinationId(), summary, consumeStack))
					result.getFieldEdges().add(
							new FieldEdge(originId, edge.getFieldName(), destinationId));
		}

		return result;
	}

	private ConnectionGraph transferResult(ConnectionGraph cg, ConnectionGraph summary,
			ReferenceNode ref) {
		ConnectionGraph result = new ConnectionGraph(cg);

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

	public State applyMethodSummary(ConnectionGraph summary, int consumeStack, int produceStack,
			org.apache.bcel.generic.Type returnType, int position) {

		OpStack resultOpStack;
		ConnectionGraph resultCg;

		resultCg = publishEscapedArgs(summary, consumeStack);

		resultCg = transferInternalObjects(resultCg, summary);
		resultCg = transferFieldEdges(resultCg, summary, consumeStack);

		resultOpStack = opStack.pop(consumeStack);

		if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
			ReferenceNode ref = new ReferenceNode(position, Category.LOCAL);

			resultCg = transferResult(resultCg, summary, ref);
			resultOpStack = resultOpStack.push(ref);
		} else
			resultOpStack = resultOpStack.push(DontCareSlot.values()[produceStack], produceStack);

		return new State(localVars, resultOpStack, resultCg);

	}

	// public State applyMethodSummary(MethodSummary summary, int consumeStack,
	// int produceStack,
	// org.apache.bcel.generic.Type returnType, int position) {
	//
	// OpStack opStack = this.opStack;
	// ConnectionGraph cg = this.cg;
	//
	// cg = publishEscapedArgs(summary, opStack, cg, consumeStack);
	// cg = transferInternalObjects(cg, summary);
	// cg = transferFieldEdges(cg, summary, consumeStack);
	//
	// opStack = opStack.pop(consumeStack);
	//
	// if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
	// ReferenceNode ref = new ReferenceNode(position, Category.LOCAL);
	//
	// cg = transferResult(cg, summary, ref);
	// opStack = opStack.push(ref);
	// } else
	// opStack = opStack.push(DontCareSlot.values()[produceStack],
	// produceStack);
	//
	// return new State(localVars, opStack, cg);
	// }

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg;
	}
}
