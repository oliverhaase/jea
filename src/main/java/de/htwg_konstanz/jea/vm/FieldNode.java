package de.htwg_konstanz.jea.vm;


public class FieldNode implements NonObjectNode {
	private final String name;
	private final String originID;

	public FieldNode(String name, String originID) {
		if (name == null)
			throw new NullPointerException("name must not be null");
		this.name = name;
		this.originID = originID;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + originID.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof FieldNode))
			return false;
		FieldNode other = (FieldNode) obj;
		return name.equals(other.name) && originID.equals(other.originID);
	}

}
