package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;

public class EscapingTypes {
	private final static EscapingTypes ALL_TYPES = new EscapingTypes();

	private final Set<String> types = new HashSet<String>();

	public void add(String type) {
		if (this == ALL_TYPES)
			throw new AssertionError("nothing can be added to ALL_TYPES");
		types.add(type);
	}

	public boolean contains(String type) {
		if (this == ALL_TYPES)
			return true;

		for (String aType : types)
			if (aType.equals(type))
				return true;

		return false;
	}

	@CheckReturnValue
	public EscapingTypes merge(EscapingTypes other) {
		if (this == ALL_TYPES || other == ALL_TYPES)
			return ALL_TYPES;

		EscapingTypes result = new EscapingTypes();
		result.types.addAll(this.types);
		result.types.addAll(other.types);

		return result;
	}

	public static EscapingTypes getAllTypes() {
		return ALL_TYPES;
	}

	@Override
	public String toString() {
		if (this == ALL_TYPES)
			return "all types";

		return types.toString();
	}

}
