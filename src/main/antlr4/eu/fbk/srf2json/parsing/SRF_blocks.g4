/**
 * Define a grammar to parse the blocks contained in SRFs (conditions / effects / macro bodies)
 */
grammar SRF_blocks;

options { caseInsensitive = true; }

// --------------------------- GUARDIE --------------------------------------------------

root_condition: LBRACE WS? guardia WS? RBRACE ;

root_effect: LBRACE WS? effetti WS? RBRACE ;

root_macro_verifica: LBRACE WS? body_macro_verifica WS? RBRACE ;

root_macro_effetto: LBRACE WS? body_macro_effetto WS? RBRACE ;

root_macro_valorizzata: LBRACE WS? body_macro_valorizzata WS? RBRACE ;

guardia :
	NESSUNO                             |
    blocco_ricezione_comandi_manuali    |
    blocco_ricezione_comandi_automatici |
    blocco_congiunzione_logica ;

effetti : NESSUNO | (effetto (WS effetto)*) ;

// --------------- Guardie e verifiche ---------------------------------------

blocco_ricezione_comandi_manuali :
	blocco_ricezione_comando_manuale_singolo             |
    ALMENO_UNA_DELLE_SEGUENTI WS? LBRACE WS? blocco_ricezione_comando_manuale_multiplo+ RBRACE ;

blocco_ricezione_comando_manuale_singolo :
	RICEZIONE_DEL_COMANDO_MANUALE WS nome_comando_manuale (WS blocco_congiunzione_logica)?		|
    TUTTE_LE_SEGUENTI WS? LBRACE WS? RICEZIONE_DEL_COMANDO_MANUALE WS nome_comando_manuale (WS blocco_congiunzione_logica)? WS? RBRACE ;

blocco_ricezione_comando_manuale_multiplo :
	RICEZIONE_DEL_COMANDO_MANUALE WS nome_comando_manuale   |
    TUTTE_LE_SEGUENTI WS? LBRACE WS? RICEZIONE_DEL_COMANDO_MANUALE WS nome_comando_manuale (WS blocco_congiunzione_logica)? WS? RBRACE ;

blocco_ricezione_comandi_automatici :
	blocco_ricezione_comando_automatico_singolo          |
    ALMENO_UNA_DELLE_SEGUENTI WS? LBRACE WS? blocco_ricezione_comando_automatico_multiplo+ RBRACE ;
        
blocco_ricezione_comando_automatico_singolo :
	RICEZIONE_DEL_COMANDO WS nome_comando_automatico argomenti_formali? WS? blocco_congiunzione_logica?		|
    TUTTE_LE_SEGUENTI WS? LBRACE WS? RICEZIONE_DEL_COMANDO WS nome_comando_automatico argomenti_formali? WS? blocco_congiunzione_logica? WS? RBRACE ;

blocco_ricezione_comando_automatico_multiplo :
	RICEZIONE_DEL_COMANDO WS nome_comando_automatico argomenti_formali?       |
    TUTTE_LE_SEGUENTI WS? LBRACE WS? RICEZIONE_DEL_COMANDO WS nome_comando_automatico argomenti_formali? WS? blocco_congiunzione_logica? WS? RBRACE ;
        
blocco_congiunzione_logica :
    (verifica                                                                                                                                                   |
     ((se_part COMMA WS?)? (TUTTE_LE_SEGUENTI | ALMENO_UNA_DELLE_SEGUENTI | SOLO_UNA_DELLE_SEGUENTI)  WS? LBRACE WS? blocco_congiunzione_logica WS? RBRACE WS?) |
     (se_part? WS? LBRACE WS? ((verifica | blocco_congiunzione_logica) WS?)+ RBRACE)
    ) (WS? blocco_congiunzione_logica)?
;

verifica :
	(se_part COMMA WS?)? VERIFICA WS CHE WS condizione_verifica (WS (E|O) WS CHE WS condizione_verifica)* ;

se_part :       SE WS condizione_se         (WS (E|O) WS SE WS condizione_se        )* WS? ;
se_part_macro : SE WS condizione_se_macro   (WS (E|O) WS SE WS condizione_se_macro  )* WS? ;

// ----------------------- Condizioni ----------------------------

condizione_se :
	condizione_attributo_ind_non_macro |
	condizioni_lista_ind
;
	
condizione_verifica :
	condizione_attributo_cong_non_macro |
	condizioni_lista_cong
;

condizione_attributo_ind_non_macro :
    (LO                                                                             WS)? STATO                                  WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         				|
    (IL WS)? RIPRISTINO WS DELLO                                                    WS STATO                                    WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         				|
	(IL WS)? PARAMETRO                                                              WS nome_parametro                           WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         				|
  	(IL WS)? (PRECEDENTE WS)? CONTROLLO                                             WS nome_controllo                           WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         				|
    (article WS)? ((PRECEDENTE | RIPRISTINO) WS (di_article WS)?)? VARIABILE        WS nome_variabile                           WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         				|
    (L_ESITO | ESITO) WS DEL WS COMANDO WS MANUALE                                  WS nome_comando_manuale                     WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         			    |
  	(IL WS)? (RIPRISTINO WS DEL WS)? TIMER                                          WS nome_timer                               WS (NON WS)? VERBO_E WS (DISATTIVO|ATTIVO|SCADUTO)    				    |
  	(IL WS)? CONTATORE                                                              WS nome_contatore                           WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind     	                |
  	(L_ARGOMENTO | ARGOMENTO)  	                                                    WS nome_argomento                           WS (NON WS)? VERBO_E WS disuguaglianza_valore_ind         				|
  	(LA WS)? MACRO                                                                  WS nome_macro_valore argomenti_attuali?     WS? (NON WS)? VERBO_E WS disuguaglianza_valore_ind                      |
  	condizione_attributo_ind_non_macro_with_condizione_permanenza
