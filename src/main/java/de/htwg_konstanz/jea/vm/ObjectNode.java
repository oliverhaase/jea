package de.htwg_konstanz.jea.vm;

public class ObjectNode implements Node {
	private final String id;
	private EscapeState escapeState;

	private ObjectNode(String id, EscapeState escapeState) {
		this.id = id;
		this.escapeState = escapeState;
	}

	public static ObjectNode newPhantomObjectNode(int id) {
		return new ObjectNode("p" + id, EscapeState.ARG_ESCAPE);
	}

	public static ObjectNode newInternalObjectNode(int id) {
		return new ObjectNode("i" + id, EscapeState.NO_ESCAPE);
	}

	public static ObjectNode newSubObjectNode(ObjectNode origin, String fieldName) {
		return new ObjectNode(origin.id + "." + fieldName, origin.escapeState);
	}

	public static ObjectNode newGlobalObjectNode() {
		return new ObjectNode("global", EscapeState.GLOBAL_ESCAPE);
	}

	public String getID() {
		return id;
	}

	public EscapeState getEscapeState() {
		return escapeState;
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
