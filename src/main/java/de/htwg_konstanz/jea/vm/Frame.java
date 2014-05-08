package de.htwg_konstanz.jea.vm;

import java.util.Set;

public final class Frame {
	private final LocalVars localVars;
	private final OpStack opStack;
	private final ConnectionGraph cg;
	private Slot returnValue;

	public Frame(LocalVars localVars, OpStack opStack, ConnectionGraph cg, Slot returnValue) {
		if (localVars == null || opStack == null || cg == null)
			throw new NullPointerException("params to Frame.<init> must not be null");
		this.localVars = localVars;
		this.opStack = opStack;
		this.cg = cg;
		this.returnValue = returnValue;
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

	public OpStack getOpStack() {
		return opStack;
	}

	public LocalVars getLocalVars() {
		return localVars;
	}

	public ConnectionGraph getCG() {
		return cg;
	}

	public Slot getReturnValue() {
		return returnValue;
	}

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg
				+ ((returnValue != null) ? (", " + returnValue) : "");
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
