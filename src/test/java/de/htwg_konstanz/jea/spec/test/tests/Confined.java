package de.htwg_konstanz.jea.spec.test.tests;

import de.htwg_konstanz.jea.spec.test.classes.SimpleClass;
import de.htwg_konstanz.jea.spec.test.classes.StaticClass;

@SuppressWarnings("unused")
public class Confined {

	private SimpleClass simpleClass;

	private void test1() {
		SimpleClass simpleClass = new SimpleClass();
	}

	private void test2() {
		SimpleClass simpleClass = new SimpleClass();
	}

	private void test3() {
		simpleClass = new SimpleClass();
	}

	private void test4() {
		StaticClass.s = simpleClass;
	}

	private void test5() {
		test3();
		test4();
	}

}
