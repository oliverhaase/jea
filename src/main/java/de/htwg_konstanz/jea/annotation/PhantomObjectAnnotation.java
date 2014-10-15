package de.htwg_konstanz.jea.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.htwg_konstanz.jea.vm.Node.EscapeState;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface PhantomObjectAnnotation {
	public String id();

	public EscapeState escapeState();

	public int index();

	public String parentID();

	public String fieldName();
}
