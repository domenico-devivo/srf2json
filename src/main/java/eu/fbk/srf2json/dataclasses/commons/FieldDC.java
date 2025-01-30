package eu.fbk.srf2json.dataclasses.commons;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.commons.types.TypedDC;

@JsonPropertyOrder({ "name", "type", "range" })
public abstract class FieldDC extends TypedDC implements NamedDC {
    protected String name;
    protected String typeName;

    public FieldDC() {
        super();

        this.name = null;
    }

    public FieldDC setName(String name) {
        if (name != null) this.name = processName(name);
        return this;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public FieldDC setType(TypeDefinitionDC type) {
        super.setType(type);
        return this;
    }
}
