package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.jcip.annotations.Immutable;

@Immutable
@EqualsAndHashCode(exclude = { "escapeState" })
public class ObjectNode implements Node {
	private final static ObjectNode global = new ObjectNode("global", EscapeState.GLOBAL_ESCAPE,
			null, false);

	@Getter
	private final String id;
	@Getter
	private final EscapeState escapeState;
	@Getter
	private final String type;
	@Getter
	private final boolean isPhantom;

	private ObjectNode(String id, EscapeState escapeState, String type, boolean isPhantom) {
		this.id = id;
		this.escapeState = escapeState;
		this.type = type;
		this.isPhantom = isPhantom;
	}

	public static ObjectNode newPhantomObjectNode(int id) {
		return new ObjectNode("p" + id, EscapeState.ARG_ESCAPE, null, true);
	}

	public static ObjectNode newInternalObjectNode(String id, String type) {
		return new ObjectNode(id, EscapeState.NO_ESCAPE, type, false);
	}

	public static ObjectNode newSubObjectNode(ObjectNode origin, String fieldName) {
		return new ObjectNode(origin.id + "." + fieldName, origin.escapeState, null, true);
	}

	public static ObjectNode getGlobalObjectNode() {
		return global;
	}

	public ObjectNode increaseEscapeState(EscapeState escapeState) {
		if (this.escapeState.moreConfinedThan(escapeState))
			return new ObjectNode(this.getId(), escapeState, this.type, this.isPhantom);
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
