package de.htwg_konstanz.jea.annotation;

import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

public @interface ReferenceNodeAnnotation {
	public int internalID();

	public Category category();

	public int id();
}
