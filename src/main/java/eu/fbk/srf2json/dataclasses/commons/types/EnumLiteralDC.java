package eu.fbk.srf2json.dataclasses.commons.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.IGenericIDSortable;
import eu.fbk.srf2json.dataclasses.commons.NamedDC;

@JsonPropertyOrder({ "tag", "globalValue", "localValue", "literalValue" })
public class EnumLiteralDC implements IGenericIDSortable<Integer>, NamedDC {
	private String tag;
	private Integer globalValue;
	private Integer localValue;
	protected String literalValue;
	
	public EnumLiteralDC() {
		super();
		this.tag = new String ("Literal");
		this.globalValue = null;
		this.localValue = null;
		this.literalValue = null;
	}

	public EnumLiteralDC setGlobalValue(Integer globalValue) {
		if (globalValue != null) this.globalValue = globalValue;
		return this;
	}

	public EnumLiteralDC setLocalValue(Integer localValue) {
		if (localValue != null) this.localValue = localValue;
		return this;
	}

	public Integer getLocalValue() {
		return localValue;
	}

	public EnumLiteralDC setLiteralValue(String literalValue) {
		if (literalValue != null) this.literalValue = processName(literalValue);
		return this;
	}
	
	public String getLiteralValue() {
		return this.literalValue;
	}
	
	@Override
	public String toString() {
		//String a = new String(this.literalValue + " (" + this.tag + ", " + (localValue != null ? localValue.toString() : "null") + ", " + (globalValue != null ? globalValue.toString() : "null") + ")");
		return this.literalValue + " (" + this.tag + ", " + (localValue != null ? localValue.toString() : "null") + ", " + (globalValue != null ? globalValue.toString() : "null") + ")";
	}

	@Override
	public Integer getSubTreeID() {
		return localValue;
	}
}
