package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class FieldNode implements NonObjectNode {
	@Getter
	private final String name;
	private final String originID;
	@Getter
	private final EscapeState escapeState;

	public FieldNode(@NonNull String name, @NonNull String originID,
			@NonNull EscapeState escapeState) {
		this.name = name;
		this.originID = originID;
		this.escapeState = escapeState;
	}

	@Override
	public String toString() {
		return name;
	}

}
