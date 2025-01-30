package eu.fbk.srf2json.dataclasses.commons.types;

import eu.fbk.srf2json.dataclasses.commons.NamedDC;

public class NamedTypeDefinitionDC extends TypeDefinitionDC implements NamedDC {
	protected String name;
	
	public NamedTypeDefinitionDC() {
		super();
		
		this.name = null;
	}
	
	public NamedTypeDefinitionDC setName(String name) {
		if (name != null) this.name = name;
		return this;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
}
