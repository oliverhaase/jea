package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class FieldNode implements NonObjectNode {
	@Getter
	private final String name;
	private final String originID;

	public FieldNode(@NonNull String name, @NonNull String originID) {
		this.name = name;
		this.originID = originID;
	}

	@Override
	public String toString() {
		return name;
	}

}
