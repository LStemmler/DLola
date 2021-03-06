/**
 * Define the DLola grammar
 */
 
grammar DLola;

@header {
    package parser;
}

dlolaFormat  : dlolaDef* ;

dlolaDef : streamDef | nodeDef | channelDef ;

streamDef : constantDef | virtualDef ;


constantDef : 'const' type identifier DEFINE literal ;

virtualDef : 'virtual' type identifier DEFINE expression ;

nodeDef : 'node' identifier LBRACE nodeSpec RBRACE ;

nodeSpec : nodeStream* ;

nodeStream : inputDef | outputDef | triggerDef ;

inputDef : 'input' type identifier (COMMA identifier)* ;

outputDef : ('output' | 'uoutput') type identifier DEFINE expression ;

triggerDef : 'trigger' identifier (COMMA identifier)* ;

channelDef : ('channel' | 'dchannel' | 'uchannel' | 'udchannel') identifier integer? integer identifier identifier identifier*;

type : 'int' | 'bool' ;


literal : 'true' | 'false' | integer ;


expression : LPAREN expression RPAREN | restrExpr | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr | negExpr | andExpr | orExpr | impExpr | equivExpr;


equivExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | comparExpr | equExpr | negExpr | andExpr | orExpr | impExpr) (equivOp equivExpr)? ;

impExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | comparExpr | equExpr | negExpr | andExpr | orExpr) (impOp impExpr)? ;

orExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | comparExpr | equExpr | negExpr | andExpr) (orOp orExpr)?;

andExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | comparExpr | equExpr | negExpr) (andOp andExpr)? ;

negExpr : (negOp)? (literal | identifier | ifExpr | shiftExpr | restrExpr | comparExpr | equExpr) ;

equExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | expExpr | multExpr | subExpr | addExpr) (equOp addExpr)? ;

comparExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | expExpr | multExpr | subExpr | addExpr) (compOp addExpr)? ;

addExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | expExpr | subExpr | multExpr | subExpr) (addOp addExpr)? ;

subExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | expExpr | multExpr) (subOp subExpr)? ;

multExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr | expExpr) (multOp multExpr)? ;

expExpr : (literal | identifier | ifExpr | shiftExpr | restrExpr) (expOp expExpr)? ;

restrExpr : literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN ;

equivOp : '<->' ;

impOp: '->' | '<-' ;

orOp :  '|' ;

andOp : '&' ;

negOp: '!' ;

equOp : '=' | '!=' ;

compOp : '<' | '<=' |'>=' | '>' ;

addOp : '+' ;

subOp : '-' ;

multOp : '*' | '/' | '%' ;

expOp :  '^' ;


ifExpr : 'if' expression LBRACE expression RBRACE elseExpr ;

elseExpr : 'elif' expression LBRACE expression RBRACE elseExpr | 'else' LBRACE expression RBRACE ;

shiftExpr : identifier LBRACK integer COMMA (literal | identifier) RBRACK ;

identifier : IDENTIFIER ;

integer : INTEGER ;

IDENTIFIER : ([a-z] | [A-Z]) ([a-z] | [A-Z] | [0-9] | '_')* ;

INTEGER : ('-')? [0-9]+ ;

LPAREN : '(' ;

RPAREN : ')' ;

LBRACE : '{' ;

RBRACE : '}' ;

LBRACK : '[' ;

RBRACK : ']' ;

DEFINE : ':=' ;

COMMA  : ',' ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

