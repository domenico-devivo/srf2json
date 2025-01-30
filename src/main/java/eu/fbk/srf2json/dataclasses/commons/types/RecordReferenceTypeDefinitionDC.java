package eu.fbk.srf2json.dataclasses.commons.types;

import eu.fbk.srf2json.dataclasses.definitions.RecordDefinitionDC;

public class RecordReferenceTypeDefinitionDC extends ReferenceTypeDefinitionDC {
    private RecordDefinitionDC record;

    public RecordReferenceTypeDefinitionDC(RecordDefinitionDC record) {
        super();

        this.record = record;
    }

    @Override
    public String getName() {
        return this.record.getName();
    }
}
