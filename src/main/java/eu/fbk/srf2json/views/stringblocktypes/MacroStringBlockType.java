package eu.fbk.srf2json.views.stringblocktypes;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.definitions.MacroDefinitionDC;

public abstract class MacroStringBlockType implements StringBlockType {
    MacroDefinitionDC macroDefinitionDC;

    public MacroStringBlockType(MacroDefinitionDC macroDefinitionDC) {
        this.macroDefinitionDC = macroDefinitionDC;
    }

    @Override
    public String extractStringBlock() {
        return macroDefinitionDC.getRawBody();
    }

    @Override
    public String getEnclosingObjectName() {
        return macroDefinitionDC.getName();
    }


    @Override
    public int hashCode() {
        return macroDefinitionDC.hashCode() + label().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return macroDefinitionDC.equals(((MacroStringBlockType)o).macroDefinitionDC);
    }
}
