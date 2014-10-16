package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.jcip.annotations.Immutable;

@Immutable
@EqualsAndHashCode
public class ReferenceNode implements NonObjectNode, Slot {
	public static enum Category {
		ARG, LOCAL, GLOBAL, RETURN
	};

	private final static ReferenceNode GLOBAL_REF = new ReferenceNode(-1, Category.GLOBAL);
	private final static ReferenceNode RETURN_REF = new ReferenceNode(-1, Category.RETURN);

	@Getter
	private final String id;

	public ReferenceNode(int id, @NonNull Category category) {
		this.id = category.toString() + id;
	}

	public static ReferenceNode getGlobalRef() {
		return GLOBAL_REF;
	}

	public static ReferenceNode getReturnRef() {
		return RETURN_REF;
	}

	@Override
	public String toString() {
		return id;
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
