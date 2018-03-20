package dlolaExprTree;

import main.Debug;

public enum Operator {
	EQUIV, IMPR, IMPL, OR, AND, NEG, EQ, NEQ, LT, LEQ, GEQ, GT, ADD, SUB, MUL, DIV, MOD, EXP;

	public static Operator fromString(String str) {
		switch (str) {
		case "+":
			return ADD;
		case "&":
			return AND;
		case "/":
			return DIV;
		case "=":
			return EQ;
		case "<->":
			return EQUIV;
		case "^":
			return EXP;
		case ">=":
			return GEQ;
		case ">":
			return GT;
		case "<-":
			return IMPL;
		case "->":
			return IMPR;
		case "<=":
			return LEQ;
		case "<":
			return LT;
		case "%":
			return MOD;
		case "*":
			return MUL;
		case "!":
			return NEG;
		case "!=":
			return NEQ;
		case "|":
			return OR;
		case "-":
			return SUB;
		default:
			try {
				throw new TypeException("String '"+str + "' does not represent an operator");
			} catch (TypeException e) {
				e.printStackTrace();
				Debug.abort();
				return null;
			}
		}
	}

	public String toCode() {
		if (this == NEG)
			return toString();
		return " " + toString() + " ";
	}

	@Override
	public String toString() {
		switch (this) {
		case ADD:
			return "+";
		case AND:
			return "&";
		case DIV:
			return "/";
		case EQ:
			return "=";
		case EQUIV:
			return "<->";
		case EXP:
			return "^";
		case GEQ:
			return ">=";
		case GT:
			return ">";
		case IMPL:
			return "<-";
		case IMPR:
			return "->";
		case LEQ:
			return "<=";
		case LT:
			return "<";
		case MOD:
			return "%";
		case MUL:
			return "*";
		case NEG:
			return "!";
		case NEQ:
			return "!=";
		case OR:
			return "|";
		case SUB:
			return "-";
		}
		return null;
	}

	// equivOp : '<->' ;
	//
	// impOp: '->' | '<-' ;
	//
	// orOp : '|' ;
	//
	// andOp : '&' ;
	//
	// negOp: '!' ;
	//
	// equOp : '=' | '!=' ;
	//
	// compOp : '<' | '<=' |'>=' | '>' ;
	//
	// addOp : '+' ;
	//
	// subOp : '-' ;
	//
	// multOp : '*' | '/' | '%' ;
	//
	// expOp : '^' ;
}
