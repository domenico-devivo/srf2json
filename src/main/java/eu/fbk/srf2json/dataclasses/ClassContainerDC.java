package eu.fbk.srf2json.dataclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

public abstract class ClassContainerDC implements INameSortableCaseInsensitive {
	protected List<ClassDC> classes;

	@JsonIgnore
	protected LogicTypeDC logicType;
	
	public ClassContainerDC() {
		super();
		
		this.classes = new ArrayList<>();
		
		this.logicType = null;
	}
	
	public ClassContainerDC addClass(ClassDC classDC) {
		if (classDC != null) {
			this.classes.add(classDC);
			classDC.setParent(this);
			this.logicType.getPlantType().getProject().getClassIdManager().registerClass(
					classDC,
					this.logicType.getPlantType().getName(),
					this.logicType.isLdS()
			);
		}
		return this;
	}
	
	public ClassContainerDC addClasses(Collection<ClassDC> classes) {
		if (classes != null) classes.forEach(classDC -> this.addClass(classDC));
		return this;
	}

	public LogicTypeDC getLogicType() {
		return logicType;
	}

	public ClassDC findClass(String className) {
		return classes.stream().filter(classDC -> classDC.getName().equalsIgnoreCase(className)).findFirst().orElse(null);
	}
	
	// ---------------------------------------------------------------
	
	public abstract boolean isLogic();

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		return sortList(this.classes);
	}

	public boolean isEmpty() {
		return classes.isEmpty();
	}
}
