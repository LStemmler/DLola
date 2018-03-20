package ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class GSFrame extends JFrame {
	
	Graph graph;
	JPanel panel;
	Viewer viewer;
	ViewPanel viewPanel;

	public GSFrame(Graph graph) {
		super();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.graph = graph;
        EventQueue.invokeLater(this::display);
	}
	
	public GSFrame(Graph graph, String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.graph = graph;
        EventQueue.invokeLater(this::display);
	}

    private void display() {
        panel = new JPanel(new GridLayout()){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
        //panel.setBorder(BorderFactory.createLineBorder(Color.blue, 5));
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        viewPanel = viewer.addDefaultView(false);
        panel.add(viewPanel);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
