package de.htwg_konstanz.jea.annotation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import de.htwg_konstanz.jea.vm.Heap;

public final class AnnotationHelper {

	private AnnotationHelper() {
		throw new AssertionError("May not be called");
	}

	public static boolean writeAnnotationToMethod(CtClass clazz,
			CtMethod method, Annotation annotation, ConstPool cp) {
		AnnotationsAttribute attribute = new AnnotationsAttribute(cp,
				AnnotationsAttribute.visibleTag);
		attribute.addAnnotation(annotation);
		method.getMethodInfo().addAttribute(attribute);
		boolean success = true;
		try {
			clazz.writeFile();
		} catch (NotFoundException | IOException | CannotCompileException e) {
			success = false;
			e.printStackTrace();
		}
		;
		return success;
	}

	public static Annotation createAnnotation(Map<String, MemberValue> values,
			String annotationTypeName, ConstPool cp) {
		Annotation annotation = new Annotation(annotationTypeName, cp);
		for (String key : values.keySet()) {
			annotation.addMemberValue(key, values.get(key));
		}

		return annotation;
	}

	public static ArrayMemberValue convertToAnnotationArray(
			List<MemberValue> list, ConstPool cp) {
		ArrayMemberValue value = new ArrayMemberValue(
				new AnnotationMemberValue(cp), cp);
		value.setValue(list.toArray(new AnnotationMemberValue[list.size()]));
		return value;
	}

	public static ArrayMemberValue convertToStringArray(List<MemberValue> list,
			ConstPool cp) {
		ArrayMemberValue value = new ArrayMemberValue(
				new StringMemberValue(cp), cp);
		value.setValue(list.toArray(new AnnotationMemberValue[list.size()]));
		return value;
	}

	/**
	 * Example how to write an annotation to file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RuntimeException
	 * @throws NotFoundException
	 * @throws ClassNotFoundException
	 */
	static void write() throws FileNotFoundException, IOException,
			RuntimeException, NotFoundException, ClassNotFoundException {
		ClassPool pool = ClassPool.getDefault();
		// there is something wrong with the paths!!
		CtClass clazz = pool.makeClass(new FileInputStream(
				"src\\test-classes\\playground\\MyTestClass.class"));
		CtMethod method = clazz.getDeclaredMethod("foo");
		ConstPool cp = clazz.getClassFile().getConstPool();

		Heap heap = new Heap();
		Annotation annotation = heap.convertToAnnotation(cp);
		System.out.println(AnnotationHelper.writeAnnotationToMethod(clazz,
				method, annotation, cp));
	}

	/**
	 * example how to read an annotation from file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RuntimeException
	 * @throws NotFoundException
	 * @throws ClassNotFoundException
	 */
	static void read() throws FileNotFoundException, IOException,
			RuntimeException, NotFoundException, ClassNotFoundException {
		ClassPool pool = ClassPool.getDefault();
		CtClass clazz = pool.makeClass(new FileInputStream(
				"playground\\MyTestClass.class"));
		CtMethod method = clazz.getDeclaredMethod("foo");
		MethodSummaryAnnotation anno = (MethodSummaryAnnotation) method
				.getAnnotation(MethodSummaryAnnotation.class);
		System.out.println(Heap.newInstanceByAnnotation(anno).toString());
	}

}
