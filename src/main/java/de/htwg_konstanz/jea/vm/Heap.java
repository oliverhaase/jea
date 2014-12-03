package de.htwg_konstanz.jea.vm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import de.htwg_konstanz.jea.annotation.AnnotationCreator;
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
public final class Heap implements AnnotationCreator {
	private final static Heap ALIEN_GRAPH = new Heap();

	@Getter
	private final ObjectNodes objectNodes = new ObjectNodes();;
	@Getter
	private final Set<FieldEdge> fieldEdges = new HashSet<>();
	@Getter
	private final Set<ReferenceNode> referenceNodes = new HashSet<>();
	private final Set<PointToEdge> pointsToEdges = new HashSet<>();
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

		checkHeap();
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
			heap.pointsToEdges.add(PointToEdge.getInstanceByAnnotation(pointsToEdge));
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
		for (PointToEdge pointsToEdge : pointsToEdges)
			if (pointsToEdge.getReferenceId().equals(ref.getId()))
				result.add(objectNodes.getObjectNode(pointsToEdge.getObjectId()));
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
		checkHeap();
		Heap result = new Heap(this);

		for (ObjectNode object : dereference(ref)) {
			result.objectNodes.increaseEscapeState(object, EscapeState.GLOBAL_ESCAPE);
		}

		result.checkHeap();
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
		checkHeap();
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

