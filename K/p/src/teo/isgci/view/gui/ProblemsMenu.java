/*
 * Menu to select a preferred problem (for colouring).
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ProblemsMenu.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.view.gui;

//import.java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

import teo.isgci.core.App;
import teo.isgci.data.db.*;
import teo.isgci.data.problem.*;

public class ProblemsMenu extends JMenu implements ActionListener {
    protected Vector items;
    protected ISGCIMainFrame parent;
    protected ButtonGroup group;

    public ProblemsMenu(ISGCIMainFrame parent, String label) {
        super(label);
        this.parent = parent;
        items = new Vector();
        group = new ButtonGroup();

        addRadio("None", true);

        Problem[] problems = App.DataProvider.getProblems();
        for (int i = 0; i < problems.length; i++)
            addRadio((problems[i]).getName(),false);
    }

    /**
     * Add a radiobutton to this menu.
     */
    private void addRadio(String s, boolean def) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(s, def);
        item.setActionCommand(s);
        item.addActionListener(this);
        add(item);
        group.add(item);
        items.addElement(item);
        
    }

    public void actionPerformed(ActionEvent event) {
        App.getViewManager(parent).setProblem(
        		App.DataProvider.getProblem(event.getActionCommand()));
    }
}

/* EOF */

