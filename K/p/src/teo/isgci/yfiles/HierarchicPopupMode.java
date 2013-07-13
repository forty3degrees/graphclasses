package teo.isgci.yfiles;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import teo.isgci.core.App;
import teo.isgci.data.db.Algo;
import teo.isgci.data.gc.GraphClass;
import teo.isgci.view.gui.GraphClassInformationDialog;
import teo.isgci.view.gui.ISGCIMainFrame;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.view.Graph2D;
import y.view.PopupMode;
import y.view.ShapeNodeRealizer;


public class HierarchicPopupMode extends PopupMode implements ActionListener {
	    
	private JMenuItem  addUp;
	private JMenuItem  addDown;
	private JMenuItem  infoItem;
	private JMenuItem  namingItem;
	private JMenuItem  selectItem;
	private JMenuItem  selectItemUp;
	private JMenuItem  selectItemDown;
	private JMenu  namingMenu;
    private ISGCIMainFrame parent;
	private final YFilesDrawingService drawingService;
    private Graph2D graph2D;
    
    public HierarchicPopupMode(ISGCIMainFrame parent, YFilesDrawingService drawingService, Graph2D graph2D) {
    	super();
    	this.drawingService = drawingService;
    	this.graph2D = graph2D; 
    	this.parent = parent;
    }
    
  	public JPopupMenu getPaperPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      populatePopup(pm, x, y, null, false, null);
      return pm;
    }

    public JPopupMenu getNodePopup(Node v) {
      Graph2D graph = getGraph2D();
      JPopupMenu pm = new JPopupMenu();
      populatePopup(pm, graph.getCenterX(v), graph.getCenterY(v), v, true, v);
      return pm;
    }

    public JPopupMenu getSelectionPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      populatePopup(pm, x, y, null, getGraph2D().selectedNodes().ok(), null);
      return pm;
    }
    
    public JPopupMenu getEdgePopup(final Edge e) {
        JPopupMenu pm = new JPopupMenu();
        addEdgeActions(pm, new EdgeList(e).edges());
        return pm;
      }
    
    private void addEdgeActions(JPopupMenu pm, EdgeCursor sec) {
        pm.add(new EdgeAction("Information", sec, this.parent));
    }

  	protected void populatePopup(JPopupMenu pm, final double x, final double y, Node node, boolean selected, final Node n) {
          
      	parent.add(selectItem = new JMenuItem("Show Neighbors"));
        selectItem.addActionListener(this);
          
        parent.add(selectItemUp = new JMenuItem("Show SuperClasses"));
        selectItemUp.addActionListener(this);
          
        parent.add(selectItemDown = new JMenuItem("Show Subclasses"));
        selectItemDown.addActionListener(this);
            
        parent.add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);  
          
        parent.add(addUp = new JMenuItem("Add Superclasses"));
        addUp.addActionListener(this);
          
        parent.add(addDown = new JMenuItem("Add Subclasses"));
        addDown.addActionListener(this);
            
        parent.add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);
        
        if (n != null) {
        	namingMenu = new JMenu("Change Name");
        	GraphClass gClass = App.DataProvider.getClass(n.toString());
        	Iterator<GraphClass> classes = Algo.equNodes(gClass).iterator();

        	while (classes.hasNext()) {
        		System.out.println("drin2");
        		final GraphClass c = classes.next();
        		System.out.println(c);
        		JMenuItem item = new JMenuItem(c.toString());
        		item.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    	System.out.println(n.toString() + " -> " + c.toString());
                    	drawingService.graph2D.setLabelText(n, c.toString());
                    	drawingService.graph2D.updateViews();
                    }
                });
            	namingMenu.add(item);
        	}
        	
        }
         
        pm.add(selectItem);
        pm.add(selectItemUp);
        pm.add(selectItemDown);

        pm.addSeparator();
          
        pm.add(addUp);
        pm.add(addDown);
         
        pm.addSeparator();
         
        pm.add(infoItem);
        if (n != null) {
        	pm.add(namingMenu);
        }
    }
  	

    /**
     * Eventhandler for menu selections
     */
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();

        Object source = event.getSource();
        if (source == infoItem) {
        	// TINO: view!!!
        	String sub = graph2D.selectedNodes().node().toString();
        	JDialog d;
        	if (sub.length() >= 0) {
        		//ToDo: Catch unknown Sequences (\cap ... )
        	/*sub = sub.substring(6, sub.indexOf("</html>"));
        	if (sub.indexOf('?') >= 0) {
        		String newSub = sub.substring(0, sub.indexOf(" "));
        		newSub += "$\\cap$ ";
        		newSub += sub.substring(sub.indexOf("?") + 1);
        		sub = newSub;
        		
        	}*/
        		sub = sub.replace("<html>", "");
        		sub = sub.replace("</html>", "");
        		
        		if (sub.contains("<sub>")) {
	        		if (sub.substring(sub.indexOf("<sub>"), sub.indexOf("</sub>")).contains(",")) {
	        			sub = sub.replace("<sub>", "_{");
	            		sub = sub.replace("</sub>", "}");
	        		} else {
	        			sub = sub.replace("<sub>", "_");
	            		sub = sub.replace("</sub>", "");
	        		}
        		}
        		
        		
            d = new GraphClassInformationDialog(
                    this.parent, App.DataProvider.getClass(sub));
        	} else {
  		    		d = new GraphClassInformationDialog(
  		    				this.parent, App.DataProvider.getClass(
  		    						graph2D.selectedNodes().current().toString()));
        	}
	        d.setLocation(50, 50);
	        d.pack();
	        d.setSize(800, 600);
	        d.setVisible(true);
            
        } else if (object == selectItem) {
        	drawingService.selectNeighbors();
        } else if (object == selectItemUp) {
        	drawingService.selectSuperClasses();
        } else if (object == selectItemDown) {
        	drawingService.selectSubClasses();
        } else if (object == addUp) {
        	Collection<GraphClass> graphClasses = drawingService.getSelection();
        	graphClasses = App.DataProvider.getNodes(graphClasses, true, false);
        	App.getViewManager(parent).add(graphClasses);
        } else if (object == addDown) {
        	Collection<GraphClass> graphClasses = drawingService.getSelection();
        	graphClasses = App.DataProvider.getNodes(graphClasses, false, true);
        	App.getViewManager(parent).add(graphClasses);
        }
    }
}