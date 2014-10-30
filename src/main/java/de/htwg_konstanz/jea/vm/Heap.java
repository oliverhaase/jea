package de.htwg_konstanz.jea.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.annotation.CheckReturnValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import de.htwg_konstanz.jea.annotation.AnnotationHelper;
import de.htwg_konstanz.jea.annotation.FieldEdgeAnnotation;
import de.htwg_konstanz.jea.annotation.InternalObjectAnnotation;
import de.htwg_konstanz.jea.annotation.MethodSummaryAnnotation;
import de.htwg_konstanz.jea.annotation.PhantomObjectAnnotation;
import de.htwg_konstanz.jea.annotation.PointsToEdgesAnnotation;
import de.htwg_konstanz.jea.annotation.ReferenceNodeAnnotation;
import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

@EqualsAndHashCode
public final class Heap {
	private final static Heap ALIEN_GRAPH = new Heap();

	@Getter
	private final ObjectNodes objectNodes = new ObjectNodes();;
	@Getter
	private final Set<FieldEdge> fieldEdges = new HashSet<>();
	@Getter
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
	private final Set<Pair<String, String>> pointsToEdges = new HashSet<>();
	@Getter
	private final ObjectNodes escapedObjects = new ObjectNodes();
	@Getter
	private final ObjectNodes localObjects = new ObjectNodes();

	public Heap() {
	}

	/**
	 * Initializes the Heap with the basic Nodes and the corresponding
	 * references. These are the GlobalObject and - if the return value is a
	 * reference - the ReturnObject. Furthermore there is one PhantomObject for
	 * each argument.
	 * 
	 * @param indexes
	 *            the positions of the reference arguments. The index is used as
	 *            ID for the Objects.
	 * @param hasRefReturnType
	 *            if the return value is of the Type Reference, a new reference
	 *            to the {@code EmptyReturnObjectSet} is added to the Heap
	 */
	public Heap(Set<Integer> indexes, boolean hasRefReturnType) {
		addPointsToEdge(ReferenceNode.getGlobalRef(), GlobalObject.getInstance());

		for (Integer index : indexes) {
			ReferenceNode ref = new ReferenceNode(index, Category.ARG);
			ObjectNode obj = PhantomObject.newPhantomObject(index);

			addPointsToEdge(ref, obj);
		}

		if (hasRefReturnType) {
			addPointsToEdge(ReferenceNode.getReturnRef(), EmptyReturnObjectSet.getInstance());
		}

	}

	/**
	 * Copy Constructor
	 */
	public Heap(Heap original) {
		objectNodes.addAll(original.objectNodes);
		referenceNodes.addAll(original.referenceNodes);

		pointsToEdges.addAll(original.pointsToEdges);
		fieldEdges.addAll(original.fieldEdges);
	}

	/**
	 * Creates a Heap instance from a MethodSummaryAnnotation.
	 * 
	 * @param a
	 *            the MethodSummaryAnnotation
	 * @return the Heap representation
	 */
	public static Heap newInstanceByAnnotation(@NonNull MethodSummaryAnnotation a) {
		Heap heap = new Heap();
		ObjectNodes allNodes = new ObjectNodes();
		allNodes.add(GlobalObject.getInstance());

		for (InternalObjectAnnotation internalObject : a.internalObjects()) {
			allNodes.add(InternalObject.newInstanceByAnnotation(internalObject));
		}
		for (PhantomObjectAnnotation phantomObject : a.phantomObjects()) {
			allNodes.add(PhantomObject.newInstanceByAnnotation(phantomObject));
		}
		for (String argEscapedObjectID : a.argEscapedObjectIDs()) {
			heap.objectNodes.add(allNodes.getObjectNode(argEscapedObjectID));
		}
		for (String globallyEscapedObjectID : a.globallyEscapedObjectIDs()) {
			heap.escapedObjects.add(allNodes.getObjectNode(globallyEscapedObjectID));
		}
		for (String localObjectID : a.localObjectIDs()) {
			heap.localObjects.add(allNodes.getObjectNode(localObjectID));
		}
		for (FieldEdgeAnnotation fieldEdge : a.fieldEdges()) {
			heap.fieldEdges.add(FieldEdge.newInstanceByAnnotation(fieldEdge));
		}
		for (ReferenceNodeAnnotation referenceNode : a.referenceNodes()) {
			heap.referenceNodes.add(ReferenceNode.newInstanceByAnnotation(referenceNode));
		}
		for (PointsToEdgesAnnotation pointsToEdge : a.pointsToEdges()) {
			heap.pointsToEdges.add(new Pair<String, String>(pointsToEdge.referenceNodeID(),
					pointsToEdge.objectID()));
		}
		return heap;
	}

