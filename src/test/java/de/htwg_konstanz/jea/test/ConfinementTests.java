package de.htwg_konstanz.jea.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.htwg_konstanz.jea.ProgramBuilder;
import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.test.classes.SimpleClass;
import de.htwg_konstanz.jea.test.classes.StaticClass;

@SuppressWarnings("unused")
public class ConfinementTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConfined() {
		String[] classes = { "de.htwg_konstanz.jea.spec.test.classes.SimpleClass",
				"de.htwg_konstanz.jea.spec.test.tests.Confined" };
		Program program = TestHelper.analyze(classes, "Confined");
		assertTrue(program.confinedClasses().contains(
				"de.htwg_konstanz.jea.spec.test.classes.SimpleClass"));
	}

	@Test
	public void testEscapeStatic() {
		String[] classes = { "de.htwg_konstanz.jea.spec.test.classes.SimpleClass",
				"de.htwg_konstanz.jea.spec.test.tests.EscapeStatic" };
		Program program = TestHelper.analyze(classes, "Escape Static");
		assertTrue(program.escapingClasses().contains(
				"de.htwg_konstanz.jea.spec.test.classes.SimpleClass"));
	}

}
