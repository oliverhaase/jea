package de.htwg_konstanz.jea.test.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.test.TestHelper;
import de.htwg_konstanz.jea.test.classes.PublicClass;
import de.htwg_konstanz.jea.test.classes.SimpleClass;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class ConfinedTest {
	private String classToTest;

	public ConfinedTest(String classToTest) {
		this.classToTest = classToTest;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> localClasses() {
		Class<?>[] declaredClasses = ConfinedTest.class.getDeclaredClasses();
		Collection<Object[]> params = new ArrayList<Object[]>();

		for (Class<?> clazz : declaredClasses) {
			params.add(new Object[] { clazz.getName() });
		}

		return params;
	}

	@Test
	public void testClass() {
		String[] classes = { "de.htwg_konstanz.jea.test.classes.SimpleClass", classToTest };
		Program program = TestHelper.analyze(classes, classToTest);
		assertTrue(program.confinedClasses().contains(
				"de.htwg_konstanz.jea.test.classes.SimpleClass"));
	}

	private static class Local {
		private void test() {
			SimpleClass simpleClass = new SimpleClass();
		}
	}

	private static class ParameterToLocal {

		private void test() {
			storeLocal(new SimpleClass());
		}

		private void storeLocal(SimpleClass sc) {
			SimpleClass simpleClass = sc;
		}
	}

	private static class ReturnToLocal {

		private void test() {
			SimpleClass returnSimpleClass = returnSimpleClass();
		}

		private SimpleClass returnSimpleClass() {
			return new SimpleClass();
		}
	}

	private static class ToField {

		private SimpleClass simpleClass;

		private void test() {
			simpleClass = new SimpleClass();
		}
	}

	private static class NullToField {

		private SimpleClass simpleClass;

		private void test() {
			SimpleClass sc = null;
			simpleClass = sc;
		}

		private void test(SimpleClass o) {
			simpleClass = o;
		}

		private void callTest() {
			test(null);
		}
	}

	private static class ParameterToField {

		private SimpleClass simpleClass;

		private void test(SimpleClass sc) {
			simpleClass = sc;
		}
	}

	private static class CallParameterToField {

		private void test() {
			ParameterToField parameterToField = new ParameterToField();
			parameterToField.test(new SimpleClass());
		}
	}

	private static class ToPublicField {

		public SimpleClass simpleClass;

		private void test() {
			simpleClass = new SimpleClass();
		}
	}

	private static class Recursive {
		private void test(int i) {
			if (i < 5)
				test(++i);
		}
	}

	private static class RecursiveReturn {
		private int recursive(int i) {
			int result;
			if (i < 5) {
				result = recursive(++i);
			} else
				return i;
			return i + result;
		}

		public void test() {
			recursive(0);
		}
	}

	private static class RecursiveReturnReference {
		private Object recursive(Object o) {
			Object result;
			if (o == this) {
				result = recursive(o);
			} else
				return o;
			return result;
		}

		public void test() {
			recursive(new SimpleClass());
		}
	}

	private static class RecursiveReturnInteger {
		private int recursive(Integer i) {
			int result;
			if (i < 5) {
				result = recursive(++i);
			} else
				return i;
			return i + result;
		}

		public void test() {
			recursive(0);

		}
	}

	private static class CallRecursive {
		private void test() {
			RecursiveReturnReference r = new RecursiveReturnReference();
			r.recursive(new SimpleClass());
			r.test();
		}
	}

	private static class EscapeToAlienMethod {
		private void test() {
			new PublicClass().escape(new SimpleClass());
		}
	}

	private static class EscapeToGlobalObject {
		private void test() {
			SimpleClass simpleClass = new PublicClass().getSimpleClass();
			simpleClass.field = new SimpleClass();
		}
	}
}