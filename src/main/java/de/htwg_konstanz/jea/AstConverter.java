package de.htwg_konstanz.jea;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import de.htwg_konstanz.jea.gen.ByteCodeClass;
import de.htwg_konstanz.jea.gen.EntryPoint;
import de.htwg_konstanz.jea.gen.ExitPoint;
import de.htwg_konstanz.jea.gen.Field;
import de.htwg_konstanz.jea.gen.Instruction;
import de.htwg_konstanz.jea.gen.Method;
import de.htwg_konstanz.jea.gen.SimpleInstruction;

public class AstConverter {
	private final JavaClass bcelClass;

	public AstConverter(JavaClass bcelClass) {
		this.bcelClass = bcelClass;
	}

	public ByteCodeClass convert() {
		ByteCodeClass clazz = new ByteCodeClass();
		clazz.setName(bcelClass.getClassName());

		for (org.apache.bcel.classfile.Field bcelField : bcelClass.getFields()) {
			Field field = new Field(bcelField.getName());
			clazz.addField(field);
		}

		ConstantPoolGen cpg = new ConstantPoolGen(bcelClass.getConstantPool());
		AstConverterVisitor visitor = new AstConverterVisitor(cpg);

		for (org.apache.bcel.classfile.Method bcelMethod : bcelClass.getMethods()) {
			Method method = new Method();

			method.setIsStatic(bcelMethod.isStatic());
			method.setIsAbstract(bcelMethod.isAbstract());
			method.setIsNative(bcelMethod.isNative());
			method.setMethodName(bcelMethod.getName());
			method.setArgTypes(bcelMethod.getArgumentTypes());

			clazz.addMethod(method);

			if (method.getIsAbstract() || method.getIsNative())
				continue;

			method.setMaxLocals(bcelMethod.getCode().getMaxLocals());

			InstructionHandle[] instructionHandles = new InstructionList(bcelMethod.getCode()
					.getCode()).getInstructionHandles();

			method.addInstruction(new EntryPoint("entry point", -1, 0, 0));

			for (InstructionHandle instructionHandle : instructionHandles)
				method.addInstruction(convertInstruction(instructionHandle, visitor, cpg));

			method.addInstruction(new ExitPoint("exit point", -1, 0, 0));

		}
		return clazz;
	}

	private Instruction convertInstruction(InstructionHandle instructionHandle,
			AstConverterVisitor visitor, ConstantPoolGen cpg) {
		Instruction instruction;

		instructionHandle.accept(visitor);

		if (visitor.hasBeenVisited()) {
			instruction = visitor.getInstruction();
			visitor.clear();
		} else {
			instruction = new SimpleInstruction();
		}

		instruction.setLabel(instructionHandle.toString(false));
		instruction.setPosition(instructionHandle.getPosition());
		instruction.setConsumeStack(instructionHandle.getInstruction().consumeStack(cpg));
		instruction.setProduceStack(instructionHandle.getInstruction().produceStack(cpg));
		return instruction;
	}

}
