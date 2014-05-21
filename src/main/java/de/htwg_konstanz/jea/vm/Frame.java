package de.htwg_konstanz.jea.vm;

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

	public Frame applyMethodSummary(MethodSummary summary, int consumeStack, int produceStack,
			org.apache.bcel.generic.Type returnType) {
		if (summary.isAlien()) {
			OpStack opStack = this.opStack;
			ConnectionGraph cg = this.cg;

			for (int i = 0; i < consumeStack; i++) {
				Slot arg = opStack.peek();
				if (arg instanceof ReferenceNode) {
					Set<ObjectNode> objects = cg.dereference((ReferenceNode) arg);
					cg = cg.publish((ReferenceNode) arg);

				}
				opStack = opStack.pop();
			}

			if (returnType instanceof org.apache.bcel.generic.ReferenceType)
				opStack.push(cg.getGlobalReference());
			else
				opStack.push(DontCareSlot.values()[produceStack], produceStack);

			return new Frame(localVars, opStack, cg);
		}

		throw new AssertionError("not yet implemented");

	}

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg;
	}
}
