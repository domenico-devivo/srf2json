package eu.fbk.srf2json.dataclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;
import eu.fbk.srf2json.logic.ClassIdManager;
import eu.fbk.srf2json.logic.SRFDictionary;
import eu.fbk.srf2json.views.ViewsManager;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "projectName", "latestRelease", "plantTypes" })
public class ProjectDC implements INameSortableCaseInsensitive {
	private String projectName;
	private ReleaseDC latestRelease;
	private final List<PlantTypeDC> plantTypes;

	@JsonIgnore
	private SRFDictionary dictionary;
	
	@JsonIgnore
	private final ClassIdManager classIdManager;
	
	// This collection is needed just to access all the classes inside the project as a flat collection (without hierarchy)
	@JsonIgnore
	private final Collection<ClassDC> flatClassesCollection;

	@JsonIgnore
	private final ViewsManager viewsManager;

	public ProjectDC() {
		super();
		
		this.plantTypes = new ArrayList<>();
		this.projectName = null;
		this.latestRelease = null;

		this.classIdManager = new ClassIdManager();
		this.dictionary = null;
		this.flatClassesCollection = new ArrayList<>();

		this.viewsManager = new ViewsManager(this);
	}
	
	public ProjectDC setProjectName(String projectName) {
		if (projectName != null) this.projectName = projectName;
		return this;
	}

	public ProjectDC setLatestRelease(ReleaseDC latestRelease) {
		if (latestRelease != null) this.latestRelease = latestRelease;
		return this;
	}

	public ProjectDC addPlantType(PlantTypeDC plantType) {
		if (plantType != null) {
			this.plantTypes.add(plantType);
		}
		return this;
	}
	
	public ClassIdManager getClassIdManager() {
		return this.classIdManager;
	}
	
	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		return sortList(this.plantTypes);
	}
	
	public SRFDictionary getDictionary() {
		return this.dictionary;
	}
	
	public void setDictionary(SRFDictionary dictionary) {
		if (dictionary != null) {
			this.dictionary = dictionary;
			classIdManager.setDictionary(dictionary);
		}
	}

	public void addClassToFlatCollection(ClassDC classDC) {
		flatClassesCollection.add(classDC);
	}

	public Stream<ClassDC> getFlatClassesStream() {
		return flatClassesCollection.stream();
	}

	public Stream<PlantTypeDC> getPlantTypesStream() {
		return plantTypes.stream();
	}

	public ViewsManager getViewsManager() {
		return this.viewsManager;
	}

	public void assignIdsToPlants() {
		int currentId = 1;
		for (PlantTypeDC plantTypeDC : plantTypes) {
			plantTypeDC.setId(currentId);
			currentId++;
		}
	}
}
