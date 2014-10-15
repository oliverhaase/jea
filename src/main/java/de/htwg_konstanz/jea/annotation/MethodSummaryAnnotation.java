package de.htwg_konstanz.jea.annotation;

public @interface MethodSummaryAnnotation {
	public InternalObjectAnnotation[] internalObjects();

	public PhantomObjectAnnotation[] phantomObjects();

	// all nodes from objectNodes
	public String[] argEscapedObjectIDs();

	// all nodes from escapedObjects
	public String[] globallyEscapedObjectIDs();

	public String[] localObjectIDs();

	public FieldEdgeAnnotation[] fieldEdges();

	public ReferenceNodeAnnotation[] referenceNodes();

	public PointsToEdgesAnnotation[] pointsToEdges();
}
