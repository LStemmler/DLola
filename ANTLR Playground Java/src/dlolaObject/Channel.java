package dlolaObject;

import java.text.ParseException;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import org.graphstream.graph.Edge;

import main.DLolaRunner;
import semanticAnalysis.SymbolTable;
public class Channel extends DLolaObject {

	
	boolean bandwidth_infinite = false;
	int bandwidth;
	int delay;
	boolean directed = false;		//Only from A to B?
	Node A;
	Node B;
	Edge channel;
	
	
	public Channel(ParseTree channelDef, SymbolTable symbolTable) throws ParseException {

		super(channelDef, symbolTable);
		
		if (Trees.getNodeText(channelDef.getChild(0), DLolaRunner.ruleNames).equals("dchannel")) {
			directed = true;
		}
		
		//Bandwidth attribute present?
		int i=2;
		if (Trees.getNodeText(channelDef.getChild(3), DLolaRunner.ruleNames).equals("integer")) {
			//yes
			bandwidth = Integer.parseInt(channelDef.getChild(i++).getChild(0).getText());
		} else {
			bandwidth_infinite = true;
		}
		delay = Integer.parseInt(channelDef.getChild(i++).getChild(0).getText());
		A = (Node) symbolTable.getObject(channelDef.getChild(i++).getChild(0).getText());
		B = (Node) symbolTable.getObject(channelDef.getChild(i++).getChild(0).getText());
		
		ensure(A != null && B != null, "Channel between nonexisting nodes");

		channel = DLolaRunner.systemModel.getNetworkGraph().addEdge(identifier, A.getIdentifier(), B.getIdentifier(), directed);
	}


	public boolean isBandwidth_infinite() {
		return bandwidth_infinite;
	}


	public int getBandwidth() {
		return bandwidth;
	}


	public int getDelay() {
		return delay;
	}


	public boolean isDirected() {
		return directed;
	}


	public Node getA() {
		return A;
	}


	public Node getB() {
		return B;
	}


	public Edge getChannel() {
		return channel;
	}

}
