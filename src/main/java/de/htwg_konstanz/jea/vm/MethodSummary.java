package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import lombok.EqualsAndHashCode;
import net.jcip.annotations.Immutable;
import de.htwg_konstanz.jea.vm.Node.EscapeState;

@Immutable
@EqualsAndHashCode
public class MethodSummary {
	private final static MethodSummary ALIEN_SUMMARY = new MethodSummary();

	public MethodSummary() {

	}

	public MethodSummary(ConnectionGraph cg) {
		this(cg, new HashSet<ObjectNode>());
	}

	public MethodSummary(ConnectionGraph cg, Set<ObjectNode> resultObjects) {
		ObjectNodes objectNodes = new ObjectNodes();
		objectNodes.addAll(cg.getObjectNodes());

		Set<FieldEdge> fieldEdges = new HashSet<>();
		fieldEdges.addAll(cg.getFieldEdges());

		for (ObjectNode resultObject : resultObjects) {
			objectNodes.remove(resultObject);
			objectNodes.add(resultObject.increaseEscapeState(EscapeState.ARG_ESCAPE));
		}

		objectNodes = propagateEscapeState(
				propagateEscapeState(objectNodes, fieldEdges, EscapeState.GLOBAL_ESCAPE),
				fieldEdges, EscapeState.ARG_ESCAPE);

		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();
			if (current.getEscapeState() == EscapeState.GLOBAL_ESCAPE) {
				objIterator.remove();
			}
		}

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

	public MethodSummary merge(MethodSummary other) {
		return this;
	}

	public Set<String> getEscapingTypes() {
		return new HashSet<String>();
	}

}
