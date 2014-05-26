package de.htwg_konstanz.jea.vm;

public interface Node {
	enum EscapeState {
		GLOBAL_ESCAPE("^"), ARG_ESCAPE(">"), NO_ESCAPE("v");

		private final String symbol;

		private EscapeState(String symbol) {
			this.symbol = symbol;
		}

		public boolean moreConfinedThan(EscapeState other) {
			return ordinal() > other.ordinal();
		}

		@Override
		public String toString() {
			return symbol;
		}
	};
}
