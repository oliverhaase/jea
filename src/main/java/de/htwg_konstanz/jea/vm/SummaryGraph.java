package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class SummaryGraph {
	protected final Set<ObjectNode> objectNodes;
	protected final Set<FieldNode> fieldNodes;
	protected final Set<Pair<NonObjectNode, ObjectNode>> pointsToEdges;
	protected final Set<Pair<ObjectNode, FieldNode>> fieldEdges;

	public SummaryGraph() {
		objectNodes = new HashSet<>();
		fieldNodes = new HashSet<>();
		pointsToEdges = new HashSet<>();
		fieldEdges = new HashSet<>();
	}

	public SummaryGraph(Set<ObjectNode> objectNodes, Set<FieldNode> fieldNodes,
			Set<Pair<NonObjectNode, ObjectNode>> pointsToEdges,
			Set<Pair<ObjectNode, FieldNode>> fieldEdges) {
		this.objectNodes = objectNodes;
		this.fieldNodes = fieldNodes;
		this.pointsToEdges = pointsToEdges;
		this.fieldEdges = fieldEdges;
	}

	@Override
	public String toString() {
		return "SG[" + objectNodes + ", " + fieldNodes + ", " + pointsToEdges + ", " + fieldEdges
				+ "]";
	}

	public SummaryGraph merge(SummaryGraph other) {
		SummaryGraph result = new SummaryGraph();

		result.objectNodes.addAll(this.objectNodes);
		result.objectNodes.addAll(other.objectNodes);

		result.fieldNodes.addAll(this.fieldNodes);
		result.fieldNodes.addAll(other.fieldNodes);

		pointsToEdges.addAll(this.pointsToEdges);
		pointsToEdges.addAll(other.pointsToEdges);

		fieldEdges.addAll(this.fieldEdges);
		fieldEdges.addAll(other.fieldEdges);

		return result;
	}

}