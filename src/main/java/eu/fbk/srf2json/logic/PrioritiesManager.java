package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.VisitorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrioritiesManager<T> {

	public enum TransitionType { PERMANENZA, ATTUAZIONE, NORMALIZZAZIONE, ATTUAZIONE_CON_SOCCORSO };

	List<PrioritiesStore<T>> currentPrioritiesStores = null;
	PrioritiesStore<T> currentPrioritiesStore = null;

	Integer revealedPriority = null;
	
	PrioritiesStore<T> initialTransitionsStore = new PrioritiesStore<T>();
	Map<String, List<PrioritiesStore<T>>> overallPriorities = new HashMap<>();

	public PrioritiesStore<T> getInitialTransitionsStore() {
		return initialTransitionsStore;
	}

	public PrioritiesStore<T> getExitTransitionsStore(String stateName, TransitionType transitionType) {
		return doGetExitTransitionsStore(convertStateName(stateName), transitionType);
	}
	
	private PrioritiesStore<T> doGetExitTransitionsStore(String stateName, TransitionType transitionType) {
		List<PrioritiesStore<T>> prioritiesStores = overallPriorities.get(stateName);
		return (prioritiesStores == null) ? null : prioritiesStores.get(transitionType.ordinal());
	}

	public void initializeState() throws IllegalStateException {
		if (currentPrioritiesStores != null) {
			throw new IllegalStateException("Initializing a new state while there is a state that has been initialized but not finalized");
		}
		
		int typesCount = TransitionType.values().length;
		
		currentPrioritiesStores = new ArrayList<>(typesCount);
		
		for (int i = 0; i < typesCount; i++) {
			currentPrioritiesStores.add(new PrioritiesStore<T>());
		}
		
		// The first transition we expect to meet is Permanenza (but it can be absent in case of the state "*")
		currentPrioritiesStore = getPrioritiesStoreByType(currentPrioritiesStores, TransitionType.PERMANENZA);
	}
	
	public void finalizeState(String stateName) throws IllegalArgumentException {
		doFinalizeState(convertStateName(stateName));
	}
	
	private void doFinalizeState(String stateName) throws IllegalArgumentException {
		if (overallPriorities.containsKey(stateName)) {
			throw new IllegalArgumentException("State " + stateName + " has already been registered");
		}
		
		overallPriorities.put(stateName, currentPrioritiesStores);

		currentPrioritiesStores = null;
		currentPrioritiesStore = null;
		revealedPriority = null;
	}
	
	public void registerInitialTransitionsStart() {
		currentPrioritiesStore = initialTransitionsStore;
		revealedPriority = null;
	}
	
	public void registerStateTransitionsStart(TransitionType transitionType) throws IllegalStateException {
		if (currentPrioritiesStores == null) {
			throw new IllegalStateException("No state has been initialized to start state transitions list");
		}
		currentPrioritiesStore = getPrioritiesStoreByType(currentPrioritiesStores, transitionType);
		revealedPriority = null;
	}
	
	public void registerTransitionsEnd() {
		currentPrioritiesStore = null;
		revealedPriority = null;
	}
	
	public void registerTransition(T transition) throws IllegalStateException {
		if (currentPrioritiesStore == null) {
			throw new IllegalStateException("Found a transition object while no transition type is set");
		}
		currentPrioritiesStore.addTransition(transition, revealedPriority);
		revealedPriority = null;
	}
	
	public void registerPriority(int priorityValue) {
		revealedPriority = priorityValue;
	}
	
	private String convertStateName(String stateName) {
		// we store all the state names in uppercase, since the SRFs are case-insensitive (at least for the state names)
		return VisitorUtils.eliminateSpaces(stateName).toUpperCase();
	}
	
	private PrioritiesStore<T> getPrioritiesStoreByType(List<PrioritiesStore<T>> prioritiesStores, TransitionType transitionType) {
		return prioritiesStores.get(transitionType.ordinal());
	}
}
