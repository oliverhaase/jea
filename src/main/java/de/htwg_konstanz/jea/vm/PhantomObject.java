package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public final class PhantomObject extends ObjectNode {
	@Getter
	private final int index;
	@Getter
	private final ObjectNode origin;
	@Getter
	private final String field;

	private PhantomObject(int index, EscapeState escapeState) {
		super("p" + index, escapeState);
		this.index = index;
		this.origin = null;
		this.field = null;
	}

	private PhantomObject(String id, ObjectNode origin, String field, EscapeState escapeState) {
		super(id, escapeState);
		this.index = -1;
		this.origin = origin;
		this.field = field;
	}

	public static PhantomObject newPhantomObject(int index) {
		return new PhantomObject(index, EscapeState.ARG_ESCAPE);
	}

	public static PhantomObject newSubPhantom(ObjectNode origin, String fieldName) {
		return new PhantomObject(origin.getId() + "." + fieldName, origin, fieldName,
				origin.getEscapeState());
	}

	@Override
	public PhantomObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			return new PhantomObject(this.getId(), this.origin, field, escapeState);
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
