package de.htwg_konstanz.jea.test.tests;

import de.htwg_konstanz.jea.test.classes.SimpleClass;
import de.htwg_konstanz.jea.test.classes.StaticClass;

public class EscapeStatic {

	private void test2() {
		SimpleClass simpleClass = new SimpleClass();
		StaticClass.s = simpleClass;
	}

}
