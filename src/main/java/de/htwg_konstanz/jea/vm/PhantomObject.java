package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public final class PhantomObject extends ObjectNode {
	@Getter
	private final int index;
	@Getter
	private final ObjectNode parent;
	@Getter
	private final String field;

	private PhantomObject(int index, EscapeState escapeState) {
		super("p" + index, escapeState);
		this.index = index;
		this.parent = null;
		this.field = null;
	}

	private PhantomObject(String id, ObjectNode parent, String field, EscapeState escapeState) {
		super(id, escapeState);
		this.index = -1;
		this.parent = parent;
		this.field = field;
	}

	public static PhantomObject newPhantomObject(int index) {
		return new PhantomObject(index, EscapeState.ARG_ESCAPE);
	}

	public static PhantomObject newSubPhantom(ObjectNode parent, String fieldName) {
		return new PhantomObject(parent.getId() + "." + fieldName, parent, fieldName,
				parent.getEscapeState());
	}

	@Override
	public PhantomObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			if (this.index != -1)
				return new PhantomObject(this.getIndex(), escapeState);
			else
				return new PhantomObject(this.getId(), this.parent, field, escapeState);

		return this;
	}

	public boolean isSubPhantom() {
		return parent != null;
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