	/**
	 * As {@code ref} can point to multiple objects, the Set of referneced
	 * objects is returned.
	 * 
	 * @return the objects referenced from {@code ref}
	 */
	public Set<ObjectNode> dereference(ReferenceNode ref) {
		Set<ObjectNode> result = new HashSet<>();
		for (Pair<String, String> pointsToEdge : pointsToEdges)
			if (pointsToEdge.getValue1().equals(ref.getId()))
				result.add(objectNodes.getObjectNode(pointsToEdge.getValue2()));
		return result;
	}

	/**
	 * To publish an object its EscapeState is set to GLOBAL_ESCAPE. To publish
	 * child objects too, propagateEscpaeState() should be called.
	 * 
	 * @param ref
	 *            the ReferenceNode to publish
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	public Heap publish(ReferenceNode ref) {
		Heap result = new Heap(this);

		for (ObjectNode object : dereference(ref)) {
			result.objectNodes.increaseEscapeState(object, EscapeState.GLOBAL_ESCAPE);
		}

		return result;
	}

	/**
	 * Assigns the {@code value} to the field {@code fieldName} of {@code obj}
	 * {@code (obj.fieldName = value)}. If the {@code value} doesn't exist
	 * already in this Heap, it is added. If the {@code obj} is NULL no
	 * fieldEdge is added. If the {@code obj} doesn't exist a
	 * NoSuchElementException is thrown.
	 */
	@CheckReturnValue
	public Heap addField(ObjectNode obj, String fieldName, ObjectNode value) {
		// if (obj.isGlobal())
		// return this;
		Heap result = new Heap(this);

		if (obj.equals(InternalObject.getNullObject()))
			// throw new AssertionError("assigned field to null " + obj + "." +
			// fieldName + "=" + value);
			return result;

		if (!objectNodes.existsObject(obj.getId()))
			throw new NoSuchElementException("To the object " + obj
					+ " should be added a field, but it bedoesn't exist in this Heap");

		if (!objectNodes.existsObject(value.getId()))
			result.objectNodes.add(value);

		result.fieldEdges.add(new FieldEdge(obj.getId(), fieldName, value.getId()));
		return result;
	}

	/**
	 * Adds the reference and the object to the Heap and links them.
	 */
	private void addPointsToEdge(ReferenceNode ref, ObjectNode obj) {
		referenceNodes.add(ref);
		objectNodes.add(obj);
		pointsToEdges.add(new Pair<String, String>(ref.getId(), obj.getId()));
	}

	/**
	 * Adds the reference and the object to the Heap and links them.
	 */
	@CheckReturnValue
	public Heap addReferenceAndTarget(ReferenceNode ref, ObjectNode obj) {
		Heap result = new Heap(this);
		result.addPointsToEdge(ref, obj);
		return result;
	}

	/**
	 * Adds the reference and the objects to the Heap and links the objects to
	 * the reference.
	 */
	@CheckReturnValue
	public Heap addReferenceToTargets(ReferenceNode ref, Set<ObjectNode> targets) {
		Heap result = new Heap(this);
		for (ObjectNode target : targets)
			result.addPointsToEdge(ref, target);
		return result;
	}

	/**
	 * Removes the EmptyReturnObject and the link from the ReturnReference to it
	 * and replaces it with the links to the referenced objects of {@code ref}.
	 * 
	 * @param ref
	 *            the ReferenceNode that points to the possible ReturnObjects
	 * @return
	 */
	@CheckReturnValue
	public Heap setReturnRef(ReferenceNode ref) {
		Heap result = new Heap(this);
		for (Iterator<Pair<String, String>> it = result.pointsToEdges.iterator(); it.hasNext();) {
			Pair<String, String> pointsToEdge = it.next();
			if (pointsToEdge.getValue1().equals(ReferenceNode.getReturnRef().getId())
					&& pointsToEdge.getValue2().equals(EmptyReturnObjectSet.getInstance().getId()))
				it.remove();
		}

		for (ObjectNode obj : dereference(ref))
			result.pointsToEdges.add(new Pair<String, String>(ReferenceNode.getReturnRef().getId(),
					obj.getId()));
		return result;
	}

