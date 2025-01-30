package eu.fbk.srf2json.views;

import java.util.*;
import java.util.stream.Stream;

import eu.fbk.srf2json.ClassJsonGeneratorVisitor;
import eu.fbk.srf2json.TreeUtils;
import eu.fbk.srf2json.parsing.SRF_blocksLexer;
import eu.fbk.srf2json.parsing.SRF_blocksParser;
import eu.fbk.srf2json.views.stringblocktypes.StringBlockType;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

import eu.fbk.srf2json.parsing.SRF_blocksBaseListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public class ViewsBlockListener extends SRF_blocksBaseListener {
	private enum MODE {NONE, NOME_LISTA, NOME_ATTR, NOME_CAMPO, NOME_STATO}

	private static class ParsedPair extends ImmutableTriple<String, String, Boolean> {
		public ParsedPair(String parsedAttr, String parsedFieldName, Boolean hybridMode) {
			super(parsedAttr, parsedFieldName, hybridMode);
		}

		public String getAttrName() {
			return left;
		}

		public String getFieldName() {
			return middle;
		}

		public Boolean getHybridMode() {return right;}
	}

	private static class StackManager {
		private static class ContextStackElement {
			private String parsedAttrName;
			private String parsedFieldName;
			// if the list has been encountered inside the pair, it applies; otherwise, it's null
			private boolean hybridMode;
			private String parsedListName;
			private boolean encounteredStato;
			private final Collection<ParsedPair> pairs;

			public ContextStackElement() {
				this.parsedAttrName = null;
				this.parsedFieldName = null;
				this.hybridMode = false;
				this.parsedListName = null;
				this.encounteredStato = false;
				this.pairs = new ArrayList<>();
			}

			public void resetPair() {
				parsedAttrName = null;
				parsedFieldName = null;
				encounteredStato = false;
			}

			public void flushPair() {
				if (encounteredStato) {
					pairs.add(new ParsedPair(ClassJsonGeneratorVisitor.JSONVALUE_VARIABLE_NAME_STATE, parsedFieldName, hybridMode));
				}
				if (parsedAttrName != null) {
					pairs.add(new ParsedPair(parsedAttrName, parsedFieldName, hybridMode));
				}
				resetPair();
			}

			public Stream<ViewContext> getResultStream() {
				flushPair();
				Stream<ViewContext> res = null;
				if (!pairs.isEmpty()) {
					res = pairs.stream()
							.map(pair -> {
								String attrName = pair.getAttrName();
								String fieldName = pair.getFieldName();
								return new ViewContext(
										attrName.toLowerCase(),
										fieldName == null ? null : fieldName.toLowerCase(),
										(parsedListName == null) || (pair.getHybridMode() && fieldName == null) ? null : parsedListName.toLowerCase()
								);
							});
				}
				if (res == null) {
					res = Stream.empty();
				}
				if (parsedListName != null) {
					res = Stream.concat(res, Stream.of(new ViewContext(parsedListName, null, null)));
				}
				return res;
			}
		}

		private final Stack<ContextStackElement> contextStack;

		public StackManager() {
			this.contextStack = new Stack<>();
		}

		public void startPair() {
			endPair();
			enterHybridMode();
		}

		public void endPair() {
			if (!contextStack.isEmpty()) {
				contextStack.peek().flushPair();
				contextStack.peek().hybridMode = false;
			}
		}

		public void addElement() {
			contextStack.add(new ContextStackElement());
		}

		public Stream<ViewContext> popAndGetResultStream() {
			return contextStack.pop().getResultStream();
		}

		public void setParsedAttrName(String parsedAttrName) {
			contextStack.peek().parsedAttrName = parsedAttrName;
		}

		public void setParsedFieldName(String parsedFieldName) {
			contextStack.peek().parsedFieldName = parsedFieldName;
		}

		public void setParsedListName(String parsedListName) {
			contextStack.peek().parsedListName = parsedListName;
		}

		public void markStateEncountered() {
			contextStack.peek().encounteredStato = true;
		}

		public void enterHybridMode() {
			contextStack.peek().hybridMode = true;
		}

		public void exitHybridMode() {
			contextStack.peek().hybridMode = false;
		}
	}


	private final String stringBlock;
	private final StringBlockType sbt;


	private String previous;
	private final ArrayList<ParserRuleContext> debugStack;

	private final Collection<ViewContext> results;

	private final StackManager stackManager;
	private MODE currentMode;

	private boolean enteredCondizionePermanenza;
	private Set<String> condizionePermanenzaStates;
	private boolean foundSomeState;
	private boolean encounteredGeneralCondizionePermanenza;

	public ViewsBlockListener(String stringBlock, StringBlockType sbt, Collection<ViewContext> results, Set<String> condizionePermanenzaStates) {
		super();
		this.stringBlock = stringBlock;
		this.sbt = sbt;

		this.currentMode = MODE.NONE;

		this.previous = null;
		this.debugStack = new ArrayList<>();

		this.results = results;

		this.stackManager = new StackManager();

		this.enteredCondizionePermanenza = false;
		this.condizionePermanenzaStates = condizionePermanenzaStates;
		this.foundSomeState = false;
		this.encounteredGeneralCondizionePermanenza = false;
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		StringBuilder hierarchyBuilder = new StringBuilder();

		for (ParserRuleContext ctx : debugStack) {
			// we truncate the class name in order to get only the context name (the part after the dollar sign)
			String contextClassName = ctx.getClass().toString();
			int dollarIndex = contextClassName.indexOf('$');
			if (dollarIndex >= 0) {
				contextClassName = contextClassName.substring(dollarIndex + 1);
			}

			hierarchyBuilder.append(contextClassName);
			hierarchyBuilder.append(" -> ");
		}

		hierarchyBuilder.append(node.getSymbol().toString());
		String hierarchy = hierarchyBuilder.toString();

		String contextTree = debugStack.size() > 0
			? TreeUtils.toPrettyTree(debugStack.get(debugStack.size() - 1), Arrays.asList(SRF_blocksParser.ruleNames))
			: "<empty>"
		;

		// we navigate all the hierarchy up until we reach the parse tree root
		ParseTree tmp = node.getParent();
		while (tmp.getParent() != null) {
			tmp = tmp.getParent();
		}

		String parsedSoFar = tmp.getText();

		if (previous == null || !parsedSoFar.startsWith(previous)) {
			throw new BlocksParsingException(sbt.label(), stringBlock, hierarchy, contextTree, parsedSoFar);
		}

		previous = parsedSoFar;
	}

	private void startCheck() {
		stackManager.addElement();
	}

	private void endCheck() {
		stackManager.popAndGetResultStream().forEach(results::add);
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		debugStack.add(ctx);
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		debugStack.remove(debugStack.size() - 1);
	}

	@Override
	public void exitSingle_word_raw_id(SRF_blocksParser.Single_word_raw_idContext ctx) {
		parseNonListName(ctx.getText());
	}

	@Override
	public void exitRaw_id(SRF_blocksParser.Raw_idContext ctx) {
		parseNonListName(ctx.getText());
	}

	private void parseNonListName(String ctxText) {
		switch (currentMode) {
			case NOME_ATTR:
				stackManager.setParsedAttrName(ctxText);
				break;
			case NOME_CAMPO:
				stackManager.setParsedFieldName(ctxText);
				break;
			case NOME_STATO:
				if (enteredCondizionePermanenza && !encounteredGeneralCondizionePermanenza) {
					foundSomeState = true;
					condizionePermanenzaStates.add(ctxText.toLowerCase());
				}
				break;
		}
	}

	@Override
	public void exitRaw_id_with_di(SRF_blocksParser.Raw_id_with_diContext ctx) {
		switch (currentMode) {
			case NOME_LISTA:
				stackManager.setParsedListName(ctx.getText());
				stackManager.exitHybridMode();
				break;
		}
	}

	@Override
	public void enterNome_campo(SRF_blocksParser.Nome_campoContext ctx) {
		currentMode = MODE.NOME_CAMPO;
	}

	@Override
	public void exitNome_campo(SRF_blocksParser.Nome_campoContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterValore_da_lista(SRF_blocksParser.Valore_da_listaContext ctx) {
		startCheck();
	}

	@Override
	public void exitValore_da_lista(SRF_blocksParser.Valore_da_listaContext ctx) {
		endCheck();
	}

	@Override
	public void enterValore_da_lista_con_alternativa(SRF_blocksParser.Valore_da_lista_con_alternativaContext ctx) {
		startCheck();
	}

	@Override
	public void exitValore_da_lista_con_alternativa(SRF_blocksParser.Valore_da_lista_con_alternativaContext ctx) {
		endCheck();
	}

	@Override
	public void enterA_valore_da_lista_con_alternativa(SRF_blocksParser.A_valore_da_lista_con_alternativaContext ctx) {
		startCheck();
	}

	@Override
	public void exitA_valore_da_lista_con_alternativa(SRF_blocksParser.A_valore_da_lista_con_alternativaContext ctx) {
		endCheck();
	}

	@Override
	public void enterDi_valore_da_lista_con_alternativa(SRF_blocksParser.Di_valore_da_lista_con_alternativaContext ctx) {
		startCheck();
	}

	@Override
	public void exitDi_valore_da_lista_con_alternativa(SRF_blocksParser.Di_valore_da_lista_con_alternativaContext ctx) {
		endCheck();
	}

	@Override
	public void enterDa_valore_da_lista_con_alternativa(SRF_blocksParser.Da_valore_da_lista_con_alternativaContext ctx) {
		startCheck();
	}

	@Override
	public void exitDa_valore_da_lista_con_alternativa(SRF_blocksParser.Da_valore_da_lista_con_alternativaContext ctx) {
		endCheck();
	}

	@Override
	public void enterA_valore_da_lista(SRF_blocksParser.A_valore_da_listaContext ctx) {
		startCheck();
	}

	@Override
	public void exitA_valore_da_lista(SRF_blocksParser.A_valore_da_listaContext ctx) {
		endCheck();
	}

	@Override
	public void enterDa_valore_da_lista(SRF_blocksParser.Da_valore_da_listaContext ctx) {
		startCheck();
	}

	@Override
	public void exitDa_valore_da_lista(SRF_blocksParser.Da_valore_da_listaContext ctx) {
		endCheck();
	}

	@Override
	public void enterDi_valore_da_lista(SRF_blocksParser.Di_valore_da_listaContext ctx) {
		startCheck();
	}

	@Override
	public void exitDi_valore_da_lista(SRF_blocksParser.Di_valore_da_listaContext ctx) {
		endCheck();
	}

	@Override
	public void enterValore(SRF_blocksParser.ValoreContext ctx) {
		startCheck();
	}

	@Override
	public void exitValore(SRF_blocksParser.ValoreContext ctx) {
		endCheck();
	}

	@Override
	public void enterA_valore(SRF_blocksParser.A_valoreContext ctx) {
		startCheck();
	}

	@Override
	public void exitA_valore(SRF_blocksParser.A_valoreContext ctx) {
		endCheck();
	}

	@Override
	public void enterDi_valore(SRF_blocksParser.Di_valoreContext ctx) {
		startCheck();
	}

	@Override
	public void exitDi_valore(SRF_blocksParser.Di_valoreContext ctx) {
		endCheck();
	}

	@Override
	public void enterDa_valore(SRF_blocksParser.Da_valoreContext ctx) {
		startCheck();
	}

	@Override
	public void exitDa_valore(SRF_blocksParser.Da_valoreContext ctx) {
		endCheck();
	}

	@Override
	public void enterNome_lista(SRF_blocksParser.Nome_listaContext ctx) {
		currentMode = MODE.NOME_LISTA;
	}

	@Override
	public void exitNome_lista(SRF_blocksParser.Nome_listaContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterA_nome_lista(SRF_blocksParser.A_nome_listaContext ctx) {
		currentMode = MODE.NOME_LISTA;
	}

	@Override
	public void exitA_nome_lista(SRF_blocksParser.A_nome_listaContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDa_nome_lista(SRF_blocksParser.Da_nome_listaContext ctx) {
		currentMode = MODE.NOME_LISTA;
	}

	@Override
	public void exitDa_nome_lista(SRF_blocksParser.Da_nome_listaContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDi_nome_lista(SRF_blocksParser.Di_nome_listaContext ctx) {
		currentMode = MODE.NOME_LISTA;
	}

	@Override
	public void exitDi_nome_lista(SRF_blocksParser.Di_nome_listaContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_parametro(SRF_blocksParser.Nome_parametroContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_parametro(SRF_blocksParser.Nome_parametroContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_controllo(SRF_blocksParser.Nome_controlloContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_controllo(SRF_blocksParser.Nome_controlloContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_variabile(SRF_blocksParser.Nome_variabileContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_variabile(SRF_blocksParser.Nome_variabileContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_contatore(SRF_blocksParser.Nome_contatoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_contatore(SRF_blocksParser.Nome_contatoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_macro_valore(SRF_blocksParser.Nome_macro_valoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_macro_valore(SRF_blocksParser.Nome_macro_valoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterA_nome_parametro(SRF_blocksParser.A_nome_parametroContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitA_nome_parametro(SRF_blocksParser.A_nome_parametroContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterA_nome_controllo(SRF_blocksParser.A_nome_controlloContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitA_nome_controllo(SRF_blocksParser.A_nome_controlloContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterA_nome_variabile(SRF_blocksParser.A_nome_variabileContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitA_nome_variabile(SRF_blocksParser.A_nome_variabileContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterA_nome_contatore(SRF_blocksParser.A_nome_contatoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitA_nome_contatore(SRF_blocksParser.A_nome_contatoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterA_nome_macro_valore(SRF_blocksParser.A_nome_macro_valoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitA_nome_macro_valore(SRF_blocksParser.A_nome_macro_valoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDa_nome_parametro(SRF_blocksParser.Da_nome_parametroContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDa_nome_parametro(SRF_blocksParser.Da_nome_parametroContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDa_nome_controllo(SRF_blocksParser.Da_nome_controlloContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDa_nome_controllo(SRF_blocksParser.Da_nome_controlloContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDa_nome_variabile(SRF_blocksParser.Da_nome_variabileContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDa_nome_variabile(SRF_blocksParser.Da_nome_variabileContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDa_nome_contatore(SRF_blocksParser.Da_nome_contatoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDa_nome_contatore(SRF_blocksParser.Da_nome_contatoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDa_nome_macro_valore(SRF_blocksParser.Da_nome_macro_valoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDa_nome_macro_valore(SRF_blocksParser.Da_nome_macro_valoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDi_nome_parametro(SRF_blocksParser.Di_nome_parametroContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDi_nome_parametro(SRF_blocksParser.Di_nome_parametroContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDi_nome_controllo(SRF_blocksParser.Di_nome_controlloContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDi_nome_controllo(SRF_blocksParser.Di_nome_controlloContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDi_nome_variabile(SRF_blocksParser.Di_nome_variabileContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDi_nome_variabile(SRF_blocksParser.Di_nome_variabileContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDi_nome_contatore(SRF_blocksParser.Di_nome_contatoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDi_nome_contatore(SRF_blocksParser.Di_nome_contatoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterDi_nome_macro_valore(SRF_blocksParser.Di_nome_macro_valoreContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitDi_nome_macro_valore(SRF_blocksParser.Di_nome_macro_valoreContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterCondizione_filtro_iterazione(SRF_blocksParser.Condizione_filtro_iterazioneContext ctx) {
		stackManager.startPair();
	}

	@Override
	public void exitCondizione_filtro_iterazione(SRF_blocksParser.Condizione_filtro_iterazioneContext ctx) {
		stackManager.endPair();
	}

	@Override
	public void enterValore_in_iterazione(SRF_blocksParser.Valore_in_iterazioneContext ctx) {
		stackManager.startPair();
	}

	@Override
	public void enterA_valore_in_iterazione(SRF_blocksParser.A_valore_in_iterazioneContext ctx) {
		stackManager.startPair();
	}

	@Override
	public void enterDa_valore_in_iterazione(SRF_blocksParser.Da_valore_in_iterazioneContext ctx) {
		stackManager.startPair();
	}

	@Override
	public void enterDi_valore_in_iterazione(SRF_blocksParser.Di_valore_in_iterazioneContext ctx) {
		stackManager.startPair();
	}

	@Override
	public void exitValore_in_iterazione(SRF_blocksParser.Valore_in_iterazioneContext ctx) {
		stackManager.endPair();
	}

	@Override
	public void exitA_valore_in_iterazione(SRF_blocksParser.A_valore_in_iterazioneContext ctx) {
		stackManager.endPair();
	}

	@Override
	public void exitDa_valore_in_iterazione(SRF_blocksParser.Da_valore_in_iterazioneContext ctx) {
		stackManager.endPair();
	}

	@Override
	public void exitDi_valore_in_iterazione(SRF_blocksParser.Di_valore_in_iterazioneContext ctx) {
		stackManager.endPair();
	}

	@Override
	public void enterAssegna(SRF_blocksParser.AssegnaContext ctx) {
		startCheck();
	}

	@Override
	public void exitAssegna(SRF_blocksParser.AssegnaContext ctx) {
		endCheck();
	}

	@Override
	public void enterComanda_istanza(SRF_blocksParser.Comanda_istanzaContext ctx) {
		startCheck();
	}

	@Override
	public void exitComanda_istanza(SRF_blocksParser.Comanda_istanzaContext ctx) {
		endCheck();
	}

	@Override
	public void enterCondizione_se(SRF_blocksParser.Condizione_seContext ctx) {
		startCheck();
	}

	@Override
	public void exitCondizione_se(SRF_blocksParser.Condizione_seContext ctx) {
		endCheck();
	}

	@Override
	public void enterCondizione_verifica(SRF_blocksParser.Condizione_verificaContext ctx) {
		startCheck();
	}

	@Override
	public void exitCondizione_verifica(SRF_blocksParser.Condizione_verificaContext ctx) {
		endCheck();
	}

	@Override
	public void enterCondizione_se_macro(SRF_blocksParser.Condizione_se_macroContext ctx) {
		startCheck();
	}

	@Override
	public void exitCondizione_se_macro(SRF_blocksParser.Condizione_se_macroContext ctx) {
		endCheck();
	}

	@Override
	public void enterNome_timer(SRF_blocksParser.Nome_timerContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_timer(SRF_blocksParser.Nome_timerContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_stato(SRF_blocksParser.Nome_statoContext ctx) {
		currentMode = MODE.NOME_STATO;
	}

	@Override
	public void exitNome_stato(SRF_blocksParser.Nome_statoContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_comando_piazzale(SRF_blocksParser.Nome_comando_piazzaleContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_comando_piazzale(SRF_blocksParser.Nome_comando_piazzaleContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void enterNome_attributo_booleano(SRF_blocksParser.Nome_attributo_booleanoContext ctx) {
		currentMode = MODE.NOME_ATTR;
	}

	@Override
	public void exitNome_attributo_booleano(SRF_blocksParser.Nome_attributo_booleanoContext ctx) {
		currentMode = MODE.NONE;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		super.visitTerminal(node);

		if (node.getSymbol().getType() == SRF_blocksLexer.STATO) {
			stackManager.markStateEncountered();
		}
	}

	private void handleCondizionePermanenzaEnter() {
		enteredCondizionePermanenza = true;
		foundSomeState = false;
	}

	private void handleCondizionePermanenzaExit() {
		if (!encounteredGeneralCondizionePermanenza && !foundSomeState) {
			encounteredGeneralCondizionePermanenza = true;
			condizionePermanenzaStates.clear();
			condizionePermanenzaStates.add(null);
		}
		enteredCondizionePermanenza = false;
		foundSomeState = false;
	}

	@Override
	public void enterCondizione_attributo_ind_non_macro_with_condizione_permanenza(SRF_blocksParser.Condizione_attributo_ind_non_macro_with_condizione_permanenzaContext ctx) {
		handleCondizionePermanenzaEnter();
	}

	@Override
	public void exitCondizione_attributo_ind_non_macro_with_condizione_permanenza(SRF_blocksParser.Condizione_attributo_ind_non_macro_with_condizione_permanenzaContext ctx) {
		handleCondizionePermanenzaExit();
	}

	@Override
	public void enterCondizione_attributo_cong_non_macro_with_condizione_permanenza(SRF_blocksParser.Condizione_attributo_cong_non_macro_with_condizione_permanenzaContext ctx) {
		handleCondizionePermanenzaEnter();
	}

	@Override
	public void exitCondizione_attributo_cong_non_macro_with_condizione_permanenza(SRF_blocksParser.Condizione_attributo_cong_non_macro_with_condizione_permanenzaContext ctx) {
		handleCondizionePermanenzaExit();
	}
}
