package de.htwg_konstanz.jea;

import de.htwg_konstanz.jea.gen.Program;

public class ConfinementChecker {

	public static void main(String[] args) throws ClassNotFoundException {
		String[] classes = { "de.htwg_konstanz.jea.AstConverterVisitor",
				"de.htwg_konstanz.jea.ConfinementChecker", "de.htwg_konstanz.jea.CreationChecker",
				"de.htwg_konstanz.jea.ProgramBuilder", "de.htwg_konstanz.jea.TccTestClass",
				"de.htwg_konstanz.jea.vm.ConnectionGraph",
		// "de.htwg_konstanz.jea.vm.DontCareSlot"
		};

		Program program = new ProgramBuilder(classes).build();
		program.print();

		System.out.println("confined classes: " + program.confinedClasses());
	}
}
