package de.htwg_konstanz.jea.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;

public class HeapTest {

	@Test
	public void testHeapEmpty() {
		Heap heap = new Heap(new HashSet<Integer>(), false);

		assertTrue(areLinked(heap, ReferenceNode.getGlobalRef(), GlobalObject.getInstance()));
		assertEquals(1, size(heap.getArgEscapeObjects()));
	}

	@Test
	public void testHeapEmptyReturn() {
		Heap heap = new Heap(new HashSet<Integer>(), true);

		assertTrue(areLinked(heap, ReferenceNode.getGlobalRef(), GlobalObject.getInstance()));
		assertTrue(areLinked(heap, ReferenceNode.getReturnRef(), EmptyReturnObjectSet.getInstance()));
		assertEquals(2, size(heap.getArgEscapeObjects()));
	}

	@Test
	public void testHeap1() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		indexes.add(0);
		indexes.add(2);
		Heap heap = new Heap(indexes, true);
		assertEquals(4, size(heap.getArgEscapeObjects()));

		checkConnection(heap, 0, "p" + 0);
		checkConnection(heap, 2, "p" + 2);
	}

	@Test(expected = AssertionError.class)
	public void testFieldToNull() {
		Heap heap = new Heap(new HashSet<Integer>(), false);

		InternalObject nullObject = InternalObject.getNullObject();
		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(internalRef, internalObject);
		heap = heap.addField(nullObject, "f", internalObject);
	}

	@Test
	public void testRemoveNullObject() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		indexes.add(0);
		indexes.add(1);
		Heap heap = new Heap(indexes, false);

		InternalObject nullObject = InternalObject.getNullObject();
		for (ObjectNode object : heap.dereference(heap.getRefToPhantomObject(0))) {
			heap = heap.addField(object, "f", nullObject);
		}

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(internalRef, internalObject);
		InternalObject internalObject2 = new InternalObject("5", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap.getObjectNodes().add(internalObject2);
		heap = heap.addField(internalObject, "f", internalObject2);
		heap = heap.addField(internalObject, "f2", nullObject);

		ReferenceNode nullRef = new ReferenceNode(4, Category.LOCAL);
		heap = heap.addReferenceAndTarget(nullRef, nullObject);

		heap.dereference(nullRef);

		assertEquals(6, size(heap.getObjectNodes()));

		heap.removeNullObject();

		assertEquals(5, size(heap.getObjectNodes()));

		heap.dereference(nullRef);

		Iterator<FieldEdge> iterator = heap.getFieldEdges().iterator();
		FieldEdge edge = iterator.next();
		assertEquals(internalObject.getId(), edge.getOriginId());
		assertEquals(internalObject2.getId(), edge.getDestinationId());
		assertFalse(iterator.hasNext());

	}

	@Test
	public void testDoFinalStuff() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		indexes.add(0);
		indexes.add(1);
		Heap heap = new Heap(indexes, true);

		InternalObject nullObject = InternalObject.getNullObject();
		ObjectNode globalObject = heap.dereference(ReferenceNode.getGlobalRef()).iterator().next();

		ObjectNode par0 = heap.dereference(heap.getRefToPhantomObject(0)).iterator().next();

		ObjectNode par1 = heap.dereference(heap.getRefToPhantomObject(1)).iterator().next();

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("I1", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(internalRef, internalObject);
		InternalObject internalObject2 = new InternalObject("I2", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap.getObjectNodes().add(internalObject2);
		heap = heap.addField(internalObject, "f", internalObject2);
		heap = heap.addField(internalObject, "f2", nullObject);

		heap = heap.addField(par0, "f", internalObject);
		heap = heap.addField(internalObject2, "f", par0);

		ReferenceNode internalRef2 = new ReferenceNode(4, Category.LOCAL);
		InternalObject internalObject3 = new InternalObject("I3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(internalRef2, internalObject3);
		InternalObject internalObject4 = new InternalObject("I4", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap.getObjectNodes().add(internalObject4);
		heap = heap.addField(internalObject3, "f", internalObject4);

		ReferenceNode internalRef3 = new ReferenceNode(5, Category.LOCAL);
		InternalObject internalObject5 = new InternalObject("I5", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(internalRef3, internalObject5);
		heap = heap.addField(internalObject5, "f", EmptyReturnObjectSet.getInstance());
		heap = heap.addField(EmptyReturnObjectSet.getInstance(), "f", internalObject5);

		InternalObject internalObject6 = new InternalObject("I6", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap.getObjectNodes().add(internalObject6);
		heap = heap.addField(globalObject, "f", internalObject6);
		InternalObject internalObject7 = new InternalObject("I7", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap.getObjectNodes().add(internalObject7);
		heap = heap.addField(internalObject6, "f", internalObject7);

		heap = heap.addField(par1, "f", internalObject7);

		ReferenceNode returnRef = new ReferenceNode(6, Category.LOCAL);
		InternalObject returnObject = new InternalObject("R", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(returnRef, returnObject);

		heap = heap.setReturnRef(returnRef);
		heap = heap.doFinalStuff();

		// resolveEmptyReturnObjectSet();
		assertTrue(hasFieldEdge(heap, internalObject5, returnObject));
		assertTrue(hasFieldEdge(heap, returnObject, internalObject5));

		// removeNullObject
		assertFalse(hasFieldEdge(heap, internalObject, nullObject));

		// propagateEscapeState
		assertEquals(EscapeState.ARG_ESCAPE, heap.getObjectNodes().getObjectNode("I1")
				.getEscapeState());
		assertEquals(EscapeState.ARG_ESCAPE, heap.getObjectNodes().getObjectNode("I2")
				.getEscapeState());

		// collapseGlobalGraph
		assertFalse(heap.getObjectNodes().existsObject("I6"));
		assertFalse(heap.getObjectNodes().existsObject("I7"));

		assertTrue(hasFieldEdge(heap, par1, GlobalObject.getInstance()));

		// removeLocalGraph
		assertFalse(heap.getObjectNodes().existsObject("I3"));
		assertFalse(heap.getObjectNodes().existsObject("I4"));
		assertTrue(heap.getObjectNodes().existsObject("I1"));
		assertTrue(heap.getObjectNodes().existsObject("I2"));
		assertTrue(heap.getObjectNodes().existsObject("I5"));

	}

	private boolean hasFieldEdge(Heap heap, ObjectNode obj, ObjectNode value) {
		boolean r_I5 = false;
		for (FieldEdge edge : heap.getFieldEdges()) {
			if (edge.getOriginId().equals(obj.getId())
					&& edge.getDestinationId().equals(value.getId()))
				r_I5 = true;
		}
		return r_I5;
	}

	@Test
	public void testAddReferenceAndTarget() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		Heap heap = new Heap(indexes, false);

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		heap = heap.addReferenceAndTarget(internalRef, internalObject);
		assertTrue(areLinked(heap, internalRef, internalObject));
	}

	@Test
	public void testAddReferenceToTargets() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		Heap heap = new Heap(indexes, false);

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		ObjectNode internalObjectA = new InternalObject("3a", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		ObjectNode internalObjectB = new InternalObject("3b", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		ObjectNode internalObjectC = new InternalObject("3c", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		Set<ObjectNode> objects = new HashSet<ObjectNode>();
		objects.add(internalObjectA);
		objects.add(internalObjectB);
		objects.add(internalObjectC);

		heap = heap.addReferenceToTargets(internalRef, objects);
		assertTrue(areLinked(heap, internalRef, internalObjectA));
		assertTrue(areLinked(heap, internalRef, internalObjectB));
		assertTrue(areLinked(heap, internalRef, internalObjectC));

		// fügt die Objekte nicht hinzu
	}

	private int size(ObjectNodes argEscapeObjects) {
		int size = 0;
		for (@SuppressWarnings("unused")
		ObjectNode objectNode : argEscapeObjects) {
			size++;
		}
		return size;
	}

	private void checkConnection(Heap heap, int refID, String nodeID) {
		assertTrue(areLinked(heap, heap.getRefToPhantomObject(refID), heap.getObjectNodes()
				.getObjectNode(nodeID)));
	}

	private boolean areLinked(Heap heap, ReferenceNode ref, ObjectNode object) {
		Set<ReferenceNode> referenceNodes = heap.getReferenceNodes();
		assertTrue(referenceNodes.contains(ref));

		ObjectNodes argEscapeObjects = heap.getArgEscapeObjects();
		assertTrue(argEscapeObjects.existsObject(object.getId()));
		ObjectNode objectNode = argEscapeObjects.getObjectNode(object.getId());

		return heap.dereference(ref).contains(objectNode);
	}

}
