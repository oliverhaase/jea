package de.htwg_konstanz.jea.vm;

public class ObjectNode implements Node {
	private final String id;
	private final boolean isPhantom;

	public ObjectNode(int id, boolean isPhantom) {
		this.id = isPhantom ? "p" + id : "i" + id;
		this.isPhantom = isPhantom;
	}

	private ObjectNode(String id, boolean isPhantom) {
		this.id = id;
		this.isPhantom = isPhantom;
	}

	public String getID() {
		return id;
	}

	public static ObjectNode newSubObjectNode(ObjectNode origin, String fieldName) {
		return new ObjectNode(origin.id + "." + fieldName, true);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ObjectNode))
			return false;

		ObjectNode other = (ObjectNode) obj;
		return id.equals(other.id);
	}

	@Override
	public String toString() {
		return id;
	}

}
