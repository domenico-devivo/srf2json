package eu.fbk.srf2json.dataclasses.commons.types;

public class EnumLiteralStubSafeValue implements SafeValue {
    private String originalName;
    private TypeDefinitionStub enumTypeStub;

    public EnumLiteralStubSafeValue(String originalName, TypeDefinitionStub enumTypeStub) {
        this.originalName = originalName;
        this.enumTypeStub = enumTypeStub;
    }

    @Override
    public SafeValue resolve() {
        if (this.enumTypeStub.hasInstance() && this.enumTypeStub.getInstance() instanceof EnumTypeDefinitionDC) {
            return new FinalSafeValue(((EnumTypeDefinitionDC)this.enumTypeStub.getInstance()).findLiteralValue(this.originalName));
        }

        return null;
    }

    @Override
    public String toString() {
        return null;
    }
}
