package eu.fbk.srf2json.dataclasses;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public abstract class ExtensibleDC {
    @JsonIgnore
    private final Map<String, Object> __unknownFields = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> unknownFields() {
        return __unknownFields;
    }

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        // Uncomment the following line if the deserialized object should be extensible
//        __unknownFields.put(name, value);
    }
}
