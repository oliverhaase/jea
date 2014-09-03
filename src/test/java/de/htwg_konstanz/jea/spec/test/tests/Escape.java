package de.htwg_konstanz.jea.spec.test.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.spec.test.TestHelper;
import de.htwg_konstanz.jea.spec.test.classes.SimpleClass;
import de.htwg_konstanz.jea.spec.test.classes.StaticClass;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class Escape {
	private String classToTest;

	@Before
	public void initialize() {
	}

	public Escape(String classToTest) {
		this.classToTest = classToTest;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> localClasses() {
		Class<?>[] declaredClasses = Escape.class.getDeclaredClasses();
		Collection<Object[]> params = new ArrayList<Object[]>();

		for (Class<?> clazz : declaredClasses) {
			params.add(new Object[] { clazz.getName() });
		}

		return params;
	}

	@Test
	public void testClass() {
		String[] classes = { "de.htwg_konstanz.jea.spec.test.classes.SimpleClass", classToTest };
		Program program = TestHelper.analyze(classes, classToTest);
		assertTrue(program.escapingClasses().contains(
				"de.htwg_konstanz.jea.spec.test.classes.SimpleClass"));
	}

	private static class EscapeStatic {
		private void test() {
			StaticClass.s = new SimpleClass();
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
}