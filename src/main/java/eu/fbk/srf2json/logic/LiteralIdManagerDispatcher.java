package eu.fbk.srf2json.logic;

import java.util.*;
import java.util.stream.Collectors;

import eu.fbk.srf2json.dataclasses.commons.types.EnumLiteralDC;
import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;

public class LiteralIdManagerDispatcher {
	private final StateEnumsLiteralIdManager stateEnumsLiteralIdManager;
	private final OrderedLiteralIdManager orderedLiteralIdManager;
	private final UnorderedLiteralIdManager unorderedLiteralIdManager;

	public LiteralIdManagerDispatcher() {
		this.stateEnumsLiteralIdManager = new StateEnumsLiteralIdManager();
		this.orderedLiteralIdManager = new OrderedLiteralIdManager();
		this.unorderedLiteralIdManager = new UnorderedLiteralIdManager();
	}

	public void registerEnum(EnumTypeDefinitionDC enumDC, boolean isStateEnum) {
		if (isStateEnum) {
			stateEnumsLiteralIdManager.registerEnum(enumDC);
		} else if (enumDC.getOrdered()) {
			orderedLiteralIdManager.registerEnum(enumDC);
		} else {
			unorderedLiteralIdManager.registerEnum(enumDC);
		}
	}

	public void assignIds(int startId) {
		int currentId = startId;

		Map<String, Integer> alreadyAssigned = new HashMap<>();

		// first we assign IDs to all the ordered literals
		currentId = orderedLiteralIdManager.assignIds(currentId, alreadyAssigned);
		
		// then we process the unordered literals
		unorderedLiteralIdManager.assignIds(currentId, alreadyAssigned);
	}
}
