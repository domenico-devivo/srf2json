package eu.fbk.srf2json.dataclasses.commons.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.VisitorUtils;
import eu.fbk.srf2json.dataclasses.ClassDC;
import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "literals", "isOrdered" })
public class EnumTypeDefinitionDC extends NamedTypeDefinitionDC implements INameSortableCaseInsensitive {
	private String tag;
	private Boolean isOrdered;
	private List<EnumLiteralDC> literals;

	@JsonIgnore
	private ClassDC parentClass;
	
	public EnumTypeDefinitionDC() {
		super();
		
		this.isOrdered = null;
		
		this.tag = "Enumeration";
		this.literals = new ArrayList<>();
		this.parentClass = null;
	}
	
	public EnumTypeDefinitionDC setOrdered(Boolean isOrdered) {
		if (isOrdered != null) this.isOrdered = isOrdered;
		return this;
	}

	public Boolean getOrdered() {
		return isOrdered;
	}

	public EnumTypeDefinitionDC addLiteral(EnumLiteralDC literal) {
		this.literals.add(literal);
		return this;
	}
	
	public EnumTypeDefinitionDC addLiterals(List<EnumLiteralDC> literals) {
		if (literals != null) literals.forEach(this::addLiteral);
		return this;
	}

	public Stream<EnumLiteralDC> getLiteralsStream() {
		return this.literals.stream();
	}

	public EnumTypeDefinitionDC setParentClass(ClassDC parentClass) {
		if (parentClass != null) this.parentClass = parentClass;
		return this;
	}

	public ClassDC getParentClass() {
		return parentClass;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.isOrdered != null ? (this.isOrdered ? "ordered" : "unordered") : "unknown");
		sb.append(" enum: [");
		this.literals.stream().map(EnumLiteralDC::toString).forEach(s -> {sb.append(s); sb.append(", ");});
		sb.append("] (");
		sb.append(this.literals.size());
		sb.append(" literals)");
		return sb.toString();
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(this.literals);
		return new NameAsIDCaseInsensitive(this.name);
	}

	@Override
	public EnumTypeDefinitionDC setName(String name) {
		if (name != null) this.name = VisitorUtils.capitalize(name);
		return this;
	}

	public String findLiteralValue(String originalName) {
		StringBuilder sb = new StringBuilder();
		for (String word : originalName.split("\\s+")) {
			sb.append(word);
		}
		String nameWithoutSpaces = sb.toString();

		return this.getLiteralsStream()
				.map(EnumLiteralDC::getLiteralValue)
				.filter(literalValue -> literalValue.equalsIgnoreCase(nameWithoutSpaces))
				.findAny()
				.orElseThrow(() -> new IllegalStateException("No enum literal found for the string value " + originalName))
				;
	}
}
