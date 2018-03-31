package dlolaObject;

import java.text.ParseException;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import org.graphstream.graph.Edge;

import main.Debug;
import main.Global;
import semanticAnalysis.SymbolTable;
public class Channel extends DLolaObject {

	
	boolean bandwidth_infinite = false;
	int bandwidth;
	int delay;
	boolean directed = false;		//Only from first node to others
	boolean reliable = true;		//Only from first node to others
	ArrayList<Node> nodes = new ArrayList<Node>();
	ArrayList<Edge> networkChannels = new ArrayList<Edge>();
	
	
	public Channel(ParseTree channelDef, SymbolTable symbolTable) throws ParseException {

		super(channelDef, symbolTable);
		
		String channelType = Trees.getNodeText(channelDef.getChild(0), Global.ruleNames);
		switch (channelType) {
		case "udchannel": directed = true;
		case "uchannel": reliable = false;
						 break;
		case "dchannel": directed = true;
		case "channel":
		default:
		}
		
		//Bandwidth attribute present?
		int i=2;
		if (Trees.getNodeText(channelDef.getChild(3), Global.ruleNames).equals("integer")) {
			//yes
			bandwidth = Integer.parseInt(channelDef.getChild(i++).getChild(0).getText());
		} else {
			bandwidth_infinite = true;
		}
		delay = Integer.parseInt(channelDef.getChild(i++).getChild(0).getText());
		
		while (i < channelDef.getChildCount()) {
			nodes.add((Node) symbolTable.getObject(channelDef.getChild(i++).getChild(0).getText()));
		}

		for (Node t: nodes) {
			Debug.ensure(t != null, "Channel "+identifier + " between nonexisting nodes");
			Debug.ensure(nodes.indexOf(t) == nodes.lastIndexOf(t), "Multiple occurences of node "+t.getIdentifier() + " at channel " +identifier);
		}

		
		if (directed) {
			for (int b=1; b<nodes.size(); b++) {
				Edge newEdge = Global.systemModel.getNetworkGraph().addEdge(identifier + "("+b+")", nodes.get(0).getIdentifier(), nodes.get(b).getIdentifier(), directed);
				newEdge.setAttribute("Channel", this);
				networkChannels.add(newEdge);
			}
		} else {
			for (int a=1; a<nodes.size(); a++) {
				for (int b=0; b<a; b++) {
					Edge newEdge = Global.systemModel.getNetworkGraph().addEdge(identifier + "("+a+","+b+")", nodes.get(a).getIdentifier(), nodes.get(b).getIdentifier(), directed);
					newEdge.setAttribute("Channel", this);
					networkChannels.add(newEdge);
				}
			}
		}
		
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

	public boolean isReliable() {
		return reliable;
	}


	public Node getA() {
		return nodes.get(0);
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<Edge> getChannels() {
		return networkChannels;
	}
	
	public boolean isMultipointChannel() {
		return nodes.size() > 2;
	}

}
