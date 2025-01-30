package eu.fbk.srf2json.dataclasses.commons.types;

public class StateEnumLiteralDC extends EnumLiteralDC {
    public EnumLiteralDC setLiteralValue(String literalValue) {
        if (literalValue != null) this.literalValue = eliminateSpaces(literalValue);
        return this;
    }
}
