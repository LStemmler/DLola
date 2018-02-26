/**
 * Define a grammar called DLola
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

outputDef : 'output' type identifier DEFINE expression ;

triggerDef : 'trigger' identifier (COMMA identifier)* ;

channelDef : 'channel' identifier integer? integer identifier identifier | 'dchannel' identifier integer? integer identifier identifier;


type : 'int' | 'bool' ;


literal : 'true' | 'false' | integer ;


expression : LPAREN expression RPAREN | restrExpr | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr | negExpr | andExpr | orExpr | impExpr | equivExpr;


equivExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr | negExpr | andExpr | orExpr | impExpr) (equivOp equivExpr)? ;

impExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr | negExpr | andExpr | orExpr) (impOp impExpr)? ;

orExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr | negExpr | andExpr) (orOp orExpr)?;

andExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr | negExpr) (andOp andExpr)? ;

negExpr : (negOp)? (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr | comparExpr | equExpr) ;

equExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr | comparExpr) (equOp equExpr)? ;

comparExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr | subExpr | addExpr) (compOp comparExpr)? ;

addExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | subExpr | multExpr | subExpr) (addOp addExpr)? ;

subExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr | multExpr) (subOp subExpr)? ;

multExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN | expExpr) (multOp multExpr)? ;

expExpr : (literal | identifier | ifExpr | shiftExpr | LPAREN expression RPAREN) (expOp expExpr)? ;

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

elseExpr : ('elif' expression LBRACE expression RBRACE elseExpr)? 'else' LBRACE expression RBRACE ;

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

