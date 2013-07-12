package teo.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import teo.isgci.gui.ISGCIMainFrame;
import teo.isgci.gui.InclusionResultDialog;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

public class EdgeAction extends AbstractAction {
	    private EdgeCursor ec;
	    private boolean resetPCs;
	    ISGCIMainFrame m_parent;
	
	    public EdgeAction(String name, EdgeCursor edges, boolean resetPCs, ISGCIMainFrame parent) {
	      super(name);
	      this.ec = edges;
	      this.resetPCs = resetPCs;
	      m_parent = parent;
	    }
	
	    public void actionPerformed(ActionEvent a) {
	      if (!ec.ok()) {
	    	  return;
	      }
	      Edge e = ec.edge();
    	  Node s = e.source();
    	  Node t = e.target();
    	  
    	  String source = s.toString();
    	  String target = t.toString();
    	  
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
	        
	        JDialog d = InclusionResultDialog.newInstance(m_parent,
	        		ISGCIMainFrame.DataProvider.getClass(source),
	        		ISGCIMainFrame.DataProvider.getClass(target));
	        d.setLocation(50, 50);
            d.pack();
            d.setVisible(true);
	        
	      }
	      
	    
}
