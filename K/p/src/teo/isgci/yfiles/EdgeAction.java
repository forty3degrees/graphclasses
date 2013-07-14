package teo.isgci.yfiles;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import teo.isgci.core.App;
import teo.isgci.view.gui.ISGCIMainFrame;
import teo.isgci.view.gui.InclusionResultDialog;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

public class EdgeAction extends AbstractAction {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EdgeCursor ec;
    private ISGCIMainFrame parent;
    private YFilesDrawingService drawingService;

    public EdgeAction(String name, EdgeCursor edges, ISGCIMainFrame parent, YFilesDrawingService drawingService) {
      super(name);
      this.ec = edges;
      this.parent = parent;
      this.drawingService = drawingService;
    }

    public void actionPerformed(ActionEvent a) {
	      if (!ec.ok()) {
	    	  return;
	      }
	      Edge e = ec.edge();
		  Node s = e.source();
		  Node t = e.target();

		  String source = drawingService.getNodeName(s);
		  String target = drawingService.getNodeName(t);
		  
		  source = source.replace("<html>", "");
		  source = source.replace("</html>", "");
  		
		  if (source.contains("<sub>")) {
			  if (source.substring(source.indexOf("<sub>"), source.indexOf("</sub>")).contains(",")) {
				  source = source.replace("<sub>", "_{");
				  source = source.replace("</sub>", "}");
			  } else {
				  source = source.replace("<sub>", "_");
				  source = source.replace("</sub>", "");
			  }
		  }
		  target = target.replace("<html>", "");
		  target = target.replace("</html>", "");
		  		
		  if (target.contains("<sub>")) {
			  if (target.substring(target.indexOf("<sub>"), target.indexOf("</sub>")).contains(",")) {
				  target = target.replace("<sub>", "_{");
				  target = target.replace("</sub>", "}");
			  } else {
				  target = target.replace("<sub>", "_");
				  target = target.replace("</sub>", "");
			  }
		  }
	        
		  JDialog d = InclusionResultDialog.newInstance(parent,
	        		App.DataProvider.getClass(source),
	        		App.DataProvider.getClass(target));
		  d.setLocation(50, 50);
		  d.pack();
		  d.setVisible(true);
	        
    }
}

