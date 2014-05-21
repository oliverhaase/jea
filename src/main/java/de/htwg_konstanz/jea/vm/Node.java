package de.htwg_konstanz.jea.vm;

public interface Node {
	enum EscapeState {
		GLOBAL_ESCAPE, ARG_ESCAPE, NO_ESCAPE;

		public boolean moreConfinedThan(EscapeState other) {
			return ordinal() > other.ordinal();
		}
	};
}
