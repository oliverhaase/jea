package de.htwg_konstanz.jea.nj;

import lombok.Setter;

import org.apache.bcel.generic.InstructionHandle;

public class NjInstruction {
	@Setter
	private NjMethod parent;
	private final InstructionHandle ih;

	public NjInstruction(InstructionHandle ih) {
		this.ih = ih;
	}

	public NjInstruction next() {
		return parent.nextInstruction(this);
	}

	public NjInstruction previous() {
		return parent.previousInstruction(this);
	}

}
