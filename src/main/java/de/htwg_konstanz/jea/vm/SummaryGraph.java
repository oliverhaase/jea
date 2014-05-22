package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class SummaryGraph {
	protected final Set<ObjectNode> objectNodes;
	protected final Set<FieldNode> fieldNodes;
	protected final Set<Pair<NonObjectNode, String>> pointsToEdges;
	protected final Set<Pair<String, FieldNode>> fieldEdges;

	public SummaryGraph() {
		objectNodes = new HashSet<>();
		fieldNodes = new HashSet<>();
		pointsToEdges = new HashSet<>();
		fieldEdges = new HashSet<>();
	}

	public SummaryGraph(Set<ObjectNode> objectNodes, Set<FieldNode> fieldNodes,
			Set<Pair<NonObjectNode, String>> pointsToEdges, Set<Pair<String, FieldNode>> fieldEdges) {
		this.objectNodes = objectNodes;
		this.fieldNodes = fieldNodes;
		this.pointsToEdges = pointsToEdges;
		this.fieldEdges = fieldEdges;
	}

	public ObjectNode getObjectNode(String id) {
		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getId().equals(id))
				return objectNode;
		throw new AssertionError("invalid object id: " + id);
	}

	public boolean existsObject(String id) {
		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getId().equals(id))
				return true;
		return false;
	}

	public Set<ObjectNode> getSubObjectsOf(ObjectNode origin) {
		Set<ObjectNode> result = new HashSet<>();

		for (Pair<String, FieldNode> fieldEdge : fieldEdges)
			if (fieldEdge.getValue1().equals(origin.getId()))
				for (Pair<NonObjectNode, String> pointsToEdge : pointsToEdges)
					if (pointsToEdge.getValue1().equals(fieldEdge.getValue2()))
						result.add(getObjectNode(pointsToEdge.getValue2()));

		return result;
	}

	@Override
	public String toString() {
		return "SG[" + objectNodes + ", " + fieldNodes + ", " + pointsToEdges + ", " + fieldEdges
				+ "]";
	}

	public SummaryGraph merge(SummaryGraph other) {
		SummaryGraph result = new SummaryGraph();

		for (ObjectNode oneObject : this.objectNodes) {
			boolean found = false;
			for (ObjectNode otherObject : other.objectNodes) {
				if (oneObject.equals(otherObject)) {
					result.objectNodes.add(oneObject.getEscapeState().moreConfinedThan(
							otherObject.getEscapeState()) ? otherObject : oneObject);
					found = true;
				}
			}
			if (!found)
				result.objectNodes.add(oneObject);
		}

		result.objectNodes.addAll(other.objectNodes);

		result.fieldNodes.addAll(this.fieldNodes);
		result.fieldNodes.addAll(other.fieldNodes);

		result.pointsToEdges.addAll(this.pointsToEdges);
		result.pointsToEdges.addAll(other.pointsToEdges);

		result.fieldEdges.addAll(this.fieldEdges);
		result.fieldEdges.addAll(other.fieldEdges);

		return result;
	}
}