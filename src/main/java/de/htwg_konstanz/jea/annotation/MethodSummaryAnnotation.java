package de.htwg_konstanz.jea.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface MethodSummaryAnnotation {
	public InternalObjectAnnotation[] internalObjects();

	public PhantomObjectAnnotation[] phantomObjects();

	public GlobalObjectAnnotation globalObject();

	// all nodes from objectNodes
	public String[] argEscapedObjectIDs();

	// all nodes from escapedObjects
	public String[] globallyEscapedObjectIDs();

	public String[] localObjectIDs();

	public FieldEdgeAnnotation[] fieldEdges();

	public ReferenceNodeAnnotation[] referenceNodes();

	public PointsToEdgesAnnotation[] pointsToEdges();
}
