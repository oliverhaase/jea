package de.htwg_konstanz.jea.vm;

import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.annotation.CheckReturnValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.annotation.AnnotationCreator;
import de.htwg_konstanz.jea.annotation.AnnotationHelper;
import de.htwg_konstanz.jea.annotation.InternalObjectAnnotation;

@EqualsAndHashCode(callSuper = true)
public final class InternalObject extends ObjectNode implements AnnotationCreator {
	private final static InternalObject NULL_OBJECT = new InternalObject("null",
			"javax.lang.model.type.NullType", EscapeState.NO_ESCAPE);

	@Getter
	private final String type;

	public InternalObject(@NonNull String id, @NonNull String type, @NonNull EscapeState escapeState) {
		super(id, escapeState);
		this.type = type;
	}

	public static InternalObject getNullObject() {
		return NULL_OBJECT;
	}

	/**
	 * Creates an InternalObject instance from an InternalObjectAnnotation.
	 * 
	 * @param a
	 *            the InternalObjectAnnotation
	 * @return the InternalObject representation
	 */
	public static InternalObject newInstanceByAnnotation(@NonNull InternalObjectAnnotation a) {
		return new InternalObject(a.id(), a.type(), EscapeState.getFromString(a.escapeState()));
	}

	@Override
	@CheckReturnValue
	public InternalObject increaseEscapeState(EscapeState escapeState) {
		if (this.getEscapeState().moreConfinedThan(escapeState))
			return new InternalObject(this.getId(), this.type, escapeState);
		return this;
	}

	@CheckReturnValue
	public InternalObject resetEscapeState() {
		if (EscapeState.NO_ESCAPE.moreConfinedThan(this.getEscapeState()))
			return new InternalObject(this.getId(), this.type, EscapeState.NO_ESCAPE);
		return this;
	}

	@Override
	public String toString() {
		return this.getId() + getEscapeState().toString();
	}

	@Override
	public boolean isGlobal() {
		return getEscapeState() == EscapeState.GLOBAL_ESCAPE;
	}

	/**
	 * Creates an {@reference=InternalObjectAnnotation} representation of this
	 * InternalObject nested in a
	 * {@reference=javassist.bytecode.annotation.Annotation}.
	 * 
	 * @return
	 */
	@Override
	public Annotation convertToAnnotation(ConstPool cp) {
		Map<String, MemberValue> values = new HashMap<>();
		values.put("id", new StringMemberValue(getId(), cp));
		values.put("escapeState", new StringMemberValue(getEscapeState().toString(), cp));
		values.put("type", new StringMemberValue(type, cp));

		return AnnotationHelper.createAnnotation(values, InternalObjectAnnotation.class.getName(),
				cp);
	}

}
