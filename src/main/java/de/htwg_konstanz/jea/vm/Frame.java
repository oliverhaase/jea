package de.htwg_konstanz.jea.vm;

import java.util.Set;

public final class Frame {
	private final LocalVars localVars;
	private final OpStack opStack;
	private final ConnectionGraph cg;

	public Frame(LocalVars localVars, OpStack opStack, ConnectionGraph cg) {
		if (localVars == null || opStack == null || cg == null)
			throw new NullPointerException("params to Frame.<init> must not be null");
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

	// public Frame(int maxLocals, OpStack opStack, int numArgSlots) {
	// Slot[] vars = new Slot[maxLocals];
	//
	// for (int i = numArgSlots - 1; i >= 0; i--) {
	// vars[i] = opStack.peek();
	// opStack = opStack.pop();
	// }
	//
	// localVars = new LocalVars(vars);
	// this.opStack = new OpStack();
	// }

	public OpStack getOpStack() {
		return opStack;
	}

	public LocalVars getLocalVars() {
		return localVars;
	}

	public ConnectionGraph getCG() {
		return cg;
	}

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + localVars.hashCode();
		result = prime * result + opStack.hashCode();
		result = prime * result + cg.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Frame))
			return false;
		Frame other = (Frame) obj;

		return localVars.equals(other.localVars) && opStack.equals(other.opStack)
				& cg.equals(other.cg);

	}

}
