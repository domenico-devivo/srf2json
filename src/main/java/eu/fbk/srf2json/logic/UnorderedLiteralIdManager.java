package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.LogicTypeDC;
import eu.fbk.srf2json.dataclasses.PlantTypeDC;
import eu.fbk.srf2json.dataclasses.commons.types.EnumLiteralDC;
import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class UnorderedLiteralIdManager {
    private final Collection<EnumTypeDefinitionDC> registeredEnums;

    public UnorderedLiteralIdManager() {
        this.registeredEnums = new ArrayList<>();
    }

    public void registerEnum(EnumTypeDefinitionDC enumDC) {
        registeredEnums.add(enumDC);
    }

    public void assignIds(int startId, Map<String, Integer> alreadyAssigned) {
        int currentId = startId;

        Iterable<EnumTypeDefinitionDC> sortedEnums = registeredEnums.stream().sorted((e1, e2) -> {
            int intermediateRes = e1.getName().compareToIgnoreCase(e2.getName());
            if (intermediateRes != 0) {
                return intermediateRes;
            }

            ClassDC cl1 = e1.getParentClass();
            LogicTypeDC lt1 = cl1.getParent().getLogicType();
            PlantTypeDC p1 = lt1.getPlantType();

            ClassDC cl2 = e2.getParentClass();
            LogicTypeDC lt2 = cl2.getParent().getLogicType();
            PlantTypeDC p2 = lt2.getPlantType();

            // Here we have 2 enums with the same name
            intermediateRes = p1.getName().compareToIgnoreCase(p2.getName());
            if (intermediateRes != 0) {
                return intermediateRes;
            }

            // Here we have 2 enums with the same name inside the same plant
            if (lt1.isLdS() && lt2.isLdV()) {
                return -1;
            } else if (lt1.isLdV() && lt2.isLdS()) {
                return 1;
            }

            // Here we have 2 enums with the same name inside the same plant and logic type
            return cl1.getName().compareToIgnoreCase(cl2.getName());
        })::iterator;

        for (EnumTypeDefinitionDC enumDC : sortedEnums) {
            Iterable<EnumLiteralDC> iterable = enumDC.getLiteralsStream()::iterator;
            for (EnumLiteralDC literalDC : iterable) {
                String literalValue = literalDC.getLiteralValue();
                Integer existingId = alreadyAssigned.get(literalValue);
                if (existingId == null) {
                    literalDC.setGlobalValue(currentId);
                    alreadyAssigned.put(literalValue, currentId);
                    currentId++;
                } else {
                    literalDC.setGlobalValue(existingId);
                }
            }
        }
    }
}
