package eu.fbk.srf2json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.antlr.v4.runtime.Token;

import eu.fbk.srf2json.logic.PrioritiesManager;
import eu.fbk.srf2json.logic.PrioritiesManager.TransitionType;
import eu.fbk.srf2json.parsing.SRFParser.Attuazioni_con_soccorsoContext;
import eu.fbk.srf2json.parsing.SRFParser.Attuazioni_nominaliContext;
import eu.fbk.srf2json.parsing.SRFParser.IdContext;
import eu.fbk.srf2json.parsing.SRFParser.NormalizzazioniContext;
import eu.fbk.srf2json.parsing.SRFParser.PriorityContext;
import eu.fbk.srf2json.parsing.SRFParser.Scheda_statoContext;
import eu.fbk.srf2json.parsing.SRFParser.TransitionContext;
import eu.fbk.srf2json.parsing.SRFParser.Transizioni_inizialiContext;

class TransitionPrioritizerListenerTest {

	PrioritiesManager<TransitionContext> prioritiesManagerMock;
	TransitionPrioritizerListener listener;
	
	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws Exception {
		prioritiesManagerMock = mock(PrioritiesManager.class);
		listener = new TransitionPrioritizerListener(prioritiesManagerMock);		
	}

	@Test
	void testEnterScheda_statoScheda_statoContext() {
		Scheda_statoContext ctxMock = mock(Scheda_statoContext.class);
		listener.enterScheda_stato(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).initializeState();
	}

	@Test
	void testExitScheda_statoScheda_statoContext() {
		String name = "tESt";
		Scheda_statoContext ctxMock = mock(Scheda_statoContext.class);
		IdContext idMock = mock(IdContext.class);
		when(idMock.getText()).thenReturn(name);
		when(ctxMock.id()).thenReturn(idMock);
		listener.exitScheda_stato(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).finalizeState(name);
	}

	@Test
	void testEnterTransizioni_inizialiTransizioni_inizialiContext() {
		Transizioni_inizialiContext ctxMock = mock(Transizioni_inizialiContext.class);
		listener.enterTransizioni_iniziali(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerInitialTransitionsStart();
	}

	@Test
	void testEnterAttuazioni_nominaliAttuazioni_nominaliContext() {
		Attuazioni_nominaliContext ctxMock = mock(Attuazioni_nominaliContext.class);
		listener.enterAttuazioni_nominali(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerStateTransitionsStart(TransitionType.ATTUAZIONE);
	}

	@Test
	void testEnterNormalizzazioniNormalizzazioniContext() {
		NormalizzazioniContext ctxMock = mock(NormalizzazioniContext.class);
		listener.enterNormalizzazioni(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerStateTransitionsStart(TransitionType.NORMALIZZAZIONE);
	}

	@Test
	void testEnterAttuazioni_con_soccorsoAttuazioni_con_soccorsoContext() {
		Attuazioni_con_soccorsoContext ctxMock = mock(Attuazioni_con_soccorsoContext.class);
		listener.enterAttuazioni_con_soccorso(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerStateTransitionsStart(TransitionType.ATTUAZIONE_CON_SOCCORSO);
	}

	@Test
	void testExitTransizioni_inizialiTransizioni_inizialiContext() {
		Transizioni_inizialiContext ctxMock = mock(Transizioni_inizialiContext.class);
		listener.exitTransizioni_iniziali(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerTransitionsEnd();
	}

	@Test
	void testExitAttuazioni_nominaliAttuazioni_nominaliContext() {
		Attuazioni_nominaliContext ctxMock = mock(Attuazioni_nominaliContext.class);
		listener.exitAttuazioni_nominali(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerTransitionsEnd();
	}

	@Test
	void testExitNormalizzazioniNormalizzazioniContext() {
		NormalizzazioniContext ctxMock = mock(NormalizzazioniContext.class);
		listener.exitNormalizzazioni(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerTransitionsEnd();
	}

	@Test
	void testExitAttuazioni_con_soccorsoAttuazioni_con_soccorsoContext() {
		Attuazioni_con_soccorsoContext ctxMock = mock(Attuazioni_con_soccorsoContext.class);
		listener.exitAttuazioni_con_soccorso(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerTransitionsEnd();
	}

	@Test
	void testExitTransitionTransitionContext() {
		TransitionContext ctxMock = mock(TransitionContext.class);
		listener.exitTransition(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerTransition(ctxMock);
	}

	@Test
	void testExitPriorityPriorityContextSuccess() {
		String value = "42";
		PriorityContext ctxMock = mock(PriorityContext.class);	
		Token integerMock = mock(Token.class);
		when(integerMock.getText()).thenReturn(value);
		ctxMock.value = integerMock;
		
		listener.exitPriority(ctxMock);
		
		verify(prioritiesManagerMock, times(1)).registerPriority(42);
	}
	

	@Test
	void testExitPriorityPriorityContextNull() {
		listener.exitPriority(null);
		
		verifyNoInteractions(prioritiesManagerMock);
	}
	

	@Test
	void testExitPriorityPriorityContextException() {
		String value = "42.3";
		PriorityContext ctxMock = mock(PriorityContext.class);	
		Token integerMock = mock(Token.class);
		when(integerMock.getText()).thenReturn(value);
		ctxMock.value = integerMock;
		
		assertThrows(IllegalArgumentException.class, () -> listener.exitPriority(ctxMock));
	}

}
