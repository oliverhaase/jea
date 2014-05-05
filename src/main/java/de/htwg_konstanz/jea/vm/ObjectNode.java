package de.htwg_konstanz.jea.vm;

public class ObjectNode implements Node {
	private final String id;

	private ObjectNode(String id) {
		this.id = id;
	}

	public static ObjectNode newPhantomObjectNode(int id) {
		return new ObjectNode("p" + id);
	}

	public static ObjectNode newInternalObjectNode(int id) {
		return new ObjectNode("i" + id);
	}

	public static ObjectNode newSubObjectNode(ObjectNode origin, String fieldName) {
		return new ObjectNode(origin.id + "." + fieldName);
	}

	public String getID() {
		return id;
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
