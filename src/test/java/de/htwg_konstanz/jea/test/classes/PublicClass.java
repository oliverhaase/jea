package de.htwg_konstanz.jea.test.classes;

public class PublicClass {

	public void escape(SimpleClass sc) {

	}

	public void escapeToStatic(SimpleClass sc) {
		StaticClass.s = sc;
	}

	public SimpleClass getSimpleClass() {
		return new SimpleClass();
	}

}
