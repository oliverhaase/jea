package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class FieldEdge {
	@Getter
	private final String originId;
	@Getter
	private final String fieldName;
	@Getter
	private final String destinationId;

	public FieldEdge(@NonNull String originId, @NonNull String fieldName,
			@NonNull String destinationId) {
		if (originId.equals("null"))
			throw new AssertionError("assigned field to null");

		this.originId = originId;
		this.fieldName = fieldName;
		this.destinationId = destinationId;
	}

	@Override
	public String toString() {
		return "(" + originId + "." + fieldName + " = " + destinationId + ")";
	}
}
