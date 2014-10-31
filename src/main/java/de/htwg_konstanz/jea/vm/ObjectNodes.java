package de.htwg_konstanz.jea.vm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import de.htwg_konstanz.jea.vm.Node.EscapeState;

@EqualsAndHashCode
public class ObjectNodes implements Iterable<ObjectNode> {
	private final Map<String, ObjectNode> nodes;

	public ObjectNodes() {
		nodes = new HashMap<String, ObjectNode>();
	}

	/**
	 * Adds the specified element to this set if it is not already present
	 * (optional operation).
	 * 
	 * @param node
	 *            the ObjectNode to add
	 */
	public void add(ObjectNode node) {
		nodes.put(node.getId(), node);
	}

	public void addAll(ObjectNodes other) {
		nodes.putAll(other.nodes);
	}

	public void remove(ObjectNode node) {
		nodes.remove(node.getId());
	}

	public boolean existsObject(String id) {
		return nodes.containsKey(id);
	}

	public ObjectNode getObjectNode(String id) {
		ObjectNode node = nodes.get(id);
		if (node == null)
			throw new AssertionError("invalid object id: " + id + ", not in " + nodes);
		return node;
	}

	/**
	 * Increases the escapeState of the {@code object} to the
	 * {@code escapeState}. If the escapeState is less confined nothing is done
	 * and null is returned.
	 * 
	 * @param object
	 *            the object to increase
	 * @param escapeState
	 *            the new escape state
	 * @return the object with the increased escapeState
	 */
	public ObjectNode increaseEscapeState(ObjectNode object, EscapeState escapeState) {
		ObjectNode increasedObject = null;

		if (object.getEscapeState().moreConfinedThan(escapeState)) {
			increasedObject = object.increaseEscapeState(escapeState);
			remove(object);
			add(increasedObject);
		}

		return increasedObject;
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

		if (origin.isGlobal()) {
			result.add(origin);
			return result;
		}

		for (FieldEdge fieldEdge : fieldEdges)
			if (fieldEdge.getOriginId().equals(origin.getId())
					&& fieldEdge.getFieldName().equals(fieldName))
				result.add(getObjectNode(fieldEdge.getDestinationId()));

		return result;
	}

	@Override
	public Iterator<ObjectNode> iterator() {
		return nodes.values().iterator();
	}

	@Override
	public String toString() {
		return nodes.values().toString();
	}

	public Set<String> getAllObjectIds() {
		return nodes.keySet();
	}

}
