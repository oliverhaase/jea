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
import de.htwg_konstanz.jea.test.classes.SimpleClass;
import de.htwg_konstanz.jea.test.classes.StaticClass;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class Confined {
	private String classToTest;

	@Before
	public void initialize() {
	}

	public Confined(String classToTest) {
		this.classToTest = classToTest;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> localClasses() {
		Class<?>[] declaredClasses = Confined.class.getDeclaredClasses();
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

}