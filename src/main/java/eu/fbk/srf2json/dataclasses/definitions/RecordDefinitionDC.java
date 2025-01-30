package eu.fbk.srf2json.dataclasses.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.NamedDC;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "fields" })
public class RecordDefinitionDC implements DefinitionDC, INameSortableCaseInsensitive, NamedDC {
	private final String tag;
	private String name;
	private final List<RecordFieldDC> fields;

	@JsonIgnore
	private ClassDC parentClass;

	public RecordDefinitionDC() {
		super();
		
		this.name = null;

		this.tag = "Record";
		this.fields = new ArrayList<>();
		this.parentClass = null;
	}

	public RecordDefinitionDC setName(String name) {
		if (name != null) this.name = name;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public RecordDefinitionDC setParentClass(ClassDC parentClass) {
		if (parentClass != null) this.parentClass = parentClass;
		return this;
	}

	public ClassDC getParentClass() {
		return this.parentClass;
	}

	public RecordDefinitionDC addField(RecordFieldDC field) {
		this.fields.add(field);
		field.setRecord(this);
		return this;
	}
	
	public RecordDefinitionDC addFields(Collection<RecordFieldDC> fields) {
		if (fields != null) fields.forEach(this::addField);
		return this;
	}
	
	public Stream<RecordFieldDC> getFieldsStream() {
		return this.fields.stream();
	}

	public boolean isDefaultField(RecordFieldDC field) {
		// If no field is mentioned in SRF for an attribute of a record type, the first defined record field is referred
		return !fields.isEmpty() && field.equals(fields.get(0));
	}

	public RecordFieldDC getDefaultField() {
		if (fields.isEmpty()) {
			return null;
		}
		return fields.get(0);
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		return new NameAsIDCaseInsensitive(this.name);
	}
}
