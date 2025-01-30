/**
 * Define a grammar to parse the SRFs' FoglioDefinizioni
 */
grammar SRF_definitions;

options { caseInsensitive = true; }

root  : foglio_definizioni_LdS | foglio_definizioni_LdV ;

foglio_definizioni_LdS : FOGLIO_DEFINIZIONI_DELLA_CLASSE WS id WS? LBRACE WS? declarations_LdS WS? definitions_LdS WS? RBRACE ;

foglio_definizioni_LdV : FOGLIO_DEFINIZIONI_DELLA_CLASSE_DI_VISTA WS id WS? LBRACE WS? declarations_LdV WS? definitions_LdV WS? RBRACE ;

declarations_LdS : DICHIARAZIONI WS? LBRACE (WS? declaration_LdS)+ WS? RBRACE ;

declarations_LdV : DICHIARAZIONI WS? LBRACE (WS? declaration_LdV)+ WS? RBRACE ;

declaration_LdS : parameter | list | variable | timer | counter | declaration_macro_verifica | declaration_macro_valorizzata | declaration_macro_effetto | comando_manuale | comando_automatico | comando_output | attribute | controllo_dal_piazzale | comando_al_piazzale ;

declaration_LdV : parameter | list | variable | timer | counter | declaration_macro_verifica | declaration_macro_valorizzata | declaration_macro_effetto | comando_manuale | comando_automatico | comando_output | attribute ;

parameter : PARAMETRO WS accessibility WS id WS type_suffix ;

list : LISTA WS id WS type_suffix ;

variable : VARIABILE WS accessibility WS visibility WS id WS type_suffix (WS precedence)? (WS restorable)? (WS restore_variable)? ;

timer : TIMER WS accessibility WS visibility WS id WS duration (WS restorable)? ;

counter : CONTATORE WS accessibility WS visibility WS id ;

declaration_macro_verifica : MACRO_DI_VERIFICA WS accessibility WS id (WS? arguments)? ;

declaration_macro_valorizzata : MACRO_VALORIZZATA WS accessibility WS id WS type_suffix (WS? arguments)? ;

declaration_macro_effetto : MACRO_DI_EFFETTO WS id (WS? arguments)? ;

comando_manuale : COMANDO_MANUALE WS weight WS id (WS? sender)? ;

comando_automatico : COMANDO_AUTOMATICO WS id (WS? arguments)? ;

comando_output : COMANDO_DI_OUTPUT WS id WS type_suffix (WS valore_sicuro)? (WS? receiver)? ;

controllo_dal_piazzale : CONTROLLO_PIAZZALE WS accessibility WS id WS type_suffix WS valore_sicuro (WS precedence)? ;

comando_al_piazzale : COMANDO_PIAZZALE WS id WS type_suffix WS valore_sicuro ;

attribute : ATTRIBUTO WS id WS type_suffix ;

accessibility : PRIVAT_ | PROTETT_ | PUBBLIC_ ;

visibility : VISIBILE | INVISIBILE ;

type_suffix : DI_TIPO WS type_name ;

type_name : BOOLEANO | definition_integer_range | id_without_in ;

precedence : VALORE_PRECEDENTE ;

//duration : CON_DURATA_IN_SECONDI_UGUALE WS (value | (A WS (INTEGER | FLOAT))) ; // float values are allowed

duration : CON_DURATA_IN_SECONDI_UGUALE WS ((A WS duration_value) | ('al' WS PARAMETRO WS id)) ; // float values are allowed

duration_value : INTEGER | FLOAT ;

restorable : CON_RIPRISTINO ;

restore_variable : DI_RIPRISTINO ;

// for arguments see pp. 49-50 of the AIDA manual
arguments : '(' WS? CON WS argument (((WS? ',' WS?) | (WS E WS)) argument)* WS? ')';

argument : (ARGOMENTO WS)? id WS type_suffix ;

weight : LEGGERO | PESANTE ;

valore_sicuro : CON_VALORE_SICURO WS safe_value=primitive_value ;

sender : '(' WS? RICEVUTO_DA WS id WS? ')' ;

receiver : '(' WS? INVIATO_A WS id WS? ')' ;

primitive_value : boolean_value=(TRUE | FALSE) | integer_value=INTEGER | enum_value=id ;

value : (LA WS COSTANTE WS)? primitive_value
      | IL WS PARAMETRO WS id
      | IL WS (PRECEDENTE WS)? CONTROLLO WS id
      | LA WS (PRECEDENTE WS)? VARIABILE WS id
      | LL ARGOMENTO id
      | LA WS MACRO WS id ;

definitions_LdS : DEFINIZIONI WS? LBRACE WS? (definition_LdS WS?)+ RBRACE ;

definitions_LdV : DEFINIZIONI WS? LBRACE WS? (definition_LdV WS?)+ RBRACE ;

definition_LdS : definition_type | definition_record_LdS | definition_macro_verifica | definition_macro_valorizzata | definition_macro_effetto ;

definition_LdV : definition_type | definition_record_LdV | definition_macro_verifica | definition_macro_valorizzata | definition_macro_effetto ;

definition_type : TIPO WS id WS (boolean_def=BOOLEANO | integer_def=definition_integer_range | enum_def=definition_enum_def) ;

definition_integer_range : INTERO (WS DA WS min_value=INTEGER (WS A WS max_value=INTEGER)?)? ;

definition_enum_def : INSIEME WS (is_ordered WS)? literals ;

is_ordered : ORDINATO ;

literals : LBRACE WS? id WS? (',' WS? id WS?)* RBRACE ;

definition_record_LdS : RECORD WS id WS definition_record_fields_LdS ;

