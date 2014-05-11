package de.htwg_konstanz.jea.vm;

import java.util.Set;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MethodSummary {
	private final ConnectionGraph cg;
	private final Set<ObjectNode> result;

	public MethodSummary(ConnectionGraph cg) {
		this.cg = new ConnectionGraph(cg);
		result = null;
	}

	public MethodSummary(ConnectionGraph cg, ReferenceNode returnValue) {
		this.cg = new ConnectionGraph(cg);
		result = cg.dereference(returnValue);
	}

	@Override
	public String toString() {
		return cg.toString() + ", " + result;
	}

}
