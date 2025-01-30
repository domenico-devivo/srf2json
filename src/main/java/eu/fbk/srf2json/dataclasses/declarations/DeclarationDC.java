package eu.fbk.srf2json.dataclasses.declarations;

import eu.fbk.srf2json.dataclasses.commons.ReferenceHoldingDC;

import java.util.Collection;
import java.util.stream.Stream;

public interface DeclarationDC extends ReferenceHoldingDC {
    Stream<Integer> getIdsStream();
    void assignId(int id);

    String getName();

    String getParsedName();

    default boolean checkAttributeTypology(String typology) {
        return (this instanceof AttributeDC) && ((AttributeDC)this).getTypology().equals(typology);
    }

    default boolean checkAttributeTypologyAmong(Collection<String> typologies) {
        return (this instanceof AttributeDC) && typologies.contains(((AttributeDC)this).getTypology());
    }

    default boolean checkCommandTypology(String typology) {
        return (this instanceof CommandDC) && ((CommandDC)this).getTypology().equals(typology);
    }
}
