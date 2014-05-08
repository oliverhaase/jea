package de.htwg_konstanz.jea.vm;

public class MethodSummary {
	private final ConnectionGraph cg;

	public MethodSummary(Frame frame) {
		cg = frame.getCG().removeReferenceNodesExcept(frame.getReturnValue());
	}

	@Override
	public String toString() {
		return cg.toString();
	}

}
