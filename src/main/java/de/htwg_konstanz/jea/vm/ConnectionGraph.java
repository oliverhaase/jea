package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode(callSuper = true)
public final class ConnectionGraph extends SummaryGraph {
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
	private final Set<Pair<ReferenceNode, String>> pointsToEdges = new HashSet<>();

	@Getter
	private final ReferenceNode globalReference;

	public ConnectionGraph(Set<Integer> indexes, Slot[] vars) {
		globalReference = new ReferenceNode(-1, Category.GLOBAL);
		ObjectNode globalObj = ObjectNode.newGlobalObjectNode();

		referenceNodes.add(globalReference);
		objectNodes.add(globalObj);
		pointsToEdges.add(new Pair<ReferenceNode, String>(globalReference, globalObj.getId()));

		for (Integer index : indexes) {
			ReferenceNode ref = new ReferenceNode(index, Category.ARG);
			ObjectNode obj = ObjectNode.newPhantomObjectNode(index);

			referenceNodes.add(ref);
			objectNodes.add(obj);
			pointsToEdges.add(new Pair<ReferenceNode, String>(ref, obj.getId()));

			vars[index] = ref;
		}
	}

	public ConnectionGraph(ConnectionGraph original) {
		globalReference = original.globalReference;

		objectNodes.addAll(original.objectNodes);
		referenceNodes.addAll(original.referenceNodes);

		pointsToEdges.addAll(original.pointsToEdges);
		fieldEdges.addAll(original.fieldEdges);
	}

	public SummaryGraph extractSummaryGraph() {
		return new SummaryGraph(propagateEscapeState(
				propagateEscapeState(objectNodes, EscapeState.GLOBAL_ESCAPE),
				EscapeState.ARG_ESCAPE), fieldEdges);
	}

	public Set<ObjectNode> dereference(ReferenceNode ref) {
		Set<ObjectNode> result = new HashSet<>();
		for (Pair<ReferenceNode, String> pointsToEdge : pointsToEdges)
			if (pointsToEdge.getValue1().equals(ref))
				result.add(getObjectNode(pointsToEdge.getValue2()));
		return result;
	}

	public ConnectionGraph publish(ReferenceNode ref) {
		ConnectionGraph result = new ConnectionGraph(this);

		for (ObjectNode object : dereference(ref)) {
			result.objectNodes.remove(object);
			result.objectNodes.add(object.increaseEscapeState(EscapeState.GLOBAL_ESCAPE));
		}

		return result;
	}

	public ConnectionGraph addField(ObjectNode obj, String fieldName, ObjectNode value) {
		ConnectionGraph result = new ConnectionGraph(this);

		if (!existsObject(value.getId()))
			result.objectNodes.add(value);

		result.fieldEdges.add(new Triple<String, String, String>(obj.getId(), fieldName, value
				.getId()));

		return result;
	}

	public ConnectionGraph addReferenceAndTarget(ReferenceNode ref, ObjectNode obj) {
		ConnectionGraph result = new ConnectionGraph(this);
		result.referenceNodes.add(ref);
		result.objectNodes.add(obj);
		result.pointsToEdges.add(new Pair<ReferenceNode, String>(ref, obj.getId()));
		return result;
	}

	public ConnectionGraph addReferenceToTargets(ReferenceNode ref, Set<ObjectNode> targets) {
		ConnectionGraph result = new ConnectionGraph(this);
		result.referenceNodes.add(ref);
		for (ObjectNode target : targets)
			result.pointsToEdges.add(new Pair<ReferenceNode, String>(ref, target.getId()));

		return result;
	}

	@Override
	public String toString() {
		return "CG(" + pointsToEdges + ", " + fieldEdges + ")";
	}

}
