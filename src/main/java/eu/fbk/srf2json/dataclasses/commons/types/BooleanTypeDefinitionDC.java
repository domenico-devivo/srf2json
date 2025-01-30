package eu.fbk.srf2json.dataclasses.commons.types;

import eu.fbk.srf2json.ClassJsonGeneratorVisitor;

public class BooleanTypeDefinitionDC extends TypeDefinitionDC {
	@Override
	public String getName() {
		return ClassJsonGeneratorVisitor.JSONVALUE_TYPE_NAME_BOOLEAN;
	}
}
