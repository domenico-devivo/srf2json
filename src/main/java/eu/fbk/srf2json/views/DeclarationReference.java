package eu.fbk.srf2json.views;

import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.views.stringblocktypes.StringBlockType;

public class DeclarationReference {
    private final ClassDC classDC;
    private final Object data;

    public DeclarationReference(ClassDC classDC, Object data) {
        this.classDC = classDC;
        this.data = data;
    }

    @Override
    public int hashCode() {
        return classDC.hashCode() + data.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationReference odr = ((DeclarationReference)o);

        return classDC.equals(odr.classDC) && data.equals(odr.data);
    }

    public ClassDC getClassDC() {
        return classDC;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return data.toString() + " of " + classDC.toString();
    }
}
