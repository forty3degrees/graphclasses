package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import teo.isgci.gui.ISGCIMainFrame;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.view.Graph2D;

public class LayoutAction extends AbstractAction {
	public EdgeMap preferredEdgeLengthMap;
	ISGCIMainFrame parent;
    public LayoutAction(ISGCIMainFrame p) {
      super("Layout");
      parent = p;
    }

    public void actionPerformed(ActionEvent e) {
      //update preferredEdgeLengthData before launching the module
      Graph2D graph = parent.view.getGraph2D();
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();
        String eLabel = graph.getLabelText(edge);
        preferredEdgeLengthMap.set(edge, null);
        try {
          preferredEdgeLengthMap.setInt(edge, (int) Double.parseDouble(eLabel));
        }
        catch (Exception ex) {
        }
      }

      //start the module
      parent.module.start(parent.view.getGraph2D());
    }
  }
