package semanticAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import main.DLolaRunner;
import main.Debug;
import parser.DLolaParser;

public class DefinitionList {

	List<ParseTree> constantList = new ArrayList<ParseTree>();
	List<ParseTree> virtualList = new ArrayList<ParseTree>();
	List<ParseTree> inputList = new ArrayList<ParseTree>();
	List<ParseTree> outputList = new ArrayList<ParseTree>();
	List<ParseTree> triggerList = new ArrayList<ParseTree>();
	List<ParseTree> nodeList = new ArrayList<ParseTree>();
	List<ParseTree> channelList = new ArrayList<ParseTree>();

	public DefinitionList(ParseTree tree) {
		Debug.out(13, "Generating Definition List");
		findDefinitions(tree);
		Debug.out(13, "Definition List generated");
	}
	
	public void findDefinitions(ParseTree tree) {
		List<String> ruleNames = Arrays.asList(DLolaRunner.parser.getRuleNames());
		String nodeType = Utils.escapeWhitespace(Trees.getNodeText(tree, ruleNames), false);

		switch (nodeType) {
		case "constantDef":
			constantList.add(tree);
			break;
		case "virtualDef":
			virtualList.add(tree);
			break;
		case "inputDef":
			inputList.add(tree);
			break;
		case "outputDef":
			outputList.add(tree);
			break;
		case "triggerDef":
			triggerList.add(tree);
			break;
		case "nodeDef":
			nodeList.add(tree);
			for (int i = 0; i < tree.getChildCount(); i++) {
				findDefinitions(tree.getChild(i));
			}
			break;
		case "channelDef":
			channelList.add(tree);
			break;
		default:
			for (int i = 0; i < tree.getChildCount(); i++) {
				findDefinitions(tree.getChild(i));
			}
			return;
		}
	}


	public List<ParseTree> getConstantList() {
		return constantList;
	}

	public List<ParseTree> getVirtualList() {
		return virtualList;
	}

	public List<ParseTree> getInputList() {
		return inputList;
	}

	public List<ParseTree> getOutputList() {
		return outputList;
	}

	public List<ParseTree> getTriggerList() {
		return triggerList;
	}

	public List<ParseTree> getNodeList() {
		return nodeList;
	}

	public List<ParseTree> getChannelList() {
		return channelList;
	}
}
