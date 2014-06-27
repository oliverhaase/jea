package de.htwg_konstanz.jea.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.htwg_konstanz.jea.ProgramBuilder;
import de.htwg_konstanz.jea.gen.ByteCodeClass;
import de.htwg_konstanz.jea.gen.Method;
import de.htwg_konstanz.jea.gen.Program;

public class DynamicOrderTest2 {

	private final static int GOTO = 1;
	private final static int IINC = 2;
	private final static int ILOAD_1_1 = 3;
	private final static int BIPUSH = 4;
	private final static int IF = 5;
	private final static int ILOAD_1_2 = 6;
	private final static int IRETURN = 7;

	@SuppressWarnings("unused")
	private static class TestClass {
		int f(int n) {
			while (n < 10)
				n += 4;
			return n;
		}
	}

	private static Program program;
	private static ByteCodeClass testClass;
	private static Method f;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = new ProgramBuilder("de.htwg_konstanz.jea.spec.DynamicOrderTest2$TestClass")
				.build();

		testClass = program.getByteCodeClass(0);

		for (Method method : testClass.getMethods())
			if (method.getMethodName().equals("f"))
				f = method;
	}

	// succ() attribute

	@Test
	public void testSuccEntryPoint() {
		assertEquals(f.entryPoint().succ().size(), 1);
		assertTrue(f.entryPoint().succ().contains(f.getInstruction(GOTO)));
	}

	@Test
	public void testSuccGOTO() {
		assertEquals(f.getInstruction(GOTO).succ().size(), 1);
		assertTrue(f.getInstruction(GOTO).succ().contains(f.getInstruction(ILOAD_1_1)));
	}

	@Test
	public void testSuccIINC() {
		assertEquals(f.getInstruction(IINC).succ().size(), 1);
		assertTrue(f.getInstruction(IINC).succ().contains(f.getInstruction(ILOAD_1_1)));
	}

	@Test
	public void testSuccILOAD_1_1() {
		assertEquals(f.getInstruction(ILOAD_1_1).succ().size(), 1);
		assertTrue(f.getInstruction(ILOAD_1_1).succ().contains(f.getInstruction(BIPUSH)));
	}

	@Test
	public void testSuccBIPUSH() {
		assertEquals(f.getInstruction(BIPUSH).succ().size(), 1);
		assertTrue(f.getInstruction(BIPUSH).succ().contains(f.getInstruction(IF)));
	}

	@Test
	public void testSuccIF() {
		assertEquals(f.getInstruction(IF).succ().size(), 2);
		assertTrue(f.getInstruction(IF).succ().contains(f.getInstruction(ILOAD_1_2)));
		assertTrue(f.getInstruction(IF).succ().contains(f.getInstruction(IINC)));
	}

	@Test
	public void testSuccILOAD_1_2() {
		assertEquals(f.getInstruction(ILOAD_1_2).succ().size(), 1);
		assertTrue(f.getInstruction(ILOAD_1_2).succ().contains(f.getInstruction(IRETURN)));
	}

	@Test
	public void testSuccIRETURN() {
		assertEquals(f.getInstruction(IRETURN).succ().size(), 1);
		assertTrue(f.getInstruction(IRETURN).succ().contains(f.exitPoint()));
	}

	@Test
	public void testSuccExitPoint() {
		assertTrue(f.exitPoint().succ().isEmpty());
	}

	// pred() attribute

	@Test
	public void testPredEntryPoint() {
		assertTrue(f.entryPoint().pred().isEmpty());
	}

	@Test
	public void testPredGOTO() {
		assertEquals(f.getInstruction(GOTO).pred().size(), 1);
		assertTrue(f.getInstruction(GOTO).pred().contains(f.entryPoint()));
	}

	@Test
	public void testPredIINC() {
		assertEquals(f.getInstruction(IINC).pred().size(), 1);
		assertTrue(f.getInstruction(IINC).pred().contains(f.getInstruction(IF)));
	}

	@Test
	public void testPredILOAD_1_1() {
		assertEquals(f.getInstruction(ILOAD_1_1).pred().size(), 2);
		assertTrue(f.getInstruction(ILOAD_1_1).pred().contains(f.getInstruction(IINC)));
		assertTrue(f.getInstruction(ILOAD_1_1).pred().contains(f.getInstruction(GOTO)));
	}

	@Test
	public void testPredBIPUSH() {
		assertEquals(f.getInstruction(BIPUSH).pred().size(), 1);
		assertTrue(f.getInstruction(BIPUSH).pred().contains(f.getInstruction(ILOAD_1_1)));
	}

	@Test
	public void testPredIF() {
		assertEquals(f.getInstruction(IF).pred().size(), 1);
		assertTrue(f.getInstruction(IF).pred().contains(f.getInstruction(BIPUSH)));
	}

	@Test
	public void testPredILOAD_1_2() {
		assertEquals(f.getInstruction(ILOAD_1_2).pred().size(), 1);
		assertTrue(f.getInstruction(ILOAD_1_2).pred().contains(f.getInstruction(IF)));
	}

	@Test
	public void testPredIRETURN() {
		assertEquals(f.getInstruction(IRETURN).pred().size(), 1);
		assertTrue(f.getInstruction(IRETURN).pred().contains(f.getInstruction(ILOAD_1_2)));
	}

	@Test
	public void testPredExitPoint() {
		assertEquals(f.exitPoint().pred().size(), 1);
		assertTrue(f.exitPoint().pred().contains(f.getInstruction(IRETURN)));
	}

}
