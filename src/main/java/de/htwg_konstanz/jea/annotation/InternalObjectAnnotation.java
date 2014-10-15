package de.htwg_konstanz.jea.annotation;

import de.htwg_konstanz.jea.vm.Node.EscapeState;

public @interface InternalObjectAnnotation {
	public String id();

	public EscapeState escapeState();

	public String type();
}
