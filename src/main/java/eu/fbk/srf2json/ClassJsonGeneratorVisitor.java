package eu.fbk.srf2json;

import eu.fbk.srf2json.dataclasses.*;
import eu.fbk.srf2json.dataclasses.commons.ArgumentDC;
import eu.fbk.srf2json.dataclasses.commons.types.*;
import eu.fbk.srf2json.dataclasses.declarations.*;
import eu.fbk.srf2json.dataclasses.definitions.*;
import eu.fbk.srf2json.logic.LiteralIdManagerDispatcher;
import eu.fbk.srf2json.logic.TypesManager;
import eu.fbk.srf2json.logic.SRFDictionary;
import eu.fbk.srf2json.views.ViewsManager;
import eu.fbk.srf2json.parsing.SRF_definitionsBaseVisitor;
import eu.fbk.srf2json.parsing.SRF_definitionsParser;
import eu.fbk.srf2json.parsing.SRF_definitionsParser.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ClassJsonGeneratorVisitor extends SRF_definitionsBaseVisitor<Object> {
	
	public static final String JSONKEY_DECL_TYPOLOGY_PARAMETER = "Parameter";
	public static final String JSONKEY_DECL_TYPOLOGY_LIST = "List";
	public static final String JSONKEY_DECL_TYPOLOGY_VARIABILE = "Variable";
	public static final String JSONKEY_DECL_TYPOLOGY_RESPONSE_VARIABLE = "ResponseVariable";
	public static final String JSONKEY_DECL_TYPOLOGY_RESTORE_VARIABLE = "RestoreVariable";
	public static final String JSONKEY_DECL_TYPOLOGY_PREVIOUS_VARIABLE = "PreviousVariable";
	public static final String JSONKEY_DECL_TYPOLOGY_TIMER = "Timer";
	public static final String JSONKEY_DECL_TYPOLOGY_COUNTER = "Counter";
	public static final String JSONKEY_DECL_TYPOLOGY_COMANDO_MANUALE = "ManualCommand";
	public static final String JSONKEY_DECL_TYPOLOGY_COMANDO_AUTOMATICO = "AutomaticCommand";
	public static final String JSONKEY_DECL_TYPOLOGY_COMANDO_DI_OUTPUT = "OutputCommand";
	public static final String JSONKEY_DECL_TYPOLOGY_CONTROLLO_DAL_PIAZZALE = "PiazzaleControl";
	public static final String JSONKEY_DECL_TYPOLOGY_COMANDO_AL_PIAZZALE = "PiazzaleCommand";
	public static final String JSONKEY_DECL_TYPOLOGY_MACRO_DI_VERIFICA = "MacroVerifica";
	public static final String JSONKEY_DECL_TYPOLOGY_MACRO_VALORIZZATA = "MacroValorizzata";
	public static final String JSONKEY_DECL_TYPOLOGY_MACRO_DI_EFFETTO = "MacroEffetto";
	public static final String JSONKEY_DECL_TYPOLOGY_STATE = "State";
	public static final String JSONKEY_DECL_TYPOLOGY_RESTORE = "Restore";
	public static final String JSONKEY_DECL_TYPOLOGY_ATTRIBUTE = "Attribute";
	
	public static final String JSONVALUE_TYPE_NAME_BOOLEAN = "Boolean";
	public static final String JSONVALUE_TYPE_NAME_INTEGER = "Integer";
	public static final String JSONVALUE_TYPE_NAME_COMMAND_RESPONSE = "CommandResponse";
	public static final String JSONVALUE_TYPE_NAME_TIMER_STATE = "TimerState";

	public static final String JSONVALUE_VARIABLE_NAME_STATE = "stato";

	public static final String JSONVALUE_ACCESSIBILITY_PRIVATE = "private";
	public static final String JSONVALUE_ACCESSIBILITY_PROTECTED = "protected";
	public static final String JSONVALUE_ACCESSIBILITY_PUBLIC = "public";

	public static final String JSONVALUE_VISIBILITY_VISIBLE = "visible";
	public static final String JSONVALUE_VISIBILITY_INVISIBLE = "invisible";

	public static final String JSONKEY_DEF_TYPOLOGY_MACRO_DI_VERIFICA = JSONKEY_DECL_TYPOLOGY_MACRO_DI_VERIFICA;
	public static final String JSONKEY_DEF_TYPOLOGY_MACRO_VALORIZZATA = JSONKEY_DECL_TYPOLOGY_MACRO_VALORIZZATA;
	public static final String JSONKEY_DEF_TYPOLOGY_MACRO_DI_EFFETTO = JSONKEY_DECL_TYPOLOGY_MACRO_DI_EFFETTO;	

	private final LiteralIdManagerDispatcher literalIdManager;
	private final SRFDictionary dictionary;
	private final String plantName;
	private final String logicType;
	private final TypesManager typesManager;
	private final ViewsManager viewsManager;

	private String stateVariableTypeStr;
	private EnumTypeDefinitionDC stateVariableTypeEnum;
	private boolean currentEnumIsState;

	private final Map<Integer, String> accessibilityDict = Map.ofEntries(
			Map.entry(SRF_definitionsParser.PRIVAT_, JSONVALUE_ACCESSIBILITY_PRIVATE),
			Map.entry(SRF_definitionsParser.PROTETT_, JSONVALUE_ACCESSIBILITY_PROTECTED),
			Map.entry(SRF_definitionsParser.PUBBLIC_, JSONVALUE_ACCESSIBILITY_PUBLIC)
	);

	private final Map<Integer, String> visibilityDict = Map.ofEntries(
			Map.entry(SRF_definitionsParser.VISIBILE, JSONVALUE_VISIBILITY_VISIBLE),
			Map.entry(SRF_definitionsParser.INVISIBILE, JSONVALUE_VISIBILITY_INVISIBLE)
	);
	
	public ClassJsonGeneratorVisitor(LiteralIdManagerDispatcher literalIdManager, SRFDictionary dictionary, String plantName, String logicType, TypesManager typesManager, ViewsManager viewsManager) {
		super();
		
		this.literalIdManager = literalIdManager;
		this.dictionary = dictionary;
		this.plantName = plantName;
		this.logicType = logicType;
		this.typesManager = typesManager;
		this.viewsManager = viewsManager;

		this.stateVariableTypeStr = null;
		this.stateVariableTypeEnum = null;
		this.currentEnumIsState = false;
	}
	
	@Override
	protected Object aggregateResult(Object aggregate, Object nextResult) {
		if (nextResult != null) {
			return nextResult;
		}
	    return aggregate;
	}
	
	@Override
	public ClassDC visitRoot(RootContext ctx) {
		ClassDC res;

		if (ctx.foglio_definizioni_LdS() != null) {
			res = visitFoglio_definizioni_LdS(ctx.foglio_definizioni_LdS());
		} else {
			if (ctx.foglio_definizioni_LdV() != null) {
				res = visitFoglio_definizioni_LdV(ctx.foglio_definizioni_LdV());
			} else {
				throw new IllegalArgumentException("The file has been parsed neither as LdS, nor as LdV");
			}
		}

		res.setStatesEnum(stateVariableTypeEnum);
		res.getDefinitions().getEnumerativesStream().forEach(enumerative -> enumerative.setParentClass(res));

		return res;
	}
	
	@Override
	public ClassDC visitFoglio_definizioni_LdS(Foglio_definizioni_LdSContext ctx) {
		ClassDC res = new ClassDC(
			visitId(ctx.id()),
			visitDeclarations_LdS(ctx.declarations_LdS()),
			visitDefinitions_LdS(ctx.definitions_LdS())
		);

		res.getDefinitions().getRecordsStream().forEach(record -> record.setParentClass(res));

		return res;
	}
	
	@Override
	public ClassDC visitFoglio_definizioni_LdV(Foglio_definizioni_LdVContext ctx) {
		ClassDC res = new ClassDC(
			visitId(ctx.id()),
			visitDeclarations_LdV(ctx.declarations_LdV()),
			visitDefinitions_LdV(ctx.definitions_LdV())
		);

		res.getDefinitions().getRecordsStream().forEach(record -> record.setParentClass(res));

		return res;
	}
	
//============================  Declarations =================================================
	
	@Override
	public DeclarationsDC visitDeclarations_LdS(Declarations_LdSContext ctx) {
		return visitDeclarations(ctx.declaration_LdS());
	}

	@Override
	public DeclarationsDC visitDeclarations_LdV(Declarations_LdVContext ctx) {
		return visitDeclarations(ctx.declaration_LdV());
	}

	private DeclarationsDC visitDeclarations(List<? extends RuleContext> ctx_declaration) {
		DeclarationsDC res = new DeclarationsDC();
		ctx_declaration.forEach(declarationCtx -> res.addDeclaration(visitDeclaration(declarationCtx), true));
		return res;
	}
	
	// Actually never used
	@Override
	public DeclarationDC visitDeclaration_LdS(Declaration_LdSContext ctx) {
		return visitDeclaration(ctx);
	}

	// Actually never used
	@Override
	public DeclarationDC visitDeclaration_LdV(Declaration_LdVContext ctx) {
		return visitDeclaration(ctx);
	}

	private DeclarationDC visitDeclaration(RuleContext ctx) { return (DeclarationDC)visitChildren(ctx); }
	
	@Override
	public AttributeDC visitParameter(ParameterContext ctx) {
		return new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_PARAMETER)
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
			.setAccessibility(visitAccessibility(ctx.accessibility()))
		;
	}

	@Override
	public AttributeDC visitList(ListContext ctx) {
		return new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_LIST)
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
		;
	}

	@Override
	public AttributeDC visitVariable(VariableContext ctx) {
		String var_name = visitId(ctx.id());
		TypeDefinitionDC type = visitType_suffix(ctx.type_suffix());

		Boolean restore_variable = visitRestore_variable(ctx.restore_variable());

		if (restore_variable) {
			return new AttributeDC()
				.setTypology(JSONKEY_DECL_TYPOLOGY_RESTORE)
				.setName(var_name)
				.setType(type)
				.setAccessibility(visitAccessibility(ctx.accessibility()))
				.setVisibility(visitVisibility(ctx.visibility()))
				.setRestoreVariable(true)
			;
		}

		AttributeDC res;

		if (var_name.equalsIgnoreCase(JSONVALUE_VARIABLE_NAME_STATE)) {
			stateVariableTypeStr = type.getName();

			res = new AttributeDC()
				.setTypology(JSONKEY_DECL_TYPOLOGY_STATE)
				.setStateVariableName(var_name)
				.setName(var_name)
				.setType(type)
				.setAccessibility(visitAccessibility(ctx.accessibility()))
				.setVisibility(visitVisibility(ctx.visibility()))
			;
		} else {
			res = new AttributeDC()
					.setTypology(JSONKEY_DECL_TYPOLOGY_VARIABILE)
					.setName(var_name)
					.setType(type)
					.setAccessibility(visitAccessibility(ctx.accessibility()))
					.setVisibility(visitVisibility(ctx.visibility()))
			;
		}

		if (visitRestorable(ctx.restorable())) {
			res.addReference(new AttributeDC()
				.setTypology(JSONKEY_DECL_TYPOLOGY_RESTORE_VARIABLE)
				.setNameRaw(res.getName() + "_restore")
				.setType(res.getType())
				.setAccessibility(determineGeneratedAccessibility(res.getAccessibility()))
				.setVisibility(JSONVALUE_VISIBILITY_VISIBLE)
				.setSecondary(true)
				.addReference(res)
			);
		}

		if (visitPrecedence(ctx.precedence())) {
			res.addReference(new AttributeDC()
				.setTypology(JSONKEY_DECL_TYPOLOGY_PREVIOUS_VARIABLE)
				.setNameRaw(res.getName() + "_prev")
				.setType(res.getType())
				.setAccessibility(determineGeneratedAccessibility(res.getAccessibility()))
				.setVisibility(JSONVALUE_VISIBILITY_VISIBLE)
				.setSecondary(true)
				.addReference(res)
			);
		}

		return res;
	}

	@Override
	public AttributeDC visitTimer(TimerContext ctx) {
		AttributeDC res = new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_TIMER)
			.setName(visitId(ctx.id()))
			.setAccessibility(visitAccessibility(ctx.accessibility()))
			.setVisibility(visitVisibility(ctx.visibility()))
			.setDuration(visitDuration(ctx.duration()))
		;

		if (visitRestorable(ctx.restorable())) {
			res.addReference(new AttributeDC()
				.setTypology(JSONKEY_DECL_TYPOLOGY_RESTORE_VARIABLE)
				.setNameRaw(res.getName() + "_restore")
				.setType(TypesManager.getTimerStateInstance())
				.setAccessibility(determineGeneratedAccessibility(res.getAccessibility()))
				.setVisibility(JSONVALUE_VISIBILITY_VISIBLE)
				.setSecondary(true)
				.addReference(res)
			);
		}

		return res;
	}

	@Override
	public AttributeDC visitCounter(CounterContext ctx) {
		return new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_COUNTER)
			.setName(visitId(ctx.id()))
			.setAccessibility(visitAccessibility(ctx.accessibility()))
			.setVisibility(visitVisibility(ctx.visibility()))
		;
	}

	@Override
	public CommandDC visitComando_manuale(Comando_manualeContext ctx) {
		CommandDC res = new CommandDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_COMANDO_MANUALE)
			.setName(visitId(ctx.id()))
			.setHeavy(visitWeight(ctx.weight()))
			.setSender(visitSender(ctx.sender()))
			.discardArguments()
		;

		res.addReference(new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_RESPONSE_VARIABLE)
			.setNameRaw(res.getName() + "_response")
			.setType(TypesManager.getCommandResponseInstance())
			.setAccessibility(JSONVALUE_ACCESSIBILITY_PRIVATE)
			.setVisibility(JSONVALUE_VISIBILITY_VISIBLE)
			.setSecondary(true)
			.addReference(res)
		);

		return res;
	}

	@Override
	public CommandDC visitComando_automatico(Comando_automaticoContext ctx) {
		return new CommandDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_COMANDO_AUTOMATICO)
			.setName(visitId(ctx.id()))
			.addArguments(visitArguments(ctx.arguments()))
		;
	}

	@Override
	public AttributeDC visitControllo_dal_piazzale(Controllo_dal_piazzaleContext ctx) {
		TypeDefinitionDC type = visitType_suffix(ctx.type_suffix());

		AttributeDC res = new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_CONTROLLO_DAL_PIAZZALE)
			.setName(visitId(ctx.id()))
			.setType(type)
			.setAccessibility(visitAccessibility(ctx.accessibility()))
			.setSafeValue(processValore_sicuro(visitValore_sicuro(ctx.valore_sicuro()), type))
		;

		if (visitPrecedence(ctx.precedence())) {
			res.addReference(new AttributeDC()
				.setTypology(JSONKEY_DECL_TYPOLOGY_PREVIOUS_VARIABLE)
				.setNameRaw(res.getName() + "_prev")
				.setType(res.getType())
				.setAccessibility(determineGeneratedAccessibility(res.getAccessibility()))
				.setVisibility(JSONVALUE_VISIBILITY_VISIBLE)
				.setSecondary(true)
				.addReference(res)
			);
		}

		return res;
	}

	@Override
	public AttributeDC visitComando_al_piazzale(Comando_al_piazzaleContext ctx) {
		TypeDefinitionDC type = visitType_suffix(ctx.type_suffix());

		return new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_COMANDO_AL_PIAZZALE)
			.setName(visitId(ctx.id()))
			.setType(type)
			.setSafeValue(processValore_sicuro(visitValore_sicuro(ctx.valore_sicuro()), type))
		;
	}
	
	@Override
	public MacroDeclarationDC visitDeclaration_macro_verifica(Declaration_macro_verificaContext ctx) {
		return new MacroDeclarationDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_MACRO_DI_VERIFICA)
			.setName(visitId(ctx.id()))
			.setType(TypesManager.getBooleanInstance())
			.setAccessibility(visitAccessibility(ctx.accessibility()))
			.addArguments(visitArguments(ctx.arguments()))
		;
	}

	@Override
	public MacroDeclarationDC visitDeclaration_macro_valorizzata(Declaration_macro_valorizzataContext ctx) {
		return new MacroDeclarationDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_MACRO_VALORIZZATA)
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
			.setAccessibility(visitAccessibility(ctx.accessibility()))
			.addArguments(visitArguments(ctx.arguments()))
		;
	}

	@Override
	public MacroDeclarationDC visitDeclaration_macro_effetto(Declaration_macro_effettoContext ctx) {
		return new MacroDeclarationDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_MACRO_DI_EFFETTO)
			.setName(visitId(ctx.id()))
			.addArguments(visitArguments(ctx.arguments()))
		;
	}
	
	@Override
	public AttributeDC visitAttribute(AttributeContext ctx) {
		return new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_ATTRIBUTE)
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
		;
	}

	@Override
	public AttributeDC visitComando_output(Comando_outputContext ctx) {
		TypeDefinitionDC type = visitType_suffix(ctx.type_suffix());

		return new AttributeDC()
			.setTypology(JSONKEY_DECL_TYPOLOGY_COMANDO_DI_OUTPUT)
			.setName(visitId(ctx.id()))
			.setType(type)
			.setSafeValue(processValore_sicuro(visitValore_sicuro(ctx.valore_sicuro()), type))
			.setReceiver(visitReceiver(ctx.receiver()))
		;
	}
	
