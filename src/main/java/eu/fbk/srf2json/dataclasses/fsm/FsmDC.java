package eu.fbk.srf2json.dataclasses.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.fbk.srf2json.dataclasses.commons.INameSortableCaseInsensitive;
import eu.fbk.srf2json.dataclasses.commons.types.NameAsIDCaseInsensitive;

@SuppressWarnings("unused")
@JsonPropertyOrder({ "tag", "initialTransitions", "states" })
public class FsmDC implements INameSortableCaseInsensitive {
	private String tag;
	List<TransitionDC> initialTransitions;
	List<StateDC> states;
	
	public FsmDC() {
		this.tag = "State_machine";
		this.initialTransitions = new ArrayList<>();
		this.states = new ArrayList<>();
	}
	
	public FsmDC addTransition(TransitionDC transition) {
		this.initialTransitions.add(transition);
		return this;
	}
	
	public FsmDC addTransitions(Collection<TransitionDC> transitions) {
		if (transitions != null) this.initialTransitions.addAll(transitions);
		return this;
	}
	
	public FsmDC addState(StateDC state) {
		this.states.add(state);
		return this;
	}
	
	public FsmDC addStates(Collection<StateDC> states) {
		if (states != null) this.states.addAll(states);
		return this;
	}
	
	public Stream<TransitionDC> getAllTransitionsStream() {
		Stream<TransitionDC> initialTransitionsStream = this.initialTransitions == null ? null : this.initialTransitions.stream();
		
		if (this.states == null) {
			return initialTransitionsStream;
		}
		
		Stream<TransitionDC> exitTransitionsStream = this.states.stream().flatMap(StateDC::getExitTransitionsStream);
		
		if (initialTransitionsStream == null) {
			return exitTransitionsStream;
		} else {
			return Stream.concat(initialTransitionsStream, exitTransitionsStream);
		}
	}

	@Override
	public NameAsIDCaseInsensitive getSubTreeID() {
		sortList(this.initialTransitions);
		sortList(this.states);
		return null;
	}
}
