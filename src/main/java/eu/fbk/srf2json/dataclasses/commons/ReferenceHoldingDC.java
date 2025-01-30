package eu.fbk.srf2json.dataclasses.commons;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationDC;

import java.util.Collection;
import java.util.stream.Collectors;

public interface ReferenceHoldingDC {
    Collection<DeclarationDC> getReferences();

    @JsonGetter("references")
    default Collection<Integer> serializeReferences() {
        Collection<DeclarationDC> actualReferences = getReferences();

        if (actualReferences == null || actualReferences.isEmpty()) {
            return null;
        }

        Collection<Integer> res = actualReferences.stream()
            .flatMap(DeclarationDC::getIdsStream)
            .sorted()
            .collect(Collectors.toList())
        ;

        return res.isEmpty() ? null : res;
    }

    default boolean isSecondary() { return false; }

    default ReferenceHoldingDC setSecondary(Boolean secondary) { return this; }
}
