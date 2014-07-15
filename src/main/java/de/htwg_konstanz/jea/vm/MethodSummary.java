package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.jcip.annotations.Immutable;
import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@Immutable
@EqualsAndHashCode
@ToString
public class MethodSummary {
	private final static MethodSummary ALIEN_SUMMARY = new MethodSummary(GlobalObject.getInstance());
	private final static MethodSummary INITIAL_SUMMARY = new MethodSummary(
			EmptyReturnObjectSet.getInstance());

	@Getter
	private final ObjectNodes argEscapeObjects;
	@Getter
	private final Set<FieldEdge> fieldEdges;
	@Getter
	private final ObjectNodes escapedObjects;
	@Getter
	private final ObjectNodes localObjects;
	@Getter
	private final ReferenceNode resultReference;
	@Getter
	private final Set<Pair<ReferenceNode, String>> resultPointsToEdges;

	private MethodSummary(ObjectNode returnObject) {
		this.argEscapeObjects = new ObjectNodes();
		this.argEscapeObjects.add(EmptyReturnObjectSet.getInstance());

		this.fieldEdges = new HashSet<>();
		this.escapedObjects = new ObjectNodes();
		this.localObjects = new ObjectNodes();
		this.resultReference = new ReferenceNode(0, Category.RETURN);
		this.resultPointsToEdges = new HashSet<>();
		this.resultPointsToEdges.add(new Pair<ReferenceNode, String>(resultReference, returnObject
				.getId()));
	}

	// public MethodSummary(ReturnResult rr) {
	// ObjectNodes objectNodes = new ObjectNodes();
	// objectNodes.addAll(rr.getObjectNodes());
	//
	// Set<FieldEdge> fieldEdges = new HashSet<>();
	// fieldEdges.addAll(rr.getFieldEdges());
	//
	// resolveEmptyReturnObjectSet(objectNodes, fieldEdges,
	// rr.getResultValues());
	//
	// for (ObjectNode resultObject : rr.getResultValues()) {
	// objectNodes.remove(resultObject);
	// objectNodes.add(resultObject.increaseEscapeState(EscapeState.ARG_ESCAPE));
	// }
	//
	// removeNullObject(objectNodes, fieldEdges);
	//
	// objectNodes = propagateEscapeState(
	// propagateEscapeState(objectNodes, fieldEdges, EscapeState.GLOBAL_ESCAPE),
	// fieldEdges, EscapeState.ARG_ESCAPE);
	//
	// this.escapedObjects = collapseGlobalGraph(objectNodes, fieldEdges);
	//
	// this.resultReference = new ReferenceNode(hashCode(), Category.RETURN);
	// this.resultPointsToEdges = new HashSet<>();
	//
	// for (ObjectNode resultObject : rr.getResultValues()) {
	// if (this.escapedObjects.existsObject(resultObject.getId()))
	// this.resultPointsToEdges.add(new Pair<ReferenceNode,
	// String>(resultReference,
	// GlobalObject.getInstance().getId()));
	// else
	// this.resultPointsToEdges.add(new Pair<ReferenceNode,
	// String>(resultReference,
	// resultObject.getId()));
	// }
	//
	// this.localObjects = removeLocalGraph(objectNodes, fieldEdges);
	//
	// this.argEscapeObjects = objectNodes;
	// this.fieldEdges = fieldEdges;
	//
	// }

	public MethodSummary(ConnectionGraph cg) {
		ObjectNodes objectNodes = new ObjectNodes();
		objectNodes.addAll(cg.getObjectNodes());

		Set<FieldEdge> fieldEdges = new HashSet<>();
		fieldEdges.addAll(cg.getFieldEdges());

		resolveEmptyReturnObjectSet(objectNodes, fieldEdges, cg.getResultValues());

		for (ObjectNode resultObject : cg.getResultValues()) {
			objectNodes.remove(resultObject);
			objectNodes.add(resultObject.increaseEscapeState(EscapeState.ARG_ESCAPE));
		}

		removeNullObject(objectNodes, fieldEdges);

		objectNodes = propagateEscapeState(
				propagateEscapeState(objectNodes, fieldEdges, EscapeState.GLOBAL_ESCAPE),
				fieldEdges, EscapeState.ARG_ESCAPE);

		this.escapedObjects = collapseGlobalGraph(objectNodes, fieldEdges);

		this.resultReference = new ReferenceNode(hashCode(), Category.RETURN);
		this.resultPointsToEdges = new HashSet<>();

		for (ObjectNode resultObject : cg.getResultValues()) {
			if (this.escapedObjects.existsObject(resultObject.getId()))
				this.resultPointsToEdges.add(new Pair<ReferenceNode, String>(resultReference,
						GlobalObject.getInstance().getId()));
			else
				this.resultPointsToEdges.add(new Pair<ReferenceNode, String>(resultReference,
						resultObject.getId()));
		}

		this.localObjects = removeLocalGraph(objectNodes, fieldEdges);

		this.argEscapeObjects = objectNodes;
		this.fieldEdges = fieldEdges;

	}

