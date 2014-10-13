package de.htwg_konstanz.jea.vm;

import javax.annotation.CheckReturnValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public final class PhantomObject extends ObjectNode {
	@Getter
	private final int index;
	@Getter
	private final ObjectNode parent;
	@Getter
	private final String fieldName;

	private PhantomObject(int index, EscapeState escapeState) {
		super("p" + index, escapeState);
		this.index = index;
		this.parent = null;
		this.fieldName = null;
	}

	private PhantomObject(String id, ObjectNode parent, String fieldName, EscapeState escapeState) {
		super(id, escapeState);
		this.index = -1;
		this.parent = parent;
		this.fieldName = fieldName;
	}

	public static PhantomObject newPhantomObject(int index) {
		return new PhantomObject(index, EscapeState.ARG_ESCAPE);
	}

	public static PhantomObject newSubPhantom(ObjectNode parent, String fieldName) {
		return new PhantomObject(parent.getId() + "." + fieldName, parent, fieldName,
				parent.getEscapeState());
	}

	@Override
	@CheckReturnValue
	public PhantomObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			if (this.index != -1)
				return new PhantomObject(this.getIndex(), escapeState);
			else
				return new PhantomObject(this.getId(), this.parent, fieldName, escapeState);

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
