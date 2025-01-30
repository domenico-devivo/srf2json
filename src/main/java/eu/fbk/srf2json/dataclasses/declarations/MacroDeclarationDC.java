package eu.fbk.srf2json.dataclasses.declarations;

import java.util.Collection;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.ArgumentDC;
import eu.fbk.srf2json.dataclasses.commons.MacroDC;
import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "typology", "type", "range", "accessibility", "arguments" })
public class MacroDeclarationDC extends MacroDC implements DeclarationDC {
	private String accessibility;
	
	public MacroDeclarationDC() {
		super();
		
		this.accessibility = null;

		this.tag = "MacroDeclaration";
	}

	public MacroDeclarationDC setAccessibility(String accessibility) {
		if (accessibility != null) this.accessibility = accessibility;
		return this;
	}

	@Override
	public MacroDeclarationDC setTypology(String typology) {
		return (MacroDeclarationDC)super.setTypology(typology);
	}

	@Override
	public MacroDeclarationDC setName(String name) {
		return (MacroDeclarationDC)super.setName(name);
	}

	@Override
	public MacroDeclarationDC setType(TypeDefinitionDC type) {
		super.setType(type);
		return this;
	}

	@Override
	public MacroDeclarationDC addArgument(ArgumentDC argument) {
		return (MacroDeclarationDC)super.addArgument(argument);
	}

	@Override
	public MacroDeclarationDC addArguments(Collection<ArgumentDC> arguments) {
		return (MacroDeclarationDC)super.addArguments(arguments);
	}
	
	@Override
	public MacroDeclarationDC discardArguments() {
		this.arguments = null;
		return this;
	}

	@Override
	public Collection<DeclarationDC> getReferences() {
		return null;
	}

	@Override
	public Stream<Integer> getIdsStream() {
		return Stream.empty();
	}

	@Override
	public void assignId(int id) {}
}
