package de.htwg_konstanz.jea.vm;

public final class EmptyReturnObjectSet extends ObjectNode {
	private final static EmptyReturnObjectSet INSTANCE = new EmptyReturnObjectSet();

	private EmptyReturnObjectSet() {
		super("pseudo", EscapeState.ARG_ESCAPE);
	}

	public static EmptyReturnObjectSet getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isGlobal() {
		return false;
	}

	@Override
	public ObjectNode increaseEscapeState(EscapeState escapeState) {
		throw new AssertionError("increaseEscapeState() must not be called on EmptyReturnObjectSet");
	}

}
