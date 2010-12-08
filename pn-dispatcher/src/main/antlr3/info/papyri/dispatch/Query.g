grammar Query;

@header {
package info.papyri.dispatch;

import java.util.List;
import java.util.ArrayList;
}

@lexer::header {
package info.papyri.dispatch;
}

@members {
List<String> find = new ArrayList<String>();
public List<String> getStrings() {
  return find;
}
}
query 	: 	(querypart | EOF) ;
querypart
	:	clause (WS|clause)*;
queryterm
	:	(TERM|PHRASE) {find.add($queryterm.text);};
clause  :	('+'|'-')? FIELD? (queryterm | '(' querypart ')') ;

WS	:	(' '|'\r'|'\t'|'\n');
COLON	:	':';
QUOTE	:	'"';
OPERATOR:	('AND'|'OR'|'NOT'|'TO') { $channel=HIDDEN; };
TERM 	:	~(WS|COLON|QUOTE|'('|')'|'['|']')+;
DATEFIELD
	:	'date_' TERM COLON '[' (TERM|WS)+ ']' { $channel=HIDDEN; };
IDENTIFIERFIELD
  	: 	'identifier:http\\' COLON TERM { $channel=HIDDEN; };
FIELD	:	TERM COLON;
PHRASE	:	QUOTE TERM WS (TERM|WS)* QUOTE;

