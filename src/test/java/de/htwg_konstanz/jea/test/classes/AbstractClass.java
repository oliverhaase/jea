package de.htwg_konstanz.jea.test.classes;

public abstract class AbstractClass {

	public static AbstractClass instance;

	public abstract AbstractClass delegate();

	public Object foo() {
		return delegate().foo();
	}

}
