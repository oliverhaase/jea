package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode
public final class ConnectionGraph {
	private final static ConnectionGraph ALIEN_GRAPH = new ConnectionGraph(
			GlobalObject.getInstance());

	private final static ConnectionGraph INITIAL_GRAPH = new ConnectionGraph(
			EmptyReturnObjectSet.getInstance());

	@Getter
	private final ObjectNodes objectNodes;
	@Getter
	private final Set<FieldEdge> fieldEdges;
	@Getter
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
	@Getter
	private final Set<Pair<ReferenceNode, String>> pointsToEdges = new HashSet<>();
	@Getter
	private final ObjectNodes escapedObjects = new ObjectNodes();
	@Getter
	private final ObjectNodes localObjects = new ObjectNodes();

	public ConnectionGraph() {
		objectNodes = new ObjectNodes();
		fieldEdges = new HashSet<>();
	}

	public ConnectionGraph(ObjectNode returnObject) {
		objectNodes = new ObjectNodes();
		objectNodes.add(GlobalObject.getInstance());
		objectNodes.add(EmptyReturnObjectSet.getInstance());

		fieldEdges = new HashSet<>();

		pointsToEdges.add(new Pair<ReferenceNode, String>(ReferenceNode.getReturnRef(),
				returnObject.getId()));
	}

	public ConnectionGraph(Set<Integer> indexes, boolean hasRefReturnType) {
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
		}

		if (hasRefReturnType)
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

	public static ConnectionGraph getAlienGraph() {
		return ALIEN_GRAPH;
	}

	public boolean isAlien() {
		return this == ALIEN_GRAPH;
	}

	public static ConnectionGraph getInitialGraph() {
		return INITIAL_GRAPH;
	}

	@Override
	public String toString() {
		return "CG(" + pointsToEdges + ", " + fieldEdges + ")";
	}

	public Set<ObjectNode> getResultValues() {
		return dereference(ReferenceNode.getReturnRef());
	}

	private void resolveEmptyReturnObjectSet() {
		Set<FieldEdge> edgesToBeRemoved = new HashSet<>();
		Set<FieldEdge> edgesToBeAdded = new HashSet<>();

		for (FieldEdge edge : fieldEdges) {
			if (edge.getOriginId().equals(EmptyReturnObjectSet.getInstance().getId())) {
				for (ObjectNode resultObject : getResultValues())
					if (!edge.getDestinationId().equals(EmptyReturnObjectSet.getInstance().getId()))
						edgesToBeAdded.add(new FieldEdge(resultObject.getId(), edge.getFieldName(),
								edge.getDestinationId()));
				edgesToBeRemoved.add(edge);
			}
			if (edge.getDestinationId().equals(EmptyReturnObjectSet.getInstance().getId())) {
				for (ObjectNode resultObject : getResultValues())
					if (!edge.getOriginId().equals(EmptyReturnObjectSet.getInstance().getId()))
						edgesToBeAdded.add(new FieldEdge(edge.getOriginId(), edge.getFieldName(),
								resultObject.getId()));
				edgesToBeRemoved.add(edge);
			}
		}

		fieldEdges.removeAll(edgesToBeRemoved);
		fieldEdges.addAll(edgesToBeAdded);

		objectNodes.remove(EmptyReturnObjectSet.getInstance());
	}

	private void removeNullObject() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();)
			if (objIterator.next().equals(InternalObject.getNullObject()))
				objIterator.remove();

		for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator.hasNext();) {
			FieldEdge edge = edgeIterator.next();
			if (edge.getOriginId().equals(InternalObject.getNullObject().getId())
					|| edge.getDestinationId().equals(InternalObject.getNullObject().getId()))
				edgeIterator.remove();
		}
	}

	private void propagateEscapeState(EscapeState escapeState) {
		Stack<ObjectNode> workingList = new Stack<>();
		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getEscapeState() == escapeState)
				workingList.push(objectNode);

		while (!workingList.isEmpty()) {
			ObjectNode current = workingList.pop();

			for (ObjectNode subObject : objectNodes.getSubObjectsOf(current, fieldEdges))
				if (subObject.getEscapeState().moreConfinedThan(escapeState)) {
					ObjectNode updatedSubObject = subObject.increaseEscapeState(escapeState);
					objectNodes.remove(subObject);
					objectNodes.add(updatedSubObject);
					workingList.push(updatedSubObject);
				}
		}
	}

	private void collapseGlobalGraph() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.GLOBAL_ESCAPE) {
				Set<FieldEdge> edgesTerminatingAtCurrent = new HashSet<>();

				for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator
						.hasNext();) {
					FieldEdge edge = edgeIterator.next();

					if (edge.getOriginId().equals(current.getId()))
						edgeIterator.remove();
					else if (edge.getDestinationId().equals(current.getId()))
						edgesTerminatingAtCurrent.add(edge);
				}
				for (FieldEdge edgeTerminatingAtCurrent : edgesTerminatingAtCurrent) {
					fieldEdges.remove(edgeTerminatingAtCurrent);
					fieldEdges.add(new FieldEdge(edgeTerminatingAtCurrent.getOriginId(),
							edgeTerminatingAtCurrent.getFieldName(), GlobalObject.getInstance()
									.getId()));
				}

				escapedObjects.add(current);
				objIterator.remove();
			}
		}
	}

	private void removeLocalGraph() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.NO_ESCAPE) {
				for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator
						.hasNext();)
					if (edgeIterator.next().getOriginId().equals(current.getId()))
						edgeIterator.remove();

				localObjects.add(current);
				objIterator.remove();
			}
		}
	}

	public ConnectionGraph doFinalStuff() {
		ConnectionGraph result = new ConnectionGraph(this);

		result.resolveEmptyReturnObjectSet();

		for (ObjectNode resultObject : result.getResultValues()) {
			result.objectNodes.remove(resultObject);
			result.objectNodes.add(resultObject.increaseEscapeState(EscapeState.ARG_ESCAPE));
		}

		result.removeNullObject();

		result.propagateEscapeState(EscapeState.GLOBAL_ESCAPE);
		result.propagateEscapeState(EscapeState.ARG_ESCAPE);

		result.collapseGlobalGraph();

		result.removeLocalGraph();

		return result;
	}

	public ObjectNodes getArgEscapeObjects() {
		return objectNodes;
	}

	public EscapingTypes argEscapingTypes() {
		EscapingTypes result = new EscapingTypes();
		for (ObjectNode obj : objectNodes)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}

	public EscapingTypes globallyEscapingTypes() {
		if (this == ALIEN_GRAPH)
			return EscapingTypes.getAllTypes();

		EscapingTypes result = new EscapingTypes();
		for (ObjectNode obj : escapedObjects)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}

	public ReferenceNode getRefToPhantomObject(int index) {
		for (ReferenceNode ref : referenceNodes)
			if (ref.getCategory() == Category.ARG && ref.getId() == index)
				return ref;
		throw new AssertionError("phantom object with id " + index + " doesn not exist");
	}

}
