/*
 * Select the naming preference for graphclasses.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/SearchDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import teo.data.db.Algo;
import teo.graph.view.NodeView;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.LessLatex;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.ShapeNodeRealizer;

public class SearchDialog extends JDialog implements ActionListener {
    protected ISGCIMainFrame parent;
    protected ButtonGroup group;
    protected JCheckBox basicBox, derivedBox, forbiddenBox;
    protected JButton searchButton, cancelButton;
    protected NodeList classesList;


    public SearchDialog(ISGCIMainFrame parent) {
        super(parent, "Search for a graphclass", true);
        this.parent = parent;
        group = new ButtonGroup();
        Algo.NamePref mode = parent.viewManager.getNamingPref();
        Container content = getContentPane();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        content.setLayout(gridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5, 5, 5, 5);
        c.weighty = 1.0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        classesList = new NodeList(parent.latex);
        classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(classesList);
        gridbag.setConstraints(scroller, c);
        content.add(scroller);
        
        c.insets = new Insets(0, 5, 0, 0);
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
    
        JPanel p = new JPanel();
        searchButton = new JButton("Search");
        cancelButton = new JButton("Cancel");
        p.add(searchButton);
        p.add(cancelButton);
        c.insets = new Insets(5, 0, 5, 0);
        gridbag.setConstraints(p, c);
        content.add(p);

        searchButton.addActionListener(this);
        cancelButton.addActionListener(this);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    
        
        
        Graph2D g = parent.view.getGraph2D();
    	NodeCursor n = g.nodes();
    	ArrayList<Node> l = new ArrayList<Node>();
    	Collection<GraphClass> gCol = new ArrayList<GraphClass>();
    	for (int i = 0; i < n.size(); ++i) {
    		Node neigh =  n.node(); 
    		String s = n.current().toString();
    		if (s.contains("<sub>")) {
        		if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
        			s = s.replace("<sub>", "_{");
            		s = s.replace("</sub>", "}");
        		} else {
        			s = s.replace("<sub>", "_");
            		s = s.replace("</sub>", "");
        		}
    		}
    		gCol.addAll(Algo.equNodes(parent.DataProvider.getClass(s)));
    		n.next();
    	}
   	
        
        List<GraphClass> names = new ArrayList(gCol);
        
        if (!names.isEmpty()) {
            Collections.sort(names, new LessLatex());
            classesList.setListData(names);
        }
        pack();
        setSize(300, 350);
    }


    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == cancelButton) {
            closeDialog();
        } else if (source == searchButton) {
            NodeView view = parent.viewManager.findNode(
                            classesList.getSelectedNode());
            //parent.graphCanvas.markOnly(view);
            for (NodeCursor nc = parent.view.getGraph2D().nodes(); nc.ok(); nc.next()) {
            	Node n = nc.node();
            	String s = n.toString();
            	
        		if (s.contains("<sub>")) {
            		if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
            			s = s.replace("<sub>", "_{");
                		s = s.replace("</sub>", "}");
            		} else {
            			s = s.replace("<sub>", "_");
                		s = s.replace("</sub>", "");
            		}
        		}
        		System.out.println(view.getLabel() + " " + s + " " + view.toString());
        		if (view.getLabel().equals(s)) {
        			System.out.println("JEP");
        			parent.view.getGraph2D().setSelected(n, true);
        		}
        		parent.view.repaint();
    	    }
            closeDialog();
        }
    }
}

/* EOF */
