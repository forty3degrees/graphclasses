package teo.isgci.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import y.layout.hierarchic.IncrementalHierarchicLayouter;

public class ISGCIToolBar {
	ISGCIMainFrame parent;
	public ISGCIToolBar(ISGCIMainFrame p) {
		parent = p;
	}
	
	public static JComponent createActionControl(final Action action, final boolean showActionText) {
        final JButton jb = new JButton();
        if (action.getValue(Action.SMALL_ICON) != null) {
          jb.putClientProperty("hideActionText", Boolean.valueOf(!showActionText));
        }
        jb.setAction(action);
        return jb;
      }
	
	public static JComponent createActionControl(final Action action) {
        return createActionControl(action, false);
      }
	
	public JToolBar createToolBar() {
    	JToolBar toolBar = new JToolBar();
        
        addLayoutActions(toolBar);    
        
               
        return toolBar;
      }
	
	protected void addLayoutActions(JToolBar toolBar) {
        final Action incrementalLayoutAction = new AbstractAction(
                "Incremental", getIconResource("resource/next.png")) {
          public void actionPerformed(ActionEvent e) {
        	  parent.layoutIncrementally();
          }
        };

        final Action layoutAction = new AbstractAction(
                "Complete", getIconResource("resource/start.png")) {
          public void actionPerformed(ActionEvent e) {
        	  parent.dolayout();
          }
        };

        final Action propertiesAction = new AbstractAction(
                "Settings...", getIconResource("resource/Properties.png")) {
          public void actionPerformed(ActionEvent e) {
            final ActionListener layoutListener = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
            	  parent.dolayout();
              }
            };
            OptionSupport.showDialog(parent.groupLayoutOptions, layoutListener, false, parent.view.getFrame());
            configureGroupLayout();
          }
        };

        toolBar.addSeparator();
        toolBar.add(new JLabel("Layout: "));
        toolBar.add(createActionControl(incrementalLayoutAction));
        toolBar.add(createActionControl(layoutAction));
        toolBar.add(createActionControl(propertiesAction));
      }
	
	public static Icon getIconResource( final String resource ) {
        try {
          final URL icon = ISGCIMainFrame.class.getResource(resource);
          if (icon == null) {
            return null;
          } else {
            return new ImageIcon(icon);
          }
        } catch (Exception e) {
        	System.out.println( e.getMessage());
          return null;
        }
      }
	
	void configureGroupLayout() {
        Object gsi = parent.groupLayoutOptions.get("Group Layering Strategy");
        if ("Recursive Layering".equals(gsi)) {
          parent.layouter.setRecursiveGroupLayeringEnabled(true);
        } else if ("Global Layering".equals(gsi)) {
        	parent.layouter.setRecursiveGroupLayeringEnabled(false);
        }

        parent.layouter.setGroupCompactionEnabled(parent.groupLayoutOptions.getBool("Enable Compact Layering"));

        Object gai = parent.groupLayoutOptions.get("Group Alignment");
        if ("Top".equals(gai)) {
        	parent.layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_TOP);
        } else if ("Center".equals(gai)) {
        	parent.layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_CENTER);
        }
        if ("Bottom".equals(gai)) {
        	parent.layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_BOTTOM);
        }
      }
}
