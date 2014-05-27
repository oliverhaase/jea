package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(exclude = { "escapeState" })
public abstract class ObjectNode implements Node {
	@Getter
	private final String id;
	@Getter
	private final EscapeState escapeState;

	protected ObjectNode(String id, EscapeState escapeState) {
		this.id = id;
		this.escapeState = escapeState;
	}

	public abstract boolean isGlobal();

	public abstract ObjectNode increaseEscapeState(EscapeState escapeState);
}
