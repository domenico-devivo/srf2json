package eu.fbk.srf2json.dataclasses;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;
import eu.fbk.srf2json.dataclasses.declarations.DeclarationsDC;
import eu.fbk.srf2json.dataclasses.definitions.DefinitionsDC;
import eu.fbk.srf2json.dataclasses.fsm.FsmDC;

/**
 * A dataclass that represents an SRF class. Can be found either in the logic
 * section or in the views one. Stores a reference to its parent (a corresponding
 * logic or views object).
 */
@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "id", "declarations", "definitions", "fsm" })
public class ClassDC implements INameSortableCaseInsensitive {
	private String tag;
	private String name;
	private Integer id;
	private DeclarationsDC declarations;
	private DefinitionsDC definitions;
	private FsmDC fsm;

	@JsonIgnore
	private ClassContainerDC parent;
	@JsonIgnore
	private EnumTypeDefinitionDC statesEnum;

	public ClassDC(String name, DeclarationsDC declarations, DefinitionsDC definitions) {
		super();
		this.name = name;
		this.declarations = declarations;
		this.definitions = definitions;

		this.id = null;
		this.fsm = null;

		this.parent = null;
		this.statesEnum = null;
	}

	public DeclarationsDC getDeclarations() {
		return this.declarations;
	}

	public DefinitionsDC getDefinitions() {
		return this.definitions;
	}

	public ClassDC setFsm(FsmDC fsm) {
		if (fsm != null) this.fsm = fsm;
		return this;
	}

	public FsmDC getFsm() {
		return this.fsm;
	}

	public ClassDC setId(Integer id) {
		if (id != null) this.id = id;
		return this;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ClassContainerDC getParent() {
		return parent;
	}

	public void setParent(ClassContainerDC parent) {
		this.parent = parent;
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		declarations.getSubTreeID();
		definitions.getSubTreeID();
		if (fsm != null) fsm.getSubTreeID();
		return new NameAsIDCaseInsensitive(name);
	}

	/**
	 * Set the enum type that contains labels for the states of this
	 * class' Finite State Machine
	 * @param statesEnum The enum type describing the states.
	 * @return Reference to this object (`this`) for chaining.
	 */
	public ClassDC setStatesEnum(EnumTypeDefinitionDC statesEnum) {
		if (statesEnum != null) {
			this.statesEnum = statesEnum;
		}
		return this;
	}

	/**
	 * Get the enum type that contains labels for the states of this
	 * class' Finite State Machine
	 * @return The enum type describing the states.
	 */
	public EnumTypeDefinitionDC getStatesEnum() {
		return this.statesEnum;
	}

	@JsonGetter("tag")
	public String getTag() {
		return parent.isLogic() ? "LogicClass" : "ViewClass";
	}

	@Override
	public String toString() {
		LogicTypeDC logicTypeDC = getParent().getLogicType();
		return name + " in " + logicTypeDC.getPlantType().getName() + " (" + logicTypeDC.getName() + ")";
	}
}
