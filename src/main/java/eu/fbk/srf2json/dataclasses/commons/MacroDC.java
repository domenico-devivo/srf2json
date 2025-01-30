package eu.fbk.srf2json.dataclasses.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.commons.types.TypedDC;

public abstract class MacroDC extends TypedDC implements INameSortableCaseInsensitive, NamedDC, ArgumentHoldingDC {
	protected String tag;
	protected String typology;
	protected String name;
	protected List<ArgumentDC> arguments;

	@JsonIgnore
	protected String parsedName;

	public MacroDC() {
		super();
		
		this.typology = null;
		this.name = null;

		this.arguments = new ArrayList<>();

		this.parsedName = null;
	}

	public MacroDC setTypology(String typology) {
		if (typology != null) this.typology = typology;
		return this;
	}

	public MacroDC setName(String name) {
		if (name != null) {
			this.parsedName = name;
			this.name = processName("macro " + name);
		}
		return this;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public MacroDC setType(TypeDefinitionDC type) {
		super.setType(type);
		return this;
	}
	
	public MacroDC addArgument(ArgumentDC argument) {
		this.arguments.add(argument);
		return this;
	}
	
	public MacroDC addArguments(Collection<ArgumentDC> arguments) {
		if (arguments != null) this.arguments.addAll(arguments);
		return this;
	}

	@Override
	public Collection<ArgumentDC> getArguments() {
		return this.arguments;
	}
	
	public MacroDC discardArguments() {
		this.arguments = null;
		return this;
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(this.arguments);
		return new NameAsIDCaseInsensitive(this.name);
	}

	public String getParsedName() {
		return parsedName;
	}
}
