package eu.fbk.srf2json.dataclasses.declarations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

/**
 * A class containing collections of {@link DeclarationDC}s to be stored
 * inside a {@link eu.fbk.srf2json.dataclasses.ClassDC}.
 */
@JsonPropertyOrder({ "attributes", "commands", "macros" })
public class DeclarationsDC implements INameSortableCaseInsensitive {
	private List<AttributeDC> attributes;
	private List<CommandDC> commands;
	private List<MacroDeclarationDC> macros;
	
	public DeclarationsDC() {
		attributes = new ArrayList<>();
		commands = new ArrayList<>();
		macros = new ArrayList<>();
	}
	
	public void addDeclaration(DeclarationDC declaration, boolean withReferences) throws IllegalArgumentException {
		if (declaration == null) {
			throw new IllegalArgumentException("The declaration to be added is null");
		}
		
		if (declaration instanceof AttributeDC) {
			attributes.add((AttributeDC)declaration);
		} else {
			if (declaration instanceof CommandDC) {
				commands.add((CommandDC)declaration);
			} else {
				if (declaration instanceof MacroDeclarationDC) {
					macros.add((MacroDeclarationDC)declaration);
				} else {
					throw new IllegalArgumentException("The declaration to be added is of unrecognized type: " + declaration.getClass().getName());
				}
			}
		}

		if (withReferences && !declaration.isSecondary() && declaration.getReferences() != null) {
			declaration.getReferences().forEach(declarationDC -> addDeclaration(declarationDC, true));
		}
	}
	
	public Stream<AttributeDC> getAttributesStream() {
		return this.attributes.stream();
	}

	public Stream<CommandDC> getCommandsStream() {
		return this.commands.stream();
	}

	public Stream<MacroDeclarationDC> getMacrosStream() {
		return this.macros.stream();
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(this.attributes);
		sortList(this.commands);
		sortList(this.macros);
		return null;
	}
}
