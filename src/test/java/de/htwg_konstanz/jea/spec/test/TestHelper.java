package de.htwg_konstanz.jea.spec.test;

import static org.junit.Assert.fail;
import de.htwg_konstanz.jea.ProgramBuilder;
import de.htwg_konstanz.jea.gen.Program;

public class TestHelper {
	public static Program analyze(String[] classes, String test) {

		System.out
				.println(String
						.format("\n################################################################################"
								+ "\n#        %-70s#\n"
								+ "################################################################################\n",
								test));

		Program program = null;
		try {
			program = new ProgramBuilder(classes).build();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Could not find class: " + classes);
		}
		program.print();

		System.out.println("confined types: ");
		for (String confinedClass : program.confinedClasses())
			System.out.println("- " + confinedClass);

		System.out.println();

		System.out.println("escaping types: ");
		for (String confinedClass : program.escapingClasses())
			System.out.println("- " + confinedClass);

		System.out.println();
		System.out.println("done.");

		return program;
	}
}
