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
public class ConfinementTest {

	@Test
	public void testEscapeStatic() {
		String[] classes = { "de.htwg_konstanz.jea.test.classes.SimpleClass",
				"de.htwg_konstanz.jea.test.tests.EscapeStatic" };
		Program program = TestHelper.analyze(classes, "Escape Static");
		assertTrue(program.escapingClasses().contains(
				"de.htwg_konstanz.jea.test.classes.SimpleClass"));
	}

}
