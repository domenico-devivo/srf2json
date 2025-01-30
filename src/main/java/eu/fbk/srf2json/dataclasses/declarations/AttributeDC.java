package eu.fbk.srf2json.dataclasses.declarations;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.NamedDC;
import eu.fbk.srf2json.dataclasses.commons.types.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "ids", "typology", "receiver", "type", "range", "accessibility", "visibility", "safeValue", "duration", "references" })
public class AttributeDC extends TypedDC implements DeclarationDC, INameSortableCaseInsensitive, NamedDC {
	private String tag;
	private String typology;
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final List<Integer> ids;
	private String name;
	private String accessibility;
	private String visibility;
	private String duration;
	private SafeValue safeValue;
	private String receiver;
	private List<DeclarationDC> references;
	
	@JsonIgnore
	private String stateVariableName; // contains the name of the variable if it's a state variable (preserving case)

	@JsonIgnore
	private String parsedName;

	@JsonIgnore
	private Boolean isRestoreVariable; // indicates that it's a restore variable

	@JsonIgnore
	private Boolean secondary; // indicated whether this attribute is secondary (generated and added later) and not defined explicitly

	public AttributeDC() {
		super();
		this.typology = null;
		this.name = null;
		this.accessibility = null;
		this.visibility = null;
		this.duration = null;
		this.safeValue = null;
		this.receiver = null;
		
		this.tag = "Attribute";
		this.ids = new ArrayList<>();
		this.stateVariableName = null;
		this.parsedName = null;
		this.isRestoreVariable = false;
		this.secondary = false;
		this.references = null;
	}
	
	public AttributeDC setTypology(String typology) {
		if (typology != null) this.typology = typology;
		return this;
	}

	public String getTypology() {
		return this.typology;
	}

	@Override
	public void assignId(int id) {
		ids.add(id);
	}

	public AttributeDC setName(String name) {
		if (name != null) {
			this.parsedName = name;
			this.name = processName(name);
		}
		return this;
	}

	public AttributeDC setNameRaw(String name) {
		if (name != null) this.name = name;
		return this;
	}

	public AttributeDC setStateVariableName(String stateVariableName) {
		if (stateVariableName != null) this.stateVariableName = stateVariableName;
		return this;
	}

	@Override
	public AttributeDC setType(TypeDefinitionDC type) {
		super.setType(type);
		return this;
	}

	public AttributeDC setAccessibility(String accessibility) {
		if (accessibility != null) this.accessibility = accessibility;
		return this;
	}

	public String getAccessibility() {
		return accessibility;
	}

	public AttributeDC setVisibility(String visibility) {
		if (visibility != null) this.visibility = visibility;
		return this;
	}

	public AttributeDC setDuration(String duration) {
		if (duration != null) this.duration = processName(duration);
		return this;
	}

	public AttributeDC setSafeValue(SafeValue safeValue) {
		if (safeValue != null) this.safeValue = safeValue;
		return this;
	}

	public AttributeDC setReceiver(String receiver) {
		if (receiver != null) this.receiver = receiver;
		return this;
	}

	public SafeValue getSafeValue() {
		if (this.safeValue == null) {
			return null;
		}

		this.safeValue = safeValue.resolve();

		return this.safeValue;
	}

	@JsonGetter("safeValue")
	public String getSafeValueAsStr() {
		SafeValue safeValue = getSafeValue();
		if (safeValue == null) {
			return null;
		}

		return safeValue.toString();
	}

	public AttributeDC setRestoreVariable(Boolean isRestoreVariable) {
		if (isRestoreVariable != null) this.isRestoreVariable = isRestoreVariable;
		return this;
	}

	@Override
	public boolean isSecondary() {
		return secondary;
	}

	@Override
	public AttributeDC setSecondary(Boolean secondary) {
		if (secondary != null) this.secondary = secondary;
		return this;
	}

	public String getName() {
		if (this.isStateVariable()) {
			return this.stateVariableName;
		}
		return this.name;
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		return new NameAsIDCaseInsensitive(this.getName());
	}
	
	public boolean isStateVariable() {
		return this.stateVariableName != null;
	}

	public String getParsedName() {
		return parsedName;
	}

	public AttributeDC addReference(DeclarationDC declarationDC) {
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
	public Stream<Integer> getIdsStream() {
		return ids.stream();
	}

	@Override
	public String toString() {
		return (type != null ? type.getName() + " " : "") + getName();
	}
}
