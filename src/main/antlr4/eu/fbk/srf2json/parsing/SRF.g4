/**
 * Define a grammar to parse the SRFs
 */
grammar SRF;

options { caseInsensitive = true; }

root  : scheda_classe ;

scheda_classe : SCHEDA_DI_CLASSE WS id WS scheda_inizializzazione scheda_stato+ ;

scheda_inizializzazione : SCHEDA_DI_INIZIALIZZAZIONE WS transizioni_iniziali ;

transizioni_iniziali : TRANSIZIONI_INIZIALI WS transition+ ;

scheda_stato : SCHEDA_DI_STATO WS id WS permanenza=transition? attuazioni_nominali normalizzazioni attuazioni_con_soccorso ;

attuazioni_nominali : ATTUAZIONI_NOMINALI WS ((NESSUNO WS) | transition+) ;

normalizzazioni : NORMALIZZAZIONI WS ((NESSUNO WS) | transition+) ;

attuazioni_con_soccorso : ATTUAZIONI_CON_SOCCORSO WS ((NESSUNO WS?) | transition+) ;

transition : (TRANSIZIONE_INIZIALE_VERSO | PERMANENZA_IN | ATTUAZIONE_VERSO | NORMALIZZAZIONE_VERSO | ATTUAZIONE_CON_SOCCORSO_VERSO) WS id param? WS conditions effects ;

conditions : (CONDIZIONI_PER_LA_TRANSIZIONE | CONDIZIONI_PER_LA_PERMANENZA | CONDIZIONI_PER_L_ATTUAZIONE | CONDIZIONI_PER_LA_NORMALIZZAZIONE | CONDIZIONI_PER_L_ATTUAZIONE_CON_SOCCORSO) block ;

effects : (EFFETTI_DELLA_TRANSIZIONE | EFFETTI_DELLA_PERMANENZA | EFFETTI_DELL_ATTUAZIONE | EFFETTI_DELLA_NORMALIZZAZIONE | EFFETTI_DELL_ATTUAZIONE_CON_SOCCORSO) block ;

id : (ID_SINGLE | NESSUNO) (WS (ID_SINGLE | NESSUNO))* ;

block : WS? high_level_contents WS? ;

high_level_contents : LBRACE contents* RBRACE ;

contents : ~(LBRACE | RBRACE) | high_level_contents ;

param : WS? HASH WS? (priority | trac) ;

priority : 'prio' WS? '=' WS? value=INTEGER ;

trac : 'trac' WS? '=' WS? QUOTE (~QUOTE)* QUOTE ;

SCHEDA_DI_CLASSE : 'Scheda di classe' | 'Scheda della classe di vista' ;

SCHEDA_DI_INIZIALIZZAZIONE : 'Scheda di inizializzazione' ;

TRANSIZIONI_INIZIALI : 'Transizioni iniziali' ;

TRANSIZIONE_INIZIALE_VERSO : 'Transizione iniziale verso' ;

CONDIZIONI_PER_LA_TRANSIZIONE : 'Condizioni per la transizione' ;

EFFETTI_DELLA_TRANSIZIONE : 'Effetti della transizione' ;

SCHEDA_DI_STATO : 'Scheda di stato' ;

PERMANENZA_IN : 'Permanenza in' ;

CONDIZIONI_PER_LA_PERMANENZA : 'Condizioni per la permanenza' ;

EFFETTI_DELLA_PERMANENZA : 'Effetti della permanenza' ;

ATTUAZIONI_NOMINALI : 'Attuazioni nominali' ;

ATTUAZIONE_VERSO : 'Attuazione verso' ;

CONDIZIONI_PER_L_ATTUAZIONE : 'Condizioni per l\'attuazione' | 'Condizioni per la attuazione';

EFFETTI_DELL_ATTUAZIONE : 'Effetti dell\'attuazione' | 'Effetti della attuazione' ;

NORMALIZZAZIONI : 'Normalizzazioni' ;

NORMALIZZAZIONE_VERSO : 'Normalizzazione verso' ;

CONDIZIONI_PER_LA_NORMALIZZAZIONE : 'Condizioni per la normalizzazione' ;

EFFETTI_DELLA_NORMALIZZAZIONE : 'Effetti della normalizzazione' ;

ATTUAZIONI_CON_SOCCORSO : 'Attuazioni con Soccorso' ;

ATTUAZIONE_CON_SOCCORSO_VERSO : 'Attuazione con Soccorso verso' ;

CONDIZIONI_PER_L_ATTUAZIONE_CON_SOCCORSO : 'Condizioni per l\'attuazione con soccorso' | 'Condizioni per la attuazione con soccorso' ;

EFFETTI_DELL_ATTUAZIONE_CON_SOCCORSO : 'Effetti dell\'attuazione con soccorso' | 'Effetti della attuazione con soccorso' ;

NESSUNO : 'nessuno' | 'nessuna' ;

ID_SINGLE : '*' | [àèéòìa-z_-] [0-9àèéòìa-z_/'-]* ;

INTEGER : [+-]? [0-9]+ ;

WS : CM? PURE_WS CM? ;

PURE_WS : [ \t\r\n]+ ; //-> skip ; // skip spaces, tabs, newlines

HASH : '#' ;

LBRACE : '{' ;

RBRACE : '}' ;

QUOTE : '"' ;

CM : ((COMMENT_LINE | COMMENT_BLOCK) PURE_WS?)+ ;

COMMENT_LINE : '//' ~('\n')* ; // -> skip ;

COMMENT_BLOCK : '/*' (~'*' | '*' ~'/')* '*/' ; // -> skip ;

CHAR : . ;
