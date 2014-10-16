package de.htwg_konstanz.jea.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface FieldEdgeAnnotation {
	public String originId();

	public String fieldName();

	public String destinationId();
}
