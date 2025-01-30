package eu.fbk.srf2json.logic;

import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;

public class StateEnumsLiteralIdManager {
    public void registerEnum(EnumTypeDefinitionDC enumDC) {
        enumDC.getLiteralsStream().forEach(literalDC -> literalDC.setGlobalValue(literalDC.getLocalValue()));
    }
}
