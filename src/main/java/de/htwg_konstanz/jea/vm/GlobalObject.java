package de.htwg_konstanz.jea.vm;

import javax.annotation.CheckReturnValue;

public final class GlobalObject extends ObjectNode {
	private final static GlobalObject INSTANCE = new GlobalObject();

	private GlobalObject() {
		super("global", EscapeState.GLOBAL_ESCAPE);
	}

	public static GlobalObject getInstance() {
		return INSTANCE;
	}

	@Override
	@CheckReturnValue
	public ObjectNode increaseEscapeState(EscapeState escapeState) {
		return this;
	}

	@Override
	public String toString() {
		return this.getId();
	}

	@Override
	public boolean isGlobal() {
		return true;
	}

}
