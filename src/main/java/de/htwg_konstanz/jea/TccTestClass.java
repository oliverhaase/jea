package de.htwg_konstanz.jea;

public class TccTestClass {
	public final static int NUMBER = 42;
	private static TccTestClass staticRef;
	private static Object staticObjectRef;
	private Object ref;

	private void private1(Object arg) {
		ref = arg;
		private2(4);
	}

	private void private2(int n) {
		n++;
	}

	private void private3(Object obj) {
		staticObjectRef = obj;
	}

	private void private4(TccTestClass tcc) {
		tcc.ref = new Short((short) 4);
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

	void m() {
		staticRef = new TccTestClass();
	}

	void n() {
		Object ref = staticRef;
	}

	Object o(int number) {
		Object x;

		if (number == 42) {
			x = new Double(3.14);
			return x;
		}
		x = new Integer(2);
		return x;
	}

	Object p(int number) {
		return new Object();
	}

	void q(TccTestClass x) {
		x.ref = new Object();
		staticRef = x;
	}

	void r() {
		private3(new Float(47.11));
	}

	void s() {
		private4(new TccTestClass());
	}

	void t(TccTestClass tcc) {
		private4(tcc);
	}

	Object u(int n) {
		if (n > 0)
			return new Object();
		else {
			Object result = new Object();
			private3(result);
			return result;
		}

	}

}
