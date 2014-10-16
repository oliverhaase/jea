package de.htwg_konstanz.jea.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ReferenceNodeAnnotation {
	public int internalID();

	public Category category();

	public int id();
}
