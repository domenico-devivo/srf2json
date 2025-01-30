package eu.fbk.srf2json;

import eu.fbk.srf2json.logic.PrioritiesManager;
import eu.fbk.srf2json.logic.PrioritiesManager.TransitionType;
import eu.fbk.srf2json.parsing.SRFBaseListener;
import eu.fbk.srf2json.parsing.SRFParser.Attuazioni_con_soccorsoContext;
import eu.fbk.srf2json.parsing.SRFParser.Attuazioni_nominaliContext;
import eu.fbk.srf2json.parsing.SRFParser.NormalizzazioniContext;
import eu.fbk.srf2json.parsing.SRFParser.PriorityContext;
import eu.fbk.srf2json.parsing.SRFParser.Scheda_statoContext;
import eu.fbk.srf2json.parsing.SRFParser.TransitionContext;
import eu.fbk.srf2json.parsing.SRFParser.Transizioni_inizialiContext;

public class TransitionPrioritizerListener extends SRFBaseListener {
	
	PrioritiesManager<TransitionContext> prioritiesManager;
	
	public TransitionPrioritizerListener(PrioritiesManager<TransitionContext> prioritiesManager) {
		super();
		this.prioritiesManager = prioritiesManager;
	}

//-------------------------------------- State ------------------------------------
	
	@Override
	public void enterScheda_stato(Scheda_statoContext ctx) {
		prioritiesManager.initializeState();
	}

	@Override
	public void exitScheda_stato(Scheda_statoContext ctx) throws IllegalArgumentException {
		String name = ctx.id().getText();
		prioritiesManager.finalizeState(name);
	}

//-------------------------------------- TransitionsStarts ------------------------------------
	
	
	@Override
	public void enterTransizioni_iniziali(Transizioni_inizialiContext ctx) {
		prioritiesManager.registerInitialTransitionsStart();
	}

	@Override
	public void enterAttuazioni_nominali(Attuazioni_nominaliContext ctx) {
		prioritiesManager.registerStateTransitionsStart(TransitionType.ATTUAZIONE);
	}

	@Override
	public void enterNormalizzazioni(NormalizzazioniContext ctx) {
		prioritiesManager.registerStateTransitionsStart(TransitionType.NORMALIZZAZIONE);
	}
	
	@Override
	public void enterAttuazioni_con_soccorso(Attuazioni_con_soccorsoContext ctx) {
		prioritiesManager.registerStateTransitionsStart(TransitionType.ATTUAZIONE_CON_SOCCORSO);
	}

//-------------------------------------- TransitionsEnds ------------------------------------

	@Override
	public void exitTransizioni_iniziali(Transizioni_inizialiContext ctx) {
		prioritiesManager.registerTransitionsEnd();
	}
	
	@Override
	public void exitAttuazioni_nominali(Attuazioni_nominaliContext ctx) {
		prioritiesManager.registerTransitionsEnd();
	}

	@Override
	public void exitNormalizzazioni(NormalizzazioniContext ctx) {
		prioritiesManager.registerTransitionsEnd();
	}

	@Override
	public void exitAttuazioni_con_soccorso(Attuazioni_con_soccorsoContext ctx) {
		prioritiesManager.registerTransitionsEnd();
	}

//-------------------------------------- Transition ------------------------------------

	@Override
	public void exitTransition(TransitionContext ctx) {
		prioritiesManager.registerTransition(ctx);
	}

//-------------------------------------- Priority ------------------------------------
	
	@Override
	public void exitPriority(PriorityContext ctx) throws IllegalArgumentException {
		if (ctx != null) {
			Integer value = null;
			try {
				value = Integer.parseInt(ctx.value.getText());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid priority value: " + ctx.value.getText());
			}
			prioritiesManager.registerPriority(value);
		}
	}
	
	
}
