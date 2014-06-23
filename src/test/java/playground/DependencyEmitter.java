package playground;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;

public class DependencyEmitter extends EmptyVisitor {
	private final JavaClass clazz;

	public DependencyEmitter(JavaClass clazz) {
		this.clazz = clazz;
	}

	@Override
	public void visitConstantClass(ConstantClass obj) {
		System.out.println(obj.getBytes(clazz.getConstantPool()));
	}

	public static void main(String[] args) throws ClassNotFoundException {
		JavaClass clazz = Repository.lookupClass("de.htwg_konstanz.jea.TccTestClass");
		DependencyEmitter de = new DependencyEmitter(clazz);
		DescendingVisitor descVisitor = new DescendingVisitor(clazz, de);
		descVisitor.visit();
	}
}
