package eu.fbk.srf2json.dataclasses.fsm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.fbk.srf2json.dataclasses.commons.IObjectWithID;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "toState", "type", "conditions", "effects", "priority" })
public class TransitionDC implements IObjectWithID<TransitionID> {
	private String tag;
	private String toState;
	private String type;
	private String conditions;
	private String effects;
	@JsonIgnore
	private String rawConditions;
	@JsonIgnore
	private String rawEffects;
	@JsonIgnore
	private Integer priority;
	
	public TransitionDC() {
		super();
		
		this.toState = null;
		this.type = null;
		this.conditions = null;
		this.effects = null;
		this.priority = null;

		this.tag = "Transition";
	}
	
	public String getToState() {
		return this.toState;
	}
	
	public TransitionDC setToState(String toState) {
		if (toState != null) this.toState = toState;
		return this;
	}
	
	public TransitionDC setType(String type) {
		if (type != null) this.type = type;
		return this;
	}

	public String getType() {
		return type;
	}

	public TransitionDC setConditions(String conditions) {
		if (conditions != null) this.conditions = conditions;
		return this;
	}
	
	public String getConditions() {
		return this.conditions;
	}
	
	public TransitionDC setEffects(String effects) {
		if (effects != null) this.effects = effects;
		return this;
	}
	
	public String getEffects() {
		return this.effects;
	}

	public TransitionDC setRawConditions(String rawConditions) {
		if (rawConditions != null) this.rawConditions = rawConditions;
		return this;
	}

	public String getRawConditions() {
		return rawConditions;
	}

	public TransitionDC setRawEffects(String rawEffects) {
		if (rawEffects != null) this.rawEffects = rawEffects;
		return this;
	}

	public String getRawEffects() {
		return rawEffects;
	}

	public TransitionDC setPriority(Integer priority) {
		if (priority != null) this.priority = priority;
		return this;
	}

	@Override
	public TransitionID getSubTreeID() {
		return new TransitionID(this.type, this.toState, this.conditions, this.effects);
	}
}
