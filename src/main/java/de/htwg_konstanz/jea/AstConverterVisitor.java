package de.htwg_konstanz.jea;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;

import de.htwg_konstanz.jea.gen.ANewArray;
import de.htwg_konstanz.jea.gen.Aaload;
import de.htwg_konstanz.jea.gen.Aastore;
import de.htwg_konstanz.jea.gen.Areturn;
import de.htwg_konstanz.jea.gen.DUP;
import de.htwg_konstanz.jea.gen.DUP2;
import de.htwg_konstanz.jea.gen.DUP2_X1;
import de.htwg_konstanz.jea.gen.DUP2_X2;
import de.htwg_konstanz.jea.gen.DUP_X1;
import de.htwg_konstanz.jea.gen.DUP_X2;
import de.htwg_konstanz.jea.gen.Dreturn;
import de.htwg_konstanz.jea.gen.Freturn;
import de.htwg_konstanz.jea.gen.GetField;
import de.htwg_konstanz.jea.gen.GetStatic;
import de.htwg_konstanz.jea.gen.GotoInstruction;
import de.htwg_konstanz.jea.gen.IfInstruction;
import de.htwg_konstanz.jea.gen.Instruction;
import de.htwg_konstanz.jea.gen.InvokeInterface;
import de.htwg_konstanz.jea.gen.InvokeSpecial;
import de.htwg_konstanz.jea.gen.InvokeStatic;
import de.htwg_konstanz.jea.gen.InvokeVirtual;
import de.htwg_konstanz.jea.gen.JsrInstruction;
import de.htwg_konstanz.jea.gen.LoadInstruction;
import de.htwg_konstanz.jea.gen.Lreturn;
import de.htwg_konstanz.jea.gen.MultiANewArray;
import de.htwg_konstanz.jea.gen.New;
import de.htwg_konstanz.jea.gen.NewArray;
import de.htwg_konstanz.jea.gen.PutField;
import de.htwg_konstanz.jea.gen.PutStatic;
import de.htwg_konstanz.jea.gen.Return;
import de.htwg_konstanz.jea.gen.SWAP;
import de.htwg_konstanz.jea.gen.SelectInstruction;
import de.htwg_konstanz.jea.gen.StoreInstruction;

public class AstConverterVisitor extends EmptyVisitor {
	private final ConstantPoolGen cpg;
	private Instruction instruction = null;

	public AstConverterVisitor(ConstantPoolGen cpg) {
		this.cpg = cpg;
	}

	public void clear() {
		instruction = null;
	}

