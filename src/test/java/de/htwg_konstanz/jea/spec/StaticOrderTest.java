package de.htwg_konstanz.jea.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.htwg_konstanz.jea.ProgramBuilder;
import de.htwg_konstanz.jea.gen.ByteCodeClass;
import de.htwg_konstanz.jea.gen.ExitPoint;
import de.htwg_konstanz.jea.gen.Method;
import de.htwg_konstanz.jea.gen.Program;

public class StaticOrderTest {

	@SuppressWarnings("unused")
	private static class TestClass {
		int f(int n) {
			return (n > 0) ? 1 : -1;
		}
	}

	private static Program program;
	private static ByteCodeClass testClass;
	private static Method f;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = new ProgramBuilder("de.htwg_konstanz.jea.spec.StaticOrderTest$TestClass").build();

		testClass = program.getByteCodeClass(0);

		for (Method method : testClass.getMethods())
			if (method.getMethodName().equals("f"))
				f = method;
	}

	@Test
	public void testNext() {
		for (int i = 0; i < f.getNumInstruction() - 1; i++)
			assertEquals(f.getInstruction(i).next(), f.getInstruction(i + 1));

		assertNull(f.getInstruction(f.getNumInstruction() - 1).next());
	}

	@Test
	public void testPrevious() {
		for (int i = 1; i < f.getNumInstruction(); i++)
			assertEquals(f.getInstruction(i).previous(), f.getInstruction(i - 1));

		assertNull(f.getInstruction(0).previous());
	}

	@Test
	public void testMethod() {
		for (int i = 0; i < f.getNumInstruction(); i++)
			assertEquals(f.getInstruction(i).method(), f);
	}

	@Test
	public void testClazz() {
		assertEquals(f.clazz(), testClass);
	}

	@Test
	public void testProgram() {
		assertEquals(testClass.program(), program);
	}

	@Test
	public void testExitPoint() {
		assertTrue(f.exitPoint() instanceof ExitPoint);
	}
}
