package semanticAnalysis;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.graphstream.graph.Edge;

import dlolaExprTree.DLolaExpr;
import dlolaExprTree.DLolaType;
import dlolaObject.*;
import main.DLolaRunner;
import main.Debug;
import main.Global;

public class SymbolTable {

	Map<String, DLolaObject> identifierList = new HashMap<String, DLolaObject>();
	List<Constant> constantList = new ArrayList<Constant>();
	List<Virtual> virtualList = new ArrayList<Virtual>();
	List<Input> inputList = new ArrayList<Input>();
	ArrayList<Output> outputList = new ArrayList<Output>();
	List<Output> triggerList = new ArrayList<Output>();
	List<Node> nodeList = new ArrayList<Node>();
	List<Channel> channelList = new ArrayList<Channel>();

	public SymbolTable(ParseTree tree) {
		DefinitionList deflist = new DefinitionList(tree);
		try {
			generateLists(deflist);
		} catch (Exception e) {
			Debug.err(3, e.getMessage());
			e.printStackTrace();
			Debug.err(2, "Symbol table generation failed");
			Debug.abort();
		}
		// TODO Auto-generated constructor stub
	}

	private void generateLists(DefinitionList deflist) throws ParseException {
		for (ParseTree nodeDef : deflist.getNodeList()) {
			Node obj = new Node(nodeDef, this);
			nodeList.add(obj);
			Debug.out(15, "Registered node "+obj.getIdentifier());
		}
		for (ParseTree channelDef : deflist.getChannelList()) {
			Channel obj = new Channel(channelDef, this);
			channelList.add(obj);
			Debug.out(15, "Registered channel "+obj.getIdentifier());
		}
		for (ParseTree constantDef : deflist.getConstantList()) {
			Constant obj = new Constant(constantDef, this);
			constantList.add(obj);
			Debug.out(15, "Registered constant "+obj.getIdentifier());
		}
		for (ParseTree inputDef : deflist.getInputList()) {
			ArrayList<String> idList = DLolaObject.extractIdentifierList(inputDef);
			for (String id : idList) {
				Input input = (Input) identifierList.get(id);
				if (input == null) {
					// Safe, new input
					Input obj = new Input(id, inputDef, this);
					inputList.add(obj);
					Debug.out(15, "Registered input "+obj.getIdentifier());
				} else {
					// Multiple similar inputs across nodes
					input.merge(inputDef, this);
					Debug.out(15, "Registered another instance of input "+input.getIdentifier());
				}
			}
		}
		for (ParseTree virtualDef : deflist.getVirtualList()) {
			Virtual obj = new Virtual(virtualDef, this);
			virtualList.add(obj);
			Debug.out(15, "Registered virtual "+obj.getIdentifier());
		}
		for (ParseTree outputDef : deflist.getOutputList()) {
			Output obj = new Output(outputDef, this);
			outputList.add(obj);
			Debug.out(15, "Registered output "+obj.getIdentifier());
		}
		for (ParseTree triggerDef : deflist.getTriggerList()) {
			ArrayList<String> idList = DLolaObject.extractIdentifierList(triggerDef);
			for (String id : idList) {
				DLolaObject out = identifierList.get(id);
				if (!(out instanceof Output)) {
					throw new ParseException("Trigger cannot be defined on "+ out.getClass().getName()+ " \"" + out.getIdentifier() + "\"", 0);
				}
				((Output) out).addTrigger();
				Debug.out(15, "Registered trigger on output "+out.getIdentifier());
			}
		}
	}

	public boolean isUndefined(String identifier) {
		 return identifierList.get(identifier) == null;
	}
	

	public DLolaObject getObject(String identifier) {
		 DLolaObject obj = identifierList.get(identifier);
		 if (obj == null) {
			 Debug.error("Reference to undefined identifier "+identifier);
		 }
		 return obj;
	}

	public Map<String, DLolaObject> getIdentifierList() {
		return identifierList;
	}

	public List<Constant> getConstantList() {
		return constantList;
	}

	public List<Virtual> getVirtualList() {
		return virtualList;
	}

	public List<Input> getInputList() {
		return inputList;
	}

	public ArrayList<Output> getOutputList() {
		return outputList;
	}

	public List<Output> getTriggerList() {
		return triggerList;
	}

	public List<Node> getNodeList() {
		return nodeList;
	}

	public List<Channel> getChannelList() {
		return channelList;
	}

	public Node getNode(org.graphstream.graph.Node n) {
		return n.getAttribute("Node");
	}

	public Channel getChannel(Edge e) {
		return e.getAttribute("Channel");
	}

	public DLolaType getType(String identifier) throws UntypedException {
		DLolaObject obj = this.getObject(identifier);
		if (obj instanceof Input) {
			return ((Input) obj).getType();
		} else if (obj instanceof Output) {
			return ((Output) obj).getType();
		} else if (obj instanceof Virtual) {
			return ((Virtual) obj).getType();
		} else if (obj instanceof Constant) {
			return ((Constant) obj).getType();
		}
		throw new UntypedException("getType on typeless Identifier");
	}

}
