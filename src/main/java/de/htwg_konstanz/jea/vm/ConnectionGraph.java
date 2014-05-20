package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode(callSuper = true)
public final class ConnectionGraph extends SummaryGraph {
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
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

	public SummaryGraph extractSummaryGraph() {
		Set<Pair<NonObjectNode, ObjectNode>> fieldPointsToEdges = new HashSet<>();
		for (Pair<NonObjectNode, ObjectNode> pointsToEdge : pointsToEdges)
			if (pointsToEdge.getValue1() instanceof FieldNode)
				fieldPointsToEdges.add(pointsToEdge);

		return new SummaryGraph(objectNodes, fieldNodes, fieldPointsToEdges, fieldEdges);
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
			fieldNode = new FieldNode(fieldName, obj.getId(), obj.getEscapeState());
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

	@Override
	public String toString() {
		return "CG(" + pointsToEdges + ", " + fieldEdges + ")";
	}

}
