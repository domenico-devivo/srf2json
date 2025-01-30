package eu.fbk.srf2json.logic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import eu.fbk.srf2json.logic.PrioritiesManager.TransitionType;

class PrioritiesManagerTest {

	@Test
	void testGetInitialTransitionsStore() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		assertEquals(prioritiesManager.initialTransitionsStore, prioritiesManager.getInitialTransitionsStore());
	}
	
	@Test
	void testGetExitTransitionsStoreSuccess() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		// It's not nice for unit tests to involve other functions, but we need it to check the result
		prioritiesManager.initializeState();
		prioritiesManager.finalizeState("TEST");
				
		assertEquals(prioritiesManager.overallPriorities.get("TEST").get(TransitionType.ATTUAZIONE_CON_SOCCORSO.ordinal()), prioritiesManager.getExitTransitionsStore("test", TransitionType.ATTUAZIONE_CON_SOCCORSO));
	}
	
	@Test
	void testGetExitTransitionsStoreMissingState() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		assertNull(prioritiesManager.getExitTransitionsStore("test", TransitionType.ATTUAZIONE_CON_SOCCORSO));
	}
	
	@Test
	void testInitializeStateSuccess() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		assertNull(prioritiesManager.currentPrioritiesStores);
		assertNull(prioritiesManager.currentPrioritiesStore);
		
		prioritiesManager.initializeState();
		
		assertNotNull(prioritiesManager.currentPrioritiesStores);
		assertNotNull(prioritiesManager.currentPrioritiesStore);
	}
	
	@Test
	void testInitializeStateException() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		prioritiesManager.initializeState();
		assertThrows(IllegalStateException.class, () -> prioritiesManager.initializeState());
	}
	
	@Test
	void testFinalizeStateSuccess() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		// It's not nice for unit tests to involve other functions, but we need it to check the result
		prioritiesManager.initializeState();
		
		prioritiesManager.revealedPriority = 42;
		
		assertEquals(0, prioritiesManager.overallPriorities.size());
		
		prioritiesManager.finalizeState("test");
		
		assertEquals(1, prioritiesManager.overallPriorities.size());
		assertTrue(prioritiesManager.overallPriorities.containsKey("TEST")); // we store all the state names in uppercase
		assertNull(prioritiesManager.currentPrioritiesStores);
		assertNull(prioritiesManager.currentPrioritiesStore);
		assertNull(prioritiesManager.revealedPriority);
	}
	
	@Test
	void testFinalizeStateException() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		// It's not nice for unit tests to involve other functions, but we need it to check the result
		prioritiesManager.initializeState();
		prioritiesManager.finalizeState("test");
		prioritiesManager.initializeState();
		
		assertThrows(IllegalArgumentException.class, () -> prioritiesManager.finalizeState("tESt"));
	}
	
	@Test
	void testRegisterInitialTransitionsStart() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		prioritiesManager.revealedPriority = 42;
		
		assertNull(prioritiesManager.currentPrioritiesStore);
		
		prioritiesManager.registerInitialTransitionsStart();
		
		assertNull(prioritiesManager.revealedPriority);
		assertEquals(prioritiesManager.initialTransitionsStore, prioritiesManager.currentPrioritiesStore);
	}
	
	@Test
	void testRegisterStateTransitionsStartSuccess() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		// It's not nice for unit tests to involve other functions, but otherwise we'll get an exception
		prioritiesManager.initializeState();
		
		prioritiesManager.revealedPriority = 42;
		prioritiesManager.registerStateTransitionsStart(TransitionType.PERMANENZA);
		PrioritiesStore<Integer> perm = prioritiesManager.currentPrioritiesStore;
		prioritiesManager.registerStateTransitionsStart(TransitionType.ATTUAZIONE);
		PrioritiesStore<Integer> att = prioritiesManager.currentPrioritiesStore;
		prioritiesManager.registerStateTransitionsStart(TransitionType.NORMALIZZAZIONE);
		PrioritiesStore<Integer> norm = prioritiesManager.currentPrioritiesStore;
		prioritiesManager.registerStateTransitionsStart(TransitionType.ATTUAZIONE_CON_SOCCORSO);
		PrioritiesStore<Integer> att_socc = prioritiesManager.currentPrioritiesStore;
		
		assertNotNull(prioritiesManager.currentPrioritiesStore);
		assertNull(prioritiesManager.revealedPriority);
		
		assertNotNull(perm);
		assertNotNull(att);
		assertNotNull(norm);
		assertNotNull(att_socc);
		
		assertNotEquals(perm, att);
		assertNotEquals(perm, norm);
		assertNotEquals(perm, att_socc);
		assertNotEquals(att, norm);
		assertNotEquals(att, att_socc);
		assertNotEquals(norm, att_socc);
	}
	
	@Test
	void testRegisterStateTransitionsStartCorrect() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		// It's not nice for unit tests to involve other functions, but otherwise we'll get an exception
		prioritiesManager.initializeState();
		
		PrioritiesStore<Integer> defaultStore = prioritiesManager.currentPrioritiesStore;
		
		prioritiesManager.registerStateTransitionsStart(TransitionType.NORMALIZZAZIONE);
		PrioritiesStore<Integer> norm = prioritiesManager.currentPrioritiesStore;
		
		assertNotEquals(defaultStore, norm);
		
		// It's not nice for unit tests to involve other functions, but we need it to check the result
		prioritiesManager.registerTransitionsEnd();
		prioritiesManager.finalizeState("test");
		PrioritiesStore<Integer> returned = prioritiesManager.getExitTransitionsStore("test", TransitionType.NORMALIZZAZIONE);
		
		assertNull(prioritiesManager.currentPrioritiesStore);
		assertNotNull(returned);
		assertEquals(norm, returned);
	}
	
	@Test
	void testRegisterStateTransitionsStartException() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		assertThrows(IllegalStateException.class, () -> prioritiesManager.registerStateTransitionsStart(TransitionType.NORMALIZZAZIONE));
	}
	
	@Test
	void testRegisterTransitionsEnd() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		prioritiesManager.currentPrioritiesStore = new PrioritiesStore<>();
		prioritiesManager.revealedPriority = 42;
		
		prioritiesManager.registerTransitionsEnd();
		
		assertNull(prioritiesManager.currentPrioritiesStore);
		assertNull(prioritiesManager.revealedPriority);
	}
	
	@Test
	void testRegisterTransitionImplicit() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		prioritiesManager.currentPrioritiesStore = new PrioritiesStore<>();
		int initialSizeImplicit = prioritiesManager.currentPrioritiesStore.implicitPrioritiesStore.size();
		int initialSizeExplicit = prioritiesManager.currentPrioritiesStore.explicitPrioritiesStore.size();
		
		prioritiesManager.registerTransition(42);
		
		assertNull(prioritiesManager.revealedPriority);
		assertEquals(initialSizeImplicit + 1, 	prioritiesManager.currentPrioritiesStore.implicitPrioritiesStore.size()); 
		assertEquals(initialSizeExplicit, 		prioritiesManager.currentPrioritiesStore.explicitPrioritiesStore.size());
		assertEquals((Integer)42, prioritiesManager.currentPrioritiesStore.implicitPrioritiesStore.get(initialSizeImplicit));
	}
	
	@Test
	void testRegisterTransitionExplicit() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		prioritiesManager.revealedPriority = 1;
		
		prioritiesManager.currentPrioritiesStore = new PrioritiesStore<>();
		int initialSizeImplicit = prioritiesManager.currentPrioritiesStore.implicitPrioritiesStore.size();
		int initialSizeExplicit = prioritiesManager.currentPrioritiesStore.explicitPrioritiesStore.size();
		
		prioritiesManager.registerTransition(42);
		
		assertNull(prioritiesManager.revealedPriority);
		assertEquals(initialSizeImplicit, 		prioritiesManager.currentPrioritiesStore.implicitPrioritiesStore.size()); 
		assertEquals(initialSizeExplicit + 1, 	prioritiesManager.currentPrioritiesStore.explicitPrioritiesStore.size());
		assertEquals((Integer)42, prioritiesManager.currentPrioritiesStore.explicitPrioritiesStore.get(1));
	}
	
	@Test
	void testRegisterTransitionImplicitException() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		assertThrows(IllegalStateException.class, () -> prioritiesManager.registerTransition(42));
	}

	@Test
	void testRegisterPriority() {
		PrioritiesManager<Integer> prioritiesManager = new PrioritiesManager<>();
		
		assertNull(prioritiesManager.revealedPriority);
		
		prioritiesManager.registerPriority(42);
		
		assertEquals((Integer)42, prioritiesManager.revealedPriority);
	}

}
