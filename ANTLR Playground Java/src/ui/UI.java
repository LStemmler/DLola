package ui;

import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.view.Viewer;

import dlolaObject.Channel;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import dlolaObject.Output;
import dlolaObject.Virtual;
import main.Global;
import routeGeneration.RelevantSubgraph;
import semanticAnalysis.SystemModel;

public class UI {

    private SystemModel systemModel;

	public UI() {
		System.setProperty("org.graphstream.ui.renderer",
		        "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	}
	
	public void setSystemModel(SystemModel systemModel) {
		this.systemModel= systemModel;
	}
	
	public void displayNetworkGraph() {
		if (!Global.displayNetworkGraph) return;
		for (Node node : systemModel.getNetworkGraph()) {
			node.addAttribute("ui.style", "shape:circle;fill-color: yellow;size: 50px; text-alignment: center;");
			node.addAttribute("ui.label", node.getId());
		}
		for (Channel chan : Global.symtable.getChannelList()) {
			boolean multipoint = chan.isMultipointChannel();
			for (Edge e: chan.getChannels()) {
				String label = chan.getIdentifier() + " [";
				if (chan.isBandwidth_infinite()) {
					label += "∞, " + chan.getDelay() + "]";
				} else {
					label += chan.getBandwidth() + ", " + chan.getDelay() + "]";
				}
				TreeSet<Integer> shiftSet = e.getAttribute("shiftSet");
				if (multipoint) {
					e.addAttribute("ui.style", "text-color: blue; fill-color: blue; text-alignment: center;text-size: 20;");
				} else {
					e.addAttribute("ui.style", "text-alignment: center;text-size: 20;");
				}
				e.addAttribute("ui.label", label);
			}
		}
		new GSFrame(systemModel.getNetworkGraph(), "Network Graph");
	}
	
	public void displayDependencyGraph() {
		if (!Global.displayDependencyGraph) return;
		for (Node node : systemModel.getDependencyGraph()) {
			String streamName = node.getId();
			DLolaObject stream = Global.symtable.getObject(streamName);
			if (stream instanceof Input) {
				node.addAttribute("ui.style", "shape:triangle;text-color: black;fill-color: yellow;size: 50px; text-alignment: center;");
			} else if (stream instanceof Virtual) {
				node.addAttribute("ui.style", "shape:circle;text-color: white;fill-color: blue;size: 50px; text-alignment: center;");
			} else if (stream instanceof Output) {
				node.addAttribute("ui.style", "shape:circle;text-color: white;fill-color: red;size: 50px; text-alignment: center;");
			}
			node.addAttribute("ui.label", node.getId());
		}
		for (int i=0; i<systemModel.getDependencyGraph().getEdgeCount(); i++) {
			Edge e = systemModel.getDependencyGraph().getEdge(i);
			TreeSet<Integer> shiftSet = e.getAttribute("shiftSet");
			e.addAttribute("ui.style", "text-alignment: center;text-size: 20;");
			e.addAttribute("ui.label", shiftSet.toString());
		}
		new GSFrame(systemModel.getDependencyGraph(), "Dependency Graph");
	}

	public void displayAST(List<String> ruleNames, ParseTree tree) {
		if (!Global.displayAST) return;
        //show AST in GUI
        JFrame frame = new JFrame("DLola AST");
        JPanel panel = new JPanel();
        TreeViewer viewer = new TreeViewer(ruleNames,tree);
        viewer.setScale(1.5);//scale a little
        panel.add(viewer);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400,800);
        frame.setVisible(true);
	}

	public void displayRelevantSubgraphs() {
		if (!Global.displayRelevantSubgraphs) return;
		for (RelevantSubgraph rs: systemModel.getAllRelevantSubgraphs()) {
			dlolaObject.Node n = rs.getOutputNode();
			for (Input in: n.getInputDependencies()) {
				SingleGraph relevantSubgraph = rs.getChannelTree();
				for (Node node : relevantSubgraph) {
					node.addAttribute("ui.style", "shape:circle;fill-color: white;size: 30px; text-alignment: center;");
					try {
						int delay = rs.getMinDelay(node, in);
						node.addAttribute("ui.label", rs.getNodeIdentifier(node)+ "   " + "Delay: " + rs.getCurDelay(node)  +" / " + delay);
					} catch (NullPointerException e) {
						node.addAttribute("ui.label", rs.getNodeIdentifier(node));
						node.addAttribute("ui.style", "shape:circle;fill-color: white;size: 30px; text-color: grey; text-alignment: center;");
					}
				}
				rs.getTarget().addAttribute("ui.style", "text-color: blue; fill-color: yellow; shape:circle;size: 30px; text-alignment: center;");
				for (Edge edge : relevantSubgraph.getEachEdge()) {
					edge.addAttribute("ui.style", "text-alignment: along;text-background-mode: plain;text-size: 15;");
					edge.addAttribute("ui.label", rs.getEdgeIdentifier(edge));
					if (rs.isIndirect(edge)) {
						edge.addAttribute("ui.style", "text-color: red; fill-color: red; text-alignment: along;text-background-mode: plain;text-size: 15;");
					}
					if (!rs.getReachableInputs(edge).contains(in)) {
						edge.addAttribute("ui.style", "text-color: grey; fill-color: grey; text-alignment: along;text-background-mode: plain;text-size: 5;");
					}
				}
				new GSFrame(relevantSubgraph, "Subgraph for Input "+ in.getIdentifier() + " to Node " + rs.getOutputNode().getIdentifier());
			}
		}
		
	}
	
}