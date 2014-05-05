package de.htwg_konstanz.jea.vm;

public enum DontCareSlot implements Slot {
	VOID_SLOT(0), NORMAL_SLOT(1), DOUBLE_SLOT(2);

	private int size;

	private DontCareSlot(int size) {
		this.size = size;
	}

	@Override
	public Slot copy() {
		return this;
	}

	@Override
	public int size() {
		return size;
	}
}
