package eu.fbk.srf2json.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.iterators.IteratorChain;

public class PrioritiesStore<T> implements Iterable<T> {
	SortedMap<Integer, T> explicitPrioritiesStore = new TreeMap<>();
	List<T> implicitPrioritiesStore = new ArrayList<>();
	
	public void addTransition(T transition, Integer priority) throws IllegalArgumentException {
		if (priority == null) {
			implicitPrioritiesStore.add(transition);
		} else {
			if (explicitPrioritiesStore.containsKey(priority)) {
				throw new IllegalArgumentException("More than one transition with priority " + priority.toString() + " has been found for one state");
			}
			explicitPrioritiesStore.put(priority, transition);
		}
	}

	public SortedMap<Integer, T> getExplicitPrioritiesStore() {
		return explicitPrioritiesStore;
	}

	public List<T> getImplicitPrioritiesStore() {
		return implicitPrioritiesStore;
	}
	
	public void insert(PrioritiesStore<T> other) throws IllegalStateException {
		if (other == null) return;
		
		other.explicitPrioritiesStore.forEach((priority, transition) -> {
			if (this.explicitPrioritiesStore.containsKey(priority)) {
				throw new IllegalStateException("Duplicate transition priority " + priority.toString() + " during the merge of two priorities stores");
			} else {
				this.explicitPrioritiesStore.put(priority, transition);
			}
		});
		this.implicitPrioritiesStore.addAll(other.implicitPrioritiesStore);
	}

	@Override
	public Iterator<T> iterator() {
		return new IteratorChain<T>(
			explicitPrioritiesStore.values().iterator(),
			implicitPrioritiesStore.iterator()
		);
	}
}
