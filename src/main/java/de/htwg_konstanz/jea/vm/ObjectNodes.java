package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ObjectNodes implements Iterable<ObjectNode> {
	private final Set<ObjectNode> nodes;

	public ObjectNodes() {
		nodes = new HashSet<ObjectNode>();
	}

	public void add(ObjectNode node) {
		nodes.add(node);
	}

	public void addAll(ObjectNodes other) {
		nodes.addAll(other.nodes);
	}

	public void remove(ObjectNode node) {
		nodes.remove(node);
	}

	public boolean existsObject(String id) {
		for (ObjectNode objectNode : nodes)
			if (objectNode.getId().equals(id))
				return true;
		return false;
	}

	public ObjectNode getObjectNode(String id) {
		for (ObjectNode objectNode : nodes)
			if (objectNode.getId().equals(id))
				return objectNode;
		throw new AssertionError("invalid object id: " + id + ", not in " + nodes);
	}

	public ObjectNodes getSubObjectsOf(ObjectNode origin, Set<FieldEdge> fieldEdges) {
		ObjectNodes result = new ObjectNodes();

		for (FieldEdge fieldEdge : fieldEdges)
			if (fieldEdge.getOriginId().equals(origin.getId()))
				result.add(getObjectNode(fieldEdge.getDestinationId()));

		return result;
	}

	public Set<ObjectNode> getFieldOf(ObjectNode origin, Set<FieldEdge> fieldEdges, String fieldName) {
		Set<ObjectNode> result = new HashSet<ObjectNode>();

		for (FieldEdge fieldEdge : fieldEdges)
			if (fieldEdge.getOriginId().equals(origin.getId())
					&& fieldEdge.getFieldName().equals(fieldName))
				result.add(getObjectNode(fieldEdge.getDestinationId()));

		return result;
	}

	@Override
	public Iterator<ObjectNode> iterator() {
		return nodes.iterator();
	}

	@Override
	public String toString() {
		return nodes.toString();
	}

}
