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
	private final static MethodSummary ALIEN_SUMMARY = new MethodSummary();

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

	private MethodSummary() {
		this.argEscapeObjects = null;
		this.fieldEdges = null;
		this.escapedObjects = null;
		this.localObjects = null;
		this.resultReference = null;
		this.resultPointsToEdges = null;
	}

	public MethodSummary(ReturnResult rr) {
		ObjectNodes objectNodes = new ObjectNodes();
		objectNodes.addAll(rr.getObjectNodes());

		Set<FieldEdge> fieldEdges = new HashSet<>();
		fieldEdges.addAll(rr.getFieldEdges());

		for (ObjectNode resultObject : rr.getResultValues()) {
			objectNodes.remove(resultObject);
			objectNodes.add(resultObject.increaseEscapeState(EscapeState.ARG_ESCAPE));
		}

		objectNodes = propagateEscapeState(
				propagateEscapeState(objectNodes, fieldEdges, EscapeState.GLOBAL_ESCAPE),
				fieldEdges, EscapeState.ARG_ESCAPE);

		this.escapedObjects = collapseGlobalGraph(objectNodes, fieldEdges);

		this.resultReference = new ReferenceNode(0, Category.RETURN);
		this.resultPointsToEdges = new HashSet<>();

		for (ObjectNode resultObject : rr.getResultValues()) {
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

	public boolean isAlien() {
		return this == ALIEN_SUMMARY;
	}

	public Set<String> getEscapingTypes() {
		Set<String> result = new HashSet<String>();
		for (ObjectNode obj : escapedObjects)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}

}
