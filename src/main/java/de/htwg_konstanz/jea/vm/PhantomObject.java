package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public final class PhantomObject extends ObjectNode {
	@Getter
	private final int index;
	private final ObjectNode origin;

	private PhantomObject(int index, PhantomObject origin, EscapeState escapeState) {
		super("p" + index, escapeState);
		this.index = index;
		this.origin = origin;
	}

	private PhantomObject(String id, ObjectNode origin, EscapeState escapeState) {
		super(id, escapeState);
		this.index = -1;
		this.origin = origin;
	}

	public static PhantomObject newPhantomObject(int index) {
		return new PhantomObject(index, null, EscapeState.ARG_ESCAPE);
	}

	public static PhantomObject newSubPhantom(ObjectNode origin, String fieldName) {
		return new PhantomObject(origin.getId() + "." + fieldName, origin, origin.getEscapeState());
	}

	@Override
	public PhantomObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			return new PhantomObject(this.getId(), this.origin, escapeState);
		return this;
	}

	@Override
	public String toString() {
		return this.getId() + getEscapeState().toString();
	}

}