;

condizione_attributo_ind_non_macro_with_condizione_permanenza :
    (LA WS)? CONDIZIONE WS DI WS PERMANENZA WS (DELLO WS STATO                      WS nome_stato)?                             WS (NON WS)? VERBO_E WS (NON WS)? (UGUALE WS a_valore)|(DIVERSO WS da_valore)
;

condizione_attributo_cong_non_macro :
    (LO                                                                             WS)? STATO                                  WS (NON WS)? SIA WS disuguaglianza_valore_cong         				    |
    (IL WS)? RIPRISTINO WS DELLO                                                    WS STATO                                    WS (NON WS)? SIA WS disuguaglianza_valore_cong         				    |
	(IL WS)? PARAMETRO                                                              WS nome_parametro                           WS (NON WS)? SIA WS disuguaglianza_valore_cong         				    |
  	(IL WS)? (PRECEDENTE WS)? CONTROLLO                                             WS nome_controllo                           WS (NON WS)? SIA WS disuguaglianza_valore_cong         				    |
    (article WS)? ((PRECEDENTE | RIPRISTINO) WS (di_article WS)?)? VARIABILE        WS nome_variabile                           WS (NON WS)? SIA WS disuguaglianza_valore_cong         				    |
    (L_ESITO | ESITO) WS DEL WS COMANDO WS MANUALE                                  WS nome_comando_manuale                     WS (NON WS)? SIA WS disuguaglianza_valore_cong         			        |
  	(IL WS)? (RIPRISTINO WS DEL WS)? TIMER                                          WS nome_timer                               WS (NON WS)? SIA WS (DISATTIVO|ATTIVO|SCADUTO)    				        |
  	(IL WS)? CONTATORE                                                              WS nome_contatore                           WS (NON WS)? SIA WS disuguaglianza_valore_cong                    	    |
  	(L_ARGOMENTO | ARGOMENTO)  	                                                    WS nome_argomento                           WS (NON WS)? SIA WS disuguaglianza_valore_cong         				    |
  	(LA WS)? MACRO                                                                  WS nome_macro_valore argomenti_attuali?     WS? (NON WS)? SIA WS disuguaglianza_valore_cong                         |
  	condizione_attributo_cong_non_macro_with_condizione_permanenza
;

condizione_attributo_cong_non_macro_with_condizione_permanenza :
    (LA WS)? CONDIZIONE WS DI WS PERMANENZA WS (DELLO WS STATO                      WS nome_stato)?                             WS (NON WS)? SIA WS (NON WS)? (UGUALE WS a_valore)|(DIVERSO WS da_valore)
;
  
