package teo.isgci.yfiles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;

import teo.isgci.core.App;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import teo.isgci.core.EdgeView;
import teo.isgci.core.GraphView;
import teo.isgci.core.IDrawingService;
import teo.isgci.core.NodeView;
import teo.isgci.data.gc.GraphClass;
import teo.isgci.view.gui.ISGCIMainFrame;
import teo.isgci.view.gui.PSGraphics;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.geom.OrientedRectangle;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.option.OptionHandler;
import y.util.D;
import y.util.Maps;
import y.view.Arrow;
import y.view.AutoDragViewMode;
import y.view.BridgeCalculator;
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.MovePortMode;
import y.view.NavigationComponent;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.Overview;
import y.view.ShapeNodeRealizer;
import y.view.ShinyPlateNodePainter;
import y.view.SmartEdgeLabelModel;
import y.view.SmartNodeLabelModel;
import y.view.TooltipMode;
import y.view.ViewMode;
import y.view.YLabel;
import y.view.GenericNodeRealizer.Factory;
import yext.export.io.EPSOutputHandler;
import yext.svg.io.SVGIOHandler;

public class YFilesDrawingService implements IDrawingService {

	private Graph2DView graphView = null;
	public Graph2D graph2D = null;
	private IncrementalHierarchicLayouter layouter = null;
	private Map<NodeView, Node> currentNodes = null;
	private List<EdgeView> currentEdges = null;
	private ISGCIMainFrame parent;
	private PageFormat pageFormat;

	private static final Color LABEL_LINE_COLOR = new Color(153, 204, 255, 255);
	private static final Color LABEL_BACKGROUND_COLOR = Color.WHITE;
	private static final String AUTO_FLIPPING_CONFIG = "AutoFlipConfig";
	public static final String NODE_CONFIGURATION = "myConf";

	static {
		registerDefaultNodeConfiguration(true);
	}

	public YFilesDrawingService(ISGCIMainFrame parent) {
		/* Store the parent frame */
		this.parent = parent;

		/* Create the graph view */
		this.graphView = new Graph2DView();
		this.graphView.setFitContentOnResize(true);
		registerViewModes();

		/* Zoom in/out at mouse pointer location */
		Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
		wheelZoomListener.setCenterZooming(false);
		this.graphView.getCanvasComponent().addMouseWheelListener(
				wheelZoomListener);

		this.graph2D = graphView.getGraph2D();
		applyRealizerDefaults(this.graph2D, true, true);

		/* Create the layouter */
		layouter = new IncrementalHierarchicLayouter();
		layouter.setOrthogonallyRouted(true);
		layouter.setRecursiveGroupLayeringEnabled(false);

		/* Add the glass panel */
		addGlassPaneComponents();

		/* Create a hierarchy manager with the given graph */
		// new HierarchyManager(rootGraph) //
		// createHierarchyManager(this.graph2D);
		// TINO

		/* Register the graph view with the DefaultGraph2DRenderer */
		BridgeCalculator bridgeCalculator = new BridgeCalculator();
		((DefaultGraph2DRenderer) this.graphView.getGraph2DRenderer())
				.setBridgeCalculator(bridgeCalculator);

	}

	@Override
	public JComponent getCanvas() {
		return this.graphView;
	}

	@Override
	public void initializeView(List<GraphView> graphs) {
		D.bug("entering initializeView");
		graph2D.clear();

		currentNodes = new HashMap<NodeView, Node>();
		currentEdges = new ArrayList<EdgeView>();
		Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

		for (GraphView view : graphs) {
			List<NodeView> nodes = view.getNodes();
			List<EdgeView> edges = view.getEdges();
			D.bug("Before: " + nodes.size() + " nodes & " + edges.size()
					+ " edges");

			NodeRealizer realizer = graph2D.getDefaultNodeRealizer()
					.createCopy();
			for (EdgeView edge : edges) {
				processEdge(nodeMap, edge);
			}

			for (Node from : nodeMap.keySet()) {
				D.bug("From " + from);
				List<Node> toNodes = nodeMap.get(from);
				for (Node to : toNodes) {
					D.bug("\tTo " + to);
					EdgeRealizer edgeRealizer = graph2D
							.getDefaultEdgeRealizer().createCopy();
					edgeRealizer.setTargetArrow(Arrow.STANDARD);
					
					Edge edge = graph2D.createEdge(from, to, edgeRealizer);
				}
			}
			for (NodeView node : nodes) {
				if (!currentNodes.containsKey(node)) {
					realizer = graph2D.getDefaultNodeRealizer().createCopy();
					realizer.setLabelText(node.getLabel());
					realizer.setFillColor(node.getColor());
					Node temp = graph2D.createNode(realizer);
					currentNodes.put(node, temp);
					D.bug("Adding ophaned node");
				}
			}
			D.bug("After: " + currentNodes.size() + " nodes & "
					+ currentEdges.size() + " edges");
		}
		this.refreshView();
	}

