package eu.fbk.srf2json.dataclasses.definitions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.FieldDC;
import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;

@JsonPropertyOrder({ "name", "type", "plant", "logic" })
public class RecordFieldDC extends FieldDC {
	private String plant;
	@JsonIgnore
	private String logicRef;
	@JsonIgnore
	private String parsedName;
	
	@JsonIgnore
	private RecordDefinitionDC record;
	
	public RecordFieldDC() {
		super();
		
		this.plant = null;
		this.logicRef = null;
		this.parsedName = null;

		this.record = null;
	}
	
	@Override
	public RecordFieldDC setName(String name) {
		if (name != null) parsedName = name;
		return (RecordFieldDC)super.setName(name);
	}

	public String getName() {
		return this.name;
	}

	public String getParsedName() {
		return parsedName;
	}
	
	public RecordFieldDC setPlant(String plant) {
		if (plant != null) this.plant = plant;
		return this;
	}
	
	public String getPlant() {
		return this.plant;
	}
	
	@Override
	public RecordFieldDC setType(TypeDefinitionDC type) {
		super.setType(type);
		return this;
	}

	public RecordFieldDC setLogicRef(String logicRef) {
		if (logicRef != null) this.logicRef = logicRef;
		return this;
	}

	public String getLogicRef() {
		return logicRef;
	}

	//-----------------------------------------------------------------
	
	public void setRecord(RecordDefinitionDC record) {
		this.record = record;
	}

	public RecordDefinitionDC getRecord() {
		return this.record;
	}

	//-----------------------------------------------------------------
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		String parentPlantTypeName = null;
		if (this.record != null) {
			parentPlantTypeName = this.record.getParentClass().getParent().getLogicType().getPlantType().getName();
			
			if (this.plant != null && !this.plant.equals(parentPlantTypeName)) {
				sb
					.append(this.plant)
					.append(".")
				;
			}
		}

		sb.append(this.type);
		sb.append(" ");
			
		if (this.record != null && parentPlantTypeName != null) {
			sb
				.append(parentPlantTypeName)
				.append(".")
				.append(this.record.getParentClass().getName())
				.append(".")
				.append(this.record.getName())
				.append(".")
			;
		}
			
		sb.append(this.name);
		
		return sb.toString();
	}
	
	@JsonGetter("plant")
	public String getPlantForExport() {
		if (this.type.isPrimitive()) {
			return null;
		}
		return this.plant;
	}

	@JsonGetter("logic")
	public String getLogicForExport() {
		if (this.type.isPrimitive()) {
			return null;
		}

		return this.logicRef;
	}
}
