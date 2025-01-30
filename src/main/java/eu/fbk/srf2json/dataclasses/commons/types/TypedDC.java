package eu.fbk.srf2json.dataclasses.commons.types;

import com.fasterxml.jackson.annotation.JsonGetter;

public abstract class TypedDC {
	protected TypeDefinitionDC type;
	
	public TypedDC() {
		super();
		
		this.type = null;
	}
	
	public TypedDC setType(TypeDefinitionDC type) {
		if (type != null) {
			if (type instanceof TypeDefinitionStub) {
				((TypeDefinitionStub)type).registerOwner(this);
			}
			
			this.type = type;
		}
		
		return this;
	}
	
	public TypeDefinitionDC getType() {
		return this.type;
	}
	
	@JsonGetter("type")
	public String serializeType() {
		return type == null ? null : type.getName();
	}

	@JsonGetter("range")
	public IntegerRangeDC serializeRange() {
		if (type instanceof IntegerTypeDefinitionDC) {
			IntegerRangeDC rangeDC = ((IntegerTypeDefinitionDC)type).getRange();
			if (rangeDC != null && rangeDC.isComplete()) {
				return rangeDC;
			}
		}

		return null;
	}
}
