package eu.fbk.srf2json.dataclasses.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IGenericIDSortable<ID extends Comparable<ID>> extends IObjectWithID<ID> {
	default <IDChild extends Comparable<IDChild>, T extends IObjectWithID<IDChild>> IDChild sortList(List<T> lst) {
		if (lst == null) {
			return null;
		}

		Map<IDChild, T> minimumIDtoElement = new HashMap<>();
		List<T> objectsWithoutID = new ArrayList<>();
		IDChild globalMinimumID = null;
		
		
		for (T el : lst) {
			IDChild minimumIDOfElement = el.getSubTreeID();
			
			if (minimumIDOfElement != null) {
				T previous = minimumIDtoElement.putIfAbsent(minimumIDOfElement, el);
				if (previous != null) {
					throw new IllegalStateException("Both " + previous + " and " + el + " have a minimum ID of " + minimumIDOfElement);
				}
				//if (globalMinimumID == null || T.compareTwoIDs(minimumIDOfElement, globalMinimumID) < 0) {
				if (globalMinimumID == null || minimumIDOfElement.compareTo(globalMinimumID) < 0) {
					globalMinimumID = minimumIDOfElement;
				}
			} else {
				objectsWithoutID.add(el);
			}
		}
		
		lst.clear();
		lst.addAll(
			minimumIDtoElement.entrySet().stream()
				//.sorted((e1, e2) -> T.compareTwoIDs(e1.getKey(), e2.getKey()))
				.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
				.map(entry -> entry.getValue())
				.collect(Collectors.toList())
		);
		lst.addAll(objectsWithoutID);
		
		return globalMinimumID;
	}
}
