package eu.fbk.srf2json.dataclasses.commons;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;

@JsonPropertyOrder({ "name", "type" })
public class ArgumentDC extends FieldDC implements INameSortableCaseInsensitive {
	@Override
	public ArgumentDC setName(String name) {
		super.setName(name);
		return this;
	}

	@Override
	public ArgumentDC setType(TypeDefinitionDC type) {
		super.setType(type);
		return this;
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		return new NameAsIDCaseInsensitive(this.name);
	}
}
