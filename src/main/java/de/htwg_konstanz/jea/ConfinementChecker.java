package de.htwg_konstanz.jea;

import java.util.Set;

import de.htwg_konstanz.jea.gen.Program;

public class ConfinementChecker {

	public static void main(String[] args) throws ClassNotFoundException {
		long startTime = System.nanoTime();

		String[] classes = { "de.htwg_konstanz.jea.AstConverter",
				"de.htwg_konstanz.jea.AstConverterVisitor",
				"de.htwg_konstanz.jea.ConfinementChecker", "de.htwg_konstanz.jea.CreationChecker",
				"de.htwg_konstanz.jea.JeaDetector", "de.htwg_konstanz.jea.ProgramBuilder",
				"de.htwg_konstanz.jea.TccTestClass", "de.htwg_konstanz.jea.vm.Heap",
				"de.htwg_konstanz.jea.vm.DontCareSlot", "de.htwg_konstanz.jea.vm.EscapingTypes",
				"de.htwg_konstanz.jea.vm.FieldEdge", "de.htwg_konstanz.jea.vm.State",
				"de.htwg_konstanz.jea.vm.StateProcessor", "de.htwg_konstanz.jea.vm.States",
				"de.htwg_konstanz.jea.vm.GlobalObject", "de.htwg_konstanz.jea.vm.InternalObject",
				"de.htwg_konstanz.jea.vm.LocalVars", "de.htwg_konstanz.jea.vm.Node",
				"de.htwg_konstanz.jea.vm.NonObjectNode", "de.htwg_konstanz.jea.vm.ObjectNode",
				"de.htwg_konstanz.jea.vm.OpStack", "de.htwg_konstanz.jea.vm.PhantomObject",
				"de.htwg_konstanz.jea.vm.ReferenceNode", "de.htwg_konstanz.jea.vm.Slot",
				"de.htwg_konstanz.jea.vm.Triple" };

		String[] allClasses = ClassPathFinder.getClassesByPath("de.htwg_konstanz");
		Set<String> classesByReflection = ClassPathFinder.getInstance().getClassesByReflection(
				"de.htwg_konstanz.jea");
		// String[] allClasses = getClasses("").toArray(new String[0]);
		Program program = new ProgramBuilder(allClasses).build();

		for (String string : allClasses) {
			System.out.println(ClassPathFinder.getInstance().getSubTypsOf(string));
		}

		program.print();

		long estimatedTime = System.nanoTime() - startTime;

		System.out.println("confined types: (" + program.confinedClasses().size() + ")");
		for (String confinedClass : program.confinedClasses())
			System.out.println("- " + confinedClass);

		System.out.println();

		System.out.println("escaping types: (" + program.escapingClasses().size() + ")");
		for (String confinedClass : program.escapingClasses())
			System.out.println("- " + confinedClass);

		System.out.println();
		System.out.println("done.(" + (estimatedTime / 1000000000) + "s)");

	}
}
