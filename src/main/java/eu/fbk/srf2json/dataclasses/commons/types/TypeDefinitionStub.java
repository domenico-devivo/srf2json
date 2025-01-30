package eu.fbk.srf2json.dataclasses.commons.types;

import java.util.ArrayList;
import java.util.Collection;

public class TypeDefinitionStub extends TypeDefinitionDC {
	private Collection<TypedDC> owners;
	private NamedTypeDefinitionDC instance;

	private String originalName;
	
	public TypeDefinitionStub(String originalName) {
		super();

		this.originalName = originalName;

		this.owners = new ArrayList<>();
		this.instance = null;
	}
	
	public void registerOwner(TypedDC owner) {
		this.owners.add(owner);
	}
	
	public void registerInstance(NamedTypeDefinitionDC instance) {
		this.instance = instance;
	}
	
	public void substituteWithInstance() {
		if (this.instance == null) {
			throw new IllegalStateException("The instance to subtitute the stubs is null");
		}
		
		this.owners.forEach(owner -> owner.setType(this.instance));
	}
	
	public boolean hasInstance() {
		return this.instance != null;
	}
	
	public NamedTypeDefinitionDC getInstance() {
		return this.instance;
	}

	@Override
	public String getName() {
		return this.originalName;
	}
}
