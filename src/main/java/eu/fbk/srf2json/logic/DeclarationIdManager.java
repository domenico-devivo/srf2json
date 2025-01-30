package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.ClassJsonGeneratorVisitor;
import eu.fbk.srf2json.dataclasses.commons.ReferenceHoldingDC;
import eu.fbk.srf2json.dataclasses.declarations.AttributeDC;
import eu.fbk.srf2json.dataclasses.declarations.CommandDC;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationDC;

import java.util.*;
import java.util.stream.Stream;

public class DeclarationIdManager {
    private final Comparator<DeclarationDC> referencesComparator = (r1, r2) -> {
        String t1 = ((AttributeDC)r1).getTypology();
        if (t1.equalsIgnoreCase(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_PREVIOUS_VARIABLE)) {
            return -1;
        }
        return 1;
    };

    public void assignIds(Stream<? extends DeclarationDC> declarationsStream, Map<String, Integer> attrIds) {
        DeclarationIdContainer declarationIdContainer = new DeclarationIdContainer();
        declarationsStream.forEach(declarationIdContainer::registerDeclarationDC);
        IdSequence idSequence = new IdSequence(attrIds == null ? null : attrIds.values());

        for (DeclarationDC declarationDC : declarationIdContainer) {
            Integer existingId = attrIds == null ? null : attrIds.get(declarationDC.getName());

            if (existingId == null) {
                declarationDC.assignId(idSequence.next());

                if (declarationDC.checkAttributeTypology(ClassJsonGeneratorVisitor.JSONKEY_DECL_TYPOLOGY_COUNTER)) {
                    declarationDC.assignId(idSequence.next());
                }
            } else {
                declarationDC.assignId(existingId);
            }

            Collection<DeclarationDC> references = declarationDC.getReferences();
            if (references != null) {
                references.stream().sorted(referencesComparator).forEach(referencedDC -> referencedDC.assignId(idSequence.next()));
            }
        }
    }
}
