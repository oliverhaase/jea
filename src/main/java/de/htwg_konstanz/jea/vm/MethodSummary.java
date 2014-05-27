package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import net.jcip.annotations.Immutable;

@Immutable
@EqualsAndHashCode
public class MethodSummary {
	private final static MethodSummary ALIEN_SUMMARY = new MethodSummary();

	public MethodSummary() {

	}

	public MethodSummary(ConnectionGraph cg) {

	}

	public MethodSummary(ConnectionGraph cg, Set<ObjectNode> resultObjects) {

	}

	public static MethodSummary getAlienSummary() {
		return ALIEN_SUMMARY;
	}

	public boolean isAlien() {
		return this == ALIEN_SUMMARY;
	}

	public MethodSummary merge(MethodSummary other) {
		return this;
	}

	public Set<String> getEscapingTypes() {
		return new HashSet<String>();
	}

}