	/**
	 * Merges {@code this} Heap with the {@code other}. If ObjectNodes exist in
	 * both, then the less confined ObjectNode is used.
	 * 
	 * @param other
	 *            the Heap to merge
	 * @return the merged Heap
	 */
	@CheckReturnValue
	public Heap merge(Heap other) {
		Heap result = new Heap();

		for (ObjectNode oneObject : this.objectNodes) {
			boolean found = false;
			for (ObjectNode otherObject : other.objectNodes) {
				if (oneObject.equals(otherObject)) {
					result.objectNodes.add(oneObject.getEscapeState().moreConfinedThan(
							otherObject.getEscapeState()) ? otherObject : oneObject);
					found = true;
				}
			}
			if (!found)
				result.objectNodes.add(oneObject);
		}

		result.objectNodes.addAll(other.objectNodes);

		result.fieldEdges.addAll(this.fieldEdges);
		result.fieldEdges.addAll(other.fieldEdges);

		result.referenceNodes.addAll(this.referenceNodes);
		result.referenceNodes.addAll(other.referenceNodes);

		result.pointsToEdges.addAll(this.pointsToEdges);
		result.pointsToEdges.addAll(other.pointsToEdges);

		return result;
	}

	public static Heap getAlienGraph() {
		return ALIEN_GRAPH;
	}

	public boolean isAlien() {
		return this == ALIEN_GRAPH;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Heap( [");
		for (Pair<String, String> pair : pointsToEdges)
			sb.append("(").append(pair.getValue1()).append(" -> ").append(pair.getValue2())
					.append(") ");
		sb.append("] ; ").append(fieldEdges.toString()).append(")");
		return sb.toString();
	}

	public Set<ObjectNode> getResultValues() {
		return dereference(ReferenceNode.getReturnRef());
	}

	/**
	 * Replaces the links to the EmptyReturnObject with the ResultValues. //TODO
	 */
	private void resolveEmptyReturnObjectSet() {
		Set<FieldEdge> edgesToBeRemoved = new HashSet<>();
		Set<FieldEdge> edgesToBeAdded = new HashSet<>();

		for (FieldEdge edge : fieldEdges) {
			if (edge.getOriginId().equals(EmptyReturnObjectSet.getInstance().getId())) {
				for (ObjectNode resultObject : getResultValues())
					if (!edge.getDestinationId().equals(EmptyReturnObjectSet.getInstance().getId()))
						edgesToBeAdded.add(new FieldEdge(resultObject.getId(), edge.getFieldName(),
								edge.getDestinationId()));
				edgesToBeRemoved.add(edge);
			}
			if (edge.getDestinationId().equals(EmptyReturnObjectSet.getInstance().getId())) {
				for (ObjectNode resultObject : getResultValues())
					if (!edge.getOriginId().equals(EmptyReturnObjectSet.getInstance().getId()))
						edgesToBeAdded.add(new FieldEdge(edge.getOriginId(), edge.getFieldName(),
								resultObject.getId()));
				edgesToBeRemoved.add(edge);
			}
		}

		fieldEdges.removeAll(edgesToBeRemoved);
		fieldEdges.addAll(edgesToBeAdded);

		for (ObjectNode resultObject : getResultValues()) {
			replacePointsToEdge(EmptyReturnObjectSet.getInstance(), resultObject);
		}

		objectNodes.remove(EmptyReturnObjectSet.getInstance());
	}

	/**
	 * Removes all NullObjects and the connected fieldEdges and pointsToEdges
	 * from the Heap.
	 */
	protected void removeNullObject() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();)
			if (objIterator.next().equals(InternalObject.getNullObject()))
				objIterator.remove();

		for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator.hasNext();) {
			FieldEdge edge = edgeIterator.next();
			if (edge.getDestinationId().equals(InternalObject.getNullObject().getId()))
				edgeIterator.remove();
		}

