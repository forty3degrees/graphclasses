package teo.isgci.view.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import teo.isgci.view.gui.ISGCIMainFrame;
import y.module.YModule;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.EditorFactory;
import y.option.GuiFactory;
import y.option.OptionHandler;
import y.view.Graph2D;

public class OptionSupport {
    private final EditorFactory editorFactory;

    /**
     * Displays the OptionHandler of the given module in a dialog and runs the module on the given graph if either the
     * <code>Ok</code> or <code>Apply</code> button were pressed. The creation of the dialog is delegated to {@link
     * #createDialog(y.option.OptionHandler, java.awt.event.ActionListener, java.awt.Frame)}
     *
     * @param module the option handler.
     * @param graph  an optional ActionListener that is added to each button of the dialog.
     * @param owner  the owner of the created dialog.
     */
    public static void showDialog(final YModule module, final Graph2D graph, final boolean runOnOk, Frame owner) {
      if (module.getOptionHandler() == null) {
        module.start(graph);
      } else {
        final ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            module.start(graph);
          }
        };

        showDialog(module.getOptionHandler(), listener, runOnOk, owner);
      }
    }

    /**
     * Displays the given OptionHandler in a dialog and invokes the given ActionListener if either the <code>Ok</code>
     * or <code>Apply</code> button were pressed. The creation of the dialog is delegated to {@link
     * #createDialog(y.option.OptionHandler, java.awt.event.ActionListener, java.awt.Frame)}
     *
     * @param oh       the option handler.
     * @param listener the ActionListener that is invoked if either the <code>Ok</code> or <code>Apply</code> button
     *                 were pressed.
     * @param owner    the owner of the created dialog.
     */
    public static void showDialog(final OptionHandler oh, final ActionListener listener,
                                  final boolean runOnOk, Frame owner) {
      final ActionListener delegatingListener = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          final String actionCommand = e.getActionCommand();
          if ("Apply".equals(actionCommand) || (runOnOk && "Ok".equals(actionCommand))) {
            EventQueue.invokeLater(new Runnable() {
              public void run() {
                listener.actionPerformed(e);
              }
            });
          }
        }
      };

      final JDialog dialog = new OptionSupport().createDialog(oh, delegatingListener, owner);
      dialog.setVisible(true);
    }

    public OptionSupport() {
      editorFactory = new DefaultEditorFactory();
    }

    /**
     * Returns an dialog for the editor of the given option handler which contains four control buttons: Ok, Apply,
     * Reset, and Cancel. An optional ActionListener can be used to add custom behavior to these buttons.
     *
     * @param oh       the option handler.
     * @param listener an optional ActionListener that is added to each button of the dialog.
     * @param owner    the owner of the created dialog.
     */
    public JDialog createDialog(final OptionHandler oh, ActionListener listener, Frame owner) {
      final JDialog dialog = new JDialog(owner, getTitle(oh), true);

      final AbstractAction applyAction = new AbstractAction("Apply") {
        public void actionPerformed(ActionEvent e) {
          if (oh.checkValues()) {
            oh.commitValues();
          }
        }
      };
      final JButton applyButton = createButton(applyAction, listener);
      applyButton.setDefaultCapable(true);
      applyButton.requestFocus();

      final AbstractAction closeAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };

      final AbstractAction okAction = new AbstractAction("Ok") {
        public void actionPerformed(ActionEvent e) {
          if (oh.checkValues()) {
            oh.commitValues();
            dialog.dispose();
          }
        }
      };

      final AbstractAction resetAction = new AbstractAction("Reset") {
        public void actionPerformed(ActionEvent e) {
          oh.resetValues();
        }
      };

      final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 11, 5));
      buttonPanel.add(createButton(okAction, listener));
      buttonPanel.add(createButton(closeAction, listener));
      buttonPanel.add(applyButton);
      buttonPanel.add(createButton(resetAction, listener));

      final Editor editor = getEditorFactory().createEditor(oh);
      final JComponent editorComponent = editor.getComponent();
      if (editorComponent instanceof JPanel) {
        editorComponent.setBorder(BorderFactory.createEmptyBorder(11, 5, 5, 5));
      } else {
        editorComponent.setBorder(BorderFactory.createEmptyBorder(9, 9, 15, 9));
      }

      final JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
      contentPanel.add(editorComponent, BorderLayout.CENTER);
      contentPanel.add(buttonPanel, BorderLayout.SOUTH);

      dialog.setContentPane(contentPanel);
      dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "APPLY_ACTION");
      dialog.getRootPane().getActionMap().put("APPLY_ACTION", applyAction);
      dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "CANCEL_ACTION");
      dialog.getRootPane().getActionMap().put("CANCEL_ACTION", closeAction);
      dialog.getRootPane().setDefaultButton(applyButton);
      dialog.pack();
      dialog.setSize(dialog.getPreferredSize());
      dialog.setResizable(false);
      dialog.setLocationRelativeTo(owner);

      return dialog;
    }

    protected EditorFactory getEditorFactory() {
      return editorFactory;
    }

    String getTitle(OptionHandler oh) {
      final Object title = oh.getAttribute(OptionHandler.ATTRIBUTE_TITLE);
      if (title instanceof String) {
        return (String) title;
      }

      final Object titleKey = oh.getAttribute(OptionHandler.ATTRIBUTE_TITLE_KEY);
      if (titleKey instanceof String) {
        return getTitle(oh, (String) titleKey);
      } else {
        return getTitle(oh, oh.getName());
      }
    }

    static String getTitle(OptionHandler oh, String title) {
      final GuiFactory guiFactory = oh.getGuiFactory();
      return guiFactory == null ? title : guiFactory.getString(title);
    }

    static JButton createButton(AbstractAction action, ActionListener listener) {
      final JButton button = new JButton(action);
      button.addActionListener(listener);
      return button;

    }
  }
