package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.vm.Node.EscapeState;

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

	private Set<ObjectNode> mapsTo(PhantomObject phantomObject, int consumeStack) {
		if (phantomObject.getOrigin() == null)
			return cg.dereference((ReferenceNode) opStack.get(opStack.size() - consumeStack
					+ phantomObject.getIndex()));

		Set<ObjectNode> result = new HashSet<>();

		if (phantomObject.getOrigin().isGlobal()) {
			result.add(GlobalObject.getInstance());
			return result;
		}

		for (ObjectNode obj : mapsTo((PhantomObject) phantomObject.getOrigin(), consumeStack))
			for (ObjectNode field : cg.getFieldOf(obj, phantomObject.getField()))
				result.add(field);

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
		ConnectionGraph cg = new ConnectionGraph(this.cg);

		// (1) transfer summary's internal objects to caller's connection graph,
		// setting escape state to NO_ESCAPE

		Set<ObjectNode> transferObjects = new HashSet<>();
		for (ObjectNode obj : summary.getSg().getObjectNodes())
			if (obj instanceof InternalObject && obj.getEscapeState() == EscapeState.ARG_ESCAPE)
				transferObjects.add(((InternalObject) obj).resetEscapeState());

		cg.objectNodes.addAll(transferObjects);

		// (2) for each field edge in summary that involve any of the
		// transferred
		// internal objects, add a corresponding edge to caller's connection
		// graph, replacing
		// the involved phantom object - if there is one - with all mapsTo
		// objects.
		// (3) take care of return value(s)
		// (4) propagate escape state
		// (5) adapt opStack
		// (6) make actual param global as need be

		for (int i = consumeStack - 1; i >= 0; i--) {
			Slot arg = opStack.peek();
			if (arg instanceof ReferenceNode) {
				ObjectNode formalArg = summary.getSg().getObjectNode("p" + i);
				for (ObjectNode actualArg : cg.dereference((ReferenceNode) arg)) {
					// map formal and actual args onto each other
					if (formalArg.getEscapeState() == EscapeState.GLOBAL_ESCAPE)
						actualArg.increaseEscapeState(EscapeState.GLOBAL_ESCAPE);
				}
			}
			opStack = opStack.pop();
		}

		if (returnType instanceof org.apache.bcel.generic.ReferenceType)
			opStack.push(cg.getGlobalReference());
		else
			opStack.push(DontCareSlot.values()[produceStack], produceStack);

		return new Frame(localVars, opStack, cg);
	}

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg;
	}
}
