package de.htwg_konstanz.jea.vm;

public class ReferenceNode implements NonObjectNode, Slot {
	// private final static ReferenceNode NULL_REFERENCE = new
	// ReferenceNode(-1);

	public static enum Category {
		ARG, LOCAL;
	};

	private final Category category;
	private final int id;

	public ReferenceNode(int id, Category category) {
		if (category == null)
			throw new NullPointerException();

		this.id = id;
		this.category = category;
	}

	// public ReferenceNode getNullReference() {
	// return NULL_REFERENCE;
	// }
	//
	// public boolean isNullReference() {
	// return this == NULL_REFERENCE;
	// }

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + category.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ReferenceNode))
			return false;

		ReferenceNode other = (ReferenceNode) obj;
		return id == other.id && category == other.category;
	}

}
