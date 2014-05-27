package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class SummaryGraph {
	@Getter
	protected final Set<ObjectNode> objectNodes;
	protected final Set<Triple<String, String, String>> fieldEdges;

	public SummaryGraph() {
		objectNodes = new HashSet<>();
		fieldEdges = new HashSet<>();
	}

	public SummaryGraph(Set<ObjectNode> objectNodes, Set<Triple<String, String, String>> fieldEdges) {
		this.objectNodes = objectNodes;
		this.fieldEdges = fieldEdges;
	}

	@Override
	public String toString() {
		return "SG[" + objectNodes + ", " + fieldEdges + "]";
	}

	public ObjectNode getObjectNode(String id) {
		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getId().equals(id))
				return objectNode;
		throw new AssertionError("invalid object id: " + id);
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

		result.fieldEdges.addAll(this.fieldEdges);
		result.fieldEdges.addAll(other.fieldEdges);

		return result;
	}
}