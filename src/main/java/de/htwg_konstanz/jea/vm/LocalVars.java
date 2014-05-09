package de.htwg_konstanz.jea.vm;

import java.util.Arrays;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class LocalVars {
	private final Slot[] vars;

	public LocalVars(Slot[] vars) {
		if (vars == null)
			throw new NullPointerException("arg to LocalVars.<init> must not be null");

		this.vars = new Slot[vars.length];

		for (int i = 0; i < vars.length; i++)
			this.vars[i] = (vars[i] == null) ? null : vars[i].copy();
	}

	public LocalVars(LocalVars original) {
		vars = new Slot[original.vars.length];

		for (int i = 0; i < vars.length; i++)
			vars[i] = (original.vars[i] == null) ? null : original.vars[i].copy();
	}

	public Slot get(int index) {
		return vars[index];
	}

	public LocalVars set(int index, Slot slot) {
		LocalVars result = new LocalVars(this);
		result.vars[index] = slot;
		return result;
	}

	@Override
	public String toString() {
		return "L" + Arrays.toString(vars);
	}

}
