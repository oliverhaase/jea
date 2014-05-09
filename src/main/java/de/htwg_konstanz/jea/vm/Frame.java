package de.htwg_konstanz.jea.vm;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public final class Frame {
	@Getter
	private final LocalVars localVars;
	@Getter
	private final OpStack opStack;
	@Getter
	private final ConnectionGraph cg;
	@Getter
	private Slot returnValue;

	public Frame(@NonNull LocalVars localVars, @NonNull OpStack opStack,
			@NonNull ConnectionGraph cg, Slot returnValue) {
		this.localVars = localVars;
		this.opStack = opStack;
		this.cg = cg;
		this.returnValue = returnValue;
	}

	public Frame(Set<Integer> indexes, int maxLocals) {
		Slot[] vars = new Slot[maxLocals];

		// initialize local vars
		for (int i = 0; i < vars.length; i++) {
			vars[i] = DontCareSlot.NORMAL_SLOT;
		}

		// have ConnectionGraph overwrite reference local vars
		cg = new ConnectionGraph(indexes, vars);

		localVars = new LocalVars(vars);
		opStack = new OpStack();
	}

	@Override
	public String toString() {
		return localVars + "| " + opStack + "| " + cg
				+ ((returnValue != null) ? (", " + returnValue) : "");
	}

}
