package de.htwg_konstanz.jea.vm;

import java.util.Stack;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.jcip.annotations.Immutable;

@Immutable
@EqualsAndHashCode
public final class OpStack {
	private final Stack<Slot> stack;

	public OpStack() {
		stack = new Stack<Slot>();
	}

	public OpStack(@NonNull OpStack original) {
		Slot[] stackArray = original.stack.toArray(new Slot[0]);
		this.stack = new Stack<Slot>();
		for (Slot slot : stackArray) {
			stack.add(slot.copy());
		}
	}

	public OpStack push(@NonNull Slot slot) {
		OpStack result = new OpStack(this);
		result.stack.push(slot);
		return result;
	}

	public OpStack push(Slot slot, int n) {
		OpStack result = new OpStack(this);
		for (int i = 0; i < n; i++)
			result = result.push(slot);
		return result;
	}

	public OpStack pop() {
		OpStack result = new OpStack(this);
		result.stack.pop();
		return result;
	}

	public Slot peek() {
		return stack.peek();
	}

	public OpStack pop(int n) {
		OpStack result = new OpStack(this);
		for (int i = 0; i < n; i++)
			result = result.pop();
		return result;
	}

	public Slot get(int index) {
		return stack.get(index);
	}

	public int size() {
		return stack.size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("S<");
		boolean inside = false;
		for (Slot slot : stack) {
			if (inside)
				builder.append(", " + slot);
			else {
				builder.append(slot);
				inside = true;
			}
		}
		builder.append(">");
		return new String(builder);
	}

}
