package teo.isgci.yfiles;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import teo.isgci.core.NodeView;
import teo.isgci.view.gui.ISGCIMainFrame;
import teo.isgci.view.gui.InclusionResultDialog;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

/**
 * Action class for the edge context menu.
 * 
 * @author Calum *
 */
public class EdgeAction extends AbstractAction {
    /** Keep the compiler happy */
	private static final long serialVersionUID = 1L;
	
	/** The edge cursor for the selected edges when the context menu is invoked. */
	private EdgeCursor ec;
	/** The parent frame. */
    private ISGCIMainFrame parent;
    /** The associated drawing service. */
    private YFilesDrawingService drawingService;

    /**
     * Creates a new instance.
     * @param name				The action name.
     * @param edges				The edges that are selected.
     * @param parent			The parent frame.
     * @param drawingService	The associated drawing service.
     */
    public EdgeAction(String name, EdgeCursor edges, ISGCIMainFrame parent, YFilesDrawingService drawingService) {
      super(name);
      this.ec = edges;
      this.parent = parent;
      this.drawingService = drawingService;
    }

    /**
     * Invoked when a context menu item is clicked.
     */
    public void actionPerformed(ActionEvent a) {
    	/* Return if no edges are selected */
	    if (!ec.ok()) {
	    	return;
	    }
	    /* Get the edge and the source and target nodes */
	    Edge e = ec.edge();
		Node s = e.source();
		Node t = e.target();

		/* Get the node views for the source and target nodes */
		NodeView source = drawingService.getNodeView(s);
		NodeView target = drawingService.getNodeView(t);
		  
		/* Create and show a new dialog */
		JDialog d = InclusionResultDialog.newInstance(parent,
			  source.getDefaultClass(), target.getDefaultClass());
		d.setLocation(50, 50);
		d.pack();
		d.setVisible(true);
	       
    }
}

