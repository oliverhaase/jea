package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode
public final class ConnectionGraph {
	private final Set<ObjectNode> objectNodes = new HashSet<>();
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
	// private final Set<FieldNode> nonStaticFieldNodes = new
	// HashSet<FieldNode>();
	// private final Set<FieldNode> staticFieldNodes = new HashSet<FieldNode>();
	private final Set<FieldNode> fieldNodes = new HashSet<>();

	private final Set<Pair<NonObjectNode, ObjectNode>> pointsToEdges = new HashSet<>();
	private final Set<Pair<ObjectNode, FieldNode>> fieldEdges = new HashSet<>();
	@Getter
	private final ReferenceNode globalReference;

	// public ConnectionGraph() {
	// }

	public ConnectionGraph(Set<Integer> indexes, Slot[] vars) {
		globalReference = new ReferenceNode(-1, Category.GLOBAL);
		ObjectNode globalObj = ObjectNode.newGlobalObjectNode();

		referenceNodes.add(globalReference);
		objectNodes.add(globalObj);
		pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(globalReference, globalObj));

		for (Integer index : indexes) {
			ReferenceNode ref = new ReferenceNode(index, Category.ARG);
			ObjectNode obj = ObjectNode.newPhantomObjectNode(index);

			referenceNodes.add(ref);
			objectNodes.add(obj);
			pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(ref, obj));

			vars[index] = ref;
		}

	}

	public ConnectionGraph(ConnectionGraph original) {
		globalReference = original.globalReference;

		objectNodes.addAll(original.objectNodes);
		referenceNodes.addAll(original.referenceNodes);
		// nonStaticFieldNodes.addAll(original.nonStaticFieldNodes);
		// staticFieldNodes.addAll(original.staticFieldNodes);
		fieldNodes.addAll(original.fieldNodes);

		pointsToEdges.addAll(original.pointsToEdges);
		fieldEdges.addAll(original.fieldEdges);
	}

	public Set<ObjectNode> dereference(NonObjectNode ref) {
		Set<ObjectNode> result = new HashSet<>();
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
			fieldNode = new FieldNode(fieldName, obj.getId());
			// result.nonStaticFieldNodes.add(fieldNode);
			result.fieldNodes.add(fieldNode);
			result.fieldEdges.add(new Pair<ObjectNode, FieldNode>(obj, fieldNode));
		}

		result.pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(fieldNode, value));

		return result;
	}

	public ConnectionGraph addReferenceAndTarget(ReferenceNode ref, ObjectNode obj) {
		ConnectionGraph result = new ConnectionGraph(this);
		result.referenceNodes.add(ref);
		result.objectNodes.add(obj);
		result.pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(ref, obj));
		return result;
	}

	public ConnectionGraph addReferenceToTargets(ReferenceNode ref, Set<ObjectNode> targets) {
		ConnectionGraph result = new ConnectionGraph(this);
		result.referenceNodes.add(ref);
		for (ObjectNode target : targets)
			result.pointsToEdges.add(new Pair<NonObjectNode, ObjectNode>(ref, target));

		return result;
	}

	public ConnectionGraph removeReferenceNodesExcept(Slot returnValue) {
		ConnectionGraph result = new ConnectionGraph(this);

		for (Iterator<ReferenceNode> refIterator = result.referenceNodes.iterator(); refIterator
				.hasNext();) {
			ReferenceNode referenceNode = refIterator.next();
			if (!referenceNode.equals(returnValue)) {
				refIterator.remove();
				for (Iterator<Pair<NonObjectNode, ObjectNode>> edgeIterator = result.pointsToEdges
						.iterator(); edgeIterator.hasNext();)
					if (edgeIterator.next().getValue1().equals(referenceNode))
						edgeIterator.remove();
			}
		}
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

}
