package de.htwg_konstanz.jea;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import de.htwg_konstanz.jea.gen.Program;

public class ConfinementChecker {

	public static void main(String[] args) throws ClassNotFoundException {
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
				"de.htwg_konstanz.jea.vm.OpStack", "de.htwg_konstanz.jea.vm.Pair",
				"de.htwg_konstanz.jea.vm.PhantomObject", "de.htwg_konstanz.jea.vm.ReferenceNode",
				"de.htwg_konstanz.jea.vm.Slot", "de.htwg_konstanz.jea.vm.Triple" };

		String[] allClasses = getClasses("de.htwg_konstanz.jea");
		Program program = new ProgramBuilder(allClasses).build();

		// Program program = new ProgramBuilder(classes).build();

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

	private static String[] getClasses(String packageName) {
		ClassLoader[] classLoaders = { ClasspathHelper.contextClassLoader(),
				ClasspathHelper.staticClassLoader() };

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false), new ResourcesScanner())
				.setUrls(ClasspathHelper.forClassLoader(classLoaders))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));

		Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

		int i = 0;
		String[] classNames = new String[classes.size()];
		for (Class<?> cls : classes) {
			classNames[i++] = cls.getName();
		}

		return classNames;
	}
}
