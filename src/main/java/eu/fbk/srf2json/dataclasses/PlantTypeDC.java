package eu.fbk.srf2json.dataclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;
import eu.fbk.srf2json.dataclasses.declarations.AttributeDC;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "id", "logicTypes" })
public class PlantTypeDC implements INameSortableCaseInsensitive {
	private String tag;
	private String name;
	private Integer id;
	private List<LogicTypeDC> logicTypes;
	
	@JsonIgnore
	private ProjectDC project;

	@JsonIgnore
	private Collection<AttributeDC> externalReferences;

	public PlantTypeDC(ProjectDC project) {
		super();
		
		this.tag = "PlantType";
		this.logicTypes = new ArrayList<>();
		
		this.name = null;
		this.id = null;
		
		this.project = project;
		project.addPlantType(this);
		this.externalReferences = new ArrayList<>();
	}
	
	public PlantTypeDC setName(String name) {
		if (name != null) this.name = name;
		return this;
	}
	
	public String getName() {
		return this.name;
	}

	public PlantTypeDC setId(Integer id) {
		if (id != null) this.id = id;
		return this;
	}

	public PlantTypeDC addLogicType(LogicTypeDC logicType) {
		if (logicType != null) {
			this.logicTypes.add(logicType);
		}
		return this;
	}
	
	public ProjectDC getProject() {
		return project;
	}
	
	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(this.logicTypes);
		return new NameAsIDCaseInsensitive(this.name);
	}

	public void addExternalReference(AttributeDC attribute) {
		this.externalReferences.add(attribute);
	}

	public LogicTypeDC getLdS() {
		return logicTypes.stream().filter(LogicTypeDC::isLdS).findFirst().orElse(null);
	}

	public LogicTypeDC getLdV() {
		return logicTypes.stream().filter(LogicTypeDC::isLdV).findFirst().orElse(null);
	}
}
