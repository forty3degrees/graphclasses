package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ExitAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
    public ExitAction() {
      super("Exit");
    }

    public void actionPerformed(ActionEvent e) {
      System.exit(0);
    }
  }
