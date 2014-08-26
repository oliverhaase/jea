package de.htwg_konstanz.jea.vm;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.htwg_konstanz.jea.vm.Node.EscapeState;
import de.htwg_konstanz.jea.vm.ReferenceNode.Category;
import edu.umd.cs.findbugs.annotations.ExpectWarning;

public class ConnectionGraphTest {

	@Test
	public void testConnectionGraphEmpty() {
		ConnectionGraph cg = new ConnectionGraph(new HashSet<Integer>(), false);

		assertTrue(areLinked(cg, ReferenceNode.getGlobalRef(), GlobalObject.getInstance()));
		assertEquals(1, size(cg.getArgEscapeObjects()));
	}

	@Test
	public void testConnectionGraphEmptyReturn() {
		ConnectionGraph cg = new ConnectionGraph(new HashSet<Integer>(), true);

		assertTrue(areLinked(cg, ReferenceNode.getGlobalRef(), GlobalObject.getInstance()));
		assertTrue(areLinked(cg, ReferenceNode.getReturnRef(), EmptyReturnObjectSet.getInstance()));
		assertEquals(2, size(cg.getArgEscapeObjects()));
	}

	@Test
	public void testConnectionGraph1() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		indexes.add(0);
		indexes.add(2);
		ConnectionGraph cg = new ConnectionGraph(indexes, true);
		assertEquals(4, size(cg.getArgEscapeObjects()));

