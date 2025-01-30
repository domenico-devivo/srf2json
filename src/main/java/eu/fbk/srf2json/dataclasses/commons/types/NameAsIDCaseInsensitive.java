package eu.fbk.srf2json.dataclasses.commons.types;

public class NameAsIDCaseInsensitive implements Comparable<NameAsIDCaseInsensitive> {
    private final String value;

    public NameAsIDCaseInsensitive(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(NameAsIDCaseInsensitive o) {
        return this.value.compareToIgnoreCase(o.getValue());
    }
}
