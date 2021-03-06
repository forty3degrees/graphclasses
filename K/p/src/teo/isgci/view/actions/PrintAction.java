package teo.isgci.view.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import teo.isgci.core.App;
import teo.isgci.view.gui.ISGCIMainFrame;

public class PrintAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

    ISGCIMainFrame parent;

    public PrintAction(ISGCIMainFrame p) {
      super("Print");
      parent = p;
    }

    public void actionPerformed(ActionEvent e) {
    	App.getDrawingService(parent).print();
    }
  }
