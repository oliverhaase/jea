package de.htwg_konstanz.jea;

import de.htwg_konstanz.jea.gen.ByteCodeClass;
import de.htwg_konstanz.jea.gen.Program;

public class CreationChecker {

	public static void main(String[] args) throws ClassNotFoundException {
		String creator = "de.htwg_konstanz.jea.vm.DontCareSlot";

		Program program = new ProgramBuilder(creator).build();

		for (ByteCodeClass clazz : program.getByteCodeClasss())
			System.out.println(clazz.getName() + " creates instances of " + clazz.creates());
	}

}
