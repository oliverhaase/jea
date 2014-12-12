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
import de.htwg_konstanz.jea.annotation.FieldEdgeAnnotation;

@EqualsAndHashCode
public class FieldEdge implements AnnotationCreator {
	@Getter
	private final String originId;
	@Getter
	private final String fieldName;
	@Getter
	private final String destinationId;

	public FieldEdge(@NonNull String originId, @NonNull String fieldName,
			@NonNull String destinationId) {
		if (originId.equals("null"))
			throw new AssertionError("assigned field to null");

		this.originId = originId;
		this.fieldName = fieldName;
		this.destinationId = destinationId;
	}

	/**
	 * Creates a FieldEdge instance from a FieldEdgeAnnotation.
	 * 
	 * @param a
	 *            the FieldEdgeAnnotation
	 * @return the FieldEdge representation
	 */
	public static FieldEdge newInstanceByAnnotation(@NonNull FieldEdgeAnnotation a) {
		return new FieldEdge(a.originId(), a.fieldName(), a.destinationId());
	}

	@Override
	public String toString() {
		return "(<" + originId + ">.<" + fieldName + "> = <" + destinationId + ">)";
	}

	/**
	 * Creates a {@reference=FieldEdgeAnnotation} representation of this
	 * FieldEdge nested in a
	 * {@reference=javassist.bytecode.annotation.Annotation}
	 * 
	 * @return
	 */
	@Override
	public Annotation convertToAnnotation(ConstPool cp) {
		Map<String, MemberValue> values = new HashMap<>();
		values.put("originId", new StringMemberValue(originId, cp));
		values.put("fieldName", new StringMemberValue(fieldName, cp));
		values.put("destinationId", new StringMemberValue(destinationId, cp));

		return AnnotationHelper.createAnnotation(values, FieldEdgeAnnotation.class.getName(), cp);
	}

}
