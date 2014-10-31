package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class PointToEdge {
	@Getter
	private final String referenceId;
	@Getter
	private final String objectId;

	public PointToEdge(@NonNull String referenceId, @NonNull String objectId) {
		this.referenceId = referenceId;
		this.objectId = objectId;
	}

	@Override
	public String toString() {
		return "(" + referenceId + ", " + objectId + ")";
	}

}
