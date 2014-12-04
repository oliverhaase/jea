package de.htwg_konstanz.jea;

import java.util.HashSet;

import de.htwg_konstanz.jea.gen.ByteCodeClass;
import de.htwg_konstanz.jea.gen.Method;
import de.htwg_konstanz.jea.gen.Program;
import de.htwg_konstanz.jea.vm.Heap;
import de.htwg_konstanz.jea.vm.InternalObject;
import de.htwg_konstanz.jea.vm.ObjectNode;

public class Analyzer {

	public static void main(String[] args) throws ClassNotFoundException {
		long startTime = System.nanoTime();

		String[] classes = { "aufgabe6.HtmlNotenspiegel" };

		// String[] allClasses =
		// ClassPathFinder.getClassesByReflection("de.htwg");
		// String[] allClasses = getClasses("").toArray(new String[0]);
		// Program program = new ProgramBuilder(allClasses).build();

		Program program = new ProgramBuilder(classes).build();

		Heap methodSummary = null;
		for (ByteCodeClass clazz : program.getByteCodeClassList()) {
			for (Method method : clazz.getMethodList()) {
				if (method.getMethodName().equals("main")) {
					methodSummary = method.methodSummary();
				}
			}
		}

		long estimatedTime = System.nanoTime() - startTime;

		HashSet<String> confinedClasses = new HashSet<String>();
		for (ObjectNode confinedClass : methodSummary.getLocalObjects())
			if (confinedClass instanceof InternalObject)
				confinedClasses.add(((InternalObject) confinedClass).getType());

		System.out.println("confined types: (" + confinedClasses.size() + ")");

		for (String clazz : confinedClasses) {
			System.out.println(clazz);
		}

		HashSet<String> escapedClasses = new HashSet<String>();
		for (ObjectNode escapedClass : methodSummary.getEscapedObjects())
			if (escapedClass instanceof InternalObject)
				escapedClasses.add(((InternalObject) escapedClass).getType());

		System.out.println("escaped types: (" + escapedClasses.size() + ")");

		for (String clazz : escapedClasses) {
			System.out.println(clazz);
		}

		//
		// System.out.println("escaping types: (" +
		// program.escapingClasses().size() + ")");
		// for (String confinedClass : program.escapingClasses())
		// System.out.println("- " + confinedClass);

		System.out.println();
		System.out.println("done.(" + (estimatedTime / 1000000000) + "s)");

	}
}