	@Override
	public void updateView(List<GraphView> graphs) {
		Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();

		for (GraphView view : graphs) {
			List<NodeView> nodes = view.getNodes();
			List<EdgeView> edges = view.getEdges();
			D.bug("Before: " + nodes.size() + " nodes & " + edges.size()
					+ " edges");

			NodeRealizer realizer = graph2D.getDefaultNodeRealizer().createCopy();
			
			/* Create a copy of the current edges so we can keep track of which
			 * ones have been removed and which of the new edges aren't present.
			 */
			List<EdgeView> excessEdges = new ArrayList<EdgeView>(this.currentEdges);
			for (EdgeView edge : edges) {
				if (excessEdges.contains(edge)) {
					/* Remove the edge from the queue so that we only have
					 * missing edges left in the collection at the end.
					 */
					excessEdges.remove(edge);
				}
				else {
					/* This is a new edge */
					processEdge(nodeMap, edge);
				}
			}
			
			/* Remove the excess edges */
			for (EdgeView edge : excessEdges) {
				
				/* This next bit doesn't work as the nodes are not
				 * found in the currentNodes collection.
				 * It doesn't matter in the current implementation
				 * as edges cannot be removed separately and when
				 * a node is removed the associated edges will also
				 * be removed.
				 */
				D.bug("From: " + edge.getFrom().toString());
				D.bug("To: " + edge.getTo().toString());				
				Node from = this.currentNodes.get(edge.getFrom());
				Node to = this.currentNodes.get(edge.getTo());				
				D.bug("From node found: " + (from != null));
				D.bug("To node found: " + (to != null));
				
				if ((from != null) && (to != null)) {
					/* Never runs - see above */
					Edge edgeToRemove = from.getEdge(to);
					this.graph2D.removeEdge(edgeToRemove);
				}
				/* Remove the edge from the main collection */
				this.currentEdges.remove(edge);
			}

			/* Add the new edges */
			for (Node from : nodeMap.keySet()) {
				D.bug("From " + from);
				List<Node> toNodes = nodeMap.get(from);
				for (Node to : toNodes) {
					D.bug("\tTo " + to);
					EdgeRealizer edgeRealizer = graph2D
							.getDefaultEdgeRealizer().createCopy();
					edgeRealizer.setTargetArrow(Arrow.STANDARD);
					graph2D.createEdge(from, to, edgeRealizer);
				}
			}
			
			/* Remove any excess nodes */
			List<NodeView> excessNodes = new ArrayList<NodeView>(this.currentNodes.keySet());
			for (NodeView node : nodes) {
				if (excessNodes.contains(node)) {
					excessNodes.remove(node);
				}				
			}
			for (NodeView node : excessNodes) {
				this.graph2D.removeNode(this.currentNodes.get(node));
				this.currentNodes.remove(node);
			}
			
			/* Add the nodes that have no edges */
			for (NodeView node : nodes) {
				if (!currentNodes.containsKey(node)) {
					realizer = graph2D.getDefaultNodeRealizer().createCopy();
					realizer.setLabelText(node.getLabel());
					realizer.setFillColor(node.getColor());
					Node temp = graph2D.createNode(realizer);
					currentNodes.put(node, temp);
				}
			}
		}
		this.refreshView();
	}

	@Override
	public void updateColors() {
        for (NodeView nodeView : this.currentNodes.keySet())
        {
        	Node n = this.currentNodes.get(nodeView);
        	ShapeNodeRealizer nr = (ShapeNodeRealizer)graph2D.getRealizer(n);          
        	nr.setFillColor(nodeView.getColor());
          	nr.repaint();
        }
	}

	@Override
	public void refreshView() {
		updateNodeSize();
		layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
		final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
		layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
		layoutExecutor.doLayout(this.graphView, layouter);
		this.graphView.fitContent();
	}

	@Override
	/**
	 * Fill the Graph with explicit information contained within the graphML string.
	 * @param gmlString
	 */
	public void loadGraphML(String gmlString) {
		byte[] bytes = gmlString.getBytes();

		InputStream is = new ByteArrayInputStream(bytes);

		try {
			IOHandler ioh = new GraphMLIOHandler();

			ioh.read(this.graph2D, is);
		} catch (IOException e) {
			String message = "Unexpected error while loading resource \""
					+ "\" due to " + e.getMessage();
			D.bug(message);
			throw new RuntimeException(message, e);
		}
		this.refreshView();
	}

