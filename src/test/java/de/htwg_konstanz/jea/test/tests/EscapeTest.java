package de.htwg_konstanz.jea.test.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.test.TestHelper;
import de.htwg_konstanz.jea.test.classes.PublicClass;
import de.htwg_konstanz.jea.test.classes.RunnableClass;
import de.htwg_konstanz.jea.test.classes.SimpleClass;
import de.htwg_konstanz.jea.test.classes.StaticClass;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class EscapeTest {
	private String classToTest;

	public EscapeTest(String classToTest) {
		this.classToTest = classToTest;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> localClasses() {
		Class<?>[] declaredClasses = EscapeTest.class.getDeclaredClasses();
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
		assertTrue(program.escapingClasses().contains(
				"de.htwg_konstanz.jea.test.classes.SimpleClass"));
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

	private static class EscapeStaticLocalVariable {
		private void test() {
			SimpleClass simpleClass = new SimpleClass();
			StaticClass.s = simpleClass;
		}
	}

	private static class EscapeStaticField {
		private SimpleClass simpleClass;

		private void test() {
			simpleClass = new SimpleClass();
			StaticClass.s = simpleClass;
		}
	}

	private static class EscapeToRunnable {
		private void test() {
			RunnableClass runnable = new RunnableClass();

			runnable.f = new SimpleClass();

		}
	}

}