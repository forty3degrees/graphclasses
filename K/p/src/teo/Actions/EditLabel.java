package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import teo.isgci.gui.ISGCIMainFrame;
import y.base.Edge;
import y.view.EdgeRealizer;
import y.view.YLabel;

public class EditLabel extends AbstractAction {
	private static final long serialVersionUID = 1L;
    Edge e;
    ISGCIMainFrame parent;

    public EditLabel(Edge e, ISGCIMainFrame p) {
      super("Edit Preferred Length");
      this.e = e;
      parent = p;
    }

    public void actionPerformed(ActionEvent ev) {

      final EdgeRealizer r = parent.view.getGraph2D().getRealizer(e);
      final YLabel label = r.getLabel();

      parent.view.openLabelEditor(label,
          label.getBox().getX(),
          label.getBox().getY(),
          null, true);
    }
  }
