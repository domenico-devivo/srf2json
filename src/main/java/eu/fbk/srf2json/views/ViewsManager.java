package eu.fbk.srf2json.views;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.fbk.srf2json.SRFErrorListener;
import eu.fbk.srf2json.VisitorUtils;
import eu.fbk.srf2json.dataclasses.*;
import eu.fbk.srf2json.dataclasses.commons.types.TypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.declarations.AttributeDC;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationDC;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationsDC;
import eu.fbk.srf2json.dataclasses.declarations.MacroDeclarationDC;
import eu.fbk.srf2json.dataclasses.definitions.DefinitionsDC;
import eu.fbk.srf2json.dataclasses.definitions.RecordDefinitionDC;
import eu.fbk.srf2json.dataclasses.definitions.RecordFieldDC;
import eu.fbk.srf2json.logic.DefaultDict;

import eu.fbk.srf2json.ClassJsonGeneratorVisitor;
import eu.fbk.srf2json.dataclasses.fsm.TransitionDC;
import eu.fbk.srf2json.views.stringblocktypes.StringBlockType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class ViewsManager {
    public enum StringBlockTypeEnum { CONDITION, EFFECT, MACRO_VERIFICA, MACRO_EFFETTO, MACRO_VALORIZZATA }

    private final ProjectDC projectDC;

	private final Collection<RecordFieldDC> registeredFields;
    private final DefaultDict<ClassDC, ReferencesManager> allDirectReferences;

    private final ReferencesResolver referencesResolver;
    private final DefaultDict<String, ViewsContainer> viewsContainers;

	public ViewsManager(ProjectDC projectDC) {
		super();

        this.projectDC = projectDC;

		this.registeredFields = new ArrayList<>();
        this.referencesResolver = new ReferencesResolver();
        this.allDirectReferences = new DefaultDict<>(classDC -> new ReferencesManager(classDC, this.referencesResolver));
        this.viewsContainers = new DefaultDict<>(ViewsContainer::new);
	}
	
	public void registerClassReferenceRecordField(RecordFieldDC recordField) {
		registeredFields.add(recordField);
	}

	public void generateViews() {
		collectListInstancesInClasses().forEach(this::scanClass);
        instantiateViews();
	}
	
	private DefaultDict<ClassDC, Map<AttributeDC, Collection<String>>> collectListInstancesInClasses() {
        // Now we iterate through all the registered record fields and for each of them find all the attributes in the
        // containing class that have as the type the record containing the field under consideration (the result get
        // grouped by class)
        DefaultDict<ClassDC, Map<AttributeDC, Collection<String>>> byClass = new DefaultDict<>(clazz -> new HashMap<>());
        DefaultDict<RecordDefinitionDC, Set<String>> recordToFields = new DefaultDict<>(recordDefinitionDC -> new HashSet<>());
		registeredFields.forEach(recordFieldDC -> {
            RecordDefinitionDC record = recordFieldDC.getRecord();
            ClassDC parentClass = record.getParentClass();
            String recordName = record.getName();
            Set<String> fieldsToCheck = recordToFields.get(record);
            if (record.isDefaultField(recordFieldDC)) {
                fieldsToCheck.add(null);
            }
            fieldsToCheck.add(recordFieldDC.getName().toLowerCase());

            parentClass.getDeclarations().getAttributesStream()
                .filter(attr -> attr.getType() != null && recordName.equalsIgnoreCase(attr.getType().getName()))
                .forEach(attributeDC -> byClass.get(parentClass).put(attributeDC, fieldsToCheck))
            ;
        });
        return byClass;
	}

	private void scanClass(ClassDC classToScan, Map<AttributeDC, Collection<String>> attributesToFind) {
        ReferencesContainer referencesContainer = new ReferencesContainer(referencesResolver);

        Set<DeclarationReference> referencesToParse = allDirectReferences.get(classToScan).collectReferencesAllBlocks(referencesContainer);

        while (!referencesToParse.isEmpty()) {
            Set<DeclarationReference> newReferencesToParse = new HashSet<>();

            for (DeclarationReference declarationRef : referencesToParse) {
                StringBlockType sbt = referencesResolver.resolveSbt(declarationRef);

                if (sbt != null) {
                    ReferencesManager currentReferences = allDirectReferences.get(declarationRef.getClassDC());
                    ReferencesContainer newRes = currentReferences.getReferencesSingleBlock(sbt);
                    newReferencesToParse.addAll(referencesContainer.update(newRes));
                }
            }

            referencesToParse = newReferencesToParse;
        }
        
        LogicTypeDC actualLogicTypeDC = classToScan.getParent().getLogicType();
        Set<ViewListContext> contextSet = referencesContainer.getDeclarationsStream()
                .filter(viewListContext ->
                        !viewListContext.getReferencedClass().getParent().getLogicType().equals(actualLogicTypeDC))
                .collect(Collectors.toSet());
        collectContexts(classToScan, contextSet);
	}

    private void collectContexts(ClassDC classDC, Set<ViewListContext> contextSet){
        LogicTypeDC logicTypeDC = classDC.getParent().getLogicType();
        PlantTypeDC plantTypeDC = logicTypeDC.getPlantType();

        ViewsContainer viewsContainer = viewsContainers.get(plantTypeDC.getName());

        contextSet.forEach(viewListContext -> {
            ClassDC targetClass = viewListContext.getReferencedClass();
            ViewClassContainer viewClassContainer = viewsContainer.getViewClassContainers(
                    targetClass.getParent().getLogicType().getPlantType().getName()
            ).get(targetClass.getName());
            viewClassContainer.registerDeclName(viewListContext.getDeclName());
        });
    }

    private void instantiateViews() {
        DefaultDict<LogicTypeDC, Map<String, ClassDC>> completeClassesMap = new DefaultDict<>(logicTypeDC -> new CaseInsensitiveMap<>());

        projectDC.getFlatClassesStream().forEach(classDC -> {
            completeClassesMap.get(classDC.getParent().getLogicType()).put(classDC.getName(), classDC);
        });

        projectDC.getPlantTypesStream().forEach(sourcePlantTypeDC -> {
            ViewsContainer viewsContainer = viewsContainers.get(sourcePlantTypeDC.getName());
            projectDC.getPlantTypesStream().forEach(targetPlantTypeDC -> {
                if (!sourcePlantTypeDC.equals(targetPlantTypeDC)) {
                    DefaultDict<String, ViewClassContainer> viewClassContainers = viewsContainer
                        .getViewClassContainers(targetPlantTypeDC.getName())
                    ;

                    fillLogicView(
                        new LogicViewDC(sourcePlantTypeDC.getLdS())
                            .setPlantType(targetPlantTypeDC.getName())
                            .setName(viewsContainer.resolveViewName(targetPlantTypeDC.getName()))
                        ,
                        completeClassesMap.get(targetPlantTypeDC.getLdS()),
                        viewClassContainers
                    );
                }
            });

            DefaultDict<String, ViewClassContainer> viewClassContainers = viewsContainers
                .get(sourcePlantTypeDC.getName())
                .getViewClassContainers(sourcePlantTypeDC.getName())
            ;

            fillLogicView(
                new LogicViewDC(sourcePlantTypeDC.getLdV())
                    .setPlantType(sourcePlantTypeDC.getName())
                    .setName(viewsContainer.resolveViewName(sourcePlantTypeDC.getName()))
                ,
                completeClassesMap.get(sourcePlantTypeDC.getLdS()),
                viewClassContainers
            );
        });

        addEmptyViews();
    }

    private void addEmptyViews() {
        projectDC.getFlatClassesStream()
            .map(ClassDC::getDefinitions)
            .flatMap(DefinitionsDC::getRecordsStream)
            .flatMap(RecordDefinitionDC::getFieldsStream)
            .filter(recordFieldDC -> !recordFieldDC.getType().isPrimitive())
            .forEach(recordFieldDC -> {
                RecordDefinitionDC sourceRecord = recordFieldDC.getRecord();
                ClassDC sourceClass = sourceRecord.getParentClass();
                LogicTypeDC sourceLogicType = sourceClass.getParent().getLogicType();

                PlantTypeDC targetPlantType = projectDC.getPlantTypesStream().filter(plantTypeDC -> plantTypeDC.getName().equalsIgnoreCase(recordFieldDC.getPlant())).findFirst().get();
                LogicTypeDC targetLogicType = recordFieldDC.getLogicRef().equalsIgnoreCase("LdS") ? targetPlantType.getLdS() : targetPlantType.getLdV();
                if (sourceLogicType.equals(targetLogicType)) {
                    return;
                }
                String targetClassName = recordFieldDC.getType().getName();
                ClassDC logicClass = targetLogicType.getLogic().findClass(targetClassName);
                if (logicClass != null) {
                    LogicViewDC view = sourceLogicType.getViewsStream().filter(logicViewDC -> targetPlantType.getName().equalsIgnoreCase(logicViewDC.getPlantType())).findFirst().orElse(null);
                    if (view == null) {
                        view = new LogicViewDC(sourceLogicType);
                    }
                    if (view.findClass(targetClassName) == null) {
                        view.addClass(new ClassDC(
                                logicClass.getName(),
                                new DeclarationsDC(),
                                new DefinitionsDC()
                        ).setId(logicClass.getId()));
                    }
                }
            });
    }

    private void fillLogicView(LogicViewDC logicViewDC, Map<String, ClassDC> classesMap, DefaultDict<String, ViewClassContainer> viewContents) {
        for (Map.Entry<String, ViewClassContainer> entry : viewContents.entrySet()) {
            ClassDC logicClassDC = classesMap.get(entry.getKey());

            if (logicClassDC != null) {
                logicViewDC.addClass(instantiateViewClass(logicClassDC, entry.getValue()));
            }
        }
    }

    private ClassDC instantiateViewClass(ClassDC logicClass, ViewClassContainer viewClassContainer) {
        DeclarationsDC declarationsDC = new DeclarationsDC();
        DefinitionsDC definitionsDC = new DefinitionsDC();

        Set<String> declNamesSetLowerCased = viewClassContainer.getDeclNamesSetLowerCased();

        Set<String> foundTypeNames = new HashSet<>();
        logicClass.getDeclarations().getAttributesStream()
            .filter(attributeDC -> attributeDC.getParsedName() != null && declNamesSetLowerCased.contains(attributeDC.getParsedName().toLowerCase()))
            .forEach(attributeDC -> {
                declarationsDC.addDeclaration(attributeDC, false);
                if (attributeDC.getType() != null) {
                    foundTypeNames.add(attributeDC.getType().getName().toLowerCase());
                }
            })
        ;
        logicClass.getDeclarations().getMacrosStream()
            .filter(macroDeclarationDC -> macroDeclarationDC.getParsedName() != null && declNamesSetLowerCased.contains(macroDeclarationDC.getParsedName().toLowerCase()))
            .forEach(macroDeclarationDC -> {
                declarationsDC.addDeclaration(macroDeclarationDC, false);
                if (macroDeclarationDC.getType() != null) {
                    foundTypeNames.add(macroDeclarationDC.getType().getName().toLowerCase());
                }
            })
        ;

        logicClass.getDefinitions().getEnumerativesStream()
            .filter(enumTypeDefinitionDC -> foundTypeNames.contains(enumTypeDefinitionDC.getName().toLowerCase()))
            .forEach(definitionsDC::addDefinition)
        ;

        logicClass.getDefinitions().getRecordsStream()
            .filter(recordDefinitionDC -> foundTypeNames.contains(recordDefinitionDC.getName().toLowerCase()))
            .forEach(definitionsDC::addDefinition)
        ;

        logicClass.getDefinitions().getMacrosStream()
            .filter(macroDefinitionDC -> macroDefinitionDC.getParsedName() != null && declNamesSetLowerCased.contains(macroDefinitionDC.getParsedName().toLowerCase()))
            .forEach(definitionsDC::addDefinition)
        ;

        return new ClassDC(
            logicClass.getName(),
            declarationsDC,
            definitionsDC
        ).setId(logicClass.getId());
    }
}
