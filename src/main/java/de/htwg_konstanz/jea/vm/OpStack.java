package de.htwg_konstanz.jea.vm;

import java.util.Stack;

public final class OpStack {
	private final Stack<Slot> stack;

	public OpStack() {
		stack = new Stack<Slot>();
	}

	public OpStack(OpStack original) {
		if (original == null)
			throw new NullPointerException("original stack must not be null");

		Slot[] stackArray = original.stack.toArray(new Slot[0]);
		this.stack = new Stack<Slot>();
		for (Slot slot : stackArray) {
			stack.add(slot.copy());
		}
	}

	public OpStack push(Slot slot) {
		if (slot == null)
			throw new NullPointerException("slot must not be null");

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

	public OpStack typedPush(Slot slot) {
		if (slot == null)
			throw new NullPointerException("slot must not be null");

		OpStack result = new OpStack(this);

		for (int i = 0; i < slot.size(); i++)
			result = result.push(slot);

		return result;
	}

	public OpStack pop() {
		OpStack result = new OpStack(this);
		result.stack.pop();
		return result;
	}

	public OpStack typedPop() {
		return pop(stack.peek().size());
	}

	public OpStack pop(int n) {
		OpStack result = new OpStack(this);
		for (int i = 0; i < n; i++)
			result = result.pop();
		return result;
	}

	public Slot peek() {
		return stack.peek();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + stack.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof OpStack))
			return false;
		OpStack other = (OpStack) obj;
		return stack.equals(other.stack);
	}

}
