package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import de.htwg_konstanz.jea.vm.Node.EscapeState;

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

		for (Triple<String, String, String> fieldEdge : fieldEdges)
			if (fieldEdge.getValue1().equals(origin.getId()))
				result.add(getObjectNode(fieldEdge.getValue3()));

		return result;
	}

	public Set<ObjectNode> getFieldOf(ObjectNode origin, String fieldName) {
		Set<ObjectNode> result = new HashSet<>();

		for (Triple<String, String, String> fieldEdge : fieldEdges)
			if (fieldEdge.getValue1().equals(origin.getId())
					&& fieldEdge.getValue2().equals(fieldName))
				result.add(getObjectNode(fieldEdge.getValue3()));

		return result;
	}

	protected Set<ObjectNode> propagateEscapeState(Set<ObjectNode> objects, EscapeState escapeState) {
		Set<ObjectNode> result = new HashSet<>();
		result.addAll(objects);

		Stack<ObjectNode> workingList = new Stack<>();
		for (ObjectNode objectNode : result)
			if (objectNode.getEscapeState() == escapeState)
				workingList.push(objectNode);

		while (!workingList.isEmpty()) {
			ObjectNode current = workingList.pop();

			for (ObjectNode subObject : getSubObjectsOf(current))
				if (subObject.getEscapeState().moreConfinedThan(escapeState)) {
					ObjectNode updatedSubObject = subObject.increaseEscapeState(escapeState);
					result.remove(subObject);
					result.add(updatedSubObject);
					workingList.push(updatedSubObject);
				}
		}
		return result;
	}

	@Override
	public String toString() {
		return "SG[" + objectNodes + ", " + fieldEdges + "]";
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