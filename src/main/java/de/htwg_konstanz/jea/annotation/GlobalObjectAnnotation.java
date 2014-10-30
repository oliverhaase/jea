package de.htwg_konstanz.jea.annotation;

public @interface GlobalObjectAnnotation {
	public boolean containedInArgEscapedObjects();

	public boolean containedInGloballyEscapedObjects();

	public boolean containedInLocalObjects();
}
