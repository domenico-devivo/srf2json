package eu.fbk.srf2json.dataclasses.commons.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "lower", "upper" })
public class IntegerRangeDC {
    private Integer lower;
    private Integer upper;

    public IntegerRangeDC() {
        super();

        this.lower = null;
        this.upper = null;
    }

    public void setLowerBound(Integer lowerbound) {
        if (lowerbound != null) this.lower = lowerbound;
    }

    public void setUpperBound(Integer upperbound) {
        if (upperbound != null) this.upper = upperbound;
    }

    public boolean isComplete() {
        return (lower != null && upper != null);
    }
}
