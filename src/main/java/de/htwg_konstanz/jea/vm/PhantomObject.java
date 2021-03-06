package de.htwg_konstanz.jea.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.annotation.CheckReturnValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.annotation.AnnotationCreator;
import de.htwg_konstanz.jea.annotation.AnnotationHelper;
import de.htwg_konstanz.jea.annotation.PhantomObjectAnnotation;

@EqualsAndHashCode(callSuper = true)
public final class PhantomObject extends ObjectNode implements AnnotationCreator {
	@Getter
	private final int index;
	@Getter
	private final String parent;
	@Getter
	private final String fieldName;

	private PhantomObject(int index, EscapeState escapeState) {
		super("p" + index, escapeState);
		this.index = index;
		this.parent = null;
		this.fieldName = null;
	}

	private PhantomObject(String id, EscapeState escapeState, int index, String parent,
			String fieldName) {
		super(id, escapeState);
		this.index = index;
		this.parent = parent;
		this.fieldName = fieldName;
	}

	private PhantomObject(String id, String parent, String fieldName, EscapeState escapeState) {
		super(id, escapeState);
		this.index = -1;
		this.parent = parent;
		this.fieldName = fieldName;
	}

	public static PhantomObject newPhantomObject(int index) {
		return new PhantomObject(index, EscapeState.ARG_ESCAPE);
	}

	public static PhantomObject newSubPhantom(ObjectNode parent, String fieldName) {
		return new PhantomObject(parent.getId() + "." + fieldName, parent.getId(), fieldName,
				parent.getEscapeState());
	}

	/**
	 * Creates an PhantomObject instance from an PhantomObjectAnnotation.
	 * 
	 * @param a
	 *            the PhantomObjectAnnotation
	 * @return the PhantomObject representation
	 */
	public static PhantomObject newInstanceByAnnotation(@NonNull PhantomObjectAnnotation a) {
		// special treatment for null-able fields
		String id = convertStringToNull(a.id());
		String escapeState = convertStringToNull(a.escapeState());
		String parentId = convertStringToNull(a.parentID());
		String fieldName = convertStringToNull(a.fieldName());

		return new PhantomObject(id, EscapeState.getFromString(escapeState), a.index(), parentId,
				fieldName);
	}

	private static String convertNullToString(String value) {
		return value == null ? "NULLSTRING" : value;
	}

	private static String convertStringToNull(@NonNull String value) {
		return value.equals("NULLSTRING") ? null : value;
	}

	@Override
	@CheckReturnValue
	public PhantomObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			if (this.index != -1)
				return new PhantomObject(this.getIndex(), escapeState);
			else
				return new PhantomObject(this.getId(), this.parent, fieldName, escapeState);

		return this;
	}

	public boolean isSubPhantom() {
		return parent != null;
	}

	@Override
	public String toString() {
		return this.getId() + getEscapeState().toString();
	}

	@Override
	public boolean isGlobal() {
		return false;
	}

	/**
	 * Creates a {@reference=PhantomObjectAnnotation} representation of this
	 * PhantomObjectnested in a
	 * {@reference=javassist.bytecode.annotation.Annotation}.
	 * 
	 * @return
	 */
	@Override
	public Annotation convertToAnnotation(ConstPool cp) {
		// special treatment for null-able fields
		String id = convertNullToString(getId());
		String escapeState = convertNullToString(Objects.toString(getEscapeState()));
		String parentId = convertNullToString(parent);
		String fieldName = convertNullToString(this.fieldName);

		Map<String, MemberValue> values = new HashMap<>();
		values.put("id", new StringMemberValue(id, cp));
		values.put("escapeState", new StringMemberValue(escapeState, cp));
		values.put("index", new IntegerMemberValue(cp, index));
		values.put("parentID", new StringMemberValue(parentId, cp));
		values.put("fieldName", new StringMemberValue(fieldName, cp));
		return AnnotationHelper.createAnnotation(values, PhantomObjectAnnotation.class.getName(),
				cp);
	}

}
