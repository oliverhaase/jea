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
import net.jcip.annotations.Immutable;
import de.htwg_konstanz.jea.annotation.AnnotationHelper;
import de.htwg_konstanz.jea.annotation.ReferenceNodeAnnotation;

@Immutable
@EqualsAndHashCode
public class ReferenceNode implements NonObjectNode, Slot {
	public static enum Category {
		ARG, LOCAL, GLOBAL, RETURN
	};

	private final static ReferenceNode GLOBAL_REF = new ReferenceNode(-1,
			Category.GLOBAL);
	private final static ReferenceNode RETURN_REF = new ReferenceNode(-1,
			Category.RETURN);

	@Getter
	private final String id;

	public ReferenceNode(int id, @NonNull Category category) {
		this.id = category.toString() + id;
	}

	private ReferenceNode(String id) {
		this.id = id;
	}

	public static ReferenceNode getGlobalRef() {
		return GLOBAL_REF;
	}

	public static ReferenceNode getReturnRef() {
		return RETURN_REF;
	}

	/**
	 * Creates an ReferenceNode instance from an ReferenceNodeAnnotation.
	 * 
	 * @param a
	 *            the ReferenceNodeAnnotation
	 * @return the ReferenceNode representation
	 */
	public static ReferenceNode newInstanceByAnnotation(
			@NonNull ReferenceNodeAnnotation a) {
		return new ReferenceNode(a.id());
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public Slot copy() {
		return this;
	}

	@Override
	public int size() {
		return 1;
	}

	/**
	 * Creates a {@reference=ReferenceNodeAnnotation} representation of this
	 * ReferenceNode nested in a
	 * {@reference=javassist.bytecode.annotation.Annotation}.
	 * 
	 * @return
	 */
	public Annotation convertToAnnotation(ConstPool cp) {
		Map<String, MemberValue> values = new HashMap<>();
		values.put("id", new StringMemberValue(id, cp));
		return AnnotationHelper.createAnnotation(values,
				ReferenceNodeAnnotation.class.getName(), cp);
	}
}