		result.checkHeap();
		return result;
	}

	/**
	 * Adds the reference and the object to the Heap and links them.
	 */
	private void addPointsToEdge(ReferenceNode ref, ObjectNode obj) {
		checkHeap();
		referenceNodes.add(ref);
		objectNodes.add(obj);
		pointsToEdges.add(new PointToEdge(ref.getId(), obj.getId()));
		checkHeap();
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
		checkHeap();
		Heap result = new Heap(this);
		for (Iterator<PointToEdge> it = result.pointsToEdges.iterator(); it.hasNext();) {
			PointToEdge pointsToEdge = it.next();
			if (pointsToEdge.getReferenceId().equals(ReferenceNode.getReturnRef().getId())
					&& pointsToEdge.getObjectId()
							.equals(EmptyReturnObjectSet.getInstance().getId()))
				it.remove();
		}

		for (ObjectNode obj : dereference(ref))
			result.addPointsToEdge(ReferenceNode.getReturnRef(), obj);
		result.checkHeap();
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
		checkHeap();
		other.checkHeap();
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

		result.checkHeap();
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
		for (PointToEdge pair : pointsToEdges)
			sb.append("(").append(pair.getReferenceId()).append(" -> ").append(pair.getObjectId())
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
		checkHeap();
		Set<FieldEdge> edgesToBeRemoved = new HashSet<>();
		Set<FieldEdge> edgesToBeAdded = new HashSet<>();

		EmptyReturnObjectSet returnSet = EmptyReturnObjectSet.getInstance();

		for (FieldEdge edge : fieldEdges) {
			if (edge.getOriginId().equals(returnSet.getId())) {
				for (ObjectNode resultObject : getResultValues())
					if (!edge.getDestinationId().equals(returnSet.getId()))
						edgesToBeAdded.add(new FieldEdge(resultObject.getId(), edge.getFieldName(),
								edge.getDestinationId()));
				edgesToBeRemoved.add(edge);
			}
			if (edge.getDestinationId().equals(returnSet.getId())) {
				for (ObjectNode resultObject : getResultValues())
					if (!edge.getOriginId().equals(returnSet.getId()))
						edgesToBeAdded.add(new FieldEdge(edge.getOriginId(), edge.getFieldName(),
								resultObject.getId()));
				edgesToBeRemoved.add(edge);
			}
		}

		fieldEdges.removeAll(edgesToBeRemoved);
		fieldEdges.addAll(edgesToBeAdded);

		if (getResultValues().isEmpty()) {
			removePointsToEdge(returnSet);
		} else {
			for (ObjectNode resultObject : getResultValues())
				replacePointsToEdge(returnSet, resultObject);
		}

		objectNodes.remove(returnSet);
		checkHeap();
	}

	/**
	 * Removes all NullObjects and the connected fieldEdges and pointsToEdges
	 * from the Heap.
	 */
	protected void removeNullObject() {
		checkHeap();
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();)
			if (objIterator.next().equals(InternalObject.getNullObject()))
				objIterator.remove();

		for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator.hasNext();) {
			FieldEdge edge = edgeIterator.next();
			if (edge.getDestinationId().equals(InternalObject.getNullObject().getId()))
				edgeIterator.remove();
		}

		removePointsToEdge(InternalObject.getNullObject());
		checkHeap();
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
		checkHeap();
		Stack<ObjectNode> workingList = new Stack<>();
		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getEscapeState() == escapeState)
				workingList.push(objectNode);

		while (!workingList.isEmpty()) {
			ObjectNode current = workingList.pop();

			checkSubObjectsOf(current); // TODO remove

			for (ObjectNode subObject : objectNodes.getSubObjectsOf(current, fieldEdges)) {
				ObjectNode increasedObj = objectNodes.increaseEscapeState(subObject, escapeState);
				if (increasedObj != null)
					workingList.push(increasedObj);
			}
		}
		checkHeap();
	}

	/**
	 * Removes all ObjectNodes with GLOBAL_ESCAPE from the Heap and adds them to
	 * the Set of {@code escapedObjects}. Replaces all FieldEdges pointing to
	 * these ObjectNodes with edges pointing to the GlobalObject and deletes all
	 * FieldEdges starting from these ObjectNodes. Replaces all pointsToEdges.
	 */
	private void collapseGlobalGraph() {
		checkHeap();
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
		checkHeap();
	}

	/**
	 * Removes all ObjectNodes with NO_ESCAPE from the Heap and adds them to the
	 * Set of {@code localObjects}. Deletes all FieldEdges starting from these
	 * ObjectNodes. Deletes all pointsToEdges to these ObjectNodes and if there
	 * are no other pointsToEdges from the ReferenceNode it will be removed too.
	 */
	private void removeLocalGraph() {
		checkHeap();
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
		checkHeap();
	}

	private void replacePointsToEdge(ObjectNode oldObject, ObjectNode newObject) {
		Set<PointToEdge> edgesToAdd = new HashSet<>();
		for (Iterator<PointToEdge> edgeIterator = pointsToEdges.iterator(); edgeIterator.hasNext();) {
			PointToEdge edge = edgeIterator.next();

			if (edge.getObjectId().equals(oldObject.getId())) {
				edgesToAdd.add(new PointToEdge(edge.getReferenceId(), newObject.getId()));
				edgeIterator.remove();
			}
		}
		pointsToEdges.addAll(edgesToAdd);
	}

	/**
	 * Removes all pointsToEdges pointing to the {@code obj}. If there are no
	 * other pointsToEdges from the removed Reference, it will be removed too.
	 * 
	 * @param obj
	 *            the ObjectNode the edge to delete points to
	 */
	private void removePointsToEdge(ObjectNode obj) {
		for (Iterator<PointToEdge> edgeIterator = pointsToEdges.iterator(); edgeIterator.hasNext();) {
			PointToEdge next = edgeIterator.next();
			if (next.getObjectId().equals(obj.getId())) {
				edgeIterator.remove();
				removeRefWithoutEdge(next.getReferenceId());
			}
		}
	}

	/**
	 * Removes the {@code reference} if there are no pointsToEdges starting from
	 * it.
	 */
	private void removeRefWithoutEdge(String reference) {
		boolean hasNoOtherEdges = true;
		for (PointToEdge pair : pointsToEdges) {
			if (pair.getReferenceId().equals(reference)) {
				hasNoOtherEdges = false;
				break;
			}
		}
		if (hasNoOtherEdges) {
			ReferenceNode node = null;
			for (ReferenceNode ref : referenceNodes)
				if (ref.getId().equals(reference))
					node = ref;
			referenceNodes.remove(node);
		}
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
		checkHeap();
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

		if (result.objectNodes.existsObject(EmptyReturnObjectSet.getInstance().getId()))
			throw new AssertionError("EmptyReturnObjectSet Error");

		result.checkHeap();
		result.checkMethodSummary();
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
	 * @param position
	 *            position of the instruction, used to create individual ids
	 * @return the resulting Heap
	 */
	@CheckReturnValue
	Heap transferInternalObjectsFrom(Heap summary, int position) {
		Heap result = new Heap(this);

		for (ObjectNode object : summary.getArgEscapeObjects())
			if (object instanceof InternalObject)
				result.getObjectNodes().add(
						new InternalObject(object.getId() + "|" + position,
								((InternalObject) object).getType(), EscapeState.NO_ESCAPE));

		return result;
	}

	public Set<ObjectNode> getFieldOf(ObjectNode object, String field) {
		return objectNodes.getFieldOf(object, fieldEdges, field);
	}

	public void checkMethodSummary() {
		if (objectNodes.existsObject(EmptyReturnObjectSet.getInstance().getId()))
			throw new AssertionError("checkMethodSummary-EmptyReturnObjectSet");

		String nullId = InternalObject.getNullObject().getId();
		if (objectNodes.existsObject(nullId))
			throw new AssertionError("checkMethodSummary-NullObject");
		for (FieldEdge fieldEdge : fieldEdges)
			if (fieldEdge.getOriginId().equals(nullId)
					|| fieldEdge.getDestinationId().equals(nullId))
				throw new AssertionError("checkMethodSummary-NullObject");
		for (PointToEdge edge : pointsToEdges)
			if (edge.getObjectId().equals(nullId))
				throw new AssertionError("checkMethodSummary-NullObject");

		for (ObjectNode objectNode : objectNodes)
			if (objectNode.isGlobal() && !objectNode.equals(GlobalObject.getInstance()))
				throw new AssertionError("checkMethodSummary-GLOBAL_ESCAPE");

		for (ObjectNode objectNode : objectNodes)
			if (objectNode.getEscapeState().equals(Node.EscapeState.NO_ESCAPE))
				throw new AssertionError("checkMethodSummary-NO_ESCAPE");

	}

	public void checkHeap() {
		checkPointsToEdge();
		checkFieldEdge();
		checkSubObjectsOfPhantoms();
	}

	private void checkPointsToEdge() {
		for (PointToEdge pointToEdge : pointsToEdges) {
			if (getReferenceNode(pointToEdge.getReferenceId()) == null)
				throw new AssertionError("checkPointsToEdge-ReferenceNode");
			if (!pointToEdge.getObjectId().equals(InternalObject.getNullObject().getId())
					&& !objectNodes.existsObject(pointToEdge.getObjectId()))
				throw new AssertionError("checkPointsToEdge-ObjectNode");
		}
	}

	private void checkFieldEdge() {
		for (FieldEdge fieldEdge : fieldEdges) {
			if (!objectNodes.existsObject(fieldEdge.getOriginId()))
				throw new AssertionError("checkFieldEdge-getOriginId");
			if (!objectNodes.existsObject(fieldEdge.getDestinationId()))
				throw new AssertionError("checkFieldEdge-getDestinationId");
		}
	}

	private void checkSubObjectsOfPhantoms() {
		for (ObjectNode object : objectNodes) {
			ObjectNodes subObjects = objectNodes.getSubObjectsOf(object, fieldEdges);
			if (object instanceof PhantomObject) {
				for (ObjectNode objectNode : objectNodes) {
					if (objectNode instanceof PhantomObject) {
						PhantomObject p = (PhantomObject) objectNode;
						if (p.isSubPhantom() && p.getParent().equals(object.getId()))
							if (!subObjects.existsObject(p.getId()))
								throw new AssertionError("SubObjectError");
					}
				}
			}
		}
	}

	private ReferenceNode getReferenceNode(String refId) {
		for (ReferenceNode referenceNode : referenceNodes) {
			if (referenceNode.getId().equals(refId))
				return referenceNode;
		}
		return null;
	}

	private void checkSubObjectsOf(ObjectNode object) {
		ObjectNodes subObjects = objectNodes.getSubObjectsOf(object, fieldEdges);
		if (object instanceof PhantomObject) {
			for (ObjectNode objectNode : objectNodes) {
				if (objectNode instanceof PhantomObject) {
					PhantomObject p = (PhantomObject) objectNode;
					if (p.isSubPhantom() && p.getParent().equals(object.getId()))
						if (!subObjects.existsObject(p.getId()))
							throw new AssertionError("SubObjectError");
				}
			}
		}
	}

	/**
	 * Creates an {@reference=MethodSummaryAnnotation} representation of this
	 * Heap nested in a {@reference=javassist.bytecode.annotation.Annotation}.
	 * 
	 * @return
	 */
	@Override
	public Annotation convertToAnnotation(ConstPool cp) {
		Collection<InternalObject> internalObjects = new HashSet<>();
		Collection<PhantomObject> phantomObjects = new HashSet<>();
		seperateNodes(objectNodes, internalObjects, phantomObjects);
		seperateNodes(escapedObjects, internalObjects, phantomObjects);
		seperateNodes(localObjects, internalObjects, phantomObjects);

		Map<String, MemberValue> values = new HashMap<>();
		values.put("internalObjects", convertToAnnotationArrayMemberValue(internalObjects, cp));
		values.put("phantomObjects", convertToAnnotationArrayMemberValue(phantomObjects, cp));

		values.put("argEscapedObjectIDs",
				convertToStringArrayMemberValue(objectNodes.getAllObjectIds(), cp));

		values.put("globallyEscapedObjectIDs",
				convertToStringArrayMemberValue(escapedObjects.getAllObjectIds(), cp));

		values.put("localObjectIDs",
				convertToStringArrayMemberValue(localObjects.getAllObjectIds(), cp));

		values.put("fieldEdges", convertToAnnotationArrayMemberValue(fieldEdges, cp));

		values.put("referenceNodes", convertToAnnotationArrayMemberValue(referenceNodes, cp));

		values.put("pointsToEdges", convertToAnnotationArrayMemberValue(pointsToEdges, cp));

		// create and return annotation
		return AnnotationHelper.createAnnotation(values, MethodSummaryAnnotation.class.getName(),
				cp);
	}

	private static void seperateNodes(ObjectNodes source, Collection<InternalObject> internal,
			Collection<PhantomObject> phantom) {
		for (ObjectNode node : source) {
			// null object is singleton, safe to compare references
			if (node == InternalObject.getNullObject()) {
				throw new AssertionError("ObjectNodes contains null object!");
			}

			if (node instanceof EmptyReturnObjectSet) {
				throw new AssertionError("ObjectNodes contains EmptyReturnObjectSet!");
			}

			if (node instanceof PhantomObject) {
				phantom.add((PhantomObject) node);
			}

			if (node instanceof InternalObject) {
				internal.add((InternalObject) node);
			}
		}
	}

	private static ArrayMemberValue convertToAnnotationArrayMemberValue(
			Collection<? extends AnnotationCreator> source, ConstPool cp) {
		Collection<MemberValue> values = new ArrayList<>();
		for (AnnotationCreator entry : source) {
			values.add(new AnnotationMemberValue(entry.convertToAnnotation(cp), cp));
		}

		ArrayMemberValue returnValue = new ArrayMemberValue(new AnnotationMemberValue(cp), cp);
		returnValue.setValue(values.toArray(new MemberValue[values.size()]));
		return returnValue;
	}

	private static ArrayMemberValue convertToStringArrayMemberValue(Collection<String> source,
			ConstPool cp) {
		Collection<MemberValue> values = new ArrayList<>();
		for (String entry : source) {
			values.add(new StringMemberValue(entry, cp));
		}

		ArrayMemberValue returnValue = new ArrayMemberValue(new StringMemberValue(cp), cp);
		returnValue.setValue(values.toArray(new MemberValue[values.size()]));
		return returnValue;
	}
}
