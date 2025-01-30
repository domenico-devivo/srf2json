package eu.fbk.srf2json.dataclasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonGetter;

import eu.fbk.srf2json.dataclasses.commons.IGenericIDSortable;
import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "logic" })
public class LogicTypeDC implements IGenericIDSortable<Integer> {
	private String tag;
	private String name;
	private LogicDC logic;
	private List<LogicViewDC> views;
	
	@JsonIgnore
	private PlantTypeDC plantType;
	
	public LogicTypeDC(PlantTypeDC plantType) {
		super();
		this.name = null;
		this.logic = null;
	
		this.tag = "LogicType";
		this.views = new ArrayList<>();
		
		this.plantType = plantType;
		plantType.addLogicType(this);
	}
	
	public String getName() {
		return this.name;
	}
	
	public LogicTypeDC setName(String name) {
		if (name != null) this.name = name;
		return this;
	}
	
	public LogicTypeDC setLogic(LogicDC logic) {
		if (logic != null) {
			this.logic = logic;
		}
		return this;
	}
	
	public LogicTypeDC addView(LogicViewDC view) {
		if (view != null) {
			this.views.add(view);
		}
		return this;
	}
	
	public PlantTypeDC getPlantType() {
		return plantType;
	}

	public LogicDC getLogic() {
		return this.logic;
	}

	public Stream<LogicViewDC> getViewsStream() {
		return this.views.stream();
	}

	//------------------------------------------------------------------------
	
	public boolean isLdS() {
		return this.name.toUpperCase().equals("LDS");
	}
	
	public boolean isLdV() {
		return this.name.toUpperCase().equals("LDV");
	}

	//------------------------------------------------------------------------
	
	@Override
	public Integer getSubTreeID() {
		this.logic.getSubTreeID();
		// The views should be already ordered at this point. We just need to ensure the ordered inside each view
		this.views.forEach(LogicViewDC::getSubTreeID);
		// Here we specify the priorities: first LdS, then LdV
		return isLdS() ? 0 : 1;
	}

	@Override
	public String toString() {
		return name + " in " + plantType.getName();
	}

	@JsonGetter("views")
	private List<LogicViewDC> getFilteredViews() {
		return views.stream().filter(logicViewDC -> !logicViewDC.isEmpty()).collect(Collectors.toList());
	}
}