	@Override
	public void loadGraphML(URL resource) {
		if (resource == null) {
			String message = "Resource \"" + resource
					+ "\" not found in classpath";
			D.showError(message);
			throw new RuntimeException(message);
		}

		try {
			IOHandler ioh = new GraphMLIOHandler();

			// Tested Version for loading explicit GraphML strings.
			// String s =
			// "<?xml version=\"1.0\" encoding=\"utf-8\"?><graphml xmlns:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><key id=\"d0\" for=\"node\" yfiles.type=\"nodegraphics\"/><key id=\"e0\" for=\"edge\" yfiles.type=\"edgegraphics\"/><graph id=\"isgci\" edgedefault=\"directed\"><desc>ISGCI graph class diagram, generated 2013-06-15 14:55 by http://www.graphclasses.org</desc><node id=\"0\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;binary tree &#8745; partial grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"1\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-chordal &#8745; X-conformal &#8745;...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"2\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite tolerance&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"3\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;chordal bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"4\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(fork,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"5\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; maximum degree 4...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"6\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bi-cograph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"7\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; claw-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"8\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid graph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"9\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;PURE-2-DIR&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"10\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;,sunlet&lt;sub&gt;4&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"11\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;independent module-composed&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"12\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"13\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;binary tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"14\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; probe interval&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"15\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"16\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(claw &#8746; 3K&lt;sub&gt;1&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"17\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;E-free &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"18\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;triad convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"19\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;1-bounded bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"20\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(XC&lt;sub&gt;11&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"21\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tolerance &#8745; tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"22\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;perfect elimination bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"23\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;hypercube&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"24\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;difference&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"25\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;2-bounded bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"26\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"27\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(K&lt;sub&gt;1,4&lt;/sub&gt;,odd-cycle)-free &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"28\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-conformal &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"29\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(P&lt;sub&gt;7&lt;/sub&gt;,odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"30\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;interval bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"31\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;solid grid graph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"32\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe interval bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"33\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;C&lt;sub&gt;6&lt;/sub&gt;-free &#8745; modular&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"34\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;P&lt;sub&gt;6&lt;/sub&gt;-free &#8745; chordal bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"35\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-chordal &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"36\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; unit grid intersection&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"37\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;C&lt;sub&gt;4&lt;/sub&gt;-free &#8745; C&lt;sub&gt;6&lt;/sub&gt;-free &#8745;...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"38\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;median &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"39\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; maximum degree 3&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"40\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;circular convex bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"41\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;partial grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"42\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe interval &#8745; tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"43\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(X&lt;sub&gt;177&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"44\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"45\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-star-chordal&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"46\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;caterpillar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"47\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;interval containment bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"48\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tree convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"49\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"50\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe bipartite chain&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"51\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;almost median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"52\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;absolute bipartite retract&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"53\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;hereditary median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"54\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;premedian&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"55\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(A,T&lt;sub&gt;2&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"56\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FF0000\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(0,2)-colorable&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"57\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;cubical&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"58\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid graph &#8745; maximum degree 3&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"59\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"60\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(P&lt;sub&gt;4&lt;/sub&gt;,triangle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"61\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;partial cube&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"62\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;biconvex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"63\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;modular&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"64\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; tolerance&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"65\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; bithreshold&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"66\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;star convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><edge source=\"1\" target=\"3\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"47\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"5\" target=\"41\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"5\" target=\"27\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"9\" target=\"49\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"9\" target=\"36\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"52\" target=\"33\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"17\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"59\" target=\"21\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"59\" target=\"13\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"47\" target=\"30\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"33\" target=\"3\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"25\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"61\" target=\"54\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"15\" target=\"62\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"16\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"21\" target=\"42\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"59\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"22\" target=\"3\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"63\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"48\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"57\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"35\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"26\" target=\"10\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"53\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"23\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"38\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"53\" target=\"59\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"62\" target=\"2\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"64\" target=\"14\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"64\" target=\"21\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"29\" target=\"6\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"29\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"31\" target=\"44\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"13\" target=\"0\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"35\" target=\"45\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"36\" target=\"30\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"38\" target=\"59\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"39\" target=\"27\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"66\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"18\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"49\" target=\"5\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"49\" target=\"38\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"51\" target=\"12\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"63\" target=\"52\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"8\" target=\"31\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"8\" target=\"58\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"14\" target=\"15\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"14\" target=\"42\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"20\" target=\"39\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"20\" target=\"5\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"13\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"28\" target=\"1\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"30\" target=\"64\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"32\" target=\"30\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"8\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"45\" target=\"1\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"55\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"57\" target=\"61\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"54\" target=\"51\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"50\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"11\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"6\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"37\" target=\"59\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"33\" target=\"53\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"16\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"26\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"55\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"16\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"43\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"37\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"20\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"22\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"40\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"32\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"9\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"28\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"26\" target=\"29\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"53\" target=\"44\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"42\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"36\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"38\" target=\"44\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"3\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"63\" target=\"12\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"50\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"50\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"58\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"32\" target=\"50\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"40\" target=\"15\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"0\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"55\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"57\" target=\"8\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge></graph></graphml>";
			// loadGraphMLString(s);

			ioh.read(this.graph2D, resource);
		} catch (Exception e) {
			String message = "Unexpected error while loading resource \""
					+ resource + "\" due to " + e.getMessage();
			D.bug(message);
			throw new RuntimeException(message, e);
		}
		this.refreshView();
	}

