package de.htwg_konstanz.jea.vm;

import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.annotation.CheckReturnValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.annotation.AnnotationHelper;
import de.htwg_konstanz.jea.annotation.PhantomObjectAnnotation;

@EqualsAndHashCode(callSuper = true)
public final class PhantomObject extends ObjectNode {
	@Getter
	private final int index;
	@Getter
	private final ObjectNode parent;
	@Getter
	private final String fieldName;

	private PhantomObject(int index, EscapeState escapeState) {
		super("p" + index, escapeState);
		this.index = index;
		this.parent = null;
		this.fieldName = null;
	}

	private PhantomObject(String id, EscapeState escapeState, int index,
			ObjectNode parent, String fieldName) {
		super(id, escapeState);
		this.index = index;
		this.parent = parent;
		this.fieldName = fieldName;
	}

	private PhantomObject(String id, ObjectNode parent, String fieldName,
			EscapeState escapeState) {
		super(id, escapeState);
		this.index = -1;
		this.parent = parent;
		this.fieldName = fieldName;
	}

	public static PhantomObject newPhantomObject(int index) {
		return new PhantomObject(index, EscapeState.ARG_ESCAPE);
	}

	public static PhantomObject newSubPhantom(ObjectNode parent,
			String fieldName) {
		return new PhantomObject(parent.getId() + "." + fieldName, parent,
				fieldName, parent.getEscapeState());
	}

	/**
	 * Creates an PhantomObject instance from an PhantomObjectAnnotation.
	 * 
	 * @param a
	 *            the PhantomObjectAnnotation
	 * @return the PhantomObject representation
	 */
	public static PhantomObject newInstanceByAnnotation(
			@NonNull PhantomObjectAnnotation a) {
		// TODO replace NullObject with a.parentID()
		return new PhantomObject(a.id(), EscapeState.getFromString(a
				.escapeState()), a.index(), InternalObject.getNullObject(),
				a.fieldName());
	}

	@Override
	@CheckReturnValue
	public PhantomObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			if (this.index != -1)
				return new PhantomObject(this.getIndex(), escapeState);
			else
				return new PhantomObject(this.getId(), this.parent, fieldName,
						escapeState);

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
	public Annotation convertToAnnotation(ConstPool cp) {
		Map<String, MemberValue> values = new HashMap<>();
		values.put("id", new StringMemberValue(getId(), cp));
		values.put("escapeState", new StringMemberValue(getEscapeState()
				.toString(), cp));
		values.put("index", new IntegerMemberValue(index, cp));
		// TODO replace "" with parentId
		values.put("parentID", new StringMemberValue("", cp));
		values.put("fieldName", new StringMemberValue(fieldName, cp));
		return AnnotationHelper.createAnnotation(values,
				PhantomObjectAnnotation.class.getName(), cp);
	}

}
