package eu.fbk.srf2json.dataclasses.commons.types;

import eu.fbk.srf2json.ClassJsonGeneratorVisitor;

public class IntegerTypeDefinitionDC extends TypeDefinitionDC {
	private IntegerRangeDC range;
	
	public IntegerTypeDefinitionDC() {
		super();
		
		this.range = new IntegerRangeDC();
	}
	
	public IntegerTypeDefinitionDC setLowerBound(Integer lowerbound) {
		range.setLowerBound(lowerbound);
		return this;
	}

	public IntegerTypeDefinitionDC setUpperBound(Integer upperbound) {
		range.setUpperBound(upperbound);
		return this;
	}
	
	@Override
	public String getName() {
		return ClassJsonGeneratorVisitor.JSONVALUE_TYPE_NAME_INTEGER;
	}
	
	public IntegerRangeDC getRange() {
		return range;
	}
}