//============================  Definitions  =================================================

	@Override
	public DefinitionsDC visitDefinitions_LdS(Definitions_LdSContext ctx) {
		return visitDefinitions(ctx.definition_LdS());
	}

	@Override
	public DefinitionsDC visitDefinitions_LdV(Definitions_LdVContext ctx) {
		return visitDefinitions(ctx.definition_LdV());
	}

	private DefinitionsDC visitDefinitions(List<? extends RuleContext> ctx_definition) {
		DefinitionsDC res = new DefinitionsDC();
		ctx_definition.forEach(definitionCtx -> res.addDefinition(visitDefinition(definitionCtx)));
		return res;
	}
	
	// Actually never used
	@Override
	public DefinitionDC visitDefinition_LdS(Definition_LdSContext ctx) {
		return visitDefinition(ctx);
	}

	// Actually never used
	@Override
	public DefinitionDC visitDefinition_LdV(Definition_LdVContext ctx) {
		return visitDefinition(ctx);
	}

	private DefinitionDC visitDefinition(RuleContext ctx) { return (DefinitionDC)visitChildren(ctx); }
	
	@Override
	public TypeDefinitionDC visitDefinition_type(Definition_typeContext ctx) {
		TypeDefinitionDC res;
		
		if (ctx.boolean_def != null) {
			res = TypesManager.getBooleanInstance();
		} else {
			if (ctx.integer_def != null) {
				res = visitDefinition_integer_range(ctx.integer_def);
			} else {
				if (ctx.enum_def != null) {
					String name = visitId(ctx.id());
					currentEnumIsState = name.equals(stateVariableTypeStr);
					EnumTypeDefinitionDC enumDef = visitDefinition_enum_def(ctx.enum_def);
					if (currentEnumIsState) {
						stateVariableTypeEnum = enumDef;
						currentEnumIsState = false;
					}
					enumDef.setName(name);
					typesManager.registerEnumDefinition(enumDef);
					res = enumDef;
				} else {
					throw new IllegalArgumentException("The type definition is not a boolean, integer, or enum");
				}
			}
		}
				
		return res;
	}

	@Override
	public RecordDefinitionDC visitDefinition_record_LdS(Definition_record_LdSContext ctx) {
		RecordDefinitionDC res = new RecordDefinitionDC()
			.setName(visitId(ctx.id()))
			.addFields(visitDefinition_record_fields_LdS(ctx.definition_record_fields_LdS()))
		;

		this.typesManager.registerRecordReferenceIfAny(new RecordReferenceTypeDefinitionDC(res));

		return res;
	}

	@Override
	public RecordDefinitionDC visitDefinition_record_LdV(Definition_record_LdVContext ctx) {
		RecordDefinitionDC res = new RecordDefinitionDC()
			.setName(visitId(ctx.id()))
			.addFields(visitDefinition_record_fields_LdV(ctx.definition_record_fields_LdV()))
		;

		this.typesManager.registerRecordReferenceIfAny(new RecordReferenceTypeDefinitionDC(res));

		return res;
	}

	@Override
	public Collection<RecordFieldDC> visitDefinition_record_fields_LdS(Definition_record_fields_LdSContext ctx) {
		if (ctx == null) return null;
		Collection<RecordFieldDC> res = new ArrayList<>();
		ctx.definition_record_field_LdS().forEach(field -> res.add(visitDefinition_record_field_LdS(field)));
		return res;
	}

	@Override
	public Collection<RecordFieldDC> visitDefinition_record_fields_LdV(Definition_record_fields_LdVContext ctx) {
		if (ctx == null) return null;
		Collection<RecordFieldDC> res = new ArrayList<>();
		ctx.definition_record_field_LdV().forEach(field -> res.add(visitDefinition_record_field_LdV(field)));
		return res;
	}

	@Override
	public RecordFieldDC visitDefinition_record_field_LdS(Definition_record_field_LdSContext ctx) {
		if (ctx == null) return null;
		
		RecordFieldDC res = new RecordFieldDC();
		
		TypeDefinitionDC type_name = visitType_suffix(ctx.type_suffix());
		
		String plantName = visitId_without_in(ctx.plant_name);
		if (plantName == null) {
			plantName = this.plantName;
		} else {
			viewsManager.registerClassReferenceRecordField(res);
		}

		return res
			.setName(visitId(ctx.id()))
			.setType(type_name)
			.setPlant(plantName)
			.setLogicRef("LdS") // we cannot refer to LdV from LdS
		;
	}

	@Override
	public RecordFieldDC visitDefinition_record_field_LdV(Definition_record_field_LdVContext ctx) {
		if (ctx == null) return null;

		RecordFieldDC res = new RecordFieldDC();

		Boolean hasLogicRef = visitLogic_ref(ctx.logic_ref());

		String logicRef = "LdV";
		if (hasLogicRef != null && hasLogicRef) {
			logicRef = "LdS";
			viewsManager.registerClassReferenceRecordField(res);
		}
		return res
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
			.setPlant(this.plantName)
			.setLogicRef(logicRef)
		;
	}

	@Override
	public IntegerTypeDefinitionDC visitDefinition_integer_range(Definition_integer_rangeContext ctx) {
//		Uncomment the following code when the integer ranges can be specified by the SRF
//		return new IntegerTypeDefinitionDC()
//			.setLowerBound(ctx.min_value == null ? null : ctx.min_value.getText())
//			.setUpperBound(ctx.max_value == null ? null : ctx.max_value.getText())
//		;
		return new IntegerTypeDefinitionDC()
			.setLowerBound(0)
			.setUpperBound(255)
		;
	}

	@Override
	public EnumTypeDefinitionDC visitDefinition_enum_def(Definition_enum_defContext ctx) {
		EnumTypeDefinitionDC res = new EnumTypeDefinitionDC()
			.setOrdered(visitIs_ordered(ctx.is_ordered()))
			.addLiterals(visitLiterals(ctx.literals()))
		;
		literalIdManager.registerEnum(res, currentEnumIsState);
		return res;
	}

	@Override
	public MacroDefinitionDC visitDefinition_macro_verifica(Definition_macro_verificaContext ctx) {
		return new MacroDefinitionDC()
			.setTypology(JSONKEY_DEF_TYPOLOGY_MACRO_DI_VERIFICA)
			.setName(visitId(ctx.id()))
			.setType(TypesManager.getBooleanInstance())
			.setBody(visitHigh_level_contents(ctx.high_level_contents()))
			.setRawBody(ctx.high_level_contents().getText())
			.discardArguments()
		;
	}

	@Override
	public MacroDefinitionDC visitDefinition_macro_valorizzata(Definition_macro_valorizzataContext ctx) {
		return new MacroDefinitionDC()
			.setTypology(JSONKEY_DEF_TYPOLOGY_MACRO_VALORIZZATA)
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
			.setBody(visitHigh_level_contents(ctx.high_level_contents()))
			.setRawBody(ctx.high_level_contents().getText())
			.discardArguments()
		;
	}
	private void printCtx(ParseTree ctx) {
		for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);
			if ( child instanceof TerminalNode ) {
				TerminalNode tnode = (TerminalNode) child;
				System.err.println(tnode.getSymbol() + " " + tnode.getSymbol().getChannel());
			}
			printCtx(child);
		}
	}
	@Override
	public MacroDefinitionDC visitDefinition_macro_effetto(Definition_macro_effettoContext ctx) {
		return new MacroDefinitionDC()
			.setTypology(JSONKEY_DEF_TYPOLOGY_MACRO_DI_EFFETTO)
			.setName(visitId(ctx.id()))
			.setBody(visitHigh_level_contents(ctx.high_level_contents()))
			.setRawBody(ctx.high_level_contents().getText())
			.discardArguments()
		;
	}
	
	//============================  Common functions =================================================

	@Override
	public String visitId(IdContext ctx) {
		if (ctx == null) return null;
		return ctx.getText();
	}
	
	@Override
	public String visitId_without_in(Id_without_inContext ctx) {
		if (ctx == null) return null;
		return ctx.getText();
	}
	
	@Override
	public TypeDefinitionDC visitType_suffix(Type_suffixContext ctx) {
		if (ctx == null) return null;
		return visitType_name(ctx.type_name());
	}

	@Override
	public TypeDefinitionDC visitType_name(Type_nameContext ctx) {
		if (ctx == null) return null;
		
		if (ctx.BOOLEANO() != null) {
			return TypesManager.getBooleanInstance();
		}
		
		if (ctx.definition_integer_range() != null) {
			return visitDefinition_integer_range(ctx.definition_integer_range());
		}
		
		if (ctx.id_without_in() != null) {
			String typeName = ctx.id_without_in().getText();
			String canonicalName = (this.dictionary != null) ? this.dictionary.lookup(this.plantName, this.logicType, typeName) : typeName; 
			return this.typesManager.getTypeDefinitionStub(canonicalName);
		}
		
		throw new IllegalArgumentException("Unrecognized type name: " + ctx.getText());
	}

	@Override
	public String visitAccessibility(AccessibilityContext ctx) {
		if (ctx == null) return null;

		int tokenIndex = ((TerminalNode)ctx.getChild(0)).getSymbol().getType();
		String res = this.accessibilityDict.get(tokenIndex);
		if (res == null) {
			throw new IllegalArgumentException("No accessibility value found for a token with index " + tokenIndex);
		}

		return res;
	}

	@Override
	public String visitVisibility(VisibilityContext ctx) {
		if (ctx == null) return null;

		int tokenIndex = ((TerminalNode)ctx.getChild(0)).getSymbol().getType();
		String res = this.visibilityDict.get(tokenIndex);
		if (res == null) {
			throw new IllegalArgumentException("No visibility value found for a token with index " + tokenIndex);
		}

		return res;
	}

	@Override
	public Boolean visitPrecedence(PrecedenceContext ctx) {
		return ctx != null;
	}

	@Override
	public Boolean visitRestorable(RestorableContext ctx) {
		return ctx != null;
	}

	@Override
	public Boolean visitRestore_variable(Restore_variableContext ctx) {
		return ctx != null;
	}

	@Override
	public String visitSender(SenderContext ctx) {
		if (ctx == null) return null;

		return visitId(ctx.id());
	}

	@Override
	public String visitReceiver(ReceiverContext ctx) {
		if (ctx == null) return null;

		return visitId(ctx.id());
	}

	@Override
	public Collection<ArgumentDC> visitArguments(ArgumentsContext ctx) {
		if (ctx == null) return null;
		Collection<ArgumentDC> res = new ArrayList<>();
		ctx.argument().forEach(arg -> res.add(visitArgument(arg)));
		return res;
	}

	@Override
	public ArgumentDC visitArgument(ArgumentContext ctx) {
		if (ctx == null) return null;
		return new ArgumentDC()
			.setName(visitId(ctx.id()))
			.setType(visitType_suffix(ctx.type_suffix()))
		;
	}
	
	@Override
	public String visitValore_sicuro(Valore_sicuroContext ctx) {
		if (ctx == null) return null;
		return ctx.safe_value.getText();
	}

	public SafeValue processValore_sicuro(String parsedValue, TypeDefinitionDC type) {
		if ((type instanceof BooleanTypeDefinitionDC) || (type instanceof IntegerTypeDefinitionDC)) {
			return new FinalSafeValue(parsedValue.toLowerCase());
		}

		// otherwise it's an enum literal
		return new EnumLiteralStubSafeValue(parsedValue, (TypeDefinitionStub)type);
	}

	@Override
	public String visitDuration(DurationContext ctx) {
		if (ctx == null) return null;

		return ctx.duration_value() != null ? ctx.duration_value().getText() : (ctx.id() != null ? ctx.id().getText() : null);
	}

	@Override
	public Boolean visitWeight(WeightContext ctx) {
		if (ctx == null) return null;
		if (ctx.getText().equalsIgnoreCase("PESANTE")) return true;
		if (ctx.getText().equalsIgnoreCase("LEGGERO")) return false;
		return null;
	}
	
	@Override
	public Object visitValue(ValueContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visitIs_ordered(Is_orderedContext ctx) {
		return ctx != null;
	}

	@Override
	public Boolean visitLogic_ref(Logic_refContext ctx) {
		if (ctx == null) return null;
		return true;
	}

	@Override
	public List<EnumLiteralDC> visitLiterals(LiteralsContext ctx) {
		if (ctx == null) return null;
		List<EnumLiteralDC> res = new ArrayList<>();
		int localValue = 0;
		for (SRF_definitionsParser.IdContext lit : ctx.id()) {
			String literalValue = visitId(lit);
			EnumLiteralDC literalDC = (currentEnumIsState ? new StateEnumLiteralDC() : new EnumLiteralDC())
					.setLiteralValue(literalValue)
					.setLocalValue(localValue)
			;
			res.add(literalDC);
			localValue++;
		}
		return res;
	}

	@Override
	public String visitHigh_level_contents(High_level_contentsContext ctx) {
		return VisitorUtils.getTextReplacingWSWithASpace(ctx, SRF_definitionsParser.class, false);
	}

	private String determineGeneratedAccessibility(String originalAccessibility) {
		if (originalAccessibility.equals(JSONVALUE_ACCESSIBILITY_PRIVATE)) {
			return JSONVALUE_ACCESSIBILITY_PRIVATE;
		}

		return JSONVALUE_ACCESSIBILITY_PROTECTED;
	}
}
