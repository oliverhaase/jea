package de.htwg_konstanz.jea;

import java.util.Set;

import de.htwg_konstanz.jea.gen.Program;

public class CreationChecker {

	public static void main(String[] args) throws ClassNotFoundException {
		String creator = "de.htwg_konstanz.tcc.TccTestClass";
		// String creator = "de.htwg_konstanz.tcc.CreationChecker";
		String creature = "java.lang.String";
		// String creature = "de.htwg_konstanz.tcc.AstConverter";

		Program program = new ProgramBuilder(creator).build();

		Set<String> methods = program.checkCreation(creature);
		System.out.println("The following methods of class " + creator + "\n"
				+ "create instances of class " + creature + ":");
		for (String method : methods)
			System.out.println("- " + method);

	}
}
