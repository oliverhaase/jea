package de.htwg_konstanz.jea;

public class TccTestClass {
	public final static int NUMBER = 42;
	private Object ref;

	private void private1(Object arg) {
		ref = arg;
	}

	void f(String ex) {
		ex = new String();
	}

	int g(Object[] ex) {
		ex[0] = new Object();
		return 0;
	}

	double h(Object[] ex) {
		ref = new Byte((byte) 42);
		return 3.14;
	}

	void i() {
		new Character('c');
		private1(null);
	}

	void j() {
		Integer i = new Integer(42);
		private1(i);
	}

	void k(int i) {
		while (true) {
			Object o = new Object();
		}
	}
	
	void l() {
		Object ref = this.ref;
	}

}
