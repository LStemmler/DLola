package routeGeneration;

import java.util.ArrayList;
import java.util.HashSet;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import dlolaExprTree.DLolaType;
import dlolaObject.DLolaExpTreeObject;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import dlolaObject.Output;
import dlolaObject.Virtual;
import main.Debug;
import main.Global;
import semanticAnalysis.UntypedException;

public class SubexpressionTree {

	/*
	 * SubexpressionTree
	 * 
	 * Every ParseTree section that does not split into multiple subtrees requiring
	 * inputs or pass an identifier is considered equivalent and represented by a
	 * single SubexpressionTree. The head is the highest such node in the ParseTree,
	 * and the OptTree the node with the lowest bandwidth requirement. If
	 * optSubBandwidth < bandwidth, the bandwidth can be further reduced through
	 * additional splits.
	 * 
	 */

	HashSet<Input> reqInputList;
	HashSet<ParseTree> representedNodes;
	ParseTree head;
	ParseTree optTree;
	int bandwidth = 1000000; // Minimum bandwidth of this stage (without split, of optTree)
	int optSubBandwith = 1000000; // Minimum bandwidth after splits (if <bandwidth, optTree is probably not
								  // relevant). High magic number so that it is not undefined, but always replaced
	boolean recursive = false; // Can reach itself as a (past) subexpression
	boolean input = false; // Represents an input
	boolean complete = false; // Represents an input
	int timeShift = 0; // The time shift of the split children relative to the head. Only relevant for shiftExpressions
	ArrayList<SubexpressionTree> splitList;

	public static void generateSubexpressionTrees() {
		SubexpressionTree subextree;
		for (Output out : Global.symtable.getOutputList()) {
			// generating subextrees
			subexpressionTreeRec(out.getExpTree(), true);
			subextree = Global.systemModel.getSubexpTree(out.getExpTree());
			subextree.updateChildData(new HashSet<>());
		}
		if (Global.debugVerbosity > 16) {
			for (Output out : Global.symtable.getOutputList()) {
				Debug.out(17, "SubexpressionTree for Output " + out.getIdentifier() + ":");
				printSubgraphTree(Global.systemModel.getSubexpTree(out.getExpTree()), new HashSet<>(), 0);
			}
		}
	}

	private void updateChildData(HashSet<SubexpressionTree> visited) {
		if (!complete) {
			if (!visited.contains(this)) {
				for (int i = 0; i < splitList.size(); i++) {
					// Ensures all split children point to the final SubexpressionTree
					ParseTree parseTree = splitList.get(i).getHead();
					if (parseTree != null) {
						SubexpressionTree finalTree = Global.systemModel.getSubexpTree(parseTree);
						splitList.set(i, finalTree);
					}
				}
				visited.add(this);
				for (SubexpressionTree child : splitList) {
					child.updateChildData(visited);
				}
				visited.remove(this);
				complete = true;
			} else {
				if (recursive) {
					complete = true;
					return;
				}
				recursive = true;
				for (SubexpressionTree child : splitList) {
					child.updateChildData(visited);
				}
			}
		}
		

//		try {
//			Debug.out(16, "Split List for SubexpTree " + head.getText() + " / " + optTree.getText() +" :");
//			printSubtree(head);
//		} catch (NullPointerException e) {
//			Debug.out(17, "NULL");
//		}
//	}
//	
//	void printSubtree (ParseTree pt) {
//		try {
//			System.out.println(pt.getText());
//			for (int i=0; i<pt.getChildCount(); i++) {
//				printSubtree(pt.getChild(i));
//			}
//		} catch (NullPointerException e) {
//			Debug.out(17, "NULL");
//		}
	}
	
	

	private SubexpressionTree() {
		reqInputList = new HashSet<>();
		representedNodes = new HashSet<>();
		splitList = new ArrayList<>();
		// Empty SubexpressionTree for recursive generation
	}

	public static SubexpressionTree InputSubexpressionTree(Input input) {
		SubexpressionTree SET = new SubexpressionTree();
		SET.reqInputList.add(input);
		SET.bandwidth = input.getType().size();
		SET.optSubBandwith = input.getType().size();
		SET.representedNodes = new HashSet<>();
		SET.input = true;
		SET.complete = true;
		return SET;
	}

