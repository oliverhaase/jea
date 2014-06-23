package de.htwg_konstanz.jea.nj;

import java.util.List;
import java.util.Vector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NjMethod {
	@Getter
	private final org.apache.bcel.classfile.Method bcelMethod;

	private final List<NjInstruction> instructions = new Vector<>();
	private NjEntryPoint entry;
	private NjExitPoint exit;

	public void setEntry(NjEntryPoint entry) {
		this.entry = entry;
		entry.setParent(this);
	}

	public void setExit(NjExitPoint exit) {
		this.exit = exit;
		exit.setParent(this);
	}

	public void addInstruction(NjInstruction instruction) {
		instructions.add(instruction);
		instruction.setParent(this);
	}

	public NjInstruction nextInstruction(NjInstruction instruction) {
		return (instructions.indexOf(instruction) == instructions.size() - 1) ? null : instructions
				.get(instructions.indexOf(instruction) + 1);
	}

	public NjInstruction previousInstruction(NjInstruction instruction) {
		return (instructions.indexOf(instruction) == 0) ? null : instructions.get(instructions
				.indexOf(instruction) - 1);
	}
}
