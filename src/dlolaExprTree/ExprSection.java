package dlolaExprTree;

import java.util.ArrayList;

import dlolaObject.Input;
import main.Global;

public class ExprSection {

	ArrayList<DLolaExpr> representedRelevantExpressions = new ArrayList<>();
	DLolaExpr head;
	DLolaExpr opt;
	int optSize;
	int optSubSize;
	int timeShift;
	ArrayList<ExprSection> parents = new ArrayList<ExprSection>();
	public ArrayList<DLolaExpr> getRepresentedRelevantExpressions() {
		return representedRelevantExpressions;
	}

	public DLolaExpr getHead() {
		return head;
	}

	public DLolaExpr getOpt() {
		return opt;
	}

	public int getOptSize() {
		return optSize;
	}

	public int getOptSubSize() {
		return optSubSize;
	}

	public ArrayList<ExprSection> getParents() {
		return parents;
	}

	public ArrayList<ExprSection> getChildren() {
		return children;
	}

	void calculateTimeShift() {
		timeShift = 0;
		for (DLolaExpr expr: representedRelevantExpressions) {
			if (expr instanceof ShiftExpr) {
				timeShift +=((ShiftExpr) expr).timeShift;
			}
		}
		if (getRequiredInputs().size() == 0) timeShift = Global.STAT_DELAY;
	}

	public int getTimeShift() {
		return timeShift;
	}
	
	ArrayList<ExprSection> children = new ArrayList<ExprSection>();

	public static ExprSection createExprSection(DLolaExpr head) {
		ExprSection existing = ExpressionMap.getExprSection(head);
		if (existing != null) {
			if (existing.head == head) {
				return existing;
			} else {
				return existing.splitAt(head);
			}
		}
		return new ExprSection(head, null);
	}

	public boolean isRecursive () {
		return head.recursive;
	}

	private ExprSection(DLolaExpr head, ExprSection replace) {
		this.head = head;
		expandDownwards(head, replace);
		findOptimum();
	}

	private void findOptimum() {
		optSize = Integer.MAX_VALUE; 
		optSubSize = 0; 
		for (DLolaExpr represented: representedRelevantExpressions) {
			if (optSize > represented.getBandwidth()) {
				opt = represented;
				optSize = represented.getBandwidth();
			}
		}
		optSubSize = representedRelevantExpressions.get(representedRelevantExpressions.size()-1).optSubBandwidth;
	}
	
	public boolean shouldSplit() {
		return optSize > optSubSize;
	}


	void expandDownwards(DLolaExpr current, ExprSection replace) {
		ExprSection existing = ExpressionMap.getExprSection(current);
		if (existing != replace) {
			if (existing.head == current) {
				registerChild(existing);
				existing.registerParent(this);
				return;
			} else {
				existing.splitAt(current);
				existing = ExpressionMap.getExprSection(current);
				registerChild(existing);
				existing.registerParent(this);
				return;
			}
		} else {
			ExpressionMap.putExprSection(current, this);
			representedRelevantExpressions.add(current);
			
			ArrayList<DLolaExpr> relevantSubexpressions = new ArrayList<>();
			for (DLolaExpr subex: current.subexpressions) {
				if (subex.getMinBandwidth()>0) relevantSubexpressions.add(subex);
			}
			if (relevantSubexpressions.size() > 1) {
				for (DLolaExpr subex: relevantSubexpressions) {
					this.children.add(createExprSection(subex));
				}
			} else if (relevantSubexpressions.size() == 1) {
				expandDownwards(relevantSubexpressions.get(0), replace);
			}
		}
	}

	private ExprSection splitAt(DLolaExpr head) {
		
		ExprSection newSection = new ExprSection(head, this);
		for (int i=children.size()-1; i>=0; i--) {
			children.get(i).unregisterParent(this);
			unregisterChild(children.get(i));
		}
		this.registerChild(newSection);
		newSection.registerParent(this);
		representedRelevantExpressions.removeAll(newSection.representedRelevantExpressions);
		findOptimum();
		return newSection;
	}

	private boolean registerParent(ExprSection parent) {
		if (this.parents.contains(parent)) return false;
		this.parents.add(parent);
		return true;
	}

	private boolean registerChild(ExprSection child) {
		if (this.children.contains(child)) return false;
		this.children.add(child);
		return true;
	}
	
	private boolean unregisterParent(ExprSection parent) {
		if (!this.parents.contains(parent)) return false;
		this.parents.remove(parent);
		return true;
	}
	private boolean unregisterChild(ExprSection child) {
		if (!this.children.contains(child)) return false;
		this.children.remove(child);
		return true;
	}
	
	public String toString() {
		String retstring = "ExprSection for " +head.toString() +": " + representedRelevantExpressions.get(0).toString();
		for (int i=1; i<representedRelevantExpressions.size(); i++) {
			retstring += ", "+ representedRelevantExpressions.get(i).toString();
		}
		retstring += " (TimeShift "+ timeShift+ ", Bandwidth "+optSize +" at "+opt.toString() +", optSubSize "+ optSubSize +", children: ";
		for (int i=0; i<children.size(); i++) {
			retstring += children.get(i).head.toString();
			if (i <children.size()-1) {
				retstring += ", ";
			}
		}
		retstring += ")";
		return retstring;
	}

	public ArrayList<Input> getRequiredInputs() {
		return head.requiredInputs;
	}
}
