package de.htwg_konstanz.jea.vm;

import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.annotation.AnnotationCreator;
import de.htwg_konstanz.jea.annotation.AnnotationHelper;
import de.htwg_konstanz.jea.annotation.PointsToEdgesAnnotation;

@EqualsAndHashCode
public class PointToEdge implements AnnotationCreator {
	@Getter
	private final String referenceId;
	@Getter
	private final String objectId;

	public PointToEdge(@NonNull String referenceId, @NonNull String objectId) {
		this.referenceId = referenceId;
		this.objectId = objectId;
	}

	@Override
	public String toString() {
		return "(" + referenceId + ", " + objectId + ")";
	}

	@Override
	public Annotation convertToAnnotation(ConstPool cp) {
		Map<String, MemberValue> values = new HashMap<>();
		values.put("referenceNodeID", new StringMemberValue(referenceId, cp));
		values.put("objectID", new StringMemberValue(objectId, cp));
		return AnnotationHelper.createAnnotation(values, PointsToEdgesAnnotation.class.getName(),
				cp);
	}

}
