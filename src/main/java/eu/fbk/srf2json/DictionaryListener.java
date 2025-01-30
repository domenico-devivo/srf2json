package eu.fbk.srf2json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import eu.fbk.srf2json.logic.SRFDictionary;
import eu.fbk.srf2json.parsing.SRF_dictionaryBaseListener;
import eu.fbk.srf2json.parsing.SRF_dictionaryParser.*;

public class DictionaryListener extends SRF_dictionaryBaseListener {

	private enum ExpectedStringSemantics {
		NONE, PLANT_NAME, CLASS_NAME, ALIASES, ATTR_DECLARATION
	}
	
	private final SRFDictionary dictionary;
	private String plantName;
	private String logicType;
	private String className;
	private Collection<String> aliases;
	private Integer classId;
	private String attrName;
	private Map<String, Integer> attrIds;
	
	private ExpectedStringSemantics expectedStringSemantics;
	private boolean isIdentifiersSection;
	
	public DictionaryListener(SRFDictionary dictionary) {
		super();
		this.dictionary = dictionary;
		
		this.plantName = null;
		this.logicType = null;
		this.className = null;
		this.aliases = null;
		this.classId = null;
		this.attrName = null;
		this.attrIds = null;
		
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
		this.isIdentifiersSection = false;
	}
	
	@Override
	public void enterAlias_plant(Alias_plantContext ctx) {
		this.expectedStringSemantics = ExpectedStringSemantics.PLANT_NAME;
	}

	@Override
	public void exitAlias_plant(Alias_plantContext ctx) {
		this.plantName = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterAlias_lds_declarations(Alias_lds_declarationsContext ctx) {
		this.logicType = "LDS";
	}

	@Override
	public void exitAlias_lds_declarations(Alias_lds_declarationsContext ctx) {
		this.logicType = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterAlias_ldv_declarations(Alias_ldv_declarationsContext ctx) {
		this.logicType = "LDV";
	}

	@Override
	public void exitAlias_ldv_declarations(Alias_ldv_declarationsContext ctx) {
		this.logicType = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterAlias_class_declaration(Alias_class_declarationContext ctx) {
		this.expectedStringSemantics = ExpectedStringSemantics.CLASS_NAME;
	}

	@Override
	public void exitAlias_class_declaration(Alias_class_declarationContext ctx) {
		this.className = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterClass_aliases(Class_aliasesContext ctx) {
		this.aliases = new ArrayList<>();
		this.expectedStringSemantics = ExpectedStringSemantics.ALIASES;
	}
	
	@Override
	public void exitClass_aliases(Class_aliasesContext ctx) {
		this.dictionary.addEntry(
			this.plantName,
			this.logicType,
			this.className,
			this.aliases
		);
		this.aliases = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterIds_plant(Ids_plantContext ctx) {
		this.expectedStringSemantics = ExpectedStringSemantics.PLANT_NAME;
		this.isIdentifiersSection = true;
	}

	@Override
	public void exitIds_plant(Ids_plantContext ctx) {
		this.plantName = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
		this.isIdentifiersSection = false;
	}

	@Override
	public void enterIds_lds_declarations(Ids_lds_declarationsContext ctx) {
		this.logicType = "LDS";
	}

	@Override
	public void exitIds_lds_declarations(Ids_lds_declarationsContext ctx) {
		this.logicType = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterIds_ldv_declarations(Ids_ldv_declarationsContext ctx) {
		this.logicType = "LDV";
	}

	@Override
	public void exitIds_ldv_declarations(Ids_ldv_declarationsContext ctx) {
		this.logicType = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterIds_class_declaration(Ids_class_declarationContext ctx) {
		this.attrIds = new HashMap<>();
		this.expectedStringSemantics = ExpectedStringSemantics.CLASS_NAME;
	}

	@Override
	public void exitIds_class_declaration(Ids_class_declarationContext ctx) {
		this.dictionary.addIdInfo(
				this.plantName,
				this.logicType,
				this.className,
				this.classId,
				this.attrIds
		);
		this.className = null;
		this.classId = null;
		this.attrIds = null;
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
	}

	@Override
	public void enterIds_attr_declaration(Ids_attr_declarationContext ctx) {
		this.expectedStringSemantics = ExpectedStringSemantics.ATTR_DECLARATION;
	}

	@Override
	public void exitIds_attr_declaration(Ids_attr_declarationContext ctx) {
		this.expectedStringSemantics = ExpectedStringSemantics.NONE;
		this.attrName = null;
	}

	@Override
	public void exitName_of_plant(Name_of_plantContext ctx) {
		this.processString(ctx.getText());
	}

	@Override
	public void exitName(NameContext ctx) {
		this.processString(ctx.getText());
	}

	@Override
	public void exitName_without_spaces(Name_without_spacesContext ctx) {
		this.processString(ctx.getText());
	}

	@Override
	public void exitId_value(Id_valueContext ctx) {
		this.processInteger(ctx.getText());
	}

	private void processString(String parsedString) {
		switch (this.expectedStringSemantics) {
			case PLANT_NAME: {
				this.plantName = parsedString;
				break;
			}
			case CLASS_NAME: {
				this.className = parsedString;
				break;
			}
			case ALIASES: {
				this.aliases.add(parsedString);
				break;
			}
			case ATTR_DECLARATION: {
				this.attrName = parsedString;
				break;
			}
			case NONE: {
				break;
			}
			default: {
				throw new IllegalStateException("Unrecognized expected string semantics value: " + this.expectedStringSemantics);
			}
		}
	}

	private void processInteger(String parsedIntegerStr) {
		Integer parsedInteger = Integer.parseInt(parsedIntegerStr);

		switch (this.expectedStringSemantics) {
			case CLASS_NAME: {
				this.classId = parsedInteger;
				break;
			}
			case ATTR_DECLARATION: {
				this.attrIds.put(this.attrName, parsedInteger);
				break;
			}
			case NONE: {
				break;
			}
			default: {
				throw new IllegalStateException("Unrecognized expected integer semantics value: " + this.expectedStringSemantics);
			}
		}
	}
}
