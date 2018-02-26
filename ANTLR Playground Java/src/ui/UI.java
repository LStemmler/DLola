package ui;

import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

import dlolaObject.Channel;
import dlolaObject.DLolaObject;
import dlolaObject.Input;
import dlolaObject.Output;
import dlolaObject.Virtual;
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
		for (Node node : systemModel.getNetworkGraph()) {
			node.addAttribute("ui.style", "shape:circle;fill-color: yellow;size: 50px; text-alignment: center;");
			node.addAttribute("ui.label", node.getId());
		}
		for (Channel chan : systemModel.getSymbolTable().getChannelList()) {
			Edge e = chan.getChannel();
			String label = chan.getIdentifier() + " [";
			if (chan.isBandwidth_infinite()) {
				label += "∞, " + chan.getDelay() + "]";
			} else {
				label += chan.getDelay() + ", " + chan.getDelay() + "]";
			}
			TreeSet<Integer> shiftSet = e.getAttribute("shiftSet");
			e.addAttribute("ui.style", "text-alignment: center;text-size: 20;");
			e.addAttribute("ui.label", label);
		}
		Viewer ngraphviewer = systemModel.getNetworkGraph().display();
	}
	
	public void displayDependencyGraph() {
		for (Node node : systemModel.getDependencyGraph()) {
			String streamName = node.getId();
			DLolaObject stream = systemModel.getSymbolTable().getObject(streamName);
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
		Viewer dgraphviewer = systemModel.getDependencyGraph().display();
	}

	public void displayAST(List<String> ruleNames, ParseTree tree) {
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
	
}
