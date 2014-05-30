package de.htwg_konstanz.jea;

import java.io.IOException;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.htwg_konstanz.jea.gen.Program;

public class ProgramBuilder {
	private final String[] classNames;

	public ProgramBuilder(String... classNames) {
		this.classNames = new String[classNames.length];
		for (int i = 0; i < classNames.length; i++)
			this.classNames[i] = classNames[i];
	}

	public Program build() throws ClassNotFoundException {
		Program program = new Program();

		for (String className : classNames) {
			JavaClass clazz = Repository.lookupClass(className);
			program.addByteCodeClass(new AstConverter(clazz).convert());
		}

		return program;
	}

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {
		Program program = new ProgramBuilder("de.htwg_konstanz.jea.TccTestClass").build();
		program.print();
		System.out.println(program.escapingTypes());
	}
}
