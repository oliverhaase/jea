package de.htwg_konstanz.jea.vm;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Triple<T, K, M> {
	private final T value1;
	private final K value2;
	private final M value3;
	private final String value4;

	public Triple(T value1, K value2, M value3, String value4) {
		if (value1 == null || value2 == null || value3 == null)
			throw new NullPointerException("Triple components must not be null");

		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
	}

	public T getValue1() {
		return value1;
	}

	public K getValue2() {
		return value2;
	}

	public M getValue3() {
		return value3;
	}

	@Override
	public String toString() {
		return "(" + value1 + ", " + value2 + ", " + value3 + ")";
	}

	// @Override
	// public int hashCode() {
	// final int prime = 31;
	// int result = 1;
	// result = prime * result + value1.hashCode();
	// result = prime * result + value2.hashCode();
	// result = prime * result + value3.hashCode();
	// return result;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj) {
	// return true;
	// }
	//
	// if (!(obj instanceof Triple<?, ?, ?>)) {
	// return false;
	// }
	// Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
	//
	// return value1.equals(other.value1) && value2.equals(other.value2)
	// && value3.equals(other.value3);
	// }

	public static final void main(String[] args) {
		String value1 = "hello";
		int i = 28121965;
		Integer value2a = new Integer(28121965);
		Integer value2b = new Integer(i);
		Double value3 = new Double(3.14);

		Triple<String, Integer, Double> triple1 = new Triple<>(value1, value2a, value3, "world");
		Triple<String, Integer, Double> triple2 = new Triple<>(value1, value2b, value3, "world");

		System.out.println("triple 1 und triple 2 sind "
				+ (triple1.equals(triple2) ? "gleich" : "ungleich"));

	}
}
