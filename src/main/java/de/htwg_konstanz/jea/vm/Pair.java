package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class Pair<T, K> {
	@Getter
	private final T value1;
	@Getter
	private final K value2;

	public Pair(@NonNull T value1, @NonNull K value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public String toString() {
		return "(" + value1 + ", " + value2 + ")";
	}

}
