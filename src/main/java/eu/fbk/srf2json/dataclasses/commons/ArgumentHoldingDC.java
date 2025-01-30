package eu.fbk.srf2json.dataclasses.commons;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Collection;

public interface ArgumentHoldingDC {
    Collection<ArgumentDC> getArguments();

    @JsonGetter("arguments")
    default Collection<ArgumentDC> serializeArguments() {
        Collection<ArgumentDC> actualArguments = getArguments();

        if (actualArguments == null || actualArguments.isEmpty()) {
            return null;
        }

        return actualArguments;
    }
}