		removePointsToEdge(InternalObject.getNullObject());
	}

	/**
	 * Increases the EscapeState of a child ObjectNode to the EscapeState of the
	 * parent Node for all Nodes with the specified {@code escapeState}.
	 * 
	 * @param escapeState
	 *            the EscapeState for which ObjectNodes the children should be
	 *            updated
	 */
	private void propagateEscapeState(EscapeState escapeState) {
		Stack<ObjectNode> workingList = new Stack<>();
		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getEscapeState() == escapeState)
				workingList.push(objectNode);

		while (!workingList.isEmpty()) {
			ObjectNode current = workingList.pop();

			for (ObjectNode subObject : objectNodes.getSubObjectsOf(current, fieldEdges)) {
				ObjectNode increasedObj = objectNodes.increaseEscapeState(subObject, escapeState);
				if (increasedObj != null)
					workingList.push(increasedObj);
			}
		}
	}

	/**
	 * Removes all ObjectNodes with GLOBAL_ESCAPE from the Heap and adds them to
	 * the Set of {@code escapedObjects}. Replaces all FieldEdges pointing to
	 * these ObjectNodes with edges pointing to the GlobalObject and deletes all
	 * FieldEdges starting from these ObjectNodes. Replaces all pointsToEdges.
	 */
	private void collapseGlobalGraph() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.GLOBAL_ESCAPE
					&& !current.equals(GlobalObject.getInstance())) {
				Set<FieldEdge> edgesTerminatingAtCurrent = new HashSet<>();

				for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator
						.hasNext();) {
					FieldEdge edge = edgeIterator.next();

					if (edge.getOriginId().equals(current.getId()))
						edgeIterator.remove();
					else if (edge.getDestinationId().equals(current.getId()))
						edgesTerminatingAtCurrent.add(edge);
				}
				for (FieldEdge edgeTerminatingAtCurrent : edgesTerminatingAtCurrent) {
					fieldEdges.remove(edgeTerminatingAtCurrent);
					fieldEdges.add(new FieldEdge(edgeTerminatingAtCurrent.getOriginId(),
							edgeTerminatingAtCurrent.getFieldName(), GlobalObject.getInstance()
									.getId()));
				}

				replacePointsToEdge(current, GlobalObject.getInstance());

				escapedObjects.add(current);
				objIterator.remove();
			}
		}
	}

	private void replacePointsToEdge(ObjectNode oldObject, ObjectNode newObject) {
		Set<Pair<String, String>> edgesToAdd = new HashSet<>();
		for (Iterator<Pair<String, String>> edgeIterator = pointsToEdges.iterator(); edgeIterator
				.hasNext();) {
			Pair<String, String> edge = edgeIterator.next();

			if (edge.getValue2().equals(oldObject.getId())) {
				edgesToAdd.add(new Pair<String, String>(edge.getValue1(), newObject.getId()));
				edgeIterator.remove();
			}
		}
		pointsToEdges.addAll(edgesToAdd);
	}

	/**
	 * Removes all ObjectNodes with NO_ESCAPE from the Heap and adds them to the
	 * Set of {@code localObjects}. Deletes all FieldEdges starting from these
	 * ObjectNodes. Deletes all pointsToEdges to these ObjectNodes and if there
	 * are no other pointsToEdges from the ReferenceNode it will be removed too.
	 */
	private void removeLocalGraph() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.NO_ESCAPE) {
				for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator
						.hasNext();)
					if (edgeIterator.next().getOriginId().equals(current.getId()))
						edgeIterator.remove();

				removePointsToEdge(current);

				localObjects.add(current);
				objIterator.remove();
			}
		}
	}

	/**
	 * Removes all pointsToEdges pointing to the {@code obj}. If there are no
	 * other pointsToEdges from the removed Reference, it will be removed too.
	 * 
	 * @param obj
	 *            the ObjectNode the edge to delete points to
	 */
	private void removePointsToEdge(ObjectNode obj) {
		for (Iterator<Pair<String, String>> edgeIterator = pointsToEdges.iterator(); edgeIterator
				.hasNext();) {
			Pair<String, String> next = edgeIterator.next();
			if (next.getValue2().equals(obj.getId())) {
				edgeIterator.remove();
				removeRefWithoutEdge(next.getValue1());
			}
		}
	}

	/**
	 * Removes the {@code reference} if there are no pointsToEdges starting from
	 * it.
	 */
	private void removeRefWithoutEdge(String reference) {
		boolean hasNoOtherEdges = true;
		for (Pair<String, String> pair : pointsToEdges) {
			if (pair.getValue1().equals(reference)) {
				hasNoOtherEdges = false;
				break;
			}
		}
		if (hasNoOtherEdges)
			referenceNodes.remove(reference);
	}

	/**
	 * Propagates the EscapeStates of the ObjectNodes and deletes all irrelevant
	 * Nodes. Local Objects (NO_ESCAPE) are removed and global Objects
	 * (GLOBAL_ESCAPE) are repalced with the {@code GlobalObject}. So only
	 * ObjectNodes referenced by the arguments (ARG_ESCAPE) and the ReturnValues
	 * are kept in the Heap.
	 * 
	 * @return the Heap that summarizes all necessary information
	 */
	@CheckReturnValue
	public Heap doFinalStuff() {
		Heap result = new Heap(this);

		result.resolveEmptyReturnObjectSet();

		for (ObjectNode resultObject : result.getResultValues()) {
			result.objectNodes.increaseEscapeState(resultObject, EscapeState.ARG_ESCAPE);
		}

		result.removeNullObject();

		result.propagateEscapeState(EscapeState.GLOBAL_ESCAPE);
		result.propagateEscapeState(EscapeState.ARG_ESCAPE);

		result.collapseGlobalGraph();

		result.removeLocalGraph();

		return result;
	}

	public ObjectNodes getArgEscapeObjects() {
		return objectNodes;
	}

	public EscapingTypes argEscapingTypes() {
		EscapingTypes result = new EscapingTypes();
		for (ObjectNode obj : objectNodes)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}

	public EscapingTypes globallyEscapingTypes() {
		if (this == ALIEN_GRAPH)
			return EscapingTypes.getAllTypes();

		EscapingTypes result = new EscapingTypes();
		for (ObjectNode obj : escapedObjects)
			if (obj instanceof InternalObject)
				result.add(((InternalObject) obj).getType());
		return result;
	}

	public ReferenceNode getRefToPhantomObject(int index) {
		for (ReferenceNode ref : referenceNodes)
			if (ref.getId().equals(Category.ARG.toString() + index))
				return ref;
		throw new AssertionError("phantom object with id " + index + " does not exist");
	}

	/**
	 * Adds all internal objects of {@code summary} that are linked to an
	 * argument to {@code this}.
	 * 
	 * @param summary
	 *            the Heap representing the MethodSummary
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	Heap transferInternalObjectsFrom(Heap summary) {
		Heap result = new Heap(this);

		for (ObjectNode object : summary.getArgEscapeObjects())
			if (object instanceof InternalObject)
				result.getObjectNodes().add(((InternalObject) object).resetEscapeState());

		return result;
	}

	/**
	 * Links the resultValues from {@code summary} to the {@code resultRef} and
	 * adds the {@code resultRef} and the result Objects to {@code this}.
	 * 
	 * @param summary
	 *            the Heap representing the MethodSummary
	 * @param resultRef
	 *            the Reference to link the results to
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	Heap transferResultFrom(Heap summary, ReferenceNode resultRef) {
		Heap result = new Heap(this);

		if (summary.isAlien())
			result = result.addReferenceAndTarget(resultRef, GlobalObject.getInstance());
		else
			for (ObjectNode resultValue : summary.getResultValues()) {
				result = result.addReferenceAndTarget(resultRef, resultValue);
			}

		return result;
	}

	public Set<ObjectNode> getFieldOf(ObjectNode object, String field) {
		return objectNodes.getFieldOf(object, fieldEdges, field);
	}

	/**
	 * Creates an {@reference=MethodSummaryAnnotation} representation of this
	 * Heap nested in a {@reference=javassist.bytecode.annotation.Annotation}.
	 * 
	 * @return
	 */
	public Annotation convertToAnnotation(ConstPool cp) {

		Map<String, MemberValue> values = new HashMap<>();

		List<MemberValue> internalObjects = new ArrayList<>();
		List<MemberValue> phantomPbjects = new ArrayList<>();

		values.put(
				"argEscapedObjectIDs",
				seperateObjectNodesByTypeAndGetIds(objectNodes, internalObjects, phantomPbjects, cp));
		values.put(
				"globallyEscapedObjectIDs",
				seperateObjectNodesByTypeAndGetIds(escapedObjects, internalObjects, phantomPbjects,
						cp));
		values.put(
				"localObjectIDs",
				seperateObjectNodesByTypeAndGetIds(localObjects, internalObjects, phantomPbjects,
						cp));

		values.put("internalObjects",
				convertListToArrayMember(internalObjects, new AnnotationMemberValue(cp), cp));
		values.put("phantomObjects",
				convertListToArrayMember(phantomPbjects, new AnnotationMemberValue(cp), cp));

		values.put("fieldEdges", convertFieldEdges(cp));

		values.put("referenceNodes", convertReferenceNodes(cp));

		values.put("pointsToEdges", convertPointsToEdges(cp));

		// create and return annotation
		return AnnotationHelper.createAnnotation(values, MethodSummaryAnnotation.class.getName(),
				cp);
	}

	private static ArrayMemberValue seperateObjectNodesByTypeAndGetIds(ObjectNodes source,
			List<MemberValue> internalObjects, List<MemberValue> phantomPbjects, ConstPool cp) {
		List<MemberValue> ids = new ArrayList<>();

		for (ObjectNode node : source) {
			// null object is singelton, safe to compare references
			if (node == InternalObject.getNullObject()) {
				throw new AssertionError("ObjectNodes contains null object!");
			}

			if (node instanceof EmptyReturnObjectSet) {
				throw new AssertionError("ObjectNodes contains null object!");
			}

			if (node instanceof PhantomObject) {
				phantomPbjects.add(new AnnotationMemberValue(((PhantomObject) node)
						.convertToAnnotation(cp), cp));
			}

			if (node instanceof InternalObject) {
				internalObjects.add(new AnnotationMemberValue(((InternalObject) node)
						.convertToAnnotation(cp), cp));
			}

			ids.add(new StringMemberValue(node.getId(), cp));
		}

		return convertListToArrayMember(ids, new StringMemberValue(cp), cp);
	}

	private static ArrayMemberValue convertListToArrayMember(List<MemberValue> values,
			MemberValue arrayType, ConstPool cp) {
		ArrayMemberValue value = new ArrayMemberValue(arrayType, cp);
		value.setValue(values.toArray(new MemberValue[values.size()]));
		return value;
	}

	private ArrayMemberValue convertFieldEdges(ConstPool cp) {
		List<MemberValue> values = new ArrayList<>();
		for (FieldEdge fieldEdge : fieldEdges) {
			values.add(new AnnotationMemberValue(fieldEdge.convertToAnnotation(cp), cp));
		}

		return convertListToArrayMember(values, new AnnotationMemberValue(cp), cp);
	}

	private ArrayMemberValue convertReferenceNodes(ConstPool cp) {
		List<MemberValue> values = new ArrayList<>();
		for (ReferenceNode referenceNode : referenceNodes) {
			values.add(new AnnotationMemberValue(referenceNode.convertToAnnotation(cp), cp));
		}

		return convertListToArrayMember(values, new AnnotationMemberValue(cp), cp);
	}

	private ArrayMemberValue convertPointsToEdges(ConstPool cp) {
		List<MemberValue> values = new ArrayList<>();
		for (Pair<String, String> pointsToEdge : this.pointsToEdges) {
			Map<String, MemberValue> vals = new HashMap<>();
			vals.put("referenceNodeID", new StringMemberValue(pointsToEdge.getValue1(), cp));
			vals.put("objectID", new StringMemberValue(pointsToEdge.getValue2(), cp));
			values.add(new AnnotationMemberValue(AnnotationHelper.createAnnotation(vals,
					PointsToEdgesAnnotation.class.getName(), cp), cp));
		}
		return convertListToArrayMember(values, new AnnotationMemberValue(cp), cp);
	}
}
