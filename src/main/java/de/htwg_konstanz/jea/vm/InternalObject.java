package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public final class InternalObject extends ObjectNode {
	private final static InternalObject NULL_OBJECT = new InternalObject("null",
			"javax.lang.model.type.NullType", EscapeState.NO_ESCAPE);

	@Getter
	private final String type;

	private InternalObject(String id, String type, EscapeState escapeState) {
		super(id, escapeState);
		this.type = type;
	}

	public static InternalObject newInternalObject(String id, String type) {
		return new InternalObject(id, type, EscapeState.NO_ESCAPE);
	}

	public static InternalObject getNullObject() {
		return NULL_OBJECT;
	}

	@Override
	public InternalObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			return new InternalObject(this.getId(), this.type, escapeState);
		return this;
	}

	public InternalObject resetEscapeState() {
		if (EscapeState.NO_ESCAPE.moreConfinedThan(this.getEscapeState()))
			return new InternalObject(this.getId(), this.type, EscapeState.NO_ESCAPE);
		return this;
	}

	@Override
	public String toString() {
		return this.getId() + getEscapeState().toString();
	}

	@Override
	public boolean isGlobal() {
		return false;
	}

}
