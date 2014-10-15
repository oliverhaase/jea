package de.htwg_konstanz.jea.annotation;

import de.htwg_konstanz.jea.vm.Node.EscapeState;

public @interface PhantomObjectAnnotation {
	public String id();

	public EscapeState escapeState();

	public int index();

	public String parentID();

	public String fieldName();
}
