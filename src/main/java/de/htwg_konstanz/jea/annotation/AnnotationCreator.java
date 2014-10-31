package de.htwg_konstanz.jea.annotation;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public interface AnnotationCreator {
	Annotation convertToAnnotation(ConstPool cp);
}
