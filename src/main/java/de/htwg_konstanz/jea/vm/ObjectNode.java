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

	public static ObjectNode newInternalObjectNode(String id) {
		return new ObjectNode(id, EscapeState.NO_ESCAPE);
	}

	public static ObjectNode newSubObjectNode(ObjectNode origin, String fieldName) {
		return new ObjectNode(origin.id + "." + fieldName, origin.escapeState);
	}

	public static ObjectNode newGlobalObjectNode() {
		return new ObjectNode("global", EscapeState.GLOBAL_ESCAPE);
	}

	public ObjectNode increaseEscapeState(EscapeState escapeState) {
		if (this.escapeState.moreConfinedThan(escapeState))
			return new ObjectNode(getId(), escapeState);
		return this;
	}

	@Override
	public String toString() {
		if (escapeState == EscapeState.GLOBAL_ESCAPE)
			return id + "^";
		if (escapeState == EscapeState.ARG_ESCAPE)
			return id + ">";
		return id + "v";
	}

}
