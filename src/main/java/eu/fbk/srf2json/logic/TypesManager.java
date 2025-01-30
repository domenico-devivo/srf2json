package eu.fbk.srf2json.logic;

import java.util.HashMap;
import java.util.Map;

import eu.fbk.srf2json.dataclasses.commons.types.*;

public class TypesManager {
	private static BooleanTypeDefinitionDC booleanInstance = new BooleanTypeDefinitionDC();
	private static CommandResponseTypeDefinitionDC commandResponseInstance = new CommandResponseTypeDefinitionDC();
	private static TimerStateTypeDefinitionDC timerStateInstance = new TimerStateTypeDefinitionDC();
	private Map<String, TypeDefinitionStub> typeDefinitionStubs;
	
	public TypesManager() {
		super();
		
		this.typeDefinitionStubs = new HashMap<>();
	}

	public static BooleanTypeDefinitionDC getBooleanInstance() {
		return booleanInstance;
	}
	public static CommandResponseTypeDefinitionDC getCommandResponseInstance() {
		return commandResponseInstance;
	}
	public static TimerStateTypeDefinitionDC getTimerStateInstance() {
		return timerStateInstance;
	}
	
	public TypeDefinitionStub getTypeDefinitionStub(String typeName) {
		return this.typeDefinitionStubs.computeIfAbsent(
			typeName.toUpperCase(),
			dummy -> new TypeDefinitionStub(typeName)
		);
	}
	
	public void registerEnumDefinition(EnumTypeDefinitionDC enumDefinition) {
		TypeDefinitionStub stub = this.getTypeDefinitionStub(enumDefinition.getName());
		stub.registerInstance(enumDefinition);
	}
	
	public void registerClassReferenceIfAny(ClassReferenceTypeDefinitionDC classReference) {
		TypeDefinitionStub stub = this.typeDefinitionStubs.get(classReference.getName().toUpperCase());
		if (stub != null) {
			stub.registerInstance(classReference);
		}
	}

	public void registerRecordReferenceIfAny(RecordReferenceTypeDefinitionDC recordReference) {
		TypeDefinitionStub stub = this.typeDefinitionStubs.get(recordReference.getName().toUpperCase());
		if (stub != null) {
			stub.registerInstance(recordReference);
		}
	}

	public void instantiateTypes() {
		this.typeDefinitionStubs.forEach((typeName, stub) -> {
			if (!stub.hasInstance()) {
				stub.registerInstance(new ReferenceTypeDefinitionDC().setName(stub.getName()));
			}
			
			stub.substituteWithInstance();
		});
	}
}
