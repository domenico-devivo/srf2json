package eu.fbk.srf2json.dataclasses;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "author", "project" })
public class LogicProjectDC implements INameSortableCaseInsensitive {
	private String tag;
	private String author;
	private ProjectDC project;
	
	public LogicProjectDC() {
		super();
		
		this.tag = "LogicProject";
		this.author = null;
		this.project = new ProjectDC();
	}
	
	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		return this.project.getSubTreeID();
	}
	
	public LogicProjectDC setAuthor(String author) {
		if (author != null) this.author = author;
		return this;
	}
	
	public ProjectDC getProject() {
		return this.project;
	}
}
