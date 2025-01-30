package eu.fbk.srf2json.dataclasses.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

@JsonPropertyOrder({ "macros", "records", "enumeratives" })
public class DefinitionsDC implements INameSortableCaseInsensitive {
	private List<MacroDefinitionDC> macros;
	private List<RecordDefinitionDC> records;
	private List<EnumTypeDefinitionDC> enumeratives;
	
	public DefinitionsDC() {
		macros = new ArrayList<>();
		records = new ArrayList<>();
		enumeratives = new ArrayList<>();
	}
	
	public void addDefinition(DefinitionDC definition) throws IllegalArgumentException {
		if (definition == null) {
			throw new IllegalArgumentException("The definition to be added is null");
		}
		
		if (definition instanceof MacroDefinitionDC) {
			macros.add((MacroDefinitionDC)definition);
		} else {
			if (definition instanceof RecordDefinitionDC) {
				records.add((RecordDefinitionDC)definition);
			} else {
				if (definition instanceof EnumTypeDefinitionDC) {
					enumeratives.add((EnumTypeDefinitionDC)definition);
				} else {
					throw new IllegalArgumentException("The definition to be added is of unrecognized type: " + definition.getClass().getName());
				}
			}
		}
	}
	
	public Stream<MacroDefinitionDC> getMacrosStream() {
		return macros.stream();
	}
	
	public Stream<RecordDefinitionDC> getRecordsStream() {
		return records.stream();
	}

	public Stream<EnumTypeDefinitionDC> getEnumerativesStream() {
		return enumeratives.stream();
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(macros);
		sortList(records);
		sortList(enumeratives);
		return null;
	}
}