definition_record_LdV : RECORD WS id WS definition_record_fields_LdV ;

definition_record_fields_LdS : CON WS definition_record_field_LdS ((WS? ',' WS?) | (WS E WS) definition_record_field_LdS)* ;

definition_record_fields_LdV : CON WS definition_record_field_LdV ((WS? ',' WS?) | (WS E WS) definition_record_field_LdV)* ;

definition_record_field_LdS : CAMPO WS id WS type_suffix (WS IN WS plant_name=id_without_in)? ;

definition_record_field_LdV : CAMPO WS id WS type_suffix (WS logic_ref)? ;

definition_macro_verifica : MACRO_DI_VERIFICA WS id (WS? definition_arguments)? WS? high_level_contents ;

definition_macro_valorizzata : MACRO_VALORIZZATA WS id WS type_suffix (WS? definition_arguments)? WS? high_level_contents ;

definition_macro_effetto : MACRO_DI_EFFETTO WS id (WS? definition_arguments)? WS? high_level_contents ;

definition_arguments : '(' WS? CON WS definition_argument (((WS? ',' WS?) | (WS E WS)) definition_argument)* WS? ')';

definition_argument : (ARGOMENTO WS)? id ;

id : (ID_SINGLE | A | E | IN | DA | CON | LA | IL | LL | NESSUNO) (WS (ID_SINGLE | A | E | IN | DA | CON | LA | IL | LL | NESSUNO))* ;

id_without_in : (ID_SINGLE | A | E | DA | CON | LA | IL | LL | NESSUNO) (WS (ID_SINGLE | A | E | DA | CON | LA | IL | LL | NESSUNO))* ;

logic_ref : DI_LDS ;

//block : WS? contents WS?;

high_level_contents : (LBRACE contents* RBRACE) ;

contents : ~(LBRACE | RBRACE) | high_level_contents ;

FOGLIO_DEFINIZIONI_DELLA_CLASSE : 'FoglioDefinizioni della classe' ;

FOGLIO_DEFINIZIONI_DELLA_CLASSE_DI_VISTA : 'FoglioDefinizioni della classe di vista' ;

DICHIARAZIONI : 'Dichiarazioni' ;

DEFINIZIONI : 'Definizioni' ;

PARAMETRO : 'Parametro' ;

LISTA : 'Lista' ;

VARIABILE : 'Variabile' ;

TIMER : 'Timer' ;

CONTATORE : 'Contatore' ;

MACRO_DI_VERIFICA : 'Macro di verifica' ;

MACRO_VALORIZZATA : 'Macro valorizzata' ;

MACRO_DI_EFFETTO : 'Macro di effetto' ;

COMANDO_MANUALE : 'Comando manuale' ;

COMANDO_AUTOMATICO : 'Comando automatico' ;

COMANDO_DI_OUTPUT : 'Comando di output' ;

CONTROLLO_PIAZZALE : 'Controllo piazzale' ;

COMANDO_PIAZZALE : 'Comando piazzale' ;

ATTRIBUTO : 'Attributo' ;

DI_TIPO : 'di tipo' ;

PRIVAT_ : 'privato' | 'privata' ;

PROTETT_ : 'protetto' | 'protetta' ;

PUBBLIC_ : 'pubblico' | 'pubblica' ;

VISIBILE : 'visibile' ;

INVISIBILE : 'invisibile' ;

VALORE_PRECEDENTE : 'con valore precedente' | 'e valore precedente' ;

CON_RIPRISTINO : 'con ripristino' ;

DI_RIPRISTINO : 'di ripristino' ;

RICEVUTO_DA : 'ricevuto da' ;

INVIATO_A : 'inviato a' ;

LEGGERO : 'leggero' ;

PESANTE : 'pesante' ;

CON_VALORE_SICURO : 'con valore sicuro' ;

CON_DURATA_IN_SECONDI_UGUALE : 'con durata in secondi uguale' ;

DI_LDS : 'di LDS' ;

ARGOMENTO : 'argomento' ;

NESSUNO : 'nessuno' | 'nessuna' ;

TIPO : 'Tipo' ;

INTERO : 'intero' ;

BOOLEANO : 'booleano' ;

TRUE : 'true' ;

FALSE : 'false' ;

INSIEME : 'insieme' ;

ORDINATO : 'ordinato' ;

RECORD : 'Record' ;

CAMPO : 'campo' ;

COSTANTE : 'costante' ;

PRECEDENTE : 'precedente' ;

CONTROLLO : 'controllo' ;

MACRO : 'macro' ;

A : 'a' | 'ad' ;

E : 'e' | 'ed' ;

IN : 'in' ;

DA : 'da' ;

CON : 'con' ;

LA : 'la' | 'alla' | 'della' ;

IL : 'il' | 'al' | 'del' ;

LL : 'l\'' | 'all\'' | 'dell\'' ;

ID_SINGLE : '*' | [àèéòìa-z_-] [0-9àèéòìa-z_/'-]* ;

INTEGER : [+-]? [0-9]+ ;

FLOAT : [+-]? [0-9]* '.' [0-9]+ ;

WS : CM? PURE_WS CM? ;

PURE_WS : [ \t\r\n]+ ; //-> skip ; // skip spaces, tabs, newlines

LBRACE : '{' ;

RBRACE : '}' ;

CM : ((COMMENT_LINE | COMMENT_BLOCK) PURE_WS?)+ ;

COMMENT_LINE : '//' ~('\n')* ;

COMMENT_BLOCK : '/*' (~'*' | '*' ~'/')* '*/' ; // -> skip ;

CHAR : . ;