	public boolean hasBeenVisited() {
		return instruction != null;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	@Override
	public void visitPUTFIELD(org.apache.bcel.generic.PUTFIELD bcelInstruction) {
		PutField instruction = new PutField();
		instruction.setFieldName(bcelInstruction.getFieldName(cpg));
		this.instruction = instruction;
	}

	@Override
	public void visitAASTORE(org.apache.bcel.generic.AASTORE bcelInstruction) {
		Aastore instruction = new Aastore();
		instruction.setFieldName("$components");
		this.instruction = instruction;
	}

	@Override
	public void visitPUTSTATIC(org.apache.bcel.generic.PUTSTATIC bcelInstruction) {
		PutStatic instruction = new PutStatic();
		this.instruction = instruction;
	}

	@Override
	public void visitGETFIELD(org.apache.bcel.generic.GETFIELD bcelInstruction) {
		GetField instruction = new GetField();
		instruction.setFieldName(bcelInstruction.getFieldName(cpg));
		instruction.setFieldType(bcelInstruction.getFieldType(cpg));
		this.instruction = instruction;
	}

	@Override
	public void visitAALOAD(org.apache.bcel.generic.AALOAD bcelInstruction) {
		Aaload instruction = new Aaload();
		instruction.setFieldName("$components");
		instruction.setFieldType(bcelInstruction.getType(cpg));
		this.instruction = instruction;
	}

	@Override
	public void visitGETSTATIC(org.apache.bcel.generic.GETSTATIC bcelInstruction) {
		GetStatic instruction = new GetStatic();
		this.instruction = instruction;
	}

	@Override
	public void visitLoadInstruction(org.apache.bcel.generic.LoadInstruction bcelInstruction) {
		LoadInstruction instruction = new LoadInstruction();
		instruction.setIndex(bcelInstruction.getIndex());
		this.instruction = instruction;
	}

	@Override
	public void visitStoreInstruction(org.apache.bcel.generic.StoreInstruction bcelInstruction) {
		StoreInstruction instruction = new StoreInstruction();
		instruction.setIndex(bcelInstruction.getIndex());
		this.instruction = instruction;
	}

	@Override
	public void visitIfInstruction(org.apache.bcel.generic.IfInstruction bcelInstruction) {
		IfInstruction instruction = new IfInstruction();
		instruction.setTargetPosition(bcelInstruction.getTarget().getPosition());
		this.instruction = instruction;
	}

	@Override
	public void visitGotoInstruction(org.apache.bcel.generic.GotoInstruction bcelInstruction) {
		GotoInstruction instruction = new GotoInstruction();
		instruction.setTargetPosition(bcelInstruction.getTarget().getPosition());
		this.instruction = instruction;
	}

	@Override
	public void visitJsrInstruction(org.apache.bcel.generic.JsrInstruction bcelInstruction) {
		JsrInstruction instruction = new JsrInstruction();
		instruction.setTargetPosition(bcelInstruction.getTarget().getPosition());
		this.instruction = instruction;
	}

	@Override
	public void visitSelect(org.apache.bcel.generic.Select bcelInstruction) {
		SelectInstruction instruction = new SelectInstruction();
		instruction.setTargetPosition(bcelInstruction.getTarget().getPosition());

		int[] targetPositions = new int[bcelInstruction.getTargets().length];
		for (int i = 0; i < targetPositions.length; i++)
			targetPositions[i] = bcelInstruction.getTargets()[i].getPosition();
		instruction.setTargetPositions(targetPositions);
		this.instruction = instruction;
	}

	@Override
	public void visitINVOKEINTERFACE(org.apache.bcel.generic.INVOKEINTERFACE bcelInstruction) {
		InvokeInterface instruction = new InvokeInterface();
		this.instruction = instruction;
	}

	@Override
	public void visitINVOKESPECIAL(org.apache.bcel.generic.INVOKESPECIAL bcelInstruction) {
		InvokeSpecial instruction = new InvokeSpecial();
		instruction.setLoadClass(bcelInstruction.getLoadClassType(cpg).toString());
		instruction.setMethodName(bcelInstruction.getMethodName(cpg));
		instruction.setArgTypes(bcelInstruction.getArgumentTypes(cpg));
		this.instruction = instruction;
	}

	@Override
	public void visitINVOKESTATIC(org.apache.bcel.generic.INVOKESTATIC bcelInstruction) {
		InvokeStatic instruction = new InvokeStatic();
		instruction.setLoadClass(bcelInstruction.getLoadClassType(cpg).toString());
		instruction.setMethodName(bcelInstruction.getMethodName(cpg));
		instruction.setArgTypes(bcelInstruction.getArgumentTypes(cpg));
		this.instruction = instruction;
	}

	@Override
	public void visitINVOKEVIRTUAL(org.apache.bcel.generic.INVOKEVIRTUAL bcelInstruction) {
		InvokeVirtual instruction = new InvokeVirtual();
		this.instruction = instruction;
	}

	@Override
	public void visitNEW(org.apache.bcel.generic.NEW bcelInstruction) {
		New instruction = new New();
		instruction.setType(bcelInstruction.getLoadClassType(cpg).toString());
		this.instruction = instruction;
	}

	@Override
	public void visitNEWARRAY(org.apache.bcel.generic.NEWARRAY bcelInstruction) {
		NewArray instruction = new NewArray();
		this.instruction = instruction;
	}

	@Override
	public void visitANEWARRAY(org.apache.bcel.generic.ANEWARRAY bcelInstruction) {
		ANewArray instruction = new ANewArray();
		this.instruction = instruction;
	}

	@Override
	public void visitMULTIANEWARRAY(org.apache.bcel.generic.MULTIANEWARRAY bcelInstruction) {
		MultiANewArray instruction = new MultiANewArray();
		this.instruction = instruction;
	}

	@Override
	public void visitDUP(org.apache.bcel.generic.DUP bcelInstruction) {
		DUP instruction = new DUP();
		this.instruction = instruction;
	}

	@Override
	public void visitDUP_X1(org.apache.bcel.generic.DUP_X1 bcelInstruction) {
		DUP_X1 instruction = new DUP_X1();
		this.instruction = instruction;
	}

	@Override
	public void visitDUP_X2(org.apache.bcel.generic.DUP_X2 bcelInstruction) {
		DUP_X2 instruction = new DUP_X2();
		this.instruction = instruction;
	}

	@Override
	public void visitDUP2(org.apache.bcel.generic.DUP2 bcelInstruction) {
		DUP2 instruction = new DUP2();
		this.instruction = instruction;
	}

	@Override
	public void visitDUP2_X1(org.apache.bcel.generic.DUP2_X1 bcelInstruction) {
		DUP2_X1 instruction = new DUP2_X1();
		this.instruction = instruction;
	}

	@Override
	public void visitDUP2_X2(org.apache.bcel.generic.DUP2_X2 bcelInstruction) {
		DUP2_X2 instruction = new DUP2_X2();
		this.instruction = instruction;
	}

	@Override
	public void visitSWAP(org.apache.bcel.generic.SWAP bcelInstruction) {
		SWAP instruction = new SWAP();
		this.instruction = instruction;
	}

	@Override
	public void visitARETURN(org.apache.bcel.generic.ARETURN bcelInstruction) {
		Areturn instruction = new Areturn();
		this.instruction = instruction;
	}

	@Override
	public void visitDRETURN(org.apache.bcel.generic.DRETURN bcelInstruction) {
		Dreturn instruction = new Dreturn();
		this.instruction = instruction;
	}

	@Override
	public void visitFRETURN(org.apache.bcel.generic.FRETURN bcelInstruction) {
		Freturn instruction = new Freturn();
		this.instruction = instruction;
	}

	@Override
	public void visitLRETURN(org.apache.bcel.generic.LRETURN bcelInstruction) {
		Lreturn instruction = new Lreturn();
		this.instruction = instruction;
	}

	@Override
	public void visitRETURN(org.apache.bcel.generic.RETURN bcelInstruction) {
		Return instruction = new Return();
		this.instruction = instruction;
	}
}
