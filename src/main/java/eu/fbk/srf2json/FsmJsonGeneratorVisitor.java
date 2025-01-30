package eu.fbk.srf2json;

import eu.fbk.srf2json.dataclasses.commons.types.EnumTypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.fsm.*;
import eu.fbk.srf2json.logic.PrioritiesManager;
import eu.fbk.srf2json.logic.PrioritiesManager.TransitionType;
import eu.fbk.srf2json.logic.PrioritiesStore;
import eu.fbk.srf2json.parsing.SRFBaseVisitor;
import eu.fbk.srf2json.parsing.SRFParser;
import eu.fbk.srf2json.parsing.SRFParser.*;

import java.util.*;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class FsmJsonGeneratorVisitor extends SRFBaseVisitor<Object> {
	
	PrioritiesManager<TransitionContext> prioritiesManager;
	EnumTypeDefinitionDC statesEnum;

	public static final String JSONVALUE_TRANS_TYPE_INITIAL = "initial";
	public static final String JSONVALUE_TRANS_TYPE_PERMANENZA = "permanenza";
	public static final String JSONVALUE_TRANS_TYPE_ATTUAZIONE = "attuazione";
	public static final String JSONVALUE_TRANS_TYPE_NORMALIZZAZIONE = "normalizzazione";
	public static final String JSONVALUE_TRANS_TYPE_ATTUAZIONE_CON_SOCCORSO = "attuazione con soccorso";
	public static final String JSONVALUE_STATE_NAME_ASTERISK = "*";
	public static final String JSONVALUE_STATE_NAME_DASH = "-";
	
	final Map<Integer, String> tokenTypeToJsonString;
	
	public FsmJsonGeneratorVisitor(PrioritiesManager<TransitionContext> prioritiesManager, EnumTypeDefinitionDC statesEnum) {
		super();
		this.prioritiesManager = prioritiesManager;
		this.statesEnum = statesEnum;
		
		this.tokenTypeToJsonString = new HashMap<>(5);
		this.tokenTypeToJsonString.put(SRFParser.TRANSIZIONE_INIZIALE_VERSO, 	JSONVALUE_TRANS_TYPE_INITIAL);
		this.tokenTypeToJsonString.put(SRFParser.PERMANENZA_IN, 				JSONVALUE_TRANS_TYPE_PERMANENZA);
		this.tokenTypeToJsonString.put(SRFParser.ATTUAZIONE_VERSO, 				JSONVALUE_TRANS_TYPE_ATTUAZIONE);
		this.tokenTypeToJsonString.put(SRFParser.NORMALIZZAZIONE_VERSO, 		JSONVALUE_TRANS_TYPE_NORMALIZZAZIONE);
		this.tokenTypeToJsonString.put(SRFParser.ATTUAZIONE_CON_SOCCORSO_VERSO, JSONVALUE_TRANS_TYPE_ATTUAZIONE_CON_SOCCORSO);
	}

	public FsmDC visitRoot(SRFParser.RootContext ctx) { return visitScheda_classe(ctx.scheda_classe()); }

//============================  Schede =================================================================

	@Override
	public FsmDC visitScheda_classe(Scheda_classeContext ctx) {
		FsmDC res = new FsmDC()
			.addTransitions(visitScheda_inizializzazione(ctx.scheda_inizializzazione()))
		;
		ctx.scheda_stato().stream()
			.map(this::visitScheda_stato)
			.filter(Objects::nonNull)
			.forEach(res::addState)
		;
		return res;
	}

	@Override
	public Collection<TransitionDC> visitScheda_inizializzazione(Scheda_inizializzazioneContext ctx) {
		return visitTransizioni_iniziali(ctx.transizioni_iniziali());
	}

	@Override
	public StateDC visitScheda_stato(Scheda_statoContext ctx) {
		String stateName = visitId(ctx.id());
		if (stateName.equals(JSONVALUE_STATE_NAME_ASTERISK)) {
			return null;
		}
		stateName = lookupInStatesEnum(stateName);
		
		Collection<TransitionDC> array = new ArrayList<>();
		for (TransitionType transitionType : TransitionType.values()) {
			PrioritiesStore<TransitionContext> combinedPrioritiesStore = new PrioritiesStore<>();
			
			combinedPrioritiesStore.insert(prioritiesManager.getExitTransitionsStore(stateName, transitionType));
			combinedPrioritiesStore.insert(prioritiesManager.getExitTransitionsStore(JSONVALUE_STATE_NAME_ASTERISK, transitionType));

			populateTransitionsArray(array, combinedPrioritiesStore, stateName);
		}
		
		return new StateDC()
			.setName(stateName)
			.addTransitions(array)
		;
	}

	@Override
	public Collection<TransitionDC> visitTransizioni_iniziali(Transizioni_inizialiContext ctx) {
		Collection<TransitionDC> array = new ArrayList<>();
		PrioritiesStore<TransitionContext> prioritiesStore = prioritiesManager.getInitialTransitionsStore();
		populateTransitionsArray(array, prioritiesStore, null);
		return array;
	}
	
	@Override
	public TransitionDC visitTransition(TransitionContext ctx) {
		return new TransitionDC()
			.setToState(lookupInStatesEnum(visitId(ctx.id())))
			.setType(determineTransitionTypeAsJsonString(ctx))
			.setConditions(visitConditions(ctx.conditions()))
			.setEffects(visitEffects(ctx.effects()))
			.setRawConditions(ctx.conditions().block().high_level_contents().getText())
			.setRawEffects(ctx.effects().block().high_level_contents().getText())
		; // The priority gets assigned later on
	}

	@Override
	public String visitConditions(ConditionsContext ctx) {
		if (ctx == null) return null;
		return visitBlock(ctx.block());
	}

	@Override
	public String visitEffects(EffectsContext ctx) {
		if (ctx == null) return null;
		return visitBlock(ctx.block());
	}

	@Override
	public String visitBlock(BlockContext ctx) {
		if (ctx == null) return null;
		return visitHigh_level_contents(ctx.high_level_contents());
	}

	@Override
	public String visitHigh_level_contents(High_level_contentsContext ctx) {
		if (ctx == null) return null;
		return VisitorUtils.getTextReplacingWSWithASpace(ctx, SRFParser.class, false);
	}

	@Override
	public String visitId(IdContext ctx) {
		if (ctx == null) return null;
		return ctx.getText();
	}

//============================  Non-producing nodes =================================================

	@Override
	public Object visitAttuazioni_nominali(Attuazioni_nominaliContext ctx) {
		return null;
	}

	@Override
	public Object visitNormalizzazioni(NormalizzazioniContext ctx) {
		return null;
	}

	@Override
	public Object visitAttuazioni_con_soccorso(Attuazioni_con_soccorsoContext ctx) {
		return null;
	}
	
	@Override
	public Object visitPriority(PriorityContext ctx) {
		return null;
	}

//============================  Helper functions =================================================

	private String determineTransitionTypeAsJsonString(TransitionContext ctx) throws IllegalStateException {
		// Adapted from org.antlr.v4.runtime.ParserRuleContext.getToken
		for (ParseTree o : ctx.children) {
			if ( o instanceof TerminalNode ) {
				TerminalNode tnode = (TerminalNode)o;
				Token symbol = tnode.getSymbol();
				if ( tokenTypeToJsonString.containsKey(symbol.getType()) ) {
					return tokenTypeToJsonString.get(symbol.getType());
				}
			}
		}
		
		throw new IllegalStateException("Failed to determine transition type of a context: " + ctx);
	}
	
	private void populateTransitionsArray(Collection<TransitionDC> array, PrioritiesStore<TransitionContext> prioritiesStore, String currentStateName) {
		int priority = 1;
		for (TransitionContext transition_ctx : prioritiesStore) {
			TransitionDC visited = visitTransition(transition_ctx);
			if (visited != null) {
				if (visited.getToState().equals(JSONVALUE_STATE_NAME_DASH)) {
					visited.setToState(currentStateName);
				}
				visited.setPriority(priority);
				priority++;
				array.add(visited);
			}
		}
	}
	
	private String lookupInStatesEnum(String originalStateName) {
		if (isSpecialStateName(originalStateName)) {
			return originalStateName;
		}
		
		return statesEnum.findLiteralValue(originalStateName);
	}
	
	private boolean isSpecialStateName(String stateName) {
		return stateName.equals(JSONVALUE_STATE_NAME_ASTERISK) || stateName.equals(JSONVALUE_STATE_NAME_DASH);
	}
}
