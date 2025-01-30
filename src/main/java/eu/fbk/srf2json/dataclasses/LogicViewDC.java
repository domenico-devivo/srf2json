package eu.fbk.srf2json.dataclasses;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A dataclass that represents a view. It specifies the Plant Type
 * it targets
 */
@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "plantType", "name", "classes" })
public class LogicViewDC extends ClassContainerDC {
	private String tag;
	private String plantType;
	private String name;

	public LogicViewDC(LogicTypeDC logicType) {
		super();
		
		this.tag = "LogicView";

		this.plantType = null;
		this.name = null;
		
		this.logicType = logicType;
		logicType.addView(this);
	}
	
	public LogicViewDC setPlantType(String plantType) {
		if (plantType != null) this.plantType = plantType;
		return this;
	}

	public String getPlantType() {
		return plantType;
	}

	public LogicViewDC setName(String name) {
		if (name != null) this.name = name;
		return this;
	}

	@Override
	public LogicViewDC addClass(ClassDC classDC) {
		super.addClass(classDC);
		return this;
	}
	
	@Override
	public LogicViewDC addClasses(Collection<ClassDC> classes) {
		super.addClasses(classes);
		return this;
	}

	@Override
	public boolean isLogic() {
		return false;
	}
}