		checkConnection(cg, 0, "p" + 0);
		checkConnection(cg, 2, "p" + 2);
	}

	@Test(expected = AssertionError.class)
	public void testFieldToNull() {
		ConnectionGraph cg = new ConnectionGraph(new HashSet<Integer>(), false);

		InternalObject nullObject = InternalObject.getNullObject();
		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(internalRef, internalObject);
		cg = cg.addField(nullObject, "f", internalObject);
	}

	@Test
	public void testRemoveNullObject() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		indexes.add(0);
		indexes.add(1);
		ConnectionGraph cg = new ConnectionGraph(indexes, false);

		InternalObject nullObject = InternalObject.getNullObject();
		for (ObjectNode object : cg.dereference(cg.getRefToPhantomObject(0))) {
			cg = cg.addField(object, "f", nullObject);
		}

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(internalRef, internalObject);
		InternalObject internalObject2 = new InternalObject("5", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg.getObjectNodes().add(internalObject2);
		cg = cg.addField(internalObject, "f", internalObject2);
		cg = cg.addField(internalObject, "f2", nullObject);

		cg = cg.addReferenceAndTarget(new ReferenceNode(4, Category.LOCAL), nullObject);

		assertEquals(6, size(cg.getObjectNodes()));

		cg.removeNullObject();

		assertEquals(5, size(cg.getObjectNodes()));

		Iterator<FieldEdge> iterator = cg.getFieldEdges().iterator();
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
		ConnectionGraph cg = new ConnectionGraph(indexes, true);

		InternalObject nullObject = InternalObject.getNullObject();
		ObjectNode globalObject = cg.dereference(ReferenceNode.getGlobalRef()).iterator().next();

		ObjectNode par0 = cg.dereference(cg.getRefToPhantomObject(0)).iterator().next();

		ObjectNode par1 = cg.dereference(cg.getRefToPhantomObject(1)).iterator().next();

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("I1", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(internalRef, internalObject);
		InternalObject internalObject2 = new InternalObject("I2", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg.getObjectNodes().add(internalObject2);
		cg = cg.addField(internalObject, "f", internalObject2);
		cg = cg.addField(internalObject, "f2", nullObject);

		cg = cg.addField(par0, "f", internalObject);
		cg = cg.addField(internalObject2, "f", par0);

		ReferenceNode internalRef2 = new ReferenceNode(4, Category.LOCAL);
		InternalObject internalObject3 = new InternalObject("I3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(internalRef2, internalObject3);
		InternalObject internalObject4 = new InternalObject("I4", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg.getObjectNodes().add(internalObject4);
		cg = cg.addField(internalObject3, "f", internalObject4);

		ReferenceNode internalRef3 = new ReferenceNode(5, Category.LOCAL);
		InternalObject internalObject5 = new InternalObject("I5", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(internalRef3, internalObject5);
		cg = cg.addField(internalObject5, "f", EmptyReturnObjectSet.getInstance());
		cg = cg.addField(EmptyReturnObjectSet.getInstance(), "f", internalObject5);

		InternalObject internalObject6 = new InternalObject("I6", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg.getObjectNodes().add(internalObject6);
		cg = cg.addField(globalObject, "f", internalObject6);
		InternalObject internalObject7 = new InternalObject("I7", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg.getObjectNodes().add(internalObject7);
		cg = cg.addField(internalObject6, "f", internalObject7);

		cg = cg.addField(par1, "f", internalObject7);

		ReferenceNode returnRef = new ReferenceNode(6, Category.LOCAL);
		InternalObject returnObject = new InternalObject("R", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(returnRef, returnObject);

		cg = cg.setReturnRef(returnRef);
		cg = cg.doFinalStuff();

		// resolveEmptyReturnObjectSet();
		assertTrue(hasFieldEdge(cg, internalObject5, returnObject));
		assertTrue(hasFieldEdge(cg, returnObject, internalObject5));

		// removeNullObject
		assertFalse(hasFieldEdge(cg, internalObject, nullObject));

		// propagateEscapeState
		assertEquals(EscapeState.ARG_ESCAPE, cg.getObjectNode("I1").getEscapeState());
		assertEquals(EscapeState.ARG_ESCAPE, cg.getObjectNode("I2").getEscapeState());

		// collapseGlobalGraph
		assertFalse(cg.getObjectNodes().existsObject("I6"));
		assertFalse(cg.getObjectNodes().existsObject("I7"));

		assertTrue(hasFieldEdge(cg, par1, GlobalObject.getInstance()));

		// removeLocalGraph
		assertFalse(cg.getObjectNodes().existsObject("I3"));
		assertFalse(cg.getObjectNodes().existsObject("I4"));
		assertTrue(cg.getObjectNodes().existsObject("I1"));
		assertTrue(cg.getObjectNodes().existsObject("I2"));
		assertTrue(cg.getObjectNodes().existsObject("I5"));

	}

	private boolean hasFieldEdge(ConnectionGraph cg, ObjectNode obj, ObjectNode value) {
		boolean r_I5 = false;
		for (FieldEdge edge : cg.getFieldEdges()) {
			if (edge.getOriginId().equals(obj.getId())
					&& edge.getDestinationId().equals(value.getId()))
				r_I5 = true;
		}
		return r_I5;
	}

	@Test
	public void testAddReferenceAndTarget() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		ConnectionGraph cg = new ConnectionGraph(indexes, false);

		ReferenceNode internalRef = new ReferenceNode(3, Category.LOCAL);
		InternalObject internalObject = new InternalObject("3", "java.lang.Object",
				EscapeState.NO_ESCAPE);
		cg = cg.addReferenceAndTarget(internalRef, internalObject);
		assertTrue(areLinked(cg, internalRef, internalObject));
	}

	@Test
	public void testAddReferenceToTargets() {
		HashSet<Integer> indexes = new HashSet<Integer>();
		ConnectionGraph cg = new ConnectionGraph(indexes, false);

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

		cg = cg.addReferenceToTargets(internalRef, objects);
		assertTrue(areLinked(cg, internalRef, internalObjectA));
		assertTrue(areLinked(cg, internalRef, internalObjectB));
		assertTrue(areLinked(cg, internalRef, internalObjectC));

		// fügt die Objekte nicht hinzu
	}

	private int size(ObjectNodes argEscapeObjects) {
		int size = 0;
		for (ObjectNode objectNode : argEscapeObjects) {
			size++;
		}
		return size;
	}

	private void checkConnection(ConnectionGraph cg, int refID, String nodeID) {
		assertTrue(areLinked(cg, cg.getRefToPhantomObject(refID), cg.getObjectNodes()
				.getObjectNode(nodeID)));
	}

	private boolean areLinked(ConnectionGraph cg, ReferenceNode ref, ObjectNode object) {
		Set<ReferenceNode> referenceNodes = cg.getReferenceNodes();
		assertTrue(referenceNodes.contains(ref));

		ObjectNodes argEscapeObjects = cg.getArgEscapeObjects();
		assertTrue(argEscapeObjects.existsObject(object.getId()));
		ObjectNode objectNode = argEscapeObjects.getObjectNode(object.getId());

		return cg.dereference(ref).contains(objectNode);
	}

}
