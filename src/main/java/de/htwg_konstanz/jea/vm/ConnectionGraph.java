package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

public final class ConnectionGraph {
	private final Set<ObjectNode> objectNodes = new HashSet<ObjectNode>();
	private final Set<ReferenceNode> referenceNodes = new HashSet<ReferenceNode>();
	private final Set<FieldNode> nonStaticFieldNodes = new HashSet<FieldNode>();
	private final Set<FieldNode> staticFieldNodes = new HashSet<FieldNode>();

	private final Set<Pair<NonObjectNode, ObjectNode>> pointsToEdges = new HashSet<Pair<NonObjectNode, ObjectNode>>();
	private final Set<Pair<ObjectNode, FieldNode>> fieldEdges = new HashSet<Pair<ObjectNode, FieldNode>>();

	// public ConnectionGraph() {
	// }

	public ConnectionGraph(Set<Integer> indexes, Slot[] vars) {
		for (Integer index : indexes) {
			vars[index] = mutableAddObjectNode(index, true);
			// ReferenceNode arg = new ReferenceNode(index);
			// referenceNodes.add(arg);
			// ObjectNode phantom = new ObjectNode(index, true);
			// objectNodes.add(phantom);
			// pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(arg,
			// phantom));
			// vars[index] = arg;
		}
	}

	public ConnectionGraph(ConnectionGraph original) {
		objectNodes.addAll(original.objectNodes);
		referenceNodes.addAll(original.referenceNodes);
		nonStaticFieldNodes.addAll(original.nonStaticFieldNodes);
		staticFieldNodes.addAll(original.staticFieldNodes);

		pointsToEdges.addAll(original.pointsToEdges);
		fieldEdges.addAll(original.fieldEdges);
	}

	public Set<ObjectNode> dereference(NonObjectNode ref) {
		Set<ObjectNode> result = new HashSet<ObjectNode>();
		for (Pair<NonObjectNode, ObjectNode> pointsToEdge : pointsToEdges)
			if (pointsToEdge.getValue1().equals(ref))
				result.add(pointsToEdge.getValue2());
		return result;
	}

	public FieldNode getFieldNode(ObjectNode obj, String fieldName) {
		for (Pair<ObjectNode, FieldNode> fieldEdge : fieldEdges)
			if (fieldEdge.getValue1().equals(obj)
					&& fieldEdge.getValue2().getName().equals(fieldName))
				return fieldEdge.getValue2();

		return null;

	}

	public ConnectionGraph addField(ObjectNode obj, String fieldName, ObjectNode value) {
		ConnectionGraph result = new ConnectionGraph(this);

		FieldNode fieldNode = getFieldNode(obj, fieldName);

		if (fieldNode == null) {
			fieldNode = new FieldNode(fieldName, obj.getID());
			result.nonStaticFieldNodes.add(fieldNode);
			result.fieldEdges.add(new Pair<ObjectNode, FieldNode>(obj, fieldNode));
		}

		result.pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(fieldNode, value));

		return result;
	}

	public ReferenceNode mutableAddObjectNode(int id, boolean isPhantom) {
		ObjectNode obj = new ObjectNode(id, isPhantom);
		objectNodes.add(obj);

		ReferenceNode ref = new ReferenceNode(id, isPhantom ? Category.ARG : Category.LOCAL);
		referenceNodes.add(ref);

		pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(ref, obj));

		return ref;
	}

	public ConnectionGraph addReferenceToTargets(ReferenceNode ref, Set<ObjectNode> targets) {
		ConnectionGraph result = new ConnectionGraph(this);
		result.referenceNodes.add(ref);
		for (ObjectNode target : targets)
			result.pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(ref, target));

		return result;
	}

	//
	// public void merge(ConnectionGraph other) {
	// objectNodes.addAll(other.objectNodes);
	// referenceNodes.addAll(other.referenceNodes);
	// nonStaticFieldNodes.addAll(other.nonStaticFieldNodes);
	// staticFieldNodes.addAll(other.staticFieldNodes);
	//
	// pointsToEdges.addAll(other.pointsToEdges);
	// deferredEdges.addAll(other.deferredEdges);
	// fieldEdges.addAll(other.fieldEdges);
	// }
	//
	// public void bypass(NonObjectNode node) {
	// Set<NonObjectNode> origins = new HashSet<NonObjectNode>();
	// for (Iterator<Pair<NonObjectNode, NonObjectNode>> it =
	// deferredEdges.iterator(); it
	// .hasNext();) {
	// Pair<NonObjectNode, NonObjectNode> edge = it.next();
	// if (edge.getValue2().equals(node)) {
	// origins.add(edge.getValue1());
	// it.remove();
	// }
	// }
	//
	// Set<ObjectNode> pointedToDestinations = new HashSet<ObjectNode>();
	// for (Iterator<Pair<NonObjectNode, ObjectNode>> it =
	// pointsToEdges.iterator(); it.hasNext();) {
	// Pair<NonObjectNode, ObjectNode> edge = it.next();
	// if (edge.getValue1().equals(node)) {
	// pointedToDestinations.add(edge.getValue2());
	// it.remove();
	// }
	// }
	//
	// Set<NonObjectNode> deferredDestinations = new HashSet<NonObjectNode>();
	// for (Iterator<Pair<NonObjectNode, NonObjectNode>> it =
	// deferredEdges.iterator(); it
	// .hasNext();) {
	// Pair<NonObjectNode, NonObjectNode> edge = it.next();
	// if (edge.getValue1().equals(node)) {
	// deferredDestinations.add(edge.getValue2());
	// it.remove();
	// }
	// }
	//
	// for (NonObjectNode origin : origins) {
	// for (ObjectNode pointedToDestination : pointedToDestinations)
	// pointsToEdges
	// .add(new Pair<NonObjectNode, ObjectNode>(origin, pointedToDestination));
	//
	// for (NonObjectNode deferredDestination : deferredDestinations)
	// deferredEdges.add(new Pair<NonObjectNode, NonObjectNode>(origin,
	// deferredDestination));
	// }
	// }
	//
	@Override
	public String toString() {
		return "CG(" + pointsToEdges + ", " + fieldEdges + ")";
	}

	//
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fieldEdges.hashCode();
		result = prime * result + nonStaticFieldNodes.hashCode();
		result = prime * result + objectNodes.hashCode();
		result = prime * result + pointsToEdges.hashCode();
		result = prime * result + referenceNodes.hashCode();
		result = prime * result + staticFieldNodes.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ConnectionGraph))
			return false;

		ConnectionGraph other = (ConnectionGraph) obj;

		return objectNodes.equals(other.objectNodes) && referenceNodes.equals(other.referenceNodes)
				&& nonStaticFieldNodes.equals(other.nonStaticFieldNodes)
				&& staticFieldNodes.equals(other.staticFieldNodes)
				&& pointsToEdges.equals(other.pointsToEdges) && fieldEdges.equals(other.fieldEdges);
	}

	// public static void main(String[] args) {
	// Set<Integer> indexes = new HashSet<Integer>();
	// indexes.add(0);
	// indexes.add(2);
	//
	// ConnectionGraph cg = new ConnectionGraph(indexes);
	//
	// }

}
