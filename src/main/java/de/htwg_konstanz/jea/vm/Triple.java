package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode
public class Triple<T, K, M> {
	@Getter
	private final T value1;
	@Getter
	private final K value2;
	@Getter
	private final M value3;

	public Triple(@NonNull T value1, @NonNull K value2, @NonNull M value3) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}

	@Override
	public String toString() {
		return "(" + value1 + ", " + value2 + ", " + value3 + ")";
	}

}
