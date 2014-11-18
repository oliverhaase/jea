package de.htwg_konstanz.jea.test.tests;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.test.TestHelper;
import de.htwg_konstanz.jea.test.classes.AbstractClass;
import de.htwg_konstanz.jea.test.classes.SimpleClass;
import de.htwg_konstanz.jea.test.classes.SubClass;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class ExecutableTest {
	private String classToTest;

	public ExecutableTest(String classToTest) {
		this.classToTest = classToTest;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> localClasses() {
		Class<?>[] declaredClasses = ExecutableTest.class.getDeclaredClasses();
		Collection<Object[]> params = new ArrayList<Object[]>();

		for (Class<?> clazz : declaredClasses) {
			params.add(new Object[] { clazz.getName() });
		}

		return params;
	}

	@Test
	public void testClass() {
		String[] classes = { classToTest };
		Program program = TestHelper.analyze(classes, classToTest);
	}

	private static class TestApplyMethodSummary {

		private void summary(SimpleClass arg1, int i) {
			if (i < 0)
				arg1.field = new SimpleClass();
			else
				arg1.field = new SimpleClass();
		}

		private void test() {

			SimpleClass local = new SimpleClass();

			summary(local, 5);

			summary(local, 5);

		}

	}

	private static class TestApplyMethodSummary2 {

		private void summary(SimpleClass arg1, int i) {
			if (i < 0)
				arg1.field = new SimpleClass();
			else
				arg1.field = new SimpleClass();
		}

		private void test() {

			SimpleClass local = new SimpleClass();

			summary(null, 5);

		}

	}

	private static class TestApplyMethodSummary3 {

		private void summary(SimpleClass arg1, int i) {
			if (i < 0)
				arg1.field.field = new SimpleClass();
			else
				arg1.field.field = new SimpleClass();
		}

		private void test() {

			SimpleClass local = new SimpleClass();

			summary(null, 5);

		}

	}

	private static class NullFieldIf {
		private void test(int i) {
			SimpleClass sc;
			if (12 < i)
				sc = null;
			else
				sc = new SimpleClass();

			sc.field = new SimpleClass();
		}
	}

	private static class NullField {
		private void test() {
			SimpleClass sc = null;
			if (sc != null)
				sc.field = new SimpleClass();
			else
				new Object();
		}
	}

	private static class NullParamIf {
		private void test(int i) {
			SimpleClass sc;
			if (12 < i)
				sc = null;
			else
				sc = new SimpleClass();

			refParam(sc);
		}

		private void refParam(SimpleClass sc) {
			if (sc != null)
				sc.field = new SimpleClass();
		}
	}

	private static class NullParam {
		private void test() {
			refParam(null);
		}

		private void refParam(SimpleClass sc) {
			if (sc != null)
				sc.field = new SimpleClass();
			else
				new Object();
		}
	}

	private static class NewToNull {
		private void test() {
			SimpleClass sc = null;
			sc = refParam();
		}

		private SimpleClass refParam() {
			return new SimpleClass();
		}
	}

	private static class TestClass {

		private SimpleClass refParam;

		private void test() {
			SimpleClass simpleClass = new SimpleClass();
			SimpleClass sc = refParam(simpleClass);
			refParam = sc;
		}

		private SimpleClass refParam(SimpleClass sc) {
			return sc;
		}
	}

	private static class ReturnGlobalObject {

		public static SimpleClass sc = null;

		private void test() {

			SimpleClass sm = getSc();
		}

		public static SimpleClass getSc() {
			return sc;
		}
	}

	private static class SuperClassTest {

		private void test() {
			SubClass subClass = new SubClass();
			SubClass.test();
		}
	}

	private static class AbstracrClassTest {

		public AbstracrClassTest() {
			AbstractClass.instance.foo();
		}
	}
}
