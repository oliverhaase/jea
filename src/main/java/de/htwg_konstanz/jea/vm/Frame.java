package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public final class Frame {
	@Getter
	private final LocalVars localVars;
	@Getter
	private final OpStack opStack;
	@Getter
	private final ConnectionGraph cg;

	public Frame(@NonNull LocalVars localVars, @NonNull OpStack opStack, @NonNull ConnectionGraph cg) {
		this.localVars = localVars;
		this.opStack = opStack;
		this.cg = cg;
	}

	public Frame(Set<Integer> indexes, int maxLocals) {
		Slot[] vars = new Slot[maxLocals];

		// initialize local vars
		for (int i = 0; i < vars.length; i++) {
			vars[i] = DontCareSlot.NORMAL_SLOT;
		}

		// have ConnectionGraph overwrite reference local vars
		cg = new ConnectionGraph(indexes, vars);

		localVars = new LocalVars(vars);
		opStack = new OpStack();
	}

	private Set<String> mapsTo(String objectId, MethodSummary summary, int consumeStack) {
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

	private ConnectionGraph publishEscapedArgs(MethodSummary summary, OpStack opStack,
			ConnectionGraph cg, int consumeStack) {
		ConnectionGraph result = cg;

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

	private ConnectionGraph transferInternalObjects(ConnectionGraph cg, MethodSummary summary) {
		ConnectionGraph result = new ConnectionGraph(cg);

		for (ObjectNode object : summary.getArgEscapeObjects())
			if (object instanceof InternalObject)
				result.getObjectNodes().add(((InternalObject) object).resetEscapeState());

		return result;
	}

	private ConnectionGraph transferFieldEdges(ConnectionGraph cg, MethodSummary summary,
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

	private ConnectionGraph transferResult(ConnectionGraph cg, MethodSummary summary) {
		ConnectionGraph result = new ConnectionGraph(cg);
		result.getReferenceNodes().add(summary.getResultReference());
		result.getPointsToEdges().addAll(summary.getResultPointsToEdges());
		return result;
	}

	public Frame applyMethodSummary(MethodSummary summary, int consumeStack, int produceStack,
			org.apache.bcel.generic.Type returnType) {
		if (summary.isAlien()) {
			OpStack opStack = this.opStack;
			ConnectionGraph cg = this.cg;
			for (int i = 0; i < consumeStack; i++) {
				Slot arg = opStack.peek();
				if (arg instanceof ReferenceNode)
					cg = cg.publish((ReferenceNode) arg);

				opStack = opStack.pop();
			}
			if (returnType instanceof org.apache.bcel.generic.ReferenceType)
				opStack.push(cg.getGlobalReference());
			else
				opStack.push(DontCareSlot.values()[produceStack], produceStack);

			return new Frame(localVars, opStack, cg);
		}

		OpStack opStack = this.opStack;
		ConnectionGraph cg = this.cg;

		cg = publishEscapedArgs(summary, opStack, cg, consumeStack);
		cg = transferInternalObjects(cg, summary);
		cg = transferFieldEdges(cg, summary, consumeStack);

		opStack = opStack.pop(consumeStack);

		if (returnType instanceof org.apache.bcel.generic.ReferenceType) {
			cg = transferResult(cg, summary);
			opStack.push(summary.getResultReference());
		} else
			opStack.push(DontCareSlot.values()[produceStack], produceStack);

		return new Frame(localVars, opStack, cg);

	}

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg;
	}
}
