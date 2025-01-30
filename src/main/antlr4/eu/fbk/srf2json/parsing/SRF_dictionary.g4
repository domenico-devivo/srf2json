/**
 * Define a grammar to parse the dictionary file with the SRF aliases
 */
grammar SRF_dictionary;

options { caseInsensitive = true; }

root : dictionary ;

dictionary : DIZIONARIO_DEL_PROGETTO WS project_name=name WS? LBRACE WS?
	    (old_dictionary_contents_format | new_dictionary_contents_format)
	RBRACE WS?
;

old_dictionary_contents_format : (alias_plant WS?)+ ;

new_dictionary_contents_format :
    ALIAS WS? LBRACE WS?
        (alias_plant WS?)+
    RBRACE WS?
    IDENTIFICATORI WS? LBRACE WS?
        (ids_plant WS?)+
    RBRACE WS?
;

alias_plant : plant_name=name_of_plant WS?
	LBRACE WS?
		alias_lds_declarations
		alias_ldv_declarations
	RBRACE
;

alias_lds_declarations : LDS WS?
	LBRACE WS? (alias_class_declaration WS?)*
	RBRACE WS?
;

alias_ldv_declarations : LDV WS?
	LBRACE WS? (alias_class_declaration WS?)*
	RBRACE WS?
;

alias_class_declaration : class_name=name_without_spaces WS?
	LBRACE WS? class_aliases
	RBRACE
;

class_aliases : name WS? (COMMA WS? name WS?)* ;

ids_plant : plant_name=name_of_plant WS?
	LBRACE WS?
		ids_lds_declarations
		ids_ldv_declarations
	RBRACE
;

ids_lds_declarations : LDS WS?
	LBRACE WS? (ids_class_declaration WS?)*
	RBRACE WS?
;

ids_ldv_declarations : LDV WS?
	LBRACE WS? (ids_class_declaration WS?)*
	RBRACE WS?
;

ids_class_declaration : class_name=name_without_spaces (WS? EQ WS? assigned_id=id_value)? WS?
	(LBRACE WS? (ids_attr_declaration WS? (COMMA WS? ids_attr_declaration WS?)*)?
	RBRACE)?
;

ids_attr_declaration : attr_name=name_without_spaces WS? EQ WS? assigned_id=id_value ;

name_of_plant : ID_SINGLE (WS ID_SINGLE)* ;

name : (ID_SINGLE | ALIAS | IDENTIFICATORI) (WS (ID_SINGLE | ALIAS | IDENTIFICATORI))* ;

name_without_spaces : ID_SINGLE | ALIAS | IDENTIFICATORI ;

id_value : INTEGER ;

DIZIONARIO_DEL_PROGETTO : 'Dizionario' WS 'del' WS 'progetto' ;

ALIAS : 'Alias' ;

IDENTIFICATORI : 'Identificatori' ;

LDS : 'LdS' ;

LDV : 'LdV' ;

LBRACE : '{' ;

RBRACE : '}' ;

COMMA : ',' ;

EQ : '=' ;

ID_SINGLE : [àèéòìa-z_-] [0-9àèéòìa-z_/'-]* ;

INTEGER : [0-9]+ ;

WS : [ \t\r\n]+ ;
