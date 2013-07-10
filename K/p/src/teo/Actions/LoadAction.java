package teo.Actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import teo.graph.view.EdgeView;
import teo.graph.view.GraphView;
import teo.graph.view.NodeView;
import teo.isgci.gui.ISGCIMainFrame;
import y.base.Edge;
import y.base.Node;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.util.D;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;

/**
 * Action that loads the current graph from a file in GraphML format.
 */
public class LoadAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
  JFileChooser chooser;
  ISGCIMainFrame parent;

  public LoadAction(ISGCIMainFrame p) {
    super("Load...");
    chooser = null;
    parent = p;
  }

  public void actionPerformed(ActionEvent e) {
    if (chooser == null) {
      chooser = new JFileChooser();
      chooser.setAcceptAllFileFilterUsed(false);
      chooser.addChoosableFileFilter(new FileFilter() {
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().endsWith(".graphml");
        }

        public String getDescription() {
          return "GraphML Format (.graphml)";
        }
      });
    }
    if (chooser.showOpenDialog(parent.mainPan) == JFileChooser.APPROVE_OPTION) {
      URL resource = null;
      try {
        resource = chooser.getSelectedFile().toURI().toURL();
      } catch (MalformedURLException urlex) {
        urlex.printStackTrace();
      }
      loadGraph(resource);
    }
  }
  
  protected GraphMLIOHandler createGraphMLIOHandler() {
      return new GraphMLIOHandler();
    }
  
  public void loadGraph(URL resource) {
      if (resource == null) {
        String message = "Resource \"" + resource + "\" not found in classpath";
        D.showError(message);
        throw new RuntimeException(message);
      }

      try {
        IOHandler ioh = null;
        ioh = createGraphMLIOHandler();
        parent.view.getGraph2D().clear();
        
        //Tested Version for loading explicit GraphML strings. 
        //String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?><graphml xmlns:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><key id=\"d0\" for=\"node\" yfiles.type=\"nodegraphics\"/><key id=\"e0\" for=\"edge\" yfiles.type=\"edgegraphics\"/><graph id=\"isgci\" edgedefault=\"directed\"><desc>ISGCI graph class diagram, generated 2013-06-15 14:55 by http://www.graphclasses.org</desc><node id=\"0\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;binary tree &#8745; partial grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"1\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-chordal &#8745; X-conformal &#8745;...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"2\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite tolerance&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"3\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;chordal bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"4\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(fork,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"5\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; maximum degree 4...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"6\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bi-cograph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"7\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; claw-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"8\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid graph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"9\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;PURE-2-DIR&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"10\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;,sunlet&lt;sub&gt;4&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"11\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;independent module-composed&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"12\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"13\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;binary tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"14\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; probe interval&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"15\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"16\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(claw &#8746; 3K&lt;sub&gt;1&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"17\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;E-free &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"18\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;triad convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"19\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;1-bounded bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"20\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(XC&lt;sub&gt;11&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"21\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tolerance &#8745; tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"22\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;perfect elimination bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"23\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;hypercube&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"24\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;difference&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"25\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;2-bounded bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"26\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"27\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(K&lt;sub&gt;1,4&lt;/sub&gt;,odd-cycle)-free &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"28\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-conformal &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"29\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(P&lt;sub&gt;7&lt;/sub&gt;,odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"30\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;interval bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"31\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;solid grid graph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"32\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe interval bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"33\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;C&lt;sub&gt;6&lt;/sub&gt;-free &#8745; modular&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"34\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;P&lt;sub&gt;6&lt;/sub&gt;-free &#8745; chordal bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"35\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-chordal &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"36\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; unit grid intersection&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"37\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;C&lt;sub&gt;4&lt;/sub&gt;-free &#8745; C&lt;sub&gt;6&lt;/sub&gt;-free &#8745;...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"38\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;median &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"39\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; maximum degree 3&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"40\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;circular convex bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"41\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;partial grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"42\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe interval &#8745; tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"43\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(X&lt;sub&gt;177&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"44\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"45\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-star-chordal&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"46\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;caterpillar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"47\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;interval containment bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"48\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tree convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"49\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"50\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe bipartite chain&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"51\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;almost median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"52\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;absolute bipartite retract&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"53\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;hereditary median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"54\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;premedian&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"55\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(A,T&lt;sub&gt;2&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"56\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FF0000\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(0,2)-colorable&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"57\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;cubical&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"58\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid graph &#8745; maximum degree 3&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"59\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"60\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(P&lt;sub&gt;4&lt;/sub&gt;,triangle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"61\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;partial cube&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"62\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;biconvex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"63\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;modular&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"64\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; tolerance&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"65\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; bithreshold&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"66\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;star convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><edge source=\"1\" target=\"3\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"47\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"5\" target=\"41\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"5\" target=\"27\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"9\" target=\"49\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"9\" target=\"36\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"52\" target=\"33\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"17\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"59\" target=\"21\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"59\" target=\"13\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"47\" target=\"30\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"33\" target=\"3\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"25\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"61\" target=\"54\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"15\" target=\"62\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"16\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"21\" target=\"42\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"59\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"22\" target=\"3\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"63\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"48\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"57\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"35\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"26\" target=\"10\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"53\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"23\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"38\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"53\" target=\"59\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"62\" target=\"2\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"64\" target=\"14\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"64\" target=\"21\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"29\" target=\"6\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"29\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"31\" target=\"44\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"13\" target=\"0\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"35\" target=\"45\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"36\" target=\"30\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"38\" target=\"59\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"39\" target=\"27\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"66\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"18\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"49\" target=\"5\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"49\" target=\"38\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"51\" target=\"12\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"63\" target=\"52\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"8\" target=\"31\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"8\" target=\"58\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"14\" target=\"15\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"14\" target=\"42\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"20\" target=\"39\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"20\" target=\"5\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"13\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"28\" target=\"1\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"30\" target=\"64\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"32\" target=\"30\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"8\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"45\" target=\"1\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"55\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"57\" target=\"61\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"54\" target=\"51\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"50\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"11\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"6\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"37\" target=\"59\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"33\" target=\"53\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"16\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"26\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"55\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"16\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"43\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"37\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"20\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"22\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"40\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"32\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"9\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"28\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"26\" target=\"29\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"53\" target=\"44\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"42\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"36\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"38\" target=\"44\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"3\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"63\" target=\"12\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"50\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"50\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"58\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"32\" target=\"50\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"40\" target=\"15\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"0\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"55\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"57\" target=\"8\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge></graph></graphml>";
        //loadGraphMLString(s);

        
        ioh.read(parent.view.getGraph2D(), resource);
      } catch (Exception e) {
        String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
        D.bug(message);
        throw new RuntimeException(message, e);
      }
      parent.view.getGraph2D().setURL(resource);
      parent.view.fitContent();
      parent.view.updateView();
    }
  
  /**
   * Fill the Graph with explicit information contained within the graphML string.
   * @param gmlString
   */
  public void loadGraphMLString(String gmlString) {
	  byte[] bytes = gmlString.getBytes();
	  
	  InputStream is = new ByteArrayInputStream(bytes);
	  
	  try {
	        IOHandler ioh = null;
	        ioh = createGraphMLIOHandler();
	        parent.view.getGraph2D().clear();
	        
	        ioh.read(parent.view.getGraph2D(), is);
	      } catch (IOException e) {
	        String message = "Unexpected error while loading resource \"" + "\" due to " + e.getMessage();
	        D.bug(message);
	        throw new RuntimeException(message, e);
	      }
  }
  
  private Map<NodeView, Node> currentNodes = null;
  private List<EdgeView> currentEdges = null;
  public void loadGraph(Collection<GraphView> graphs) {
	  
	  Graph2D graph = parent.view.getGraph2D();
	  graph.clear();

	  currentNodes = new HashMap<NodeView, Node>();
	  currentEdges = new ArrayList<EdgeView>();
	  Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
	  
	  for (GraphView view : graphs) {
		  List<NodeView> nodes = view.getNodes();
		  List<EdgeView> edges = view.getEdges();
		  D.bug("Before: " + nodes.size() + " nodes & " + edges.size() + " edges");

		  NodeRealizer realizer = graph.getDefaultNodeRealizer().createCopy();
		  for (EdgeView edge : edges) {
			  NodeView from = edge.getFromNode();
			  NodeView to = edge.getToNode();

			  Node tempFrom;
			  if (currentNodes.containsKey(from)) {
				  tempFrom = currentNodes.get(from);
			  }
			  else {
				  realizer = graph.getDefaultNodeRealizer().createCopy();
				  realizer.setLabelText(from.getLabel());
				  realizer.setFillColor(from.getColor());
				  tempFrom = graph.createNode(realizer);
				  currentNodes.put(from, tempFrom);
			  }
			  Node tempTo;
			  if (currentNodes.containsKey(to)) {
				  tempTo = currentNodes.get(to);
			  }
			  else {
				  realizer = graph.getDefaultNodeRealizer().createCopy();
				  realizer.setLabelText(to.getLabel());
				  realizer.setFillColor(to.getColor());
				  tempTo = graph.createNode(realizer);
				  currentNodes.put(to, tempTo);
			  }
			  
			  if (nodeMap.containsKey(tempFrom)) {
				  nodeMap.get(tempFrom).add(tempTo);
			  }
			  else {
				  ArrayList<Node> toNodes = new ArrayList<Node>();
				  toNodes.add(tempTo);
				  nodeMap.put(tempFrom, toNodes);
			  }
			  currentEdges.add(edge);
		  }
		  
		  for (Node from : nodeMap.keySet()) {
			  D.bug("From " + from);			  
			  List<Node> toNodes = nodeMap.get(from);
			  for (Node to : toNodes) {
				  D.bug("\tTo " + to);
				  graph.createEdge(from, to);
			  }
		  }			  
		  for (NodeView node : nodes) {
			  if (!currentNodes.containsKey(node)) {
				  realizer = graph.getDefaultNodeRealizer().createCopy();
				  realizer.setLabelText(node.getLabel());
				  realizer.setFillColor(node.getColor());
				  Node temp = graph.createNode(realizer);
				  currentNodes.put(node, temp);
				  D.bug("Adding ophaned node");
			  }
		  }	
		  D.bug("After: " + currentNodes.size() + " nodes & " + currentEdges.size() + " edges");		  
	  }
  }
  
  public void loadGraph(String resourceString) {
      try {
    	  URL resource = new URL("File:///U:/" + resourceString);
    	  loadGraph(resource);
      } catch (Exception e) {
      	
      }
    }
  
}
