package parser;

import org.antlr.v4.runtime.tree.ParseTree;

import org.antlr.v4.runtime.tree.Trees;

import main.DLolaRunner;
import main.Debug;

public abstract class DLolaTree {

	public static boolean equals(ParseTree cmp1, ParseTree cmp2) {
		if (cmp1.getChildCount() == cmp2.getChildCount()) {
			if (cmp1.getChildCount() > 0) {
				for (int i = 0; i < cmp1.getChildCount(); i++) {
					Debug.out(20, cmp1.getChild(i) + ", "+ cmp2.getChild(i));
					if (!equals(cmp1.getChild(i), cmp2.getChild(i))) {
						Debug.out(19, cmp1.getChild(i) +" not equal to "+ cmp2.getChild(i));
						return false;
					}
				}
			}
			if (Trees.getNodeText(cmp1, DLolaRunner.ruleNames)
					.equals(Trees.getNodeText(cmp2, DLolaRunner.ruleNames))) {
				return true;
			}
			Debug.out(19, "Unequal NodeTexts: "+Trees.getNodeText(cmp1, DLolaRunner.ruleNames)+ ", "+Trees.getNodeText(cmp2, DLolaRunner.ruleNames));
			return false;
		}
		Debug.out(19, "Unequal number of children");
		return false;
	}
}
