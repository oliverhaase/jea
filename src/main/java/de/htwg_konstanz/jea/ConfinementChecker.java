package de.htwg_konstanz.jea;

import de.htwg_konstanz.jea.gen.Program;

public class ConfinementChecker {

	public static void main(String[] args) throws ClassNotFoundException {
		String[] classes = {
				// "de.htwg_konstanz.jea.AstConverter",
				"de.htwg_konstanz.jea.AstConverterVisitor",
				"de.htwg_konstanz.jea.ConfinementChecker",
				"de.htwg_konstanz.jea.CreationChecker",
				// "de.htwg_konstanz.jea.JeaDetector",
				"de.htwg_konstanz.jea.ProgramBuilder",
				"de.htwg_konstanz.jea.TccTestClass",
				"de.htwg_konstanz.jea.vm.ConnectionGraph",
				"de.htwg_konstanz.jea.vm.DontCareSlot",
				"de.htwg_konstanz.jea.vm.EscapingTypes",
				"de.htwg_konstanz.jea.vm.FieldEdge",
				// "de.htwg_konstanz.jea.vm.State",
				"de.htwg_konstanz.jea.vm.StateProcessor",
				"de.htwg_konstanz.jea.vm.States",
				"de.htwg_konstanz.jea.vm.GlobalObject",
				"de.htwg_konstanz.jea.vm.InternalObject",
				// "de.htwg_konstanz.jea.vm.LocalVars",
				"de.htwg_konstanz.jea.vm.Node", "de.htwg_konstanz.jea.vm.NonObjectNode",
				"de.htwg_konstanz.jea.vm.ObjectNode", "de.htwg_konstanz.jea.vm.OpStack",
				"de.htwg_konstanz.jea.vm.Pair", "de.htwg_konstanz.jea.vm.PhantomObject",
				"de.htwg_konstanz.jea.vm.ReferenceNode", "de.htwg_konstanz.jea.vm.Slot",
				"de.htwg_konstanz.jea.vm.Triple" };

		Program program = new ProgramBuilder(classes).build();
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

	}
}
