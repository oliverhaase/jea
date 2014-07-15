package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode
public final class ConnectionGraph {
	@Getter
	private final ObjectNodes objectNodes;
	@Getter
	private final Set<FieldEdge> fieldEdges;
	@Getter
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
	@Getter
	private final Set<Pair<ReferenceNode, String>> pointsToEdges = new HashSet<>();

	public ConnectionGraph() {
		objectNodes = new ObjectNodes();
		fieldEdges = new HashSet<>();
	}

	public ConnectionGraph(Set<Integer> indexes, Slot[] vars) {
		objectNodes = new ObjectNodes();
		fieldEdges = new HashSet<>();

		referenceNodes.add(ReferenceNode.getGlobalRef());
		objectNodes.add(GlobalObject.getInstance());
		pointsToEdges.add(new Pair<ReferenceNode, String>(ReferenceNode.getGlobalRef(),
				GlobalObject.getInstance().getId()));

		for (Integer index : indexes) {
			ReferenceNode ref = new ReferenceNode(index, Category.ARG);
			ObjectNode obj = PhantomObject.newPhantomObject(index);

			referenceNodes.add(ref);
			objectNodes.add(obj);
			pointsToEdges.add(new Pair<ReferenceNode, String>(ref, obj.getId()));

			vars[index] = ref;
		}
		referenceNodes.add(ReferenceNode.getReturnRef());
	}

	public ConnectionGraph(ConnectionGraph original) {
		objectNodes = new ObjectNodes();
		fieldEdges = new HashSet<>();

		objectNodes.addAll(original.objectNodes);
		referenceNodes.addAll(original.referenceNodes);

		pointsToEdges.addAll(original.pointsToEdges);
		fieldEdges.addAll(original.fieldEdges);
	}

	public Set<ObjectNode> dereference(ReferenceNode ref) {
		Set<ObjectNode> result = new HashSet<>();
		for (Pair<ReferenceNode, String> pointsToEdge : pointsToEdges)
			if (pointsToEdge.getValue1().equals(ref))
				result.add(objectNodes.getObjectNode(pointsToEdge.getValue2()));
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
		if (obj.isGlobal())
			return this;

		ConnectionGraph result = new ConnectionGraph(this);

		if (!objectNodes.existsObject(value.getId()))
			result.objectNodes.add(value);

		result.fieldEdges.add(new FieldEdge(obj.getId(), fieldName, value.getId()));
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

	public ConnectionGraph setReturnRef(ReferenceNode ref) {
		ConnectionGraph result = new ConnectionGraph(this);
		for (ObjectNode obj : dereference(ref))
			result.pointsToEdges.add(new Pair<ReferenceNode, String>(ReferenceNode.getReturnRef(),
					obj.getId()));
		return result;
	}

	public ConnectionGraph merge(ConnectionGraph other) {
		ConnectionGraph result = new ConnectionGraph();

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

		result.referenceNodes.addAll(this.referenceNodes);
		result.referenceNodes.addAll(other.referenceNodes);

		result.pointsToEdges.addAll(this.pointsToEdges);
		result.pointsToEdges.addAll(other.pointsToEdges);

		return result;
	}

	@Override
	public String toString() {
		return "CG(" + pointsToEdges + ", " + fieldEdges + ")";
	}

	public Set<ObjectNode> getResultValues() {
		return dereference(ReferenceNode.getReturnRef());
	}

}
