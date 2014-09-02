package de.htwg_konstanz.jea.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
	@Getter
	private final Set<Pair<ReferenceNode, String>> pointsToEdges = new HashSet<>();
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
		referenceNodes.add(ReferenceNode.getGlobalRef());
		objectNodes.add(GlobalObject.getInstance());
		pointsToEdges.add(new Pair<ReferenceNode, String>(ReferenceNode.getGlobalRef(),
				GlobalObject.getInstance().getId()));

		for (Integer index : indexes) {
			ReferenceNode ref = new ReferenceNode(index, Category.ARG);
			ObjectNode obj = PhantomObject.newPhantomObject(index);

			referenceNodes.add(ref);
			objectNodes.add(obj);
			pointsToEdges.add(new Pair<ReferenceNode, String>(ref, obj.getId()));
		}

		if (hasRefReturnType) {
			objectNodes.add(EmptyReturnObjectSet.getInstance());
			referenceNodes.add(ReferenceNode.getReturnRef());
			pointsToEdges.add(new Pair<ReferenceNode, String>(ReferenceNode.getReturnRef(),
					EmptyReturnObjectSet.getInstance().getId()));
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
	 * As {@code ref} can point to multiple objects, the Set of referneced
	 * objects is returned.
	 * 
	 * @return the objects referenced from {@code ref}
	 */
	public Set<ObjectNode> dereference(ReferenceNode ref) {
		Set<ObjectNode> result = new HashSet<>();
		for (Pair<ReferenceNode, String> pointsToEdge : pointsToEdges)
			if (pointsToEdge.getValue1().equals(ref))
				result.add(objectNodes.getObjectNode(pointsToEdge.getValue2()));
		return result;
	}

	/**
	 * To publish an object its EscapeState is set to GLOBAL_ESCAPE.
	 * 
	 * @param ref
	 *            the ReferenceNode to publish
	 * @return the resulting Heap
	 */
	public Heap publish(ReferenceNode ref) {
		Heap result = new Heap(this);

		for (ObjectNode object : dereference(ref)) {
			result.objectNodes.remove(object);
			result.objectNodes.add(object.increaseEscapeState(EscapeState.GLOBAL_ESCAPE));
		}

		return result;
	}

	/**
	 * Assigns the {@code value} to the field {@code fieldName} of {@code obj}
	 * {@code (obj.fieldName = value)}. If the {@code value} doesn't exist
	 * already in this Heap, it is added.
	 */
	public Heap addField(ObjectNode obj, String fieldName, ObjectNode value) {
		// if (obj.isGlobal())
		// return this;

		if (obj.equals(InternalObject.getNullObject()))
			throw new AssertionError("assign Object to a field of null");

		Heap result = new Heap(this);

		if (!objectNodes.existsObject(value.getId()))
			result.objectNodes.add(value);

		result.fieldEdges.add(new FieldEdge(obj.getId(), fieldName, value.getId()));
		return result;
	}

	/**
	 * Adds the reference and the object to the Heap and links them.
	 */
	public Heap addReferenceAndTarget(ReferenceNode ref, ObjectNode obj) {
		Heap result = new Heap(this);
		result.referenceNodes.add(ref);
		result.objectNodes.add(obj);
		result.pointsToEdges.add(new Pair<ReferenceNode, String>(ref, obj.getId()));
		return result;
	}

	/**
	 * Adds the reference and the objects to the Heap and links the objects to
	 * the reference.
	 */
	public Heap addReferenceToTargets(ReferenceNode ref, Set<ObjectNode> targets) {
		Heap result = new Heap(this);
		result.referenceNodes.add(ref);
		for (ObjectNode target : targets)
			result.pointsToEdges.add(new Pair<ReferenceNode, String>(ref, target.getId()));

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
	public Heap setReturnRef(ReferenceNode ref) {
		Heap result = new Heap(this);
		for (Iterator<Pair<ReferenceNode, String>> it = result.pointsToEdges.iterator(); it
				.hasNext();) {
			Pair<ReferenceNode, String> pointsToEdge = it.next();
			if (pointsToEdge.getValue1().equals(ReferenceNode.getReturnRef())
					&& pointsToEdge.getValue2().equals(EmptyReturnObjectSet.getInstance().getId()))
				it.remove();
		}

		for (ObjectNode obj : dereference(ref))
			result.pointsToEdges.add(new Pair<ReferenceNode, String>(ReferenceNode.getReturnRef(),
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
		return "CG(" + pointsToEdges + ", " + fieldEdges + ")";
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

		objectNodes.remove(EmptyReturnObjectSet.getInstance());
	}

	/**
	 * Removes all NullObjects and the connected fieldEdges from the Heap.
	 */
	protected void removeNullObject() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();)
			if (objIterator.next().equals(InternalObject.getNullObject()))
				objIterator.remove();

		for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator.hasNext();) {
			FieldEdge edge = edgeIterator.next();
			if (edge.getOriginId().equals(InternalObject.getNullObject().getId())
					|| edge.getDestinationId().equals(InternalObject.getNullObject().getId()))
				edgeIterator.remove();
		}
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

			for (ObjectNode subObject : objectNodes.getSubObjectsOf(current, fieldEdges))
				if (subObject.getEscapeState().moreConfinedThan(escapeState)) {
					ObjectNode updatedSubObject = subObject.increaseEscapeState(escapeState);
					objectNodes.remove(subObject);
					objectNodes.add(updatedSubObject);
					workingList.push(updatedSubObject);
				}
		}
	}

	/**
	 * Removes all ObjectNodes with GLOBAL_ESCAPE from the CG and adds them to
	 * the Set of {@code escapedObjects}. Replaces all FieldEdges pointing to
	 * these ObjectNodes with edges pointing to the GlobalObject and deletes all
	 * FieldEdges starting from these ObjectNodes.
	 */
	private void collapseGlobalGraph() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.GLOBAL_ESCAPE) {
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

				escapedObjects.add(current);
				objIterator.remove();
			}
		}
	}

	/**
	 * Removes all ObjectNodes with NO_ESCAPE from the CG and adds them to the
	 * Set of {@code localObjects}. Deletes all FieldEdges starting from these
	 * ObjectNodes.
	 */
	private void removeLocalGraph() {
		for (Iterator<ObjectNode> objIterator = objectNodes.iterator(); objIterator.hasNext();) {
			ObjectNode current = objIterator.next();

			if (current.getEscapeState() == EscapeState.NO_ESCAPE) {
				for (Iterator<FieldEdge> edgeIterator = fieldEdges.iterator(); edgeIterator
						.hasNext();)
					if (edgeIterator.next().getOriginId().equals(current.getId()))
						edgeIterator.remove();

				localObjects.add(current);
				objIterator.remove();
			}
		}
	}

	/**
	 * Propagates the EscapeStates of the ObjectNodes and deletes all irrelevant
	 * Nodes. Local Objects (NO_ESCAPE) are removed and global Objects
	 * (GLOBAL_ESCAPE) are repalced with the {@code GlobalObject}. So only
	 * ObjectNodes referenced by the arguments (ARG_ESCAPE) and the ReturnValues
	 * are kept in the CG.
	 * 
	 * @return the Heap that summarizes all necessary information
	 */
	public Heap doFinalStuff() {
		Heap result = new Heap(this);

		result.resolveEmptyReturnObjectSet();

		for (ObjectNode resultObject : result.getResultValues()) {
			result.objectNodes.remove(resultObject);
			result.objectNodes.add(resultObject.increaseEscapeState(EscapeState.ARG_ESCAPE));
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
			if (ref.getCategory() == Category.ARG && ref.getId() == index)
				return ref;
		throw new AssertionError("phantom object with id " + index + " does not exist");
	}

	public ObjectNode getObjectNode(String id) {
		for (ObjectNode obj : objectNodes)
			if (obj.getId().equals(id))
				return obj;
		throw new AssertionError("object with id " + id + " does not exist");
	}

}