condizioni_lista_ind : condizione_lista_ind (WS (E|O) WS condizione_lista_ind)* (WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? ;

condizioni_lista_cong : condizione_lista_cong (WS (E|O) WS condizione_lista_cong)* (WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? ;

condizione_lista_ind :
	(
		(IL WS campo_full)										|
		(((article WS?)?
		    ((RIPRISTINO WS DELLO WS)? STATO WS)                                                                        |
			(nome_parametro WS)											                                                |
			((PRECEDENTE WS)? nome_controllo WS)											                            |
			(((RIPRISTINO WS DI WS) | (PRECEDENTE WS))? nome_variabile WS)			                                    |
			(nome_contatore WS)                                                                                         |
			(nome_macro_valore argomenti_attuali_in_iterazione? WS?)
		) del_campo_full)
	) WS (ESISTE WS E WS)? (NON WS)? VERBO_E WS disuguaglianza_valore_in_iterazione_ind										                                |
	((article WS?)? (RIPRISTINO WS DI WS)? nome_timer WS del_campo_full WS (ESISTE WS E WS)? (NON WS)? VERBO_E WS (DISATTIVO|ATTIVO|SCADUTO))               |
    (
    	(IL WS campo_full)									    |
    	((quantificatore WS)? unione_liste)
    ) WS (ESISTE WS E WS)? combinazione_booleana_ind																	                                    |
	(quantificatore WS)? unione_liste WS (NON WS)? ESISTE                                                                                                   |
	OGNI WS unione_liste WS NON WS ESISTE
;
	
condizione_lista_cong :
	(
		(IL WS campo_full)										|
		(((article WS?)?
		    ((RIPRISTINO WS DELLO WS)? STATO WS)                                                                        |
			(nome_parametro WS)											                                                |
			((PRECEDENTE WS)? nome_controllo WS)											                            |
			(((RIPRISTINO WS DI WS) | (PRECEDENTE WS))? nome_variabile WS)			                                    |
			(nome_contatore WS)                                                                                         |
			(nome_macro_valore argomenti_attuali_in_iterazione? WS?)
		) del_campo_full)
	) WS (ESISTA WS E WS)? (NON WS)? SIA WS disuguaglianza_valore_in_iterazione_cong										                                |
	((article WS?)? (RIPRISTINO WS DI WS)? nome_timer WS del_campo_full WS (ESISTA WS E WS)? (NON WS)? SIA WS (DISATTIVO|ATTIVO|SCADUTO))               |
    (
    	(IL WS campo_full)									    |
    	((quantificatore WS)? unione_liste)
    ) WS (ESISTA WS E WS)? combinazione_booleana_ind																	                                    |
	(quantificatore WS)? unione_liste WS (NON WS)? ESISTA                                                                                                   |
	OGNI WS unione_liste WS NON WS ESISTA
;

condizione_filtro_iterazione :
	((
		(IL WS CAMPO WS nome_campo WS)										|
		(((article WS)?
		    ((RIPRISTINO WS DELLO WS)? STATO WS)                                            |
			(nome_parametro WS) 										                    |
			((PRECEDENTE WS)? nome_controllo WS) 										    |
			(((RIPRISTINO WS ((di_article WS) | DELL_)) | PRECEDENTE)? nome_variabile WS)   |
			(nome_contatore WS)                                                             |
			(nome_macro_valore argomenti_attuali_in_iterazione? WS?)
		) (DEL WS CAMPO WS nome_campo WS)?)
	) (NON WS)? VERBO_E WS disuguaglianza_valore_in_iterazione_ind)															                                |
	((IL WS RIPRISTINO WS ((di_article WS) | DELL_))? nome_timer WS (DEL WS CAMPO WS nome_campo WS )? (NON WS)? VERBO_E WS (DISATTIVO|ATTIVO|SCADUTO))		|
	((IL WS CAMPO WS nome_campo WS)? combinazione_booleana_ind)
;

combinazione_booleana_ind :
	(NON WS)? VERBO_E WS NELLO_STATO WS unione_stati (WS? COMMA? WS? (E|O) WS combinazione_booleana_ind)? |
	(NON WS)? VERBO_E WS combinazione_booleana_attributi (WS? COMMA? WS? (E|O) WS combinazione_booleana_ind)?
;

combinazione_booleana_cong :
	(NON WS)? SIA WS NELLO_STATO WS unione_stati (WS? COMMA? WS? (E|O) WS combinazione_booleana_cong)? |
	(NON WS)? SIA WS combinazione_booleana_attributi (WS? COMMA? WS? (E|O) WS combinazione_booleana_cong)?
;

combinazione_booleana_attributi : (NON WS)? nome_attributo_booleano (WS (E|O) WS combinazione_booleana_attributi)? ;

disuguaglianza_valore_ind : (NON WS)? ((((MAGGIORE | MINORE) WS O WS)? UGUALE WS a_valore)|(MAGGIORE WS di_valore)|(MINORE WS di_valore)|(DIVERSO WS da_valore)) (WS (E|O) WS (NON WS)? (VERBO_E WS)? disuguaglianza_valore_ind)* ;

disuguaglianza_valore_cong : (NON WS)? ((((MAGGIORE | MINORE) WS O WS)? UGUALE WS a_valore)|(MAGGIORE WS di_valore)|(MINORE WS di_valore)|(DIVERSO WS da_valore)) (WS (E|O) WS (NON WS)? (SIA WS)? disuguaglianza_valore_cong)* ;

disuguaglianza_valore_in_iterazione_ind : (NON WS)? ((((MAGGIORE | MINORE) WS O WS)? UGUALE WS a_valore_in_iterazione)|(MAGGIORE WS di_valore_in_iterazione)|(MINORE WS di_valore_in_iterazione)|(DIVERSO WS da_valore_in_iterazione)) (WS (E|O) WS (NON WS)? (VERBO_E WS)? disuguaglianza_valore_in_iterazione_ind)* ;

disuguaglianza_valore_in_iterazione_cong : (NON WS)? ((((MAGGIORE | MINORE) WS O WS)? UGUALE WS a_valore_in_iterazione)|(MAGGIORE WS di_valore_in_iterazione)|(MINORE WS di_valore_in_iterazione)|(DIVERSO WS da_valore_in_iterazione)) (WS (E|O) WS (NON WS)? (SIA WS)? disuguaglianza_valore_in_iterazione_cong)* ;

valore :
	((IL WS VALORE WS) | (LA WS COSTANTE WS))? costante      	        |
    IL WS PARAMETRO WS nome_parametro							        |
    IL WS (PRECEDENTE WS)? CONTROLLO WS nome_controllo			        |
    (((LA (WS PRECEDENTE)?) | (IL WS (PRECEDENTE | RIPRISTINO) WS DELLA)) WS VARIABILE WS nome_variabile)			|
    L_ARGOMENTO 	WS nome_argomento							        |
    IL WS CONTATORE WS nome_contatore							        |
    IL WS RIPRISTINO WS DELLO WS STATO                                  |
    LA WS MACRO 	WS nome_macro_valore argomenti_attuali? 	        |
	valore_da_lista_con_alternativa
;
	
a_valore :
	((AL WS VALORE WS) | (ALLA WS COSTANTE WS) | (A WS))? costante      |
    AL WS PARAMETRO 	WS nome_parametro						        |
    AL WS (PRECEDENTE WS)? CONTROLLO 	WS nome_controllo		        |
    (((ALLA (WS PRECEDENTE)?) | (AL WS (PRECEDENTE | RIPRISTINO) WS DELLA)) WS VARIABILE WS nome_variabile)			|
    ALL_ARGOMENTO 		WS nome_argomento						        |
    AL WS CONTATORE 	WS nome_contatore						        |
    AL WS RIPRISTINO WS DELLO WS STATO                                  |
    ALLA WS MACRO 		WS nome_macro_valore argomenti_attuali?         |
	a_valore_da_lista_con_alternativa
;
	
di_valore :
	((DEL WS VALORE WS) | (DELLA WS COSTANTE WS) | (DI WS))? costante   |
    DEL WS PARAMETRO 	WS nome_parametro						        |
    DEL WS (PRECEDENTE WS)? CONTROLLO 	WS nome_controllo		        |
    (((DELLA (WS PRECEDENTE)?) | (DEL WS (PRECEDENTE | RIPRISTINO) WS DELLA)) WS VARIABILE WS nome_variabile)			|
    DELL_ARGOMENTO 		WS nome_argomento						        |
    DEL WS CONTATORE 	WS nome_contatore						        |
    DEL WS RIPRISTINO WS DELLO WS STATO                                 |
    DELLA WS MACRO 		WS nome_macro_valore argomenti_attuali?         |
	di_valore_da_lista_con_alternativa
;
	
da_valore :
	((DAL WS VALORE WS) | (DALLA WS COSTANTE WS) | (DA WS))? costante   |
    DAL WS PARAMETRO 	WS nome_parametro						        |
    DAL WS (PRECEDENTE WS)? CONTROLLO 	WS nome_controllo		        |
    (((DALLA (WS PRECEDENTE)?) | (DAL WS (PRECEDENTE | RIPRISTINO) WS DELLA)) WS VARIABILE WS nome_variabile)			|
    DALL_ARGOMENTO 		WS nome_argomento						        |
    DAL WS CONTATORE 	WS nome_contatore						        |
    DAL WS RIPRISTINO WS DELLO WS STATO                                 |
    DALLA WS MACRO 		WS nome_macro_valore argomenti_attuali?         |
	da_valore_da_lista_con_alternativa
;
	
valore_da_lista_con_alternativa : valore_da_lista (WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? WS? COMMA WS? ALTRIMENTI WS (valore | (IL_VALORE WS di_valore)) ;

a_valore_da_lista_con_alternativa : a_valore_da_lista (WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? WS? COMMA WS? ALTRIMENTI WS (valore | (IL_VALORE WS di_valore)) ;

di_valore_da_lista_con_alternativa : di_valore_da_lista (WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? WS? COMMA WS? ALTRIMENTI WS (valore | (IL_VALORE WS di_valore)) ;

da_valore_da_lista_con_alternativa : da_valore_da_lista (WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? WS? COMMA WS? ALTRIMENTI WS (valore | (IL_VALORE WS di_valore)) ;

valore_da_lista :
	IL WS campo_full 														                |
	nome_parametro WS del_campo_full 										                |
	nome_controllo WS del_campo_full 										                |
	(IL WS RIPRISTINO WS ((di_article WS) | DELL_)?)? nome_variabile WS del_campo_full 		|
	nome_contatore WS del_campo_full 										                |
	nome_macro_valore argomenti_attuali_in_iterazione? WS? del_campo_full
;
	
a_valore_da_lista : 
	AL WS campo_full 														                |
	a_nome_parametro WS del_campo_full 										                |
	a_nome_controllo WS del_campo_full 										                |
	(AL WS RIPRISTINO WS ((di_article WS) | DELL_)?)? a_nome_variabile WS del_campo_full 	|
	a_nome_contatore WS del_campo_full 										                |
	a_nome_macro_valore argomenti_attuali_in_iterazione? WS? del_campo_full
;
	
da_valore_da_lista : 
	DAL WS campo_full 														                |
	da_nome_parametro WS del_campo_full 									                |
	da_nome_controllo WS del_campo_full 									                |
	(DAL WS RIPRISTINO WS ((di_article WS) | DELL_)?)? da_nome_variabile WS del_campo_full 	|
	da_nome_contatore WS del_campo_full 									                |
	da_nome_macro_valore argomenti_attuali_in_iterazione? WS? del_campo_full
;
	
di_valore_da_lista : 
	DEL WS campo_full 														                |
	di_nome_parametro WS del_campo_full 									                |
	di_nome_controllo WS del_campo_full 									                |
	(DEL WS RIPRISTINO WS ((di_article WS) | DELL_)?)? di_nome_variabile WS del_campo_full 	|
	di_nome_contatore WS del_campo_full 									                |
	di_nome_macro_valore argomenti_attuali_in_iterazione? WS? del_campo_full
;

valore_in_iterazione :
	(LA WS COSTANTE WS)? costante      										|
    IL WS PARAMETRO WS nome_parametro										|
    IL WS CONTROLLO WS nome_controllo										|
    LA WS VARIABILE WS nome_variabile										|
    L_ARGOMENTO WS nome_argomento											|
    IL WS CONTATORE WS nome_contatore										|
    IL WS RIPRISTINO WS DELLA WS VARIABILE WS nome_variabile                |
    LA WS MACRO WS nome_macro_valore argomenti_attuali_in_iterazione? 		|
    IL WS CAMPO WS nome_campo												|
    (
    	(nome_parametro WS)											|
    	((PRECEDENTE WS)? nome_controllo WS)						|
    	((PRECEDENTE WS)? nome_variabile WS)						|
    	(nome_contatore WS)											|
    	(nome_macro_valore argomenti_attuali_in_iterazione? WS?)
    ) DEL WS CAMPO WS nome_campo                                               |
    IL WS RIPRISTINO WS (
        DELLO WS STATO                                              |
        DI WS nome_variabile WS DEL WS CAMPO WS nome_campo
    )
;

a_valore_in_iterazione :
	((ALLA WS COSTANTE WS) | (A WS))? costante      						|
    AL WS PARAMETRO WS nome_parametro										|
    AL WS CONTROLLO WS nome_controllo										|
    ALLA WS VARIABILE WS nome_variabile										|
    ALL_ARGOMENTO WS nome_argomento											|
    AL WS CONTATORE WS nome_contatore										|
    AL WS RIPRISTINO WS DELLA WS VARIABILE WS nome_variabile                |
    ALLA WS MACRO WS nome_macro_valore argomenti_attuali_in_iterazione?		|
    AL WS CAMPO WS nome_campo												|
    (
    	(a_nome_parametro WS)										|
    	(a_nome_controllo WS) 										|
    	(a_article WS PRECEDENTE WS nome_controllo WS)				|
    	(a_nome_variabile WS)										|
    	(a_article WS PRECEDENTE WS nome_variabile WS)				|
    	(a_nome_contatore WS)										|
    	(a_nome_macro_valore argomenti_attuali_in_iterazione? WS?)
    ) DEL WS CAMPO WS nome_campo                                               |
    AL WS RIPRISTINO WS (
        DELLO WS STATO                                              |
        DI WS nome_variabile WS DEL WS CAMPO WS nome_campo
    )
;

da_valore_in_iterazione :
	((DALLA WS COSTANTE WS) | (DA WS))? costante      						|
    DAL WS PARAMETRO WS nome_parametro										|
    DAL WS CONTROLLO WS nome_controllo										|
    DALLA WS VARIABILE WS nome_variabile									|
    DALL_ARGOMENTO WS nome_argomento										|
    DAL WS CONTATORE WS nome_contatore										|
    DAL WS RIPRISTINO WS DELLA WS VARIABILE WS nome_variabile               |
    DALLA WS MACRO WS nome_macro_valore argomenti_attuali_in_iterazione?	|
    DAL WS CAMPO WS nome_campo												|
    (
    	(da_nome_parametro WS)										|
    	(da_nome_controllo WS)										|
    	(da_article WS PRECEDENTE WS nome_controllo WS)				|
    	(da_nome_variabile WS)										|
    	(da_article WS PRECEDENTE WS nome_variabile WS)				|
    	(da_nome_contatore WS)										|
    	(da_nome_macro_valore argomenti_attuali_in_iterazione? WS?)
    ) DEL WS CAMPO WS nome_campo                                               |
    DAL WS RIPRISTINO WS (
        DELLO WS STATO                                              |
        DI WS nome_variabile WS DEL WS CAMPO WS nome_campo
    )
;

di_valore_in_iterazione :
	((DELLA WS COSTANTE WS) | (DI WS))? costante      						|
    DEL WS PARAMETRO WS nome_parametro										|
    DEL WS CONTROLLO WS nome_controllo										|
    DELLA WS VARIABILE WS nome_variabile									|
    DELL_ARGOMENTO WS nome_argomento										|
    DEL WS CONTATORE WS nome_contatore										|
    DEL WS RIPRISTINO WS DELLA WS VARIABILE WS nome_variabile               |
    DELLA WS MACRO WS nome_macro_valore argomenti_attuali_in_iterazione?	|
    DEL WS CAMPO WS nome_campo												|
    (
    	(di_nome_parametro WS)										|
    	(di_nome_controllo WS)										|
    	(di_article WS PRECEDENTE WS nome_controllo WS)				|
    	(di_nome_variabile WS)										|
    	(di_article WS PRECEDENTE WS nome_variabile WS)				|
    	(di_nome_contatore WS)										|
    	(di_nome_macro_valore argomenti_attuali_in_iterazione? WS?)
    ) DEL WS CAMPO WS nome_campo                                               |
    DEL WS RIPRISTINO WS (
        DELLO WS STATO                                              |
        DI WS nome_variabile WS DEL WS CAMPO WS nome_campo
    )
;

unione_stati : costante_enumerativa ((WS? COMMA WS? costante_enumerativa)* WS O WS costante_enumerativa)? ;

unione_liste :
    nome_lista |
    nome_lista_in_unione ((WS? COMMA WS? nome_lista_in_unione)* WS O WS nome_lista_in_unione)?
;

a_unione_liste :
    a_nome_lista |
    a_nome_lista_in_unione ((WS? COMMA WS? nome_lista_in_unione)* WS O WS nome_lista_in_unione)?
;

da_unione_liste :
    da_nome_lista |
    da_nome_lista_in_unione ((WS? COMMA WS? nome_lista_in_unione)* WS O WS nome_lista_in_unione)?
;

di_unione_liste :
    di_nome_lista |
    di_nome_lista_in_unione ((WS? COMMA WS? nome_lista_in_unione)* WS O WS nome_lista_in_unione)?
;

di_maybe_quantificatore_unione_liste : (di_quantificatore WS unione_liste) | di_unione_liste ;

del_campo_full : (DEL WS CAMPO WS nome_campo WS)? di_maybe_quantificatore_unione_liste ;
campo_full : (CAMPO WS)? nome_campo WS di_maybe_quantificatore_unione_liste ;

quantificatore : OGNI | ALMENO_UN_ | UNICO | L_UNICO ;

di_quantificatore : (DI WS quantificatore) | DELL_UNICO ;

argomenti_formali : WS? LPAR WS? CON WS argomento ((WS? COMMA WS? argomento)* WS E WS argomento)? WS? RPAR WS? ;

argomenti_attuali : WS? LPAR WS? CON WS argomento WS UGUALE WS a_valore ((WS? COMMA WS? argomento WS UGUALE WS a_valore)* WS E WS argomento WS UGUALE WS a_valore)? WS? RPAR WS? ;

argomenti_attuali_in_iterazione : WS? LPAR WS? CON WS argomento WS UGUALE WS a_valore_in_iterazione ((WS? COMMA WS? argomento WS UGUALE WS a_valore_in_iterazione)* WS E WS argomento WS UGUALE WS a_valore_in_iterazione)? WS? RPAR WS? ;

argomento : (ARGOMENTO WS)? nome_argomento ;

costante : costante_booleana | costante_intera | costante_enumerativa ;

costante_booleana : TRUE | FALSE ;

costante_intera : UNSIGNED_INTEGER ;

costante_enumerativa : literal ;

// ------------------------ EFFETTI ----------------------------------------------------------

effetto : effetto_incondizionato | blocco_se_condizione_effetto | effetto_condizionato ;

effetto_incondizionato : comanda_istanza | assegna | chiamata_macro_effetto ;

effetto_condizionato : SE WS condizione_se (WS (E|O) WS (SE WS)? condizione_se)* WS? COMMA WS? effetto_incondizionato (WS? COMMA WS? ALTRIMENTI WS effetto_incondizionato)? ;

comanda_istanza : COMANDA WS ((AL WS CAMPO WS nome_campo WS di_unione_liste) | (a_unione_liste)) WS DI_ESEGUIRE WS (IL WS COMANDO WS (DI WS)?)?
     nome_comando_automatico argomenti_attuali_in_iterazione? (WS? COMMA WS? QUANDO WS
     condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)? ;

assegna :
	ASSEGNA WS AL_COMANDO WS nome_comando_piazzale WS IL_VALORE WS di_valore								|
	ASSEGNA WS ALLA WS VARIABILE WS nome_variabile WS IL_VALORE WS di_valore								|

	(ATTIVA|DISATTIVA) WS (IL WS)? TIMER WS nome_timer													    |
	(INCREMENTA|DECREMENTA|AZZERRA) WS (IL WS)? CONTATORE WS nome_contatore									|
	
	ASSEGNA WS a_nome_variabile WS (DEL WS CAMPO WS nome_campo WS)? di_unione_liste WS IL_VALORE WS (di_valore_in_iterazione | di_valore_da_lista_con_alternativa)
	(WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)?		|
	
	(ATTIVA|DISATTIVA) WS nome_timer WS (DEL WS CAMPO WS nome_campo WS)? di_unione_liste
	(WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)?		|
	
	(INCREMENTA|DECREMENTA|AZZERRA) WS nome_contatore WS (DEL WS CAMPO WS nome_campo WS)? di_unione_liste
	(WS? COMMA WS? QUANDO WS condizione_filtro_iterazione (WS (E|O) WS condizione_filtro_iterazione)*)?     |

	APPLICA WS GLI WS EFFETTI WS DELLA WS PERMANENZA (WS DELLO WS STATO WS nome_stato)?
;

chiamata_macro_effetto : APPLICA WS GLI WS EFFETTI WS DELLA WS MACRO WS nome_macro_effetto argomenti_attuali? ;

blocco_se_condizione_effetto : se_part LBRACE WS?
        (effetto WS?)+
    RBRACE (WS? COMMA? WS? ALTRIMENTI WS? LBRACE WS?
        (effetto WS?)+
    RBRACE)?
;

// ------------------------ MACROS -----------------------------------------------------------

definizione_macro_verifica : MACRO WS DI WS VERIFICA WS nome_macro_valore argomenti_formali? WS? LBRACE WS? body_macro_verifica WS? RBRACE ;

definizione_macro_effetto : MACRO WS DI WS EFFETTO WS nome_macro_effetto argomenti_formali? WS? LBRACE WS? body_macro_effetto WS? RBRACE ;

definizione_macro_valorizzata : MACRO WS VALORIZZATA WS nome_macro_valore WS DI WS TIPO WS nome_tipo argomenti_formali? WS? LBRACE WS? body_macro_valorizzata WS? RBRACE ;

body_macro_verifica : blocco_congiunzione_logica ;

body_macro_effetto : effetti ;

body_macro_valorizzata : (condizione_macro_valorizzata WS?)* assegna_valore_macro ;

condizione_macro_valorizzata : blocco_se_condizione_macro_valorizzata | assegna_valore_macro_condizionato ;

blocco_se_condizione_macro_valorizzata : se_part LBRACE WS? (condizione_macro_valorizzata WS?)+ RBRACE ;

assegna_valore_macro_condizionato : se_part_macro COMMA WS? assegna_valore_macro ;

assegna_valore_macro : ASSEGNA WS ALLA WS MACRO WS IL_VALORE WS valore ;

condizione_se_macro : 
	condizione_attributo_ind_macro |
	condizioni_lista_ind
;
  
condizione_attributo_ind_macro : condizione_attributo_ind_non_macro ;

// ------------------------ HYPOTHETIC (NOT PROVIDED) ----------------------------------------

nome_campo : single_word_id ;

nome_parametro : id ;

nome_controllo : id ;

nome_variabile : id ;

nome_contatore : id ;

nome_macro_valore : id ;

nome_argomento : id ;

nome_lista : id_with_di ;

nome_lista_in_unione : nome_lista WS RICHIESTO_COME WS id ;

nome_tipo : id ;

a_nome_parametro : a_id ;

a_nome_controllo : a_id ;

a_nome_variabile : a_id ;

a_nome_contatore : a_id ;

a_nome_macro_valore : a_id ;

a_nome_argomento : a_id ;

a_nome_lista : a_id_with_di ;

a_nome_lista_in_unione : a_nome_lista WS RICHIESTO_COME WS id ;

a_nome_tipo : a_id ;

da_nome_parametro : da_id ;

da_nome_controllo : da_id ;

da_nome_variabile : da_id ;

da_nome_contatore : da_id ;

da_nome_macro_valore : da_id ;

da_nome_argomento : da_id ;

da_nome_lista : da_id_with_di ;

da_nome_lista_in_unione : da_nome_lista WS RICHIESTO_COME WS id ;

da_nome_tipo : da_id ;

di_nome_parametro : di_id ;

di_nome_controllo : di_id ;

di_nome_variabile : di_id ;

di_nome_contatore : di_id ;

di_nome_macro_valore : di_id ;

di_nome_argomento : di_id ;

di_nome_lista : di_id_with_di ;

di_nome_lista_in_unione : di_nome_lista WS RICHIESTO_COME WS id ;

di_nome_tipo : di_id ;

nome_comando_manuale : id ;

nome_comando_automatico : id ;

nome_timer : id ;

nome_stato : id ;

nome_comando_piazzale : id ;

nome_macro_effetto : id ;

//TODO: What's this???
nome_attributo_booleano : id ;

//TODO: Check if it's correct
literal : id ;

single_word_raw_id : (ID_SINGLE | keyword_or_id) ;

raw_id : (ID_SINGLE | keyword_or_id) (WS (ID_SINGLE | keyword_or_id))* ;

raw_id_with_di : (ID_SINGLE | keyword_or_id) (WS (ID_SINGLE | keyword_or_id_or_di))* ;

single_word_id : (article WS)? single_word_raw_id ;

id : (article WS)? raw_id ;

a_id : ((a_article WS) | ALL_) raw_id ;

da_id : ((da_article WS) | DALL_) raw_id ;

di_id : ((di_article WS) | DELL_) raw_id ;

id_with_di : (article WS)? raw_id_with_di ;

a_id_with_di : ((a_article WS) | ALL_) raw_id_with_di ;

da_id_with_di : ((da_article WS) | DALL_) raw_id_with_di ;

di_id_with_di : ((di_article WS) | DELL_) raw_id_with_di ;

article : 		IL 	| LA 	| LO 	| I 	| LE 	| GLI 			;
a_article : 	A	| 	AL 	| ALLA 	| ALLO 	| AI 	| ALLE 	| AGLI 	;
da_article :	DA 	| 	DAL | DALLA | DALLO | DAI	| DALLE	| DAGLI	;
di_article :	DI 	| 	DEL | DELLA | DELLO | DEI	| DELLE | DEGLI	;

keyword_or_id : NESSUNO | TUTTE | SEGUENTI | ALMENO | UN | UNA | UNO | SOLO | RICEZIONE | COMANDO | MANUALE | VERIFICA | VALORE |
	article | a_article | da_article ; // "di" can be found between two identifiers, so it's better to exclude from the available tokens

keyword_or_id_or_di : keyword_or_id | di_article ;



NESSUNO : 'nessuno' | 'nessuna' ;

TUTTE : 'Tutte' ;

LE : 'le' ;

GLI : 'gli' ;

SEGUENTI : 'seguenti' ;

ALMENO : 'Almeno' ;

UN : 'un' ;

UNA : 'una' ;

UNO : 'uno' ;

SOLO : 'Solo' ;

RICEZIONE : 'Ricezione' ;

COMANDO : 'comando' ;

MANUALE : 'manuale' ;

VERIFICA : 'Verifica' ;

VALORE : 'valore' ;

VALORIZZATA : 'valorizzata' ;

TIPO : 'tipo' ;

EFFETTO : 'effetto' ;


TUTTE_LE_SEGUENTI : TUTTE WS LE WS SEGUENTI ;

ALMENO_UNA_DELLE_SEGUENTI : ALMENO WS UNA WS DELLE WS SEGUENTI ;

SOLO_UNA_DELLE_SEGUENTI : SOLO WS UNA WS DELLE WS SEGUENTI ;

RICEZIONE_DEL_COMANDO : RICEZIONE WS (DEL WS)? COMANDO ;

RICEZIONE_DEL_COMANDO_MANUALE : RICEZIONE_DEL_COMANDO WS MANUALE ;

PRECEDENTE : 'precedente' ;

PARAMETRO : 'parametro' ;

CONTROLLO : 'controllo' ;

VARIABILE : 'variabile' ;

TIMER : 'timer' ;

CONTATORE : 'contatore' ;

COSTANTE : 'costante' ;

ARGOMENTO : 'argomento' ;

L_ARGOMENTO : 'l\'argomento' ;

ALL_ARGOMENTO : 'all\'argomento' ;

DELL_ARGOMENTO : 'dell\'argomento' ;

DALL_ARGOMENTO : 'dall\'argomento' ;

MACRO : 'macro' ;

CAMPO : 'campo' ;

CONDIZIONE : 'condizione' ;

PERMANENZA : 'permanenza' ;

AL_COMANDO : AL WS COMANDO ;

IL_VALORE : IL WS VALORE ;

RICHIESTO_COME : 'richiesto' WS 'come' ;

COMANDA : 'comanda' ;

DI_ESEGUIRE : 'di' WS 'eseguire' ;

ASSEGNA : 'assegna' ;

APPLICA : 'Applica' ;

EFFETTI : 'effetti' ;



DISATTIVO : 'disattivo' ;

ATTIVO : 'attivo' ;

SCADUTO : 'scaduto' ;

UGUALE : 'uguale' ;

DIVERSO : 'diverso' | 'diversa' ;

MAGGIORE : 'maggiore' ;

MINORE : 'minore' ;

STATO : 'stato' ;

NELLO_STATO : 'nello' WS STATO ;

RIPRISTINO : 'ripristino' ;

ESITO : 'esito' ;

L_ESITO : 'l\'esito' ;

QUANDO : 'quando' ;

ALTRIMENTI : 'altrimenti' ;

OGNI : 'ogni' ;

ALMENO_UN_ : ALMENO WS (UN | UNA | UNO) ;

UNICO : 'unico' ;

L_UNICO : 'l\'unico' ;

DELL_UNICO : 'dell\'unico' ;

TRUE : 'true' ;

FALSE : 'false' ;

ATTIVA : 'attiva' ;

DISATTIVA : 'disattiva' ;

INCREMENTA : 'incrementa' ;

DECREMENTA : 'decrementa' ;

AZZERRA : 'azzerra' ;



CHE : 'che' ;

SE : 'se' ;

E : 'e' | 'ed' ;

O : 'o' | 'od' | 'oppure' ;

CON : 'con' ;

IL : 'il' ;

LA : 'la' ;

LO : 'lo' ;

I : 'i' ;

A : 'a' ;

ALLA : 'alla' ;

ALLO : 'allo' ;

AI : 'ai' ;

ALLE : 'alle' ;

AGLI : 'agli' ;

AL : 'al' ;

DI : 'di' ;

DELLA : 'della' ;

DELLE : 'delle' ;

DELLO : 'dello' ;

DEL : 'del' ;

DEI : 'dei' ;

DEGLI : 'degli' ;

DA : 'da' ;

DALLA : 'dalla';

DAL : 'dal' ;

DALLO : 'dallo' ;

DAI : 'dai' ;

DALLE : 'dalle' ;

DAGLI : 'dagli' ;

ALL_ : 'all\'' ;

DELL_ : 'dell\'' ;

DALL_ : 'dall\'' ;

NON : 'non' ;

VERBO_E : 'è' | 'e\'' ;

SIA : 'sia' ;

ESISTE : 'esiste' ;

ESISTA : 'esista' ;

COMMA : ',' ;

ID_SINGLE : [àèéòìa-z_-] [0-9àèéòìa-z_/'-]* ;

UNSIGNED_INTEGER : [0-9]+ ;

WS : CM? PURE_WS CM? ;

PURE_WS : [ \t\r\n]+ ; //-> skip ; // skip spaces, tabs, newlines

LPAR : '(' ;

RPAR : ')' ;

LBRACE : '{' ;

RBRACE : '}' ;

CM : ((COMMENT_LINE | COMMENT_BLOCK) PURE_WS?)+ ;

COMMENT_LINE : '//' ~('\n')* ;

COMMENT_BLOCK : '/*' (~'*' | '*' ~'/')* '*/' ;
