package eu.fbk.srf2json.dataclasses.commons.types;

public class NameAsIDCaseSensitive implements Comparable<NameAsIDCaseSensitive> {
    private final String value;

    public NameAsIDCaseSensitive(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(NameAsIDCaseSensitive o) {
        return this.value.compareTo(o.getValue());
    }
}
