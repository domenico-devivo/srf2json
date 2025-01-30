package eu.fbk.srf2json.dataclasses.commons.types;

public class FinalSafeValue implements SafeValue {
    private String value;

    public FinalSafeValue(String value) {
        this.value = value;
    }

    @Override
    public SafeValue resolve() {
        return this;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