	public static SubexpressionTree subexpressionTreeRec(ParseTree tree, Boolean forceRelevant) {
		SubexpressionTree newsub = Global.systemModel.getSubexpTree(tree);
		if (newsub != null) {
			return newsub;
		}
		newsub = new SubexpressionTree();

		String nodeType = Utils.escapeWhitespace(Trees.getNodeText(tree, Global.ruleNames), false);

		int size = 0;

		if (nodeType.equals("identifier") || nodeType.equals("shiftExpr")) {
			String targetId;
			if (nodeType.equals("identifier")) {
				targetId = tree.getChild(0).getText();
			} else {
				targetId = tree.getChild(0).getChild(0).getText();
				newsub.timeShift = Integer.valueOf(tree.getChild(2).getChild(0).getText());
			}
			DLolaObject target = Global.symtable.getObject(targetId);
			SubexpressionTree expsub;
			register(tree, newsub);
			if (target instanceof Input) {
				expsub = ((Input) target).getInputSubexp();
				newsub.bandwidth = expsub.bandwidth;
			} else {
				DLolaExpTreeObject targetExp = (DLolaExpTreeObject) target;
				expsub = subexpressionTreeRec(targetExp.getExpTree(), true);
				try {
					newsub.bandwidth = Math.min(ParseTreeTools.exprType(targetExp.getExpTree()).size(), expsub.bandwidth);
				} catch (UntypedException e) {
					Debug.err(3, "There shouldn't be typeless expressions referred to by identifier");
					Debug.abort();
				}
			}
			newsub.head = tree;
			newsub.optSubBandwith = Math.min(expsub.bandwidth, expsub.optSubBandwith);
			newsub.optTree = tree;
			newsub.reqInputList = expsub.reqInputList;
			newsub.splitList.add(expsub);

			return newsub;
		}

		ArrayList<SubexpressionTree> relevantChildren = new ArrayList<>();
		for (int i = 0; i < tree.getChildCount(); i++) {
			SubexpressionTree childsub = subexpressionTreeRec(tree.getChild(i), false);
			if (childsub != null) {
				relevantChildren.add(childsub);
			}
		}

		if (relevantChildren.size() == 0 && !forceRelevant) {
			// No relevant children means this branch is not relevant
			return null;
		} else if (relevantChildren.size() == 0 && forceRelevant) {
			// Relevant, but no size
			newsub.head = tree;
			newsub.bandwidth = 0;
			newsub.optTree = tree;
			newsub.optSubBandwith = 0;
			register(tree, newsub);
		} else if (relevantChildren.size() == 1) {
			// Same section of the SubexpressionTree, merge
			newsub = relevantChildren.get(0);
			newsub.head = tree;

			try {
				size = ParseTreeTools.exprType(tree).size();
			} catch (UntypedException e) {
				Debug.err(3, "There shouldn't be typeless expressions with relevant children");
				Debug.abort();
			}
			newsub.trySetBandwidth(size, tree);
			register(tree, newsub);
		} else {
			// >1 children, Split

			try {
				size = ParseTreeTools.exprType(tree).size();
			} catch (UntypedException e) {
				Debug.err(3, "There shouldn't be typeless expressions with relevant children");
				Debug.abort();
			}
			newsub.bandwidth = size;
			newsub.head = tree;
			newsub.optTree = tree;
			register(tree, newsub);
			newsub.optSubBandwith = 0;

			for (SubexpressionTree child : relevantChildren) {
				newsub.optSubBandwith += Math.min(child.optSubBandwith, child.bandwidth);
				newsub.reqInputList.addAll(child.getRequiredInputs());
				newsub.splitList.add(child);
			}
		}
		return newsub;
	}

	public ParseTree getOptTree() {
		return optTree;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void trySetBandwidth(int bandwidth, ParseTree optTree) {
		if (this.bandwidth > bandwidth) {
			this.bandwidth = bandwidth;
			this.optTree = optTree;
		}
	}

	public int getOptSubBandwidth() {
		return optSubBandwith;
	}

	public void trySetOptSubBandwith(int optSubBandwith) {
		if (this.optSubBandwith > optSubBandwith) {
			this.optSubBandwith = optSubBandwith;
		}
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecurrent(boolean recurrent) {
		this.recursive = recurrent;
	}

	public HashSet<Input> getRequiredInputs() {
		return reqInputList;
	}

	public ParseTree getHead() {
		return head;
	}

	public ArrayList<SubexpressionTree> getSplitList() {
		return splitList;
	}

	public boolean hasOptimalBandwidth() {
		return bandwidth <= optSubBandwith;
	}

	public static void replace(SubexpressionTree oldtree, SubexpressionTree newtree) {
		for (ParseTree tree : oldtree.representedNodes) {
			Global.systemModel.setSubexpTree(tree, newtree);
		}
	}

	public static void register(ParseTree tree, SubexpressionTree subextree) {
		Global.systemModel.setSubexpTree(tree, subextree);
		subextree.representedNodes.add(tree);
	}

	public static void unregister(ParseTree tree) {
		SubexpressionTree oldtree = Global.systemModel.getSubexpTree(tree);
		if (oldtree != null) {
			oldtree.representedNodes.remove(tree);
		}
		Global.systemModel.unsetSubexpTree(tree);
	}

	public static void printSubgraphTree(SubexpressionTree subext, HashSet<SubexpressionTree> visited, int indent) {
		String offset = new String();
		for (int i = 0; i < indent; i++) {
			offset += "  ";
		}

		if (visited.contains(subext)) {

			Debug.out(17, offset + "revisit " + subext.getHead().getText());
			Debug.out(17, offset + "bandwidth " + subext.getBandwidth() + " at " + subext.getOptTree().getText());
			Debug.out(17, offset + "optSubBandwidth " + subext.getOptSubBandwidth());
		} else {
			visited.add(subext);
			String optTreeStr = "";
			if (subext.getHead() != null) {
				Debug.out(17, offset + "visit " + subext.getHead().getText());
				optTreeStr = " at " + subext.getOptTree().getText();
			}
			Debug.out(17, offset + "bandwidth " + subext.getBandwidth() + optTreeStr);
			Debug.out(17, offset + "optSubBandwidth " + subext.getOptSubBandwidth());
			for (SubexpressionTree child : subext.getSplitList()) {
				printSubgraphTree(child, visited, indent + 1);
			}
			visited.remove(subext);
		}
	}

	public boolean equivalent(SubexpressionTree cmp) {
		return ParseTreeTools.equals(this.head, cmp.head);
	}
	
}
