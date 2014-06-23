package de.htwg_konstanz.jea.nj;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

public class NjAstConverter {
	private final JavaClass bcelClass;

	public NjAstConverter(JavaClass bcelClass) {
		this.bcelClass = bcelClass;
	}

	public NjClass convert() {
		NjClass clazz = new NjClass(bcelClass);
		// clazz.setSuperClass(bcelClass.getSuperclassName());

		// for (org.apache.bcel.classfile.Field bcelField :
		// bcelClass.getFields()) {
		// Field field = new Field(bcelField.getName());
		// clazz.addField(field);
		// }

		for (org.apache.bcel.classfile.Method bcelMethod : bcelClass.getMethods()) {
			NjMethod method = new NjMethod(bcelMethod);

			clazz.addMethod(method);

			if (bcelMethod.isAbstract() || bcelMethod.isNative())
				continue;

			method.setEntry(new NjEntryPoint());

			for (InstructionHandle instructionHandle : new InstructionList(bcelMethod.getCode()
					.getCode()).getInstructionHandles())
				method.addInstruction(convertInstruction(instructionHandle));

			method.setExit(new NjExitPoint());

		}
		return clazz;
	}

	private NjInstruction convertInstruction(InstructionHandle instructionHandle) {
		return new NjInstruction(instructionHandle);
	}

}
