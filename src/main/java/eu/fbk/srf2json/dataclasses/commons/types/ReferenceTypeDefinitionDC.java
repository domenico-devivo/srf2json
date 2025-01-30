package eu.fbk.srf2json.dataclasses.commons.types;

public class ReferenceTypeDefinitionDC extends NamedTypeDefinitionDC {
	@Override
	public ReferenceTypeDefinitionDC setName(String name) {
		super.setName(name);
		return this;
	}
	
	@Override
	public boolean isPrimitive() {
		return false;
	}
}
