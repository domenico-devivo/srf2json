package eu.fbk.srf2json.views;

import eu.fbk.srf2json.*;
import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.ProjectDC;
import eu.fbk.srf2json.dataclasses.definitions.RecordDefinitionDC;
import eu.fbk.srf2json.dataclasses.definitions.RecordFieldDC;
import eu.fbk.srf2json.dataclasses.fsm.TransitionDC;
import eu.fbk.srf2json.logic.DefaultDict;
import eu.fbk.srf2json.parsing.SRF_blocksLexer;
import eu.fbk.srf2json.parsing.SRF_blocksParser;
import eu.fbk.srf2json.views.stringblocktypes.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ReferencesManager {
    private final ClassDC classDC;
    private final ProjectDC projectDC;
    private final DefaultDict<StringBlockType, ReferencesContainer> containers =
            new DefaultDict<>(this::scanStringBlock);
    private final ReferencesResolver referencesResolver;

    public ReferencesManager(ClassDC classDC, ReferencesResolver referencesResolver) {
        this.classDC = classDC;
        this.projectDC = classDC.getParent().getLogicType().getPlantType().getProject();
        this.referencesResolver = referencesResolver;
    }

    public ReferencesContainer getReferencesSingleBlock(StringBlockType sbt) {
        return containers.get(sbt);
    }

    public Set<DeclarationReference> collectReferencesPermanenceConditions(ReferencesContainer initialContainer, Set<String> stateNames) {
        Set<DeclarationReference> newDeclarationsToParse = new HashSet<>();

        Stream<TransitionDC> transitionsStream = classDC.getFsm().getAllTransitionsStream();
        transitionsStream
                .filter(tr -> stateNames == null || stateNames.contains(tr.getToState().toLowerCase()))
                .filter(tr -> FsmJsonGeneratorVisitor.JSONVALUE_TRANS_TYPE_PERMANENZA.equalsIgnoreCase(tr.getType()))
                .forEach(tr ->
                        newDeclarationsToParse.addAll(
                                initialContainer.update(
                                        getReferencesSingleBlock(new ConditionStringBlockType(tr))
                                )
                        )
                );

        return newDeclarationsToParse;
    }

    public Set<DeclarationReference> collectReferencesAllBlocks(ReferencesContainer initialContainer) {
        Set<DeclarationReference> newDeclarationsToParse = new HashSet<>();

        Stream<TransitionDC> transitionsStream = classDC.getFsm().getAllTransitionsStream();
        transitionsStream.forEach(tr -> {
            newDeclarationsToParse.addAll(
                initialContainer.update(getReferencesSingleBlock(new ConditionStringBlockType(tr)))
            );
            newDeclarationsToParse.addAll(
                initialContainer.update(getReferencesSingleBlock(new EffectStringBlockType(tr)))
            );
        });

        classDC.getDefinitions().getMacrosStream().forEach(macro -> {
            newDeclarationsToParse.addAll(
                initialContainer.update(getReferencesSingleBlock(referencesResolver.sbtOfMacro(macro)))
            );
        });

        return newDeclarationsToParse;
    }

    private ReferencesContainer scanStringBlock(StringBlockType sbt) {
        String stringBlock = sbt.extractStringBlock();
        Set<ViewContext> resultSet = new HashSet<>();
        Set<String> condizionePermanenzaStates = new HashSet<>();
        CharStream inputStream = CharStreams.fromString(stringBlock);
        try {
            SRF_blocksParser parser = SRFLoader.getParser(SRF_blocksParser.class, SRF_blocksLexer.class, inputStream);
            parser.removeErrorListeners();
            parser.addErrorListener(new SRFErrorListener().setErrorContext(classDC.getName(), sbt.getEnclosingObjectName()));
            ViewsBlockListener listener = new ViewsBlockListener(
                    stringBlock,
                    sbt,
                    resultSet,
                    condizionePermanenzaStates
            );
            parser.addParseListener(listener);
            sbt.parse(parser);
        } catch (BlocksParsingException e) {
            Logger.getLogger("eu.fbk.srf2json").severe(e.prepareMessage(classDC.getName(), sbt.getEnclosingObjectName()));
            throw new IncompleteOutputException(e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            String msg = BlocksParsingException.prepareGeneralMessage(
                sbt.label(),
                stringBlock,
                classDC.getName(),
                sbt.getEnclosingObjectName()
            );
            Logger.getLogger("eu.fbk.srf2json").log(Level.SEVERE, msg, e);
            throw new IncompleteOutputException(e);
        }
        if (condizionePermanenzaStates.contains(null)) {
            resultSet.add(new ViewContext(
                    ClassJsonGeneratorVisitor.JSONVALUE_VARIABLE_NAME_STATE,
                    null,
                    null
            ));
        }
        ReferencesContainer res = new ReferencesContainer(referencesResolver);
        resultSet.forEach(viewContext -> {
            ClassDC targetClass = resolveFieldType(viewContext.getFieldName(), viewContext.getListName());
            res.registerDeclaration(
                    targetClass,
                    new DeclarationReference(targetClass, viewContext.getDeclName())
            );
        });
        if (!condizionePermanenzaStates.isEmpty()) {
            if (condizionePermanenzaStates.contains(null)) {
                collectReferencesPermanenceConditions(res, null);
            } else {
                collectReferencesPermanenceConditions(res, condizionePermanenzaStates);
            }
        }
        return res;
    }

    private ClassDC resolveFieldType(String fieldName, String listName) {
        if (listName == null) {
            return classDC;
        }

        String listTypeName = classDC.getDeclarations().getAttributesStream()
                .filter(attributeDC -> listName.equalsIgnoreCase(attributeDC.getParsedName())).findFirst().get()
                .getType().getName();
        RecordDefinitionDC record = classDC.getDefinitions().getRecordsStream()
                .filter(recordDefinitionDC -> recordDefinitionDC.getName().equalsIgnoreCase(listTypeName)).findFirst().get();
        RecordFieldDC recordField = fieldName == null
                ? record.getDefaultField()
                : record.getFieldsStream()
                    .filter(recordFieldDC -> recordFieldDC.getParsedName().equalsIgnoreCase(fieldName)).findFirst().get();
        return projectDC.getFlatClassesStream().filter(classDC -> classDC.getName().equalsIgnoreCase(recordField.getType().getName()) && classDC.getParent().getLogicType().getPlantType().getName().equalsIgnoreCase(recordField.getPlant())).findFirst().get();
    }
}
