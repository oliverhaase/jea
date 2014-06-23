package de.htwg_konstanz.jea.nj;

import java.util.List;
import java.util.Vector;

import lombok.RequiredArgsConstructor;

import org.apache.bcel.classfile.JavaClass;

@RequiredArgsConstructor
public final class NjClass {
	private final JavaClass bcelClass;
	private final List<NjMethod> methods = new Vector<>();;

	public void addMethod(NjMethod method) {
		methods.add(method);
	}

}
