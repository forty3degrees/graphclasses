package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import teo.isgci.gui.ISGCIMainFrame;
import teo.isgci.gui.ISGCIMainFrame.OptionSupport;

public class OptionAction extends AbstractAction {
	private ISGCIMainFrame parent;
    public OptionAction(ISGCIMainFrame p) {
    	super("Settings...");
    	parent = p;
    }

    public void actionPerformed(ActionEvent e) {
      OptionSupport.showDialog(parent.module, parent.view.getGraph2D(), false, parent.view.getFrame());
    }
  }