	@Override
	public void exportGraphics(PSGraphics g) {
		g.drawImage(this.graphView.getImage(), 0, 0, this.graphView);
	}

	/**
	 * Export to Postscript.
	 */
	@Override
	public void exportPS(String file) throws IOException {
		IOHandler ioh = new EPSOutputHandler();
		double tmpPDT = this.graphView.getPaintDetailThreshold();
		this.graphView.setPaintDetailThreshold(0.0);
		ioh.write(this.graph2D, file);
		this.graphView.setPaintDetailThreshold(tmpPDT);
	}

	/**
	 * Export to SVG.
	 */
	@Override
	public void exportSVG(String file) throws IOException {
		IOHandler ioh = new SVGIOHandler();
		double tmpPDT = this.graphView.getPaintDetailThreshold();
		this.graphView.setPaintDetailThreshold(0.0);
		ioh.write(this.graph2D, file);
		this.graphView.setPaintDetailThreshold(tmpPDT);
	}

	/**
	 * Export to GraphML.
	 */
	@Override
	public void exportGML(String file) throws IOException {
		if (!file.endsWith(".graphml")) {
			file += ".graphml";
		}
		IOHandler ioh = new GraphMLIOHandler();
		ioh.write(this.graph2D, file);
	}

	@Override
	public void selectNeighbors() {
		Graph2D g = this.graphView.getGraph2D();
		NodeCursor n = g.selectedNodes();
		ArrayList<Node> l = new ArrayList<Node>();

		for (int i = 0; i < n.size(); ++i) {
			NodeCursor neigh = n.node().neighbors();
			for (int j = 0; j < neigh.size(); ++j) {
				l.add(neigh.node());
				neigh.next();
			}
			n.next();
		}
		for (Node no : l) {
			this.graphView.getGraph2D().setSelected(no, true);
		}
		this.graphView.updateView();
	}

	@Override
	public void selectSuperClasses() {
		Graph2D g = this.graphView.getGraph2D();
		NodeCursor n = g.selectedNodes();
		ArrayList<Node> l = new ArrayList<Node>();

		for (int i = 0; i < n.size(); ++i) {
			NodeCursor neigh = n.node().successors();
			for (int j = 0; j < neigh.size(); ++j) {
				l.add(neigh.node());
				neigh.next();
			}
			n.next();
		}
		for (Node no : l) {
			this.graphView.getGraph2D().setSelected(no, true);
		}
		this.graphView.updateView();
	}

	@Override
	public void selectSubClasses() {
		Graph2D g = this.graphView.getGraph2D();
		NodeCursor n = g.selectedNodes();
		ArrayList<Node> l = new ArrayList<Node>();

		for (int i = 0; i < n.size(); ++i) {
			NodeCursor neigh = n.node().predecessors();
			for (int j = 0; j < neigh.size(); ++j) {
				l.add(neigh.node());
				neigh.next();
			}
			n.next();
		}
		for (Node no : l) {
			this.graphView.getGraph2D().setSelected(no, true);
		}
		this.graphView.updateView();
	}

