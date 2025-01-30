package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.LogicTypeDC;

import java.util.Objects;

public class ClassIdManager {
	private final ClassIdContainer registeredClasses;
	private SRFDictionary dictionary;
	
	public ClassIdManager() {
		super();
		
		this.registeredClasses = new ClassIdContainer();
		this.dictionary = null;
	}

	public void setDictionary(SRFDictionary dictionary) {
		this.dictionary = dictionary;
	}

	public void registerClass(ClassDC classDC, String plantName, boolean isLdS) {
		this.registeredClasses.registerClass(classDC, plantName, isLdS);
	}
	
	public void assignIds() {
		IdSequence idSequence = new IdSequence(dictionary == null ? null : dictionary.getAssignedClassIds());

		for (ClassDC classDC : this.registeredClasses) {
			Integer existingId = null;
			if (dictionary != null) {
				LogicTypeDC lt = classDC.getParent().getLogicType();
				existingId = dictionary.lookupClassId(
						lt.getPlantType().getName(),
						lt.isLdS() ? "LDS" : "LDV",
						classDC.getName()
				);
			}
			classDC.setId(Objects.requireNonNullElseGet(existingId, idSequence::next));
		}
	}
}
