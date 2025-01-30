package eu.fbk.srf2json.dataclasses.declarations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.*;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "typology", "sender", "id", "isHeavy", "arguments", "references" })
public class CommandDC implements DeclarationDC, INameSortableCaseInsensitive, NamedDC, ArgumentHoldingDC {
	private String tag;
	private String typology;
	private String sender;
	private Integer id;
	private String name;
	private Boolean isHeavy;
	private List<ArgumentDC> arguments;
	private List<DeclarationDC> references;

	@JsonIgnore
	private String parsedName;
	
	public CommandDC() {
		super();
		
		this.typology = null;
		this.id = null;
		this.name = null;
		this.isHeavy = null;
		this.sender = null;

		this.tag = "Command";
		this.arguments = new ArrayList<>();
		this.references = null;

		this.parsedName = null;
	}

	public CommandDC setTypology(String typology) {
		if (typology != null) this.typology = typology;
		return this;
	}

	public String getTypology() {
		return this.typology;
	}

	public CommandDC setSender(String sender) {
		if (sender != null) this.sender = sender;
		return this;
	}

	@Override
	public void assignId(int id) {
		this.id = id;
	}

	public CommandDC setName(String name) {
		if (name != null) {
			this.parsedName = name;
			this.name = processName("event " + name);
		}
		return this;
	}
	
	public String getName() {
		return this.name;
	}

	public CommandDC setHeavy(Boolean isHeavy) {
		if (isHeavy != null) this.isHeavy = isHeavy;
		return this;
	}

	public CommandDC addArgument(ArgumentDC argument) {
		this.arguments.add(argument);
		return this;
	}
	
	public CommandDC addArguments(Collection<ArgumentDC> arguments) {
		if (arguments != null) this.arguments.addAll(arguments);
		return this;
	}

	@Override
	public Collection<ArgumentDC> getArguments() {
		return this.arguments;
	}
	
	public CommandDC discardArguments() {
		this.arguments = null;
		return this;
	}

	public CommandDC addReference(DeclarationDC declarationDC) {
		if (references == null) {
			references = new ArrayList<>();
		}
		references.add(declarationDC);
		return this;
	}

	@Override
	public Collection<DeclarationDC> getReferences() {
		return references;
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(this.arguments);
		return new NameAsIDCaseInsensitive(this.name);
	}

	@Override
	public Stream<Integer> getIdsStream() {
		return Stream.of(id);
	}

	@Override
	public String getParsedName() {
		return parsedName;
	}
}
