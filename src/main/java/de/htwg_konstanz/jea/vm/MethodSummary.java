package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MethodSummary {
	private final SummaryGraph sg;
	private final Set<ObjectNode> result;

	public MethodSummary() {
		sg = new SummaryGraph();
		result = new HashSet<ObjectNode>();
	}

	public MethodSummary(SummaryGraph sg) {
		this.sg = sg;
		result = new HashSet<ObjectNode>();
	}

	public MethodSummary(SummaryGraph sg, Set<ObjectNode> result) {
		this.sg = sg;
		this.result = result;
	}

	public MethodSummary merge(MethodSummary other) {
		Set<ObjectNode> mergedResult = new HashSet<>();
		mergedResult.addAll(this.result);
		mergedResult.addAll(other.result);

		return new MethodSummary(sg.merge(other.sg), mergedResult);
	}

	@Override
	public String toString() {
		return sg.toString() + ", " + result;
	}

}
