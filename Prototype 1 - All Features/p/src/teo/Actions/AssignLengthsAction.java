package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import teo.isgci.gui.ISGCIMainFrame;
import y.base.EdgeCursor;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;

public class AssignLengthsAction extends AbstractAction {
	ISGCIMainFrame parent;
    public AssignLengthsAction(ISGCIMainFrame p) {
      super("Assign Preferred Length");
      putValue(Action.SHORT_DESCRIPTION, "Set the preferred length of each edge to its current geometric length");
      parent = p;
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D g = parent.view.getGraph2D();
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        NodeRealizer snr = g.getRealizer(ec.edge().source());
        NodeRealizer tnr = g.getRealizer(ec.edge().target());
        double deltaX = snr.getCenterX() - tnr.getCenterX();
        double deltaY = snr.getCenterY() - tnr.getCenterY();
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        EdgeRealizer er = g.getRealizer(ec.edge());
        er.getLabel().setText(Integer.toString((int) dist));
      }
      g.updateViews();
    }
  }
