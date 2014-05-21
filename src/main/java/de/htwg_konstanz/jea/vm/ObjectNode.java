package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(exclude = { "escapeState" })
public class ObjectNode implements Node {
	@Getter
	private final String id;
	@Getter
	private final EscapeState escapeState;

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

	@Override
	public String toString() {
		return id;
	}

}
