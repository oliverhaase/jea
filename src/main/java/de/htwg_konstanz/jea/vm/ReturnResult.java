package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class ReturnResult {
	@Getter
	private final ObjectNodes objectNodes;
	@Getter
	private final Set<FieldEdge> fieldEdges;
	@Getter
	private final Set<ObjectNode> resultValues;

	private ReturnResult(@NonNull ObjectNodes objectNodes, @NonNull Set<FieldEdge> fieldEdges,
			@NonNull Set<ObjectNode> resultValues) {
		this.objectNodes = objectNodes;
		this.fieldEdges = fieldEdges;
		this.resultValues = resultValues;
	}

	public ReturnResult() {
		this(new ObjectNodes(), new HashSet<FieldEdge>(), new HashSet<ObjectNode>());
	}

	public ReturnResult(ConnectionGraph cg) {
		this(cg.getObjectNodes(), cg.getFieldEdges(), new HashSet<ObjectNode>());
	}

	public ReturnResult(ConnectionGraph cg, Set<ObjectNode> resultValues) {
		this(cg.getObjectNodes(), cg.getFieldEdges(), resultValues);
	}

	public ReturnResult merge(ReturnResult other) {
		ReturnResult result = new ReturnResult();

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

		result.resultValues.addAll(this.resultValues);
		result.resultValues.addAll(other.resultValues);

		return result;
	}

	@Override
	public String toString() {
		return "RR(" + objectNodes + ", " + fieldEdges + ", " + resultValues + ")";
	}
}
