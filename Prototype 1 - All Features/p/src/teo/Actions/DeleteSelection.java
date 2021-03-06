package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import y.view.Graph2DView;

public class DeleteSelection extends AbstractAction {
    private final Graph2DView view;

    public DeleteSelection(final Graph2DView view) {
      super("Delete Selection");
      this.view = view;
      this.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    }

    public void actionPerformed(ActionEvent e) {
      view.getGraph2D().removeSelection();
      view.getGraph2D().updateViews();
    }
  }
