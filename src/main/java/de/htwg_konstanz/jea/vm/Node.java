package de.htwg_konstanz.jea.vm;

public interface Node {
	enum EscapeState {
		NO_ESCAPE, ARG_ESCAPE, GLOBAL_ESCAPE
	};
}
