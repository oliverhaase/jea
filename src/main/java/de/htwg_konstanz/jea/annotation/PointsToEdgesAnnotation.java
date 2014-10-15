package de.htwg_konstanz.jea.annotation;

public @interface PointsToEdgesAnnotation {
	// internalID from @ReferenceNodeAnnotation
	public int internalReferenceNodeID();

	public String objectID();
}
