package eu.fbk.srf2json.dataclasses.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.ArgumentDC;
import eu.fbk.srf2json.dataclasses.commons.MacroDC;
import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.declarations.AttributeDC;

@JsonPropertyOrder({ "tag", "name", "typology", "type", "range", "arguments", "body" })
public class MacroDefinitionDC extends MacroDC implements DefinitionDC {
	private String body;

	@JsonIgnore
	private String rawBody;

	@JsonIgnore
	private Collection<AttributeDC> referredAttributes;
	
	public MacroDefinitionDC() {
		super();
		
		this.body = null;

		this.tag = "MacroDefinition";
		this.rawBody = null;
		this.referredAttributes = new ArrayList<>();
	}

	public MacroDefinitionDC setBody(String body) {
		if (body != null) this.body = body.trim();
		return this;
	}

	public String getBody() {
		return this.body;
	}

	public MacroDefinitionDC setRawBody(String rawBody) {
		if (rawBody != null) this.rawBody = rawBody;
		return this;
	}

	public String getRawBody() {
		return rawBody;
	}

	@Override
	public MacroDefinitionDC setTypology(String typology) {
		return (MacroDefinitionDC)super.setTypology(typology);
	}

	public String getTypology() {
		return this.typology;
	}
	
	@Override
	public MacroDefinitionDC setName(String name) {
		return (MacroDefinitionDC)super.setName(name);
	}

	@Override
	public MacroDefinitionDC setType(TypeDefinitionDC type) {
		super.setType(type);
		return this;
	}

	@Override
	public MacroDefinitionDC addArgument(ArgumentDC argument) {
		return (MacroDefinitionDC)super.addArgument(argument);
	}

	@Override
	public MacroDefinitionDC addArguments(Collection<ArgumentDC> arguments) {
		return (MacroDefinitionDC)super.addArguments(arguments);
	}
	
	@Override
	public MacroDefinitionDC discardArguments() {
		this.arguments = null;
		return this;
	}
	
	//--------------------------------------------------------------
	
	public void addReferredAttribute(AttributeDC attribute) {
		this.referredAttributes.add(attribute);
	}
	
	public Stream<AttributeDC> getReferredAttributesStream() {
		return this.referredAttributes.stream();
	}
}