	private void resolveEmptyReturnObjectSet(ObjectNodes objectNodes, Set<FieldEdge> fieldEdges,
			Set<ObjectNode> resultObjects) {
		Set<FieldEdge> edgesToBeRemoved = new HashSet<>();
		Set<FieldEdge> edgesToBeAdded = new HashSet<>();

		for (FieldEdge edge : fieldEdges) {
			if (edge.getOriginId().equals(EmptyReturnObjectSet.getInstance().getId())) {
				for (ObjectNode resultObject : resultObjects)
					if (!edge.getDestinationId().equals(EmptyReturnObjectSet.getInstance().getId()))
						edgesToBeAdded.add(new FieldEdge(resultObject.getId(), edge.getFieldName(),
								edge.getDestinationId()));
				edgesToBeRemoved.add(edge);
			}
			if (edge.getDestinationId().equals(EmptyReturnObjectSet.getInstance().getId())) {
				for (ObjectNode resultObject : resultObjects)
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

	private void removeNullObject(ObjectNodes objectNodes, Set<FieldEdge> fieldEdges) {
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

	private ObjectNodes removeLocalGraph(ObjectNodes objectNodes, Set<FieldEdge> fieldEdges) {
		ObjectNodes result = new ObjectNodes();

		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.NO_ESCAPE) {
				for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator
						.hasNext();)
					if (edgeIterator.next().getOriginId().equals(current.getId()))
						edgeIterator.remove();

				result.add(current);
				objIterator.remove();
			}
		}
		return result;
	}

	private ObjectNodes collapseGlobalGraph(ObjectNodes objectNodes, Set<FieldEdge> fieldEdges) {
		ObjectNodes result = new ObjectNodes();

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

				result.add(current);
				objIterator.remove();
			}
		}
		return result;
	}

	private ObjectNodes propagateEscapeState(ObjectNodes objects, Set<FieldEdge> fieldEdges,
			EscapeState escapeState) {
		ObjectNodes result = new ObjectNodes();
		result.addAll(objects);

		Stack<ObjectNode> workingList = new Stack<>();
		for (ObjectNode objectNode : result)
			if (objectNode.getEscapeState() == escapeState)
				workingList.push(objectNode);

		while (!workingList.isEmpty()) {
			ObjectNode current = workingList.pop();

			for (ObjectNode subObject : result.getSubObjectsOf(current, fieldEdges))
				if (subObject.getEscapeState().moreConfinedThan(escapeState)) {
					ObjectNode updatedSubObject = subObject.increaseEscapeState(escapeState);
					result.remove(subObject);
					result.add(updatedSubObject);
					workingList.push(updatedSubObject);
				}
		}
		return result;
	}

	public static MethodSummary getAlienSummary() {
		return ALIEN_SUMMARY;
	}

	public static MethodSummary getInitialSummary() {
		return INITIAL_SUMMARY;
	}

	public boolean isAlien() {
		return this == ALIEN_SUMMARY;
	}

	public boolean isInitial() {
		return this == INITIAL_SUMMARY;
	}

	public EscapingTypes globallyEscapingTypes() {
		if (this == ALIEN_SUMMARY)
			return EscapingTypes.getAllTypes();

		EscapingTypes result = new EscapingTypes();
		for (ObjectNode obj : escapedObjects)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}

	public EscapingTypes argEscapingTypes() {
		EscapingTypes result = new EscapingTypes();
		for (ObjectNode obj : argEscapeObjects)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}
}
