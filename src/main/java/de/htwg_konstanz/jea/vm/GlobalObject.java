package de.htwg_konstanz.jea.vm;

public final class GlobalObject extends ObjectNode {
	private final static GlobalObject INSTANCE = new GlobalObject();

	private GlobalObject() {
		super("global", EscapeState.GLOBAL_ESCAPE);
	}

	public static GlobalObject getInstance() {
		return INSTANCE;
	}

	@Override
	public ObjectNode increaseEscapeState(EscapeState escapeState) {
		return this;
	}

	@Override
	public String toString() {
		return this.getId();
	}

}
