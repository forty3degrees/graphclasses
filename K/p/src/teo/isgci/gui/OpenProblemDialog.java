/*
 * Displays a list of graph classes for which the given problem is still open.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/OpenProblemDialog.java,v 2.2 2013/03/12 18:43:39 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jgrapht.DirectedGraph;

import java.util.HashSet;
import java.util.Vector;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import teo.data.db.*;
import teo.data.services.IDataProvider;
import teo.isgci.gc.*;
import teo.isgci.problem.*;
import teo.isgci.grapht.*;
import teo.isgci.util.LessLatex;


/**
 * Displays three lists of graph classes: Minimal classes for which the given
 * problem is NP-complete, maximal classes for which the problem is polynomial,
 * and classes for which the problem is still open.
 */
public class OpenProblemDialog extends JDialog
        implements ItemListener, ActionListener, ListSelectionListener {
    protected ISGCIMainFrame parent;
    protected JCheckBox fullBoundary;
    protected NodeList npList, openList, pList;
    protected ListGroup lists;
    protected Problem problem;
    protected JButton closeButton, showButton, drawButton;


    public OpenProblemDialog(ISGCIMainFrame parent, String problem) {
        super(parent, "Boundary classes for "+problem, false);
        this.parent = parent;
        this.problem = ISGCIMainFrame.DataProvider.getProblem(problem);
        if (this.problem == null)
            throw new IllegalArgumentException(
                    "Problem "+ problem +" not found?!");

        lists = new ListGroup(3);
        JScrollPane scroller;
        Dimension listdim = new Dimension(150, 150);

        Container contents = getContentPane();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        contents.setLayout(gridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(0, 5, 0, 5);
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        JLabel label = new JLabel("Select one.", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        contents.add(label);

        fullBoundary = new JCheckBox("List all boundary classes");
        fullBoundary.addItemListener(this);
        gridbag.setConstraints(fullBoundary, c);
        contents.add(fullBoundary);

        //---- NPC/open/P labels ----
        c.gridwidth = 1;
        c.insets = new Insets(0, 5, 0, 0);
        label = new JLabel("Minimal (co)NP-complete:", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        contents.add(label);

        label = new JLabel("Open:", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        contents.add(label);

        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("Maximal P:", JLabel.LEFT);
        gridbag.setConstraints(label, c);
        contents.add(label);

        //---- NPC/open/P classes ----
        c.insets = new Insets(0, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        npList = new NodeList(ISGCIMainFrame.latex);
        scroller = new JScrollPane(npList);
        scroller.setPreferredSize(listdim);
        lists.add(npList);
        npList.addListSelectionListener(this);
        gridbag.setConstraints(scroller, c);
        contents.add(scroller);

        openList = new NodeList(ISGCIMainFrame.latex);
        scroller = new JScrollPane(openList);
        scroller.setPreferredSize(listdim);
        lists.add(openList);
        openList.addListSelectionListener(this);
        gridbag.setConstraints(scroller, c);
        contents.add(scroller);

        c.gridwidth = GridBagConstraints.REMAINDER;
        pList = new NodeList(ISGCIMainFrame.latex);
        scroller = new JScrollPane(pList);
        scroller.setPreferredSize(listdim);
        lists.add(pList);
        pList.addListSelectionListener(this);
        gridbag.setConstraints(scroller, c);
        contents.add(scroller);

        initListOpen();
        initListsMinMax();

        JPanel buttonPanel = new JPanel();
        drawButton = new JButton("Draw");
        showButton = new JButton("Class info");
        closeButton = new JButton("Close");
        buttonPanel.add(drawButton);
        buttonPanel.add(showButton);
        buttonPanel.add(closeButton);
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.0;
        gridbag.setConstraints(buttonPanel, c);
        contents.add(buttonPanel);
        handleButtons();

        drawButton.addActionListener(this);
        showButton.addActionListener(this);
        closeButton.addActionListener(this);
        pack();
        setSize(700, 300);
    }


    protected void closeDialog() {
        setVisible(false);
        dispose();
    }


    /**
     * Set the contents of the open list.
     */
    private void initListOpen() {
        Vector v = new Vector();
        for (GraphClass gc : ISGCIMainFrame.DataProvider.getGraphClasses()) {
            Complexity c = problem.getComplexity(gc);
            if (c.isUnknown())
                v.add(gc);
        }

        openList.setListData(v.iterator());
    }


    /**
     * Set the contents of P/NP lists as all boundary classes.
     */
    private void initListsBoundary() {
        TreeSet<GraphClass> npc = new TreeSet<GraphClass>(new LessLatex());
        TreeSet<GraphClass> p = new TreeSet<GraphClass>(new LessLatex());
        
        for (Inclusion e : ISGCIMainFrame.DataProvider.getInclusionGraph().edgeSet()) {
            Complexity cfrom = problem.getComplexity(e.getSuper());
            Complexity cto = problem.getComplexity(e.getSub());
            if (cfrom.likelyNotP()  &&  !cto.equals(cfrom)) {
                npc.addAll(ISGCIMainFrame.DataProvider.getEquivalentClasses(e.getSuper()));
            }
            if (cto.betterOrEqual(Complexity.P)  &&
                    (cfrom.isUnknown() || cfrom.likelyNotP())) {
                p.addAll(ISGCIMainFrame.DataProvider.getEquivalentClasses(e.getSub()));
            }
        }

        npList.setListData(npc.iterator());
        pList.setListData(p.iterator());
    }


    /**
     * Set the contents of P/NP lists as all minimal/maximal boundary
     * classes.
     */
    private void initListsMinMax() {
        TreeSet<GraphClass> npc = new TreeSet<GraphClass>(new LessLatex());
        TreeSet<GraphClass> p = new TreeSet<GraphClass>(new LessLatex());

    	IDataProvider dataProvider = ISGCIMainFrame.DataProvider;
    	
        for (GraphClass gc : dataProvider.getGraphClasses()) {
            if (npc.contains(gc)  ||   p.contains(gc))
                continue;

            Complexity c = problem.getComplexity(gc);
            Set<GraphClass> equs = dataProvider.getEquivalentClasses(gc);

notP:
            if (c.likelyNotP()) {
                for (GraphClass equ : equs)
                    for (GraphClass down :
                            GAlg.outNeighboursOf(dataProvider.getInclusionGraph(), equ)) {
                        if (problem.getComplexity(down).likelyNotP()  &&  
                                !equs.contains(down))
                            break notP;
                    }
                npc.addAll(equs);
            }

inP:
            if (c.betterOrEqual(Complexity.P)) {
                for (GraphClass equ : equs)
                    for (GraphClass up :
                            GAlg.inNeighboursOf(dataProvider.getInclusionGraph(), equ)) {
                        if (problem.getComplexity(up).betterOrEqual(
                                Complexity.P)  &&  !equs.contains(up))
                        break inP;
                }
                p.addAll(equs);
            }
        }

        npList.setListData(npc.iterator());
        pList.setListData(p.iterator());
    }
        

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == drawButton) {
            Cursor oldcursor = parent.getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            parent.viewManager.load(
                    ISGCIMainFrame.DataProvider.getNodes(lists.getSelectedNode(), this.problem));
            setCursor(oldcursor);
            closeDialog();
        } else if (source == showButton) {
            JDialog info = new GraphClassInformationDialog(
                    parent, lists.getSelectedNode());
            info.setLocation(50, 50);
            info.pack();
            info.setSize(800, 600);
            info.setVisible(true);
        } else if (source == closeButton) {
            closeDialog();
        }
    }
    
    public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
        if (source == fullBoundary) {
            if (event.getStateChange() == ItemEvent.DESELECTED)
                initListsMinMax();
            else
                initListsBoundary();
        }
    }


    public void valueChanged(ListSelectionEvent event) {
        handleButtons();
    }


    /** Enables/disables the buttons depending on whether any items are
     * selected
     */
    public void handleButtons() {
        if (lists.getSelectedItem() == null) {
            showButton.setEnabled(false);
            drawButton.setEnabled(false);
        } else {
            showButton.setEnabled(true);
            drawButton.setEnabled(true);
        }
    }


}

/* EOF */
