package eu.fbk.srf2json.dataclasses.commons.types;

import eu.fbk.srf2json.dataclasses.definitions.DefinitionDC;

public abstract class TypeDefinitionDC implements DefinitionDC {
	public abstract String getName();
	
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public String toString() {
		return getName() + (isPrimitive() ? "" : "*");
	}
}
