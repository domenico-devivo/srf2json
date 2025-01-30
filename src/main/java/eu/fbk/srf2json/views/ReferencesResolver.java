package eu.fbk.srf2json.views;

import eu.fbk.srf2json.ClassJsonGeneratorVisitor;
import eu.fbk.srf2json.dataclasses.declarations.AttributeDC;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationDC;
import eu.fbk.srf2json.dataclasses.declarations.MacroDeclarationDC;
import eu.fbk.srf2json.dataclasses.definitions.MacroDefinitionDC;
import eu.fbk.srf2json.logic.DefaultDict;
import eu.fbk.srf2json.views.stringblocktypes.MacroEffettoStringBlockType;
import eu.fbk.srf2json.views.stringblocktypes.MacroValorizzataStringBlockType;
import eu.fbk.srf2json.views.stringblocktypes.MacroVerificaStringBlockType;
import eu.fbk.srf2json.views.stringblocktypes.StringBlockType;

import java.util.Optional;

public class ReferencesResolver {
    private final DefaultDict<DeclarationReference, DeclarationDC> declarationResolver;
    private final DefaultDict<DeclarationReference, StringBlockType> sbtResolver;

    public ReferencesResolver() {
        this.declarationResolver = new DefaultDict<>(declarationRef -> {
            if (declarationRef.getData() instanceof String) {
                String declName = (String)declarationRef.getData();

                Optional<AttributeDC> foundAttributeDC = declarationRef.getClassDC().getDeclarations().getAttributesStream()
                        .filter(attributeDC -> declName.equalsIgnoreCase(attributeDC.getParsedName()))
                        .findFirst();
                if (foundAttributeDC.isPresent()) {
                    return foundAttributeDC.get();
                }

                Optional<MacroDeclarationDC> foundMacro = declarationRef.getClassDC().getDeclarations().getMacrosStream()
                        .filter(macroDC -> declName.equalsIgnoreCase(macroDC.getParsedName()))
                        .findFirst();
                if (foundMacro.isPresent()) {
                    return foundMacro.get();
                }
            }
            return null;
        });
        this.sbtResolver = new DefaultDict<>(declarationRef -> {
            if (declarationRef.getData() instanceof String) {
                String declName = (String)declarationRef.getData();

                Optional<MacroDefinitionDC> foundMacro = declarationRef.getClassDC().getDefinitions().getMacrosStream()
                        .filter(macro -> macro.getParsedName().equalsIgnoreCase(declName))
                        .findFirst();
                if (foundMacro.isPresent()) {
                    return sbtOfMacro(foundMacro.get());
                }
            }
            return null;
        });
    }

    public DeclarationDC resolveDeclaration(DeclarationReference declarationRef) {
        return declarationResolver.get(declarationRef);
    }

    public StringBlockType resolveSbt(DeclarationReference declarationRef) {
        return sbtResolver.get(declarationRef);
    }

    public StringBlockType sbtOfMacro(MacroDefinitionDC macro) {
        StringBlockType sbt;
        String typology = macro.getTypology();

        switch (typology) {
            case ClassJsonGeneratorVisitor.JSONKEY_DEF_TYPOLOGY_MACRO_DI_VERIFICA:
                sbt = new MacroVerificaStringBlockType(macro);
                break;
            case ClassJsonGeneratorVisitor.JSONKEY_DEF_TYPOLOGY_MACRO_DI_EFFETTO:
                sbt = new MacroEffettoStringBlockType(macro);
                break;
            case ClassJsonGeneratorVisitor.JSONKEY_DEF_TYPOLOGY_MACRO_VALORIZZATA:
                sbt = new MacroValorizzataStringBlockType(macro);
                break;
            default:
                throw new IllegalStateException("Unrecognized macro definition typology: " + typology);
        }

        return sbt;
    }
}
