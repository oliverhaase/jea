package de.htwg_konstanz.jea.test.tests;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.test.TestHelper;
import de.htwg_konstanz.jea.test.classes.SimpleClass;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class Executable {
	private String classToTest;

	public Executable(String classToTest) {
		this.classToTest = classToTest;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> localClasses() {
		Class<?>[] declaredClasses = Executable.class.getDeclaredClasses();
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
}
