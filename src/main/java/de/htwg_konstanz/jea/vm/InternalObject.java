package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public final class InternalObject extends ObjectNode {
	@Getter
	private final String type;

	private InternalObject(String id, String type, EscapeState escapeState) {
		super(id, escapeState);
		this.type = type;
	}

	public static InternalObject newInternalObject(String id, String type) {
		return new InternalObject(id, type, EscapeState.NO_ESCAPE);
	}

	@Override
	public InternalObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			return new InternalObject(this.getId(), this.type, escapeState);
		return this;
	}

	@Override
	public String toString() {
		return this.getId() + getEscapeState().toString();
	}

}
