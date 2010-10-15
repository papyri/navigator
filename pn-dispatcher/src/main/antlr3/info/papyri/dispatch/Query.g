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
query 	: 	querypart;
querypart
	:	clause (WS|clause)*;
clause  :	('+'|'-')? FIELD? ((TERM|PHRASE) | '(' querypart ')') {find.add($clause.text);};

WS	:	(' '|'\r'|'\t'|'\n');
COLON	:	':';
QUOTE	:	'"';
TERM 	:	~(WS|COLON|QUOTE|'('|')')+;
FIELD	:	TERM COLON;
PHRASE	:	QUOTE TERM WS (TERM|WS)* QUOTE;

