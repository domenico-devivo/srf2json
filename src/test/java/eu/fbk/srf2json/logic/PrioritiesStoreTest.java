package eu.fbk.srf2json.logic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class PrioritiesStoreTest {

	@Test
	void testGetExplicitPrioritiesStore() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		
		assertEquals(store.explicitPrioritiesStore, store.getExplicitPrioritiesStore());
	}
	
	@Test
	void testGetImplicitPrioritiesStore() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		
		assertEquals(store.implicitPrioritiesStore, store.getImplicitPrioritiesStore());
	}
	
	@Test
	void testAddTransitionImplicit() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		store.addTransition(42, null);
		
		assertEquals(1, store.implicitPrioritiesStore.size());
		assertEquals(0, store.explicitPrioritiesStore.size());
		
		assertEquals((Integer)42, store.implicitPrioritiesStore.get(0));
	}

	@Test
	void testAddTransitionExplicit() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		store.addTransition(42, 0);
		
		assertEquals(0, store.implicitPrioritiesStore.size());
		assertEquals(1, store.explicitPrioritiesStore.size());
		
		assertEquals((Integer)42, store.explicitPrioritiesStore.get(0));
	}

	@Test
	void testAddTransitionImplicitAndExplicit() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		store.addTransition(41, 1);
		store.addTransition(42, null);
		
		assertEquals(1, store.implicitPrioritiesStore.size());
		assertEquals(1, store.explicitPrioritiesStore.size());
		
		assertEquals((Integer)41, store.explicitPrioritiesStore.get(1));
		assertEquals((Integer)42, store.implicitPrioritiesStore.get(0));
	}
	
	@Test
	void testAddTransitionExplicitException() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		store.addTransition(42, 1);
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> store.addTransition(43, 1));
	    assertEquals("More than one transition with priority 1 has been found for one state", e.getMessage());
	}

	@Test
	void testInsert() {
		PrioritiesStore<Integer> storeFrom = new PrioritiesStore<>();
		PrioritiesStore<Integer> storeInto = new PrioritiesStore<>();
		
		storeFrom.addTransition(1, null);
		storeFrom.addTransition(4, null);
		storeFrom.addTransition(5, 2);
		storeInto.addTransition(3, 3);
		storeInto.addTransition(6, 1);
		storeInto.addTransition(2, null);
		
		storeInto.insert(storeFrom);
		
		assertEquals(3, storeInto.implicitPrioritiesStore.size());
		assertEquals(3, storeInto.explicitPrioritiesStore.size());
		
		assertIterableEquals(List.of(2, 1, 4), storeInto.implicitPrioritiesStore);
		
		assertEquals((Integer)6, storeInto.explicitPrioritiesStore.get(1));
		assertEquals((Integer)5, storeInto.explicitPrioritiesStore.get(2));
		assertEquals((Integer)3, storeInto.explicitPrioritiesStore.get(3));
	}
	
	@Test
	void testInsertException() {
		PrioritiesStore<Integer> storeFrom = new PrioritiesStore<>();
		PrioritiesStore<Integer> storeInto = new PrioritiesStore<>();
		
		storeFrom.addTransition(42, 1);
		storeInto.addTransition(43, 1);
		
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> storeInto.insert(storeFrom));
		assertEquals("Duplicate transition priority 1 during the merge of two priorities stores", e.getMessage());
	}
	
	@Test
	void testIterator() {
		PrioritiesStore<Integer> store = new PrioritiesStore<>();
		
		store.addTransition(1, null);
		store.addTransition(4, null);
		store.addTransition(5, 2);
		store.addTransition(3, 3);
		store.addTransition(6, 1);
		store.addTransition(2, null);
		
		assertIterableEquals(List.of(6, 5, 3, 1, 4, 2), store);
	}
	
}
