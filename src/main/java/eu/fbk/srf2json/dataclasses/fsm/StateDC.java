package eu.fbk.srf2json.dataclasses.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseSensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseSensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "name", "exitTransitions" })
public class StateDC implements INameSortableCaseSensitive {
	private String tag;
	private String name;
	private List<TransitionDC> exitTransitions;
	
	public StateDC() {
		this.name = null;

		this.tag = "State";
		this.exitTransitions = new ArrayList<>();
	}
	
	public StateDC setName(String name) {
		if (name != null) this.name = name;
		return this;
	}
	
	public String getName() {
		return this.name;
	}
	
	public StateDC addTransition(TransitionDC transition) {
		this.exitTransitions.add(transition);
		return this;
	}
	
	public StateDC addTransitions(Collection<TransitionDC> transitions) {
		if (transitions != null) this.exitTransitions.addAll(transitions);
		return this;
	}
	
	public Stream<TransitionDC> getExitTransitionsStream() {
		return this.exitTransitions.stream();
	}

	@Override
	public NameAsIDCaseSensitive getSubTreeID() {
		sortList(this.exitTransitions);
		return new NameAsIDCaseSensitive(this.name);
	}
}
