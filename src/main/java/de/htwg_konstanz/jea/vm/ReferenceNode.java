package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.jcip.annotations.Immutable;

@Immutable
@EqualsAndHashCode
public class ReferenceNode implements NonObjectNode, Slot {
	public static enum Category {
		ARG, LOCAL, GLOBAL, RETURN
	};

	private final Category category;
	private final int id;

	public ReferenceNode(int id, @NonNull Category category) {
		this.id = id;
		this.category = category;
	}

	@Override
	public String toString() {
		return category.toString() + id;
	}

	@Override
	public Slot copy() {
		return this;
	}

	@Override
	public int size() {
		return 1;
	}

}