	@Override
	public Collection<GraphClass> getSelection() {
		Graph2D g = this.graphView.getGraph2D();
		NodeCursor n = g.selectedNodes();
		Collection<GraphClass> graphClasses = new ArrayList<GraphClass>();
		for (int i = 0; i < n.size(); ++i) {
			String s = n.current().toString();
			if (s.contains("<sub>")) {
				if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>"))
						.contains(",")) {
					s = s.replace("<sub>", "_{");
					s = s.replace("</sub>", "}");
				} else {
					s = s.replace("<sub>", "_");
					s = s.replace("</sub>", "");
				}
			}
			graphClasses.add(App.DataProvider.getClass(s));
			n.next();
		}
		return graphClasses;
	}

	@Override
	public void search(NodeView view) {

		// parent.graphCanvas.markOnly(view);
		for (NodeCursor nc = this.graphView.getGraph2D().nodes(); nc.ok(); nc
				.next()) {
			Node n = nc.node();
			String s = n.toString();

			if (s.contains("<sub>")) {
				if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>"))
						.contains(",")) {
					s = s.replace("<sub>", "_{");
					s = s.replace("</sub>", "}");
				} else {
					s = s.replace("<sub>", "_");
					s = s.replace("</sub>", "");
				}
			}
			System.out.println(view.getLabel() + " " + s + " "
					+ view.toString());
			if (view.getLabel().equals(s)) {
				System.out.println("JEP");
				this.graphView.getGraph2D().setSelected(n, true);
			}
			this.graphView.repaint();
		}
	}

	@Override
	public void print() {
		/* Setup option handler */
		OptionHandler printOptions = new OptionHandler("Print Options");
		printOptions.addInt("Poster Rows", 1);
		printOptions.addInt("Poster Columns", 1);
		printOptions.addBool("Add Poster Coords", false);
		final String[] area = { "View", "Graph" };
		printOptions.addEnum("Clip Area", area, 1);

		Graph2DPrinter gprinter = new Graph2DPrinter(this.graphView);

		/* Show custom print dialog and adopt values */
		if (!printOptions.showEditor(this.graphView.getFrame())) {
			return;
		}
		gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
		gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
		gprinter.setPrintPosterCoords(printOptions.getBool("Add Poster Coords"));
		if ("Graph".equals(printOptions.get("Clip Area"))) {
			gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
		} else {
			gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
		}

		/* Show default print dialogs */
		PrinterJob printJob = PrinterJob.getPrinterJob();
		if (pageFormat == null) {
			pageFormat = printJob.defaultPage();
		}
		PageFormat pf = printJob.pageDialog(pageFormat);
		if (pf == pageFormat) {
			return;
		} else {
			pageFormat = pf;
		}

		/*
		 * Setup print job Graph2DPrinter is of type Printable
		 */
		printJob.setPrintable(gprinter, pageFormat);

		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public URL getCurrentFile() {
		return this.graph2D.getURL();
	}
	


	
	
	
	
	
	


	private void updateNodeSize() {
		// Find the maximum width of all used Labels.
		// Alternative: Skip the first run and set the current labelwidth for
		// all node = different nodeswidths
		double maxlen = 0;
		for (NodeCursor nc = graph2D.nodes(); nc.ok(); nc.next()) {
			Node n = nc.node();
			ShapeNodeRealizer nr = (ShapeNodeRealizer) graph2D.getRealizer(n);
			nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
			nr.getLabel().setFontSize(5);
			configureNodeLabel(nr.getLabel(),
					SmartNodeLabelModel.POSITION_CENTER);

			double len = nr.getLabel().getContentWidth();

			if (len > maxlen) {
				maxlen = len;
			}
		}

		for (NodeCursor nc = graph2D.nodes(); nc.ok(); nc.next()) {
			Node n = nc.node();

			ShapeNodeRealizer nr = (ShapeNodeRealizer) graph2D.getRealizer(n);
			nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);

			nr.setSize(Math.max(maxlen / 2, 30), 20);

			nr.repaint();
		}
	}

	private void processEdge(Map<Node, List<Node>> nodeMap, EdgeView edge) {
		NodeView from = edge.getFromNode();
		NodeView to = edge.getToNode();

		Node tempFrom;
		if (currentNodes.containsKey(from)) {
			tempFrom = currentNodes.get(from);
		} else {
			tempFrom = createNode(from);
		}
		Node tempTo;
		if (currentNodes.containsKey(to)) {
			tempTo = currentNodes.get(to);
		} else {
			tempTo = createNode(to);
		}

		if (nodeMap.containsKey(tempFrom)) {
			nodeMap.get(tempFrom).add(tempTo);
		} else {
			ArrayList<Node> toNodes = new ArrayList<Node>();
			toNodes.add(tempTo);
			nodeMap.put(tempFrom, toNodes);
		}
		currentEdges.add(edge);
	}
	
	private Node createNode(NodeView nodeView) {

		/* Create a new node */
		NodeRealizer realizer = graph2D.getDefaultNodeRealizer().createCopy();
		realizer.setLabelText(nodeView.getLabel());
		realizer.setFillColor(nodeView.getColor());
		Node node = graph2D.createNode(realizer);
		
		/* Keep the collections up to date */
		currentNodes.put(nodeView, node);
		return node;
	}

	private static void applyRealizerDefaults(Graph2D graph,
			boolean applyDefaultSize, boolean applyFillColor) {
		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
			GenericNodeRealizer gnr = new GenericNodeRealizer(
					graph.getRealizer(nc.node()));
			gnr.setConfiguration(NODE_CONFIGURATION);
			if (applyFillColor) {
				gnr.setFillColor(graph.getDefaultNodeRealizer().getFillColor());
			}
			gnr.setLineColor(null);
			if (applyDefaultSize) {
				gnr.setSize(graph.getDefaultNodeRealizer().getWidth(), graph
						.getDefaultNodeRealizer().getHeight());
			}
			NodeLabel label = gnr.getLabel();
			OrientedRectangle labelBounds = label.getOrientedBox();
			SmartNodeLabelModel model = new SmartNodeLabelModel();
			label.setLabelModel(model);
			label.setModelParameter(model
					.createModelParameter(labelBounds, gnr));
			graph.setRealizer(nc.node(), gnr);
		}
	}

	private static void registerDefaultNodeConfiguration(boolean drawShadows) {
		Factory factory = GenericNodeRealizer.getFactory();
		Map configurationMap = factory.createDefaultConfigurationMap();

		ShinyPlateNodePainter painter = new ShinyPlateNodePainter();
		// ShinyPlateNodePainter has an option to draw a drop shadow that is
		// more efficient
		// than wrapping it in a ShadowNodePainter.
		painter.setDrawShadow(drawShadows);

		configurationMap.put(GenericNodeRealizer.Painter.class, painter);
		configurationMap.put(GenericNodeRealizer.ContainsTest.class, painter);
		factory.addConfiguration(NODE_CONFIGURATION, configurationMap);
	}

	private void addGlassPaneComponents() {
		// get the glass pane
		JPanel glassPane = this.graphView.getGlassPane();
		// set an according layout manager
		glassPane.setLayout(new BorderLayout());

		JPanel toolsPanel = new JPanel(new GridBagLayout());
		toolsPanel.setOpaque(false);
		toolsPanel.setBackground(null);
		toolsPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 0));

		// create and add the overview to the tools panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(0, 0, 16, 0);
		JComponent overview = createOverview(this.graphView);
		toolsPanel.add(overview, gbc);

		// create and add the navigation component to the tools panel
		NavigationComponent navigationComponent = createNavigationComponent(
				this.graphView, 20, 30);
		toolsPanel.add(navigationComponent, gbc);

		// add the toolspanel to the glass pane
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		JViewport viewport = new JViewport();
		viewport.add(toolsPanel);
		viewport.setOpaque(false);
		viewport.setBackground(null);
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.setOpaque(false);
		westPanel.setBackground(null);
		westPanel.add(viewport, BorderLayout.NORTH);
		glassPane.add(westPanel, BorderLayout.WEST);
	}

	private NavigationComponent createNavigationComponent(Graph2DView view,
			double scrollStepSize, int scrollTimerDelay) {
		// create the NavigationComponent itself
		final NavigationComponent navigation = new NavigationComponent(view);
		navigation.setScrollStepSize(scrollStepSize);
		// set the duration between scroll ticks
		navigation.putClientProperty("NavigationComponent.ScrollTimerDelay",
				new Integer(scrollTimerDelay));
		// set the initial duration until the first scroll tick is triggered
		navigation.putClientProperty(
				"NavigationComponent.ScrollTimerInitialDelay", new Integer(
						scrollTimerDelay));
		// set a flag so that the fit content button will adjust the viewports
		// in an animated fashion
		navigation.putClientProperty("NavigationComponent.AnimateFitContent",
				Boolean.TRUE);

		// add a mouse listener that will make a semi transparent background, as
		// soon as the mouse enters this component
		navigation.setBackground(new Color(255, 255, 255, 0));
		MouseAdapter navigationToolListener = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				Color background = navigation.getBackground();
				// add some semi transparent background
				navigation.setBackground(new Color(background.getRed(),
						background.getGreen(), background.getBlue(), 196));
			}

			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				Color background = navigation.getBackground();
				// make the background completely transparent
				navigation.setBackground(new Color(background.getRed(),
						background.getGreen(), background.getBlue(), 0));
			}
		};
		navigation.addMouseListener(navigationToolListener);

		// add mouse listener to all sub components of the navigationComponent
		for (int i = 0; i < navigation.getComponents().length; i++) {
			Component component = navigation.getComponents()[i];
			component.addMouseListener(navigationToolListener);
		}

		return navigation;
	}

	private void registerViewModes() {

		EditMode editMode = createEditMode();
		editMode.allowNodeCreation(false);
		editMode.allowEdgeCreation(false);
		editMode.allowNodeEditing(false);

		if (editMode != null) {
			this.graphView.addViewMode(editMode);
		}

		this.graphView.addViewMode(new AutoDragViewMode());

		TooltipMode tooltipMode = createTooltipMode();
		if (tooltipMode != null) {
			this.graphView.addViewMode(tooltipMode);
		}

		// view.addViewMode(new AutoDragViewMode());
		/*
		 * editMode.setPopupMode(new PopupMode() { public JPopupMenu
		 * getEdgePopup(Edge e) { JPopupMenu pm = new JPopupMenu(); pm.add(new
		 * EditLabel(e, par)); return pm; } });
		 */
	}

	private Overview createOverview(Graph2DView view) {
		Overview ov = new Overview(view);
		// animates the scrolling
		ov.putClientProperty("Overview.AnimateScrollTo", Boolean.TRUE);
		// blurs the part of the graph which can currently not be seen
		ov.putClientProperty("Overview.PaintStyle", "Funky");
		// allows zooming from within the overview
		ov.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
		// provides functionality for navigation via keybord (zoom in (+), zoom
		// out (-), navigation with arrow keys)
		ov.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
		// determines how to differ between the part of the graph that can
		// currently be seen, and the rest
		ov.putClientProperty("Overview.Inverse", Boolean.TRUE);
		ov.setPreferredSize(new Dimension(150, 150));
		ov.setMinimumSize(new Dimension(150, 150));

		ov.setBorder(BorderFactory.createEtchedBorder());
		return ov;
	}

	private EditMode createEditMode() {
		EditMode editMode = new EditMode();
		// show the highlighting which is turned off by default
		if (editMode.getCreateEdgeMode() instanceof CreateEdgeMode) {
			((CreateEdgeMode) editMode.getCreateEdgeMode())
					.setIndicatingTargetNode(true);
		}
		if (editMode.getMovePortMode() instanceof MovePortMode) {
			((MovePortMode) editMode.getMovePortMode())
					.setIndicatingTargetNode(true);
		}

		// allow moving view port with right drag gesture
		editMode.allowMovingWithPopup(true);
		editMode.allowLabelSelection(true);

		// add hierarchy actions to the views popup menu
		editMode.setPopupMode(new HierarchicPopupMode(this.parent, this,
				this.graph2D));

		editMode.getMouseInputMode().setNodeSearchingEnabled(true);
		editMode.getMouseInputMode().setEdgeSearchingEnabled(true);

		// Add a visual indicator for the target node of an edge creation -
		// makes it easier to
		// see the target for nested graphs
		ViewMode createEdgeMode = editMode.getCreateEdgeMode();
		if (createEdgeMode instanceof CreateEdgeMode) {
			((CreateEdgeMode) createEdgeMode).setIndicatingTargetNode(true);
		}
		return editMode;
	}

	private void configureDefaultRealizers() {

		NodeRealizer nodeRealizer = this.graph2D.getDefaultNodeRealizer();
		nodeRealizer.setSize(200, 100);
		final NodeLabel nodeLabel = nodeRealizer.getLabel();
		nodeLabel.setText("Smart Node Label");
		nodeLabel.setLineColor(LABEL_LINE_COLOR);
		nodeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
		final SmartNodeLabelModel nodeLabelModel = new SmartNodeLabelModel();
		nodeLabel.setLabelModel(nodeLabelModel);
		nodeLabel.setModelParameter(nodeLabelModel.getDefaultParameter());

		final YLabel.Factory factory = EdgeLabel.getFactory();
		final Map defaultConfigImplementationsMap = factory
				.createDefaultConfigurationMap();
		DefaultLabelConfiguration customLabelConfig = new DefaultLabelConfiguration();
		customLabelConfig.setAutoFlippingEnabled(true);
		defaultConfigImplementationsMap.put(YLabel.Painter.class,
				customLabelConfig);
		defaultConfigImplementationsMap.put(YLabel.Layout.class,
				customLabelConfig);
		defaultConfigImplementationsMap.put(YLabel.BoundsProvider.class,
				customLabelConfig);
		factory.addConfiguration(AUTO_FLIPPING_CONFIG,
				defaultConfigImplementationsMap);

		EdgeRealizer edgeRealizer = this.graph2D.getDefaultEdgeRealizer();
		final EdgeLabel edgeLabel = edgeRealizer.getLabel();
		edgeLabel.setText("Smart Edge Label");
		edgeLabel.setLineColor(LABEL_LINE_COLOR);
		edgeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
		final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel();
		edgeLabel.setLabelModel(edgeLabelModel);
		edgeLabel
				.setModelParameter(edgeLabelModel
						.createDiscreteModelParameter(SmartEdgeLabelModel.POSITION_CENTER));
		edgeLabel.setConfiguration(AUTO_FLIPPING_CONFIG);
	}

	private void configureNodeLabel(NodeLabel label, int position) {
		SmartNodeLabelModel model = new SmartNodeLabelModel();
		label.setLabelModel(model);
		label.setModelParameter(model.createDiscreteModelParameter(position));
	}

	private TooltipMode createTooltipMode() {
		TooltipMode tooltipMode = new TooltipMode();
		tooltipMode.setEdgeTipEnabled(true);

		return tooltipMode;
	}

	private void layoutIncrementally() {
		Graph2D graph = this.graphView.getGraph2D();

		layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);

		// create storage for both nodes and edges
		DataMap incrementalElements = Maps.createHashedDataMap();
		// configure the mode
		final IncrementalHintsFactory ihf = layouter
				.createIncrementalHintsFactory();

		for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
			incrementalElements.set(nc.node(),
					ihf.createLayerIncrementallyHint(nc.node()));
		}

		for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
			incrementalElements.set(ec.edge(),
					ihf.createSequenceIncrementallyHint(ec.edge()));
		}
		graph.addDataProvider(
				IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY,
				incrementalElements);
		try {
			final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
			layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
			layoutExecutor.doLayout(this.graphView, layouter);
		} finally {
			graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
		}
	}

	// private void deleteSelected() {
	// Graph2D g = view.getGraph2D();
	// NodeCursor n = g.selectedNodes();
	// ArrayList<Node> l = new ArrayList<Node>();
	// Collection<GraphClass> gCol = new ArrayList<GraphClass>();
	// for (int i = 0; i < n.size(); ++i) {
	// String s = n.current().toString();
	// if (s.contains("<sub>")) {
	// if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
	// s = s.replace("<sub>", "_{");
	// s = s.replace("</sub>", "}");
	// } else {
	// s = s.replace("<sub>", "_");
	// s = s.replace("</sub>", "");
	// }
	// }
	// gCol.add(DataProvider.getClass(s));
	// n.next();
	// }
	// this.viewManager.del(App.DataProvider.getNodes(gCol, false, false));
	//
	// view.updateView();
	// }

	//
	// public void addSubClasses() {
	// Graph2D g = this.graphView.getGraph2D();
	// NodeCursor n = g.selectedNodes();
	// ArrayList<Node> l = new ArrayList<Node>();
	// Collection<GraphClass> gCol = new ArrayList<GraphClass>();
	// for (int i = 0; i < n.size(); ++i) {
	// Node neigh = n.node();
	// String s = n.current().toString();
	// if (s.contains("<sub>")) {
	// if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
	// s = s.replace("<sub>", "_{");
	// s = s.replace("</sub>", "}");
	// } else {
	// s = s.replace("<sub>", "_");
	// s = s.replace("</sub>", "");
	// }
	// }
	// gCol.add(DataProvider.getClass(s));
	// n.next();
	// }
	// this.viewManager.add(App.DataProvider.getNodes(gCol, false, true));
	//
	// this.graphView.updateView();
	// }
	//
	class OpenFoldersAndLayoutAction extends
			Graph2DViewActions.OpenFoldersAction {

		OpenFoldersAndLayoutAction() {
			super(YFilesDrawingService.this.graphView);
		}

		public void openFolder(Node folderNode, Graph2D graph) {
			NodeList children = new NodeList(graph.getHierarchyManager()
					.getInnerGraph(folderNode).nodes());
			super.openFolder(folderNode, graph);
			graph.unselectAll();
			graph.setSelected(folderNode, true);
			for (NodeCursor nc = children.nodes(); nc.ok(); nc.next()) {
				graph.setSelected(nc.node(), true);
			}

			YFilesDrawingService.this.layoutIncrementally();

			graph.unselectAll();
			graph.setSelected(folderNode, true);
			graph.updateViews();

		}
	}

	/**
	 * Collapse a group node. After collapsing the group node, an incremental
	 * layout is automatically triggered. For this, the collapsed node is
	 * treated as an incremental element.
	 */
	class CloseGroupsAndLayoutAction extends
			Graph2DViewActions.CloseGroupsAction {

		CloseGroupsAndLayoutAction() {
			super(YFilesDrawingService.this.graphView);
		}

		public void closeGroup(Node groupNode, Graph2D graph) {
			super.closeGroup(groupNode, graph);
			graph.unselectAll();
			graph.setSelected(groupNode, true);
			for (EdgeCursor ec = groupNode.edges(); ec.ok(); ec.next()) {
				graph.setSelected(ec.edge(), true);
			}

			YFilesDrawingService.this.layoutIncrementally();
			graph.unselectAll();

			graph.updateViews();
		}
	}

}