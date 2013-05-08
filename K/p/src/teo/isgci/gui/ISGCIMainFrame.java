/*
 * The main window of ISGCI. Also the class to start the program.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIMainFrame.java,v 2.4 2013/04/07 10:51:04 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.DataSet;
import teo.isgci.gc.ForbiddenClass;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.Inclusion;
import teo.isgci.problem.Problem;
import teo.isgci.xml.GraphMLWriter;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.OrientedRectangle;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.organic.OrganicLayouter;
import y.module.SmartOrganicLayoutModule;
import y.module.YModule;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.EditorFactory;
import y.option.GuiFactory;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.D;
import y.util.DefaultMutableValue2D;
import y.util.Maps;
import y.util.Value2D;
import y.view.AutoDragViewMode;
import y.view.BridgeCalculator;
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.GenericNodeRealizer.Factory;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.Graph2DViewRepaintManager;
import y.view.HitInfo;
import y.view.LineType;
import y.view.MovePortMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.ProxyShapeNodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.ShinyPlateNodePainter;
import y.view.SmartEdgeLabelModel;
import y.view.SmartNodeLabelModel;
import y.view.TooltipMode;
import y.view.ViewAnimationFactory;
import y.view.ViewMode;
import y.view.YLabel;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/*import teo.isgci.gc.GraphClass;
import java.util.ArrayList;*/


/** The main frame of the application.
 */
public class ISGCIMainFrame extends JFrame
        implements WindowListener, ActionListener, ItemListener {
	private static final Pattern PATH_SEPARATOR_PATTERN = Pattern.compile("/");
	private static final String BEVEL_NODE_CONFIGURATION = "BevelNodeConfig";
	
	private static final Color LABEL_LINE_COLOR = new Color(153, 204, 255, 255);
	  private static final Color LABEL_BACKGROUND_COLOR = Color.WHITE;
	  private static final String AUTO_FLIPPING_CONFIG = "AutoFlipConfig";
	  EdgeMap preferredEdgeLengthMap;
	  YModule module;
	protected static final Map actionNames;
	  static {
	    actionNames = new HashMap();
	    actionNames.put(Graph2DViewActions.CLOSE_GROUPS, "Close Selected Groups");
	    actionNames.put(Graph2DViewActions.OPEN_FOLDERS, "Open Selected Folders");
	    actionNames.put(Graph2DViewActions.GROUP_SELECTION, "Group Selection");
	    actionNames.put(Graph2DViewActions.UNGROUP_SELECTION, "Ungroup Selection");
	    actionNames.put(Graph2DViewActions.FOLD_SELECTION, "Fold Selection");
	    
	    

	    actionNames.put("CREATE_NEW_GROUP_NODE_ACTION", "Create Empty Group");
	    actionNames.put("CREATE_NEW_FOLDER_NODE_ACTION", "Create Empty Folder");
	    
	  }

    public static final String APPLICATIONNAME = "ISGCI";
    public static final String CONFIGURATION_GROUP = "GroupingDemo";
    private IncrementalHierarchicLayouter layouter;
    private OptionHandler groupLayoutOptions;

    public static ISGCIMainFrame tracker; // Needed for MediaTracker (hack)
    public static LatexGraphics latex;
    public static Font font;

    protected teo.Loader loader;
    private BridgeCalculator bridgeCalculator;

    // The menu
    protected JMenuItem miNew, miExport, miExit;
    protected JMenuItem miNaming, miSearching, miDrawUnproper;
    protected JMenuItem miSelectGraphClasses, miCheckInclusion;
    protected JMenuItem miGraphClassInformation;
    protected JMenuItem miCut, miCopy, miPaste, miDelete, miSelectAll;
    protected JMenu miOpenProblem, miColourProblem;
    protected JMenuItem miSmallgraphs, miHelp, miAbout;
    
    private static boolean check = false;

    // This is where the drawing goes.
    protected JScrollPane drawingPane;
    public ISGCIGraphCanvas graphCanvas;
    private JPanel mainPan;
    protected Graph2DView view;


    protected void populateGroupingMenu(JMenu hierarchyMenu) {
        // Predefined actions for open/close groups
        registerAction(hierarchyMenu, Graph2DViewActions.CLOSE_GROUPS, true);
        registerAction(hierarchyMenu, Graph2DViewActions.OPEN_FOLDERS, true);

        hierarchyMenu.addSeparator();

        // Predefined actions for group/fold/ungroup
        registerAction(hierarchyMenu, Graph2DViewActions.GROUP_SELECTION, true);
        registerAction(hierarchyMenu, Graph2DViewActions.UNGROUP_SELECTION, true);
        registerAction(hierarchyMenu, Graph2DViewActions.FOLD_SELECTION, true);
      }
    
    protected void registerAction(final Object menu, final Object key, final boolean enabled) {
        final ActionMap viewActions = view.getCanvasComponent().getActionMap();

        final Action action = viewActions.get(key);
        if (action != null) {
          final JMenuItem item = new JMenuItem(action);
          final String name = (String) actionNames.get(key);
          if (name != null) {
            item.setText(name);
          }
          item.setEnabled(enabled);

          final InputMap imap = view.getCanvasComponent().getInputMap();
          final KeyStroke[] keyStrokes = imap.allKeys();
          if (keyStrokes != null) {
            for (int i = 0; i < keyStrokes.length; ++i) {
              if (imap.get(keyStrokes[i]) == key) {
                item.setAccelerator(keyStrokes[i]);
                break;
              }
            }
          }

          if (menu instanceof JMenu) {
            ((JMenu) menu).add(item);
          } else if (menu instanceof JPopupMenu) {
            ((JPopupMenu) menu).add(item);
          }
        }
      }
       
    public boolean export() {
        boolean res = true;
        FileOutputStream f;
        try {
        	
        	File  myFile = new File("D:/myGraph.graphml");
            f = new FileOutputStream(myFile);
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.error(this, "Cannot open file for writing:\n");
            return false;
        }

        try {
               exportGML(f);            
        } catch (Exception e) {
            res = false;
            e.printStackTrace();
            MessageDialog.error(this, "Error while exporting:\n"+
                e.toString());
        }
        return res;
    }


    

    /**
     * Export to GraphML.
     */
    protected void exportGML(FileOutputStream f) throws Exception {
        Exception res = null;
        String outstr;
        Writer out = null;
        
        try {
            out = new OutputStreamWriter(f, "UTF-8");
            GraphMLWriter w = new GraphMLWriter(out,GraphMLWriter.MODE_YED,
                    graphCanvas.getDrawUnproper(),true);

            w.startDocument();
            graphCanvas.write(w);
            w.endDocument();
        } catch (IOException ex)  {
            res = ex;
        } finally {
            out.close();
        }
        
        if (res != null)
            throw res;
    }
    protected Graph2DView createGraphView() {
        Graph2DView view = new Graph2DView();
        view.setFitContentOnResize(true);
        return view;
      }
    
    public static void applyRealizerDefaults(Graph2D graph, boolean applyDefaultSize, boolean applyFillColor) {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          GenericNodeRealizer gnr = new GenericNodeRealizer(graph.getRealizer(nc.node()));
          gnr.setConfiguration(NODE_CONFIGURATION);
          if(applyFillColor) {
            gnr.setFillColor(graph.getDefaultNodeRealizer().getFillColor());
          }
          gnr.setLineColor(null);      
          if(applyDefaultSize) {
            gnr.setSize(graph.getDefaultNodeRealizer().getWidth(), graph.getDefaultNodeRealizer().getHeight());
          }
          NodeLabel label = gnr.getLabel();
          OrientedRectangle labelBounds = label.getOrientedBox();
          SmartNodeLabelModel model = new SmartNodeLabelModel();
          label.setLabelModel(model);
          label.setModelParameter(model.createModelParameter(labelBounds, gnr));
          graph.setRealizer(nc.node(), gnr);      
        }        
      }
    
    public static final String NODE_CONFIGURATION = "myConf";
    
    public static void registerDefaultNodeConfiguration(boolean drawShadows) {
        Factory factory = GenericNodeRealizer.getFactory();
        Map configurationMap = factory.createDefaultConfigurationMap();

        ShinyPlateNodePainter painter = new ShinyPlateNodePainter();
        // ShinyPlateNodePainter has an option to draw a drop shadow that is more efficient
        // than wrapping it in a ShadowNodePainter.
        painter.setDrawShadow(drawShadows);

        configurationMap.put(GenericNodeRealizer.Painter.class, painter);
        configurationMap.put(GenericNodeRealizer.ContainsTest.class, painter);
        factory.addConfiguration(NODE_CONFIGURATION, configurationMap);
      }

    static {
        registerDefaultNodeConfiguration(true);       
      }
    
    /** Creates the frame.
     * @param locationURL The path/URL to the applet/application.
     * @param isApplet true iff the program runs as an applet.
     */
    public ISGCIMainFrame(teo.Loader loader) {
        super(APPLICATIONNAME);
        
        layouter = new IncrementalHierarchicLayouter();
        layouter.setOrthogonallyRouted(true);
        layouter.setRecursiveGroupLayeringEnabled(false);
        view = createGraphView();
        
        preferredEdgeLengthMap = view.getGraph2D().createEdgeMap();
        view.getGraph2D().addDataProvider(OrganicLayouter.PREFERRED_EDGE_LENGTH_DATA, preferredEdgeLengthMap);
        module = new SmartOrganicLayoutModule();
        
        
        
        //loadGraph("resource/organic.graphml");
        
        applyRealizerDefaults(view.getGraph2D(), true, true);

        loader.register();
        this.loader = loader;
        tracker = this;

        DataSet.init(loader, "data/isgci.xml");
        ForbiddenClass.initRules(loader, "data/smallgraphs.xml");
        PSGraphics.init(loader);
        if (latex == null) {
            latex = new LatexGraphics();
            latex.init(loader);
        }

        boolean createMaps = false;
        try {
            createMaps = System.getProperty("org.isgci.mappath") != null;
        } catch (Exception e) {}

        if (createMaps) {       // Create maps and terminate
            createCanvasPanel();
            new teo.isgci.util.LandMark(this).createMaps();
            closeWindow();
        }
        
        final JMenuBar jmb = createMenuBar();
        if (jmb != null) {
            rootPane.add(jmb);
          }
         
        setJMenuBar(createMenus());
        
        getContentPane().add("Center", createMainPanel());
        getContentPane().add("Center", createCanvasPanel());
        
        /*final JToolBar jtb = createToolBar();
        if (jtb != null) {
        	getContentPane().add(jtb, BorderLayout.NORTH);
        }*/
        registerViewListeners();
        registerListeners();
        setLocation(20, 20);
        pack();
        final JToolBar jtb = createToolBar();
        if (jtb != null) {
          mainPan.add(jtb, BorderLayout.NORTH);
        }
        setVisible(true);
        
        Graph2D rootGraph = view.getGraph2D();
        //create a hierarchy manager with the given root graph
        createHierarchyManager(rootGraph);
        configureDefaultGroupNodeRealizers();
        

        //prepare option handler for group layout options
        Object[] groupStrategyEnum = {"Global Layering", "Recursive Layering"};
        Object[] groupAlignmentEnum = {"Top", "Center", "Bottom"};
        groupLayoutOptions = new OptionHandler("Layout Properties");
        ConstraintManager cm = new ConstraintManager(groupLayoutOptions);
        OptionItem gsi = groupLayoutOptions.addEnum("Group Layering Strategy", groupStrategyEnum, 0);
        OptionItem eci = groupLayoutOptions.addBool("Enable Compact Layering", true);
        OptionItem gai = groupLayoutOptions.addEnum("Group Alignment", groupAlignmentEnum, 0);
        cm.setEnabledOnValueEquals(gsi, "Recursive Layering", eci);
        cm.setEnabledOnValueEquals(gsi, "Recursive Layering", gai);
        cm.setEnabledOnCondition(cm.createConditionValueEquals(gsi, "Recursive Layering").and(
            cm.createConditionValueEquals(eci, Boolean.TRUE).inverse()), gai);

        bridgeCalculator = new BridgeCalculator();

        // register it with the DefaultGraph2DRenderer
        ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bridgeCalculator);
        
        view.fitContent();
    }
    
        
    
    protected HierarchyManager createHierarchyManager(Graph2D rootGraph) {
        return new HierarchyManager(rootGraph);
      }
    
    protected HierarchyManager getHierarchyManager() {
        return view.getGraph2D().getHierarchyManager();
      }
    
    void configureGroupLayout() {
        Object gsi = groupLayoutOptions.get("Group Layering Strategy");
        if ("Recursive Layering".equals(gsi)) {
          layouter.setRecursiveGroupLayeringEnabled(true);
        } else if ("Global Layering".equals(gsi)) {
          layouter.setRecursiveGroupLayeringEnabled(false);
        }

        layouter.setGroupCompactionEnabled(groupLayoutOptions.getBool("Enable Compact Layering"));

        Object gai = groupLayoutOptions.get("Group Alignment");
        if ("Top".equals(gai)) {
          layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_TOP);
        } else if ("Center".equals(gai)) {
          layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_CENTER);
        }
        if ("Bottom".equals(gai)) {
          layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_BOTTOM);
        }
      }
    
    protected EditMode createEditMode() {
    	
    	
    	
    	EditMode editMode = new EditMode();
        // show the highlighting which is turned off by default
        if (editMode.getCreateEdgeMode() instanceof CreateEdgeMode) {
          ((CreateEdgeMode) editMode.getCreateEdgeMode()).setIndicatingTargetNode(true);
        }
        if (editMode.getMovePortMode() instanceof MovePortMode) {
          ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);
        }

        //allow moving view port with right drag gesture
        editMode.allowMovingWithPopup(true);
        
        //add hierarchy actions to the views popup menu
        editMode.setPopupMode(createPopupMode());
        editMode.getMouseInputMode().setNodeSearchingEnabled(true);

        //Add a visual indicator for the target node of an edge creation - makes it easier to
        //see the target for nested graphs
        ViewMode createEdgeMode = editMode.getCreateEdgeMode();
        if (createEdgeMode instanceof CreateEdgeMode) {
          ((CreateEdgeMode) createEdgeMode).setIndicatingTargetNode(true);
        }
        return editMode;
      }
    
    protected void configureDefaultRealizers() {  

        NodeRealizer nodeRealizer = view.getGraph2D().getDefaultNodeRealizer();
        nodeRealizer.setSize(200, 100);
        final NodeLabel nodeLabel = nodeRealizer.getLabel();
        nodeLabel.setText("Smart Node Label");
        nodeLabel.setLineColor(LABEL_LINE_COLOR);
        nodeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
        final SmartNodeLabelModel nodeLabelModel = new SmartNodeLabelModel();
        nodeLabel.setLabelModel(nodeLabelModel);
        nodeLabel.setModelParameter(nodeLabelModel.getDefaultParameter());

        final YLabel.Factory factory = EdgeLabel.getFactory();
        final Map defaultConfigImplementationsMap = factory.createDefaultConfigurationMap();
        DefaultLabelConfiguration customLabelConfig = new DefaultLabelConfiguration();
        customLabelConfig.setAutoFlippingEnabled(true);
        defaultConfigImplementationsMap.put(YLabel.Painter.class, customLabelConfig);
        defaultConfigImplementationsMap.put(YLabel.Layout.class, customLabelConfig);
        defaultConfigImplementationsMap.put(YLabel.BoundsProvider.class, customLabelConfig);
        factory.addConfiguration(AUTO_FLIPPING_CONFIG, defaultConfigImplementationsMap);

        EdgeRealizer edgeRealizer = view.getGraph2D().getDefaultEdgeRealizer();
        final EdgeLabel edgeLabel = edgeRealizer.getLabel();
        edgeLabel.setText("Smart Edge Label");
        edgeLabel.setLineColor(LABEL_LINE_COLOR);
        edgeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
        final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel();
        edgeLabel.setLabelModel(edgeLabelModel);
        edgeLabel.setModelParameter(edgeLabelModel.createDiscreteModelParameter(SmartEdgeLabelModel.POSITION_CENTER));
        edgeLabel.setConfiguration(AUTO_FLIPPING_CONFIG);
      }
    
    protected void configureDefaultGroupNodeRealizers() {
        //Create additional configuration for default group node realizers
        DefaultHierarchyGraphFactory hgf = (DefaultHierarchyGraphFactory) getHierarchyManager().getGraphFactory();

        Map map = GenericGroupNodeRealizer.createDefaultConfigurationMap();
        GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

        factory.addConfiguration(CONFIGURATION_GROUP, map);
        Object abf = factory.getImplementation(CONFIGURATION_GROUP,
            GenericGroupNodeRealizer.GenericAutoBoundsFeature.class);
        if (abf instanceof DefaultGenericAutoBoundsFeature) {
          ((DefaultGenericAutoBoundsFeature) abf).setConsiderNodeLabelSize(true);
        }

        GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();

        //Register first, since this will also configure the node label
        gnr.setConfiguration(CONFIGURATION_GROUP);

        //Nicer colors
        gnr.setFillColor(new Color(202,236,255,132));
        gnr.setLineColor(new Color(102, 102,153,255));
        gnr.setLineType(LineType.DOTTED_1);
        NodeLabel label = gnr.getLabel();
        label.setBackgroundColor(new Color(153,204,255,255));
        label.setTextColor(Color.BLACK);
        label.setFontSize(15);

        hgf.setProxyNodeRealizerEnabled(true);

        hgf.setDefaultGroupNodeRealizer(gnr.createCopy());

        //Set the correct initial group state for folder nodes
        gnr.setGroupClosed(true);
        hgf.setDefaultFolderNodeRealizer(gnr.createCopy());
      }
    
    protected void registerViewListeners() {
        Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
        //zoom in/out at mouse pointer location 
        wheelZoomListener.setCenterZooming(false);
        view.getCanvasComponent().addMouseWheelListener(wheelZoomListener);
      }
    
    public void loadInitialGraph() {
    	view.getGraph2D().clear();
    	try {
    	
    	} catch (Exception ex) {
    		
    	}
    	
        loadGraph("myGraph.graphml");
        
        for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next())
        {
          Node n = nc.node();
          
          ShapeNodeRealizer nr = (ShapeNodeRealizer)view.getGraph2D().getRealizer(n);
          nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
          
          if (nr.getFillColor() == Color.WHITE) {
        	  nr.setLineColor(new Color(255, 153, 0));
        	  nr.setFillColor(new Color(255, 153, 0));
          }
          nr.setSize(80, 50);
          nr.getLabel().setFontSize(5);
          
          configureNodeLabel(nr.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
          
          nr.repaint();
        }
        preferredEdgeLengthMap = view.getGraph2D().createEdgeMap();
        dolayout();
     }
    
    private void configureNodeLabel(NodeLabel label, int position) {
        SmartNodeLabelModel model = new SmartNodeLabelModel();
        label.setLabelModel(model);
        label.setModelParameter(model.createDiscreteModelParameter(position));
      }
    
    public void dolayout() {
        layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
        final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
        layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
        layoutExecutor.doLayout(view, layouter);
      }
    
    protected void loadGraph(Class aClass, String resourceString) {
        try {
    	URL resource = new URL("File:///D:/" + resourceString);
        if (resource == null) {
          String message = "Resource \"" + resourceString + "\" not found in classpath of " + aClass;
          D.showError(message);
          
          throw new RuntimeException(message);
        }
        loadGraph(resource);
        } catch (Exception e) {
        	
        }
      }
    
    protected void loadGraph(String resourceString) {
        loadGraph(getClass(), resourceString);
      }
    protected GraphMLIOHandler createGraphMLIOHandler() {
        return new GraphMLIOHandler();
      }

    
    void layoutIncrementally() {
        Graph2D graph = view.getGraph2D();

        layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);

        // create storage for both nodes and edges
        DataMap incrementalElements = Maps.createHashedDataMap();
        // configure the mode
        final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();

        for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
          incrementalElements.set(nc.node(), ihf.createLayerIncrementallyHint(nc.node()));
        }

        for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
          incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
        }
        graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);
        try {
          final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
          layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
          layoutExecutor.doLayout(view, layouter);
        } finally {
          graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
        }
      }
    
    public static JComponent createActionControl(final Action action) {
        return createActionControl(action, true);
      }
    
    public static JComponent createActionControl(final Action action, final boolean showActionText) {
        final JButton jb = new JButton();
        if (action.getValue(Action.SMALL_ICON) != null) {
          jb.putClientProperty("hideActionText", Boolean.valueOf(!showActionText));
        }
        jb.setAction(action);
        return jb;
      }
    protected void loadGraph(URL resource) {
        if (resource == null) {
          String message = "Resource \"" + resource + "\" not found in classpath";
          D.showError(message);
          throw new RuntimeException(message);
        }

        try {
          IOHandler ioh = null;
          ioh = createGraphMLIOHandler();
          view.getGraph2D().clear();
          ioh.read(view.getGraph2D(), resource);
        } catch (IOException e) {
          String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
          D.bug(message);
          throw new RuntimeException(message, e);
        }
        view.getGraph2D().setURL(resource);
        view.fitContent();
        view.updateView();
      }

    /**
     * Write the entire database in GraphML to isgcifull.graphml.
     */
    private void writeGraphML() {
        OutputStreamWriter out = null;

        SimpleDirectedGraph<GraphClass, Inclusion> g =
            new SimpleDirectedGraph<GraphClass, Inclusion>(Inclusion.class);
        Graphs.addGraph(g, DataSet.inclGraph);
        GAlg.transitiveReductionBruteForce(g);

        try {
            out = new OutputStreamWriter(
                    new FileOutputStream("isgcifull.graphml"), "UTF-8");
            GraphMLWriter w = new GraphMLWriter(out,
                        GraphMLWriter.MODE_PLAIN,
                        true,
                        false);
            w.startDocument();
            for (GraphClass gc : g.vertexSet()) {
                w.writeNode(gc.getID(), gc.toString(), Color.WHITE);
            }
            for (Inclusion e : g.edgeSet()) {
                w.writeEdge(e.getSuper().getID(), e.getSub().getID(),
                        e.isProper());
            }
            w.endDocument();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates and attaches the necessary eventlisteners.
     */
    protected void registerListeners() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        miNew.addActionListener(this);
        miExport.addActionListener(this);
        miExit.addActionListener(this);
        miNaming.addActionListener(this);
        miSearching.addActionListener(this);
        miDrawUnproper.addItemListener(this);
        miSelectGraphClasses.addActionListener(this);
        miCheckInclusion.addActionListener(this);
        miGraphClassInformation.addActionListener(this);
        //miDelete.addActionListener(this);
        //miSelectAll.addActionListener(this);
        miOpenProblem.addActionListener(this);
        miSmallgraphs.addActionListener(this);
        miHelp.addActionListener(this);
        miAbout.addActionListener(this);
    }

    protected Action createLoadAction() {
        return new LoadAction();
      }

      protected Action createSaveAction() {
        return new SaveAction();
      }

      protected Action createDeleteSelectionAction() {
        return new DeleteSelection(view);
      }
    
    protected JMenuBar createMenuBar() {
    	JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        Action action;
        action = createLoadAction();
        if (action != null) {
          menu.add(action);
        }
        action = createSaveAction();
        if (action != null) {
          menu.add(action);
        }
        menu.addSeparator();
        menu.add(new PrintAction());
        menu.addSeparator();
        menu.add(new ExitAction());
        menuBar.add(menu);
        
        if (getExampleResources() != null && getExampleResources().length != 0) {
          createExamplesMenu(menuBar);
        }
        
        JMenu menu2 = new JMenu("Grouping");
        populateGroupingMenu(menu2);        
        menuBar.add(menu2);
        
        return menuBar;
      }
    
    protected void createExamplesMenu(JMenuBar menuBar) {
        final String[] fileNames = getExampleResources();
        if (fileNames == null) {
          return;
        }

        final JMenu menu = new JMenu("Example Graphs");
        menuBar.add(menu);

        for (int i = 0; i < fileNames.length; i++) {
          final String filename = fileNames[i];
          final String[] path = PATH_SEPARATOR_PATTERN.split(filename);
          menu.add(new AbstractAction(path[path.length - 1]) {
            public void actionPerformed(ActionEvent e) {
              loadGraph(filename);
            }
          });
        }
      }
    protected String[] getExampleResources() {
        return null;
      }
    /**
     * Creates the menu system.
     * @return The created JMenuBar
     * @see JMenuBar
     */
    protected JMenuBar createMenus() {
        JMenuBar mainMenuBar = new JMenuBar();
        JMenu fileMenu, editMenu, viewMenu,  graphMenu, helpMenu, problemsMenu;
        JMenuItem menu;

        fileMenu = new JMenu("File");
        fileMenu.add(miNew = new JMenuItem("New window"));
        fileMenu.add(miExport = new JMenuItem("Export drawing..."));
        fileMenu.add(miExit = new JMenuItem("Exit"));
        mainMenuBar.add(fileMenu);

        /*editMenu = new Menu("Edit");
        editMenu.add(miDelete = new MenuItem("Delete"));
        editMenu.add(miSelectAll = new MenuItem("Select all"));
        mainMenuBar.add(editMenu);*/

        viewMenu = new JMenu("View");
        viewMenu.add(miSearching = new JMenuItem("Search in drawing..."));
        viewMenu.add(miNaming = new JMenuItem("Naming preference..."));
        viewMenu.add(miDrawUnproper =
                new JCheckBoxMenuItem("Mark unproper inclusions", true));
        /* menu = new ScaleMenu();
        menu.setEnabled(false);
        viewMenu.add(menu); */
        mainMenuBar.add(viewMenu);

        graphMenu = new JMenu("Graph classes");
        miGraphClassInformation = new JMenuItem("Browse Database");
        graphMenu.add(miGraphClassInformation);
        miCheckInclusion = new JMenuItem("Find Relation...");
        graphMenu.add(miCheckInclusion);
        miSelectGraphClasses = new JMenuItem("Draw...");
        graphMenu.add(miSelectGraphClasses);
        mainMenuBar.add(graphMenu);

        problemsMenu = new JMenu("Problems");
        miOpenProblem = new JMenu("Boundary/Open classes");
        problemsMenu.add(miOpenProblem);
        for (int i = 0; i < DataSet.problems.size(); i++) {
            menu = new JMenuItem(
                    ((Problem) DataSet.problems.elementAt(i)).getName());
            miOpenProblem.add(menu);
            menu.addActionListener(this);
            menu.setActionCommand("miOpenProblem");
        }
        miColourProblem = new ProblemsMenu(this, "Colour for problem");
        problemsMenu.add(miColourProblem);
        mainMenuBar.add(problemsMenu);

        
        JMenu menu2 = new JMenu("Grouping");
        
        populateGroupingMenu(menu2);
        mainMenuBar.add(menu2);

        helpMenu = new JMenu("Help");
        miSmallgraphs = new JMenuItem("Small graphs");
        helpMenu.add(miSmallgraphs);
        miHelp = new JMenuItem("Help");
        helpMenu.add(miHelp);
        miAbout = new JMenuItem("About");
        helpMenu.add(miAbout);
        //mainMenuBar.add(Box.createHorizontalGlue());
        mainMenuBar.add(helpMenu);
        
        return mainMenuBar;
    }
    
    protected void populateGroupingPopup(JPopupMenu pm, final double x, final double y, Node node, boolean selected) {
        // Predefined actions for open/close groups
        registerAction(
            pm, Graph2DViewActions.CLOSE_GROUPS,
            node != null && getHierarchyManager().isGroupNode(node));
        registerAction(
            pm, Graph2DViewActions.OPEN_FOLDERS,
            node != null && getHierarchyManager().isFolderNode(node));

        pm.addSeparator();

        // Predefined actions for group/fold/ungroup
        registerAction(pm, Graph2DViewActions.GROUP_SELECTION, selected);
        registerAction(pm, Graph2DViewActions.UNGROUP_SELECTION, selected);
        registerAction(pm, Graph2DViewActions.FOLD_SELECTION, selected);

        pm.addSeparator();

        //We customize both "Create..." actions so that the newly created node lies at the coordinates of the mouse click
        //(for "Group Selection"/"Fold Selection", the location is determined by the content's location instead.
        JMenuItem item = new JMenuItem(new CreateNewGroupNodeAction(view){
          protected void setGroupNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
            graph.setLocation(groupNode, x, y);
          }
        });
        item.setText("Create Empty Group");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        pm.add(item);

        item = new JMenuItem(new CreateNewFolderNodeAction(view){
          protected void setFolderNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
            graph.setLocation(groupNode, x, y);
          }
        });
        item.setText("Create Empty Folder");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        pm.add(item);
        add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);       
        
        
        pm.add(infoItem);
      }
    
    
    JMenuItem  infoItem;

    /**
     * Creates the drawing canvas with scrollbars at the bottom and at the
     * right.
     * @return the panel
     */
    protected JComponent createCanvasPanel() {
        mainPan = new JPanel();
        mainPan.setLayout(new BorderLayout());

        registerViewModes();
        registerViewActions();

        //mainPan.add(graphCanvas, BorderLayout.CENTER);
        mainPan.add(view, BorderLayout.CENTER);
        
        return mainPan;
    }
    
    protected PopupMode createPopupMode() {
        return new HierarchicPopupMode();
      }
    
    protected void registerViewActions() {
        // register keyboard actions
        Graph2DViewActions actions = new Graph2DViewActions(view);
        ActionMap amap = view.getCanvasComponent().getActionMap();
        if (amap != null) {
          InputMap imap = actions.createDefaultInputMap(amap);
          
          InputMap inputMap = view.getCanvasComponent().getInputMap();
          inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "CREATE_NEW_GROUP_NODE_ACTION");
          inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), "CREATE_NEW_FOLDER_NODE_ACTION");
          view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
        }
        
        ActionMap actionMap = view.getCanvasComponent().getActionMap();
        actionMap.put(Graph2DViewActions.DELETE_SELECTION, createDeleteSelectionActionImpl());
        actionMap.put("CREATE_NEW_GROUP_NODE_ACTION", new CreateNewGroupNodeAction());
        actionMap.put("CREATE_NEW_FOLDER_NODE_ACTION", new CreateNewFolderNodeAction());
      }
    
    private Action createDeleteSelectionActionImpl() {
        final Graph2DViewActions.DeleteSelectionAction action =
                new Graph2DViewActions.DeleteSelectionAction(view);
        action.setKeepingParentGroupNodeSizes(true);
        return action;
      }
    
    
    protected TooltipMode createTooltipMode() {
        TooltipMode tooltipMode = new TooltipMode();
        tooltipMode.setEdgeTipEnabled(false);
        return tooltipMode;
      }
   
    protected void registerViewModes() {
    	
    	RollOverViewMode rm = new RollOverViewMode();
    	
        EditMode editMode = createEditMode();
        editMode.allowNodeCreation(false);
        editMode.allowEdgeCreation(false);
        
        if (editMode != null) {
          view.addViewMode(editMode);
        }
    	
    	
        view.addViewMode(new AutoDragViewMode());        
        view.addViewMode(rm); 

        TooltipMode tooltipMode = createTooltipMode();
        if(tooltipMode != null) {
          view.addViewMode(tooltipMode);
        }

        //view.addViewMode(new AutoDragViewMode());
        
        /*editMode.setPopupMode(new PopupMode() {
            public JPopupMenu getEdgePopup(Edge e) {
              JPopupMenu pm = new JPopupMenu();
              pm.add(new EditLabel(e));
              return pm;
            }
          });*/
      }
    
    /**
     * Creates the drawing canvas with scrollbars at the bottom and at the
     * right.
     * @return the panel
     */
    protected JComponent createMainPanel() {
        graphCanvas = new ISGCIGraphCanvas(this);
        drawingPane = new JScrollPane(graphCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        drawingPane.getHorizontalScrollBar().setUnitIncrement(100);
        drawingPane.getVerticalScrollBar().setUnitIncrement(100);
        
        return drawingPane;
    }


    /**
     * Center the canvas on the given point.
     */
    public void centerCanvas(Point p) {
        JViewport viewport = drawingPane.getViewport();
        Dimension port = viewport.getExtentSize();
        Dimension view = viewport.getViewSize();

        p.x -= port.width/2;
        if (p.x + port.width > view.width)
            p.x = view.width - port.width;
        if (p.x < 0)
            p.x = 0;
        p.y -= port.height/2;
        if (p.y + port.height > view.height)
            p.y = view.height - port.height;
        if (p.y < 0)
            p.y = 0;
        viewport.setViewPosition(p);
    }

    public void printPort() {
        Rectangle view = getViewport();
        System.err.println("port: "+view);
    }

    public Rectangle getViewport() {
        return drawingPane.getViewport().getViewRect();
    }

    
    /** Closes the window and possibly terminates the program. */
    public void closeWindow() {
        setVisible(false);
        dispose();
        loader.unregister();
    }

    /**
     * Eventhandler for window events
     */
    public void windowClosing(WindowEvent e) {
        closeWindow();
    }

    /**
     * Required to overload (abstract)
     */
    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}

    protected JToolBar createToolBar() {
    	JToolBar toolBar = new JToolBar();
        
        addLayoutActions(toolBar);    
        
        toolBar.addSeparator();
        toolBar.add(createActionControl(new oLayoutAction()));
        toolBar.add(createActionControl(new OptionAction()));
        toolBar.addSeparator();
        toolBar.add(new AssignLengthsAction());
        toolBar.addSeparator();
        JCheckBoxMenuItem chk = new JCheckBoxMenuItem();
        chk.setText("Animation");
        chk.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent a) {
        		check = !check;
        		/*view.setVisible(check);
        		mainPan.setVisible(check);
        		drawingPane.setVisible(!check);
        		graphCanvas.setVisible(!check);*/
        		
        	}        	
        });
        chk.setVisible(true);
        toolBar.add(chk);

        
        return toolBar;
      }
    
    protected void addLayoutActions(JToolBar toolBar) {
        final Action incrementalLayoutAction = new AbstractAction(
                "Incremental") {
          public void actionPerformed(ActionEvent e) {
            layoutIncrementally();
          }
        };

        final Action layoutAction = new AbstractAction(
                "Complete") {
          public void actionPerformed(ActionEvent e) {
            dolayout();
          }
        };

        final Action propertiesAction = new AbstractAction(
                "Settings...") {
          public void actionPerformed(ActionEvent e) {
            final ActionListener layoutListener = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                dolayout();
              }
            };
            OptionSupport.showDialog(groupLayoutOptions, layoutListener, false, view.getFrame());
            configureGroupLayout();
          }
        };

        toolBar.addSeparator();
        toolBar.add(new JLabel("Layout: "));
        toolBar.add(createActionControl(incrementalLayoutAction));
        toolBar.add(createActionControl(layoutAction));
        toolBar.add(createActionControl(propertiesAction));
      }
    
    /**
     * Eventhandler for menu selections
     */
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();

        Object source = event.getSource();
        if (source == infoItem) {
        	String sub = view.getGraph2D().selectedNodes().node().toString();
        	JDialog d;
        	if (sub.length() >= 0) {
        		//ToDo: Catch unknown Sequences (\cap ... )
        	sub = sub.substring(6, sub.indexOf("</html>"));
        	if (sub.indexOf('?') >= 0) {
        		String newSub = sub.substring(0, sub.indexOf(" "));
        		newSub += "$\\cap$ ";
        		newSub += sub.substring(sub.indexOf("?") + 1);
        		sub = newSub;
        		
        	}
        	System.out.println(sub);
            d = new GraphClassInformationDialog(
                    this, DataSet.getClass(sub));
        	} else {
        		d = new GraphClassInformationDialog(
                        this, DataSet.getClass(view.getGraph2D().selectedNodes().current().toString()));
        	}
            d.setLocation(50, 50);
            d.pack();
            d.setSize(800, 600);
            d.setVisible(true);
        } else if (object == miExit) {
            closeWindow();
        } else if (object == miNew) {
            new ISGCIMainFrame(loader);
        } else if (object == miExport) {
        	export();
            JDialog export = new ExportDialog(this);
            export.setLocation(50, 50);
            export.pack();
            export.setVisible(true);
        } else if (object == miNaming) {
            JDialog d = new NamingDialog(this);
            d.setLocation(50,50);
            d.pack();
            d.setVisible(true);
        } else if (object == miSearching) {
            JDialog search = new SearchDialog(this);
            search.setLocation(50,50);
            search.setVisible(true);
        } else if (object == miGraphClassInformation) {
        	System.out.println(this.getName());
            JDialog info = new GraphClassInformationDialog(this);
            info.setLocation(50, 50);
            info.pack();
            info.setSize(800, 600);
            info.setVisible(true);
        } else if (object == miCheckInclusion) {
            JDialog check = new CheckInclusionDialog(this);
            check.setLocation(50, 50);
            check.pack();
            check.setSize(700, 400);
            check.setVisible(true);
        } else if (object == miSelectGraphClasses) {
            JDialog select = new GraphClassSelectionDialog(this);
            select.setLocation(50, 50);
            select.pack();
            select.setSize(500, 400);
            select.setVisible(true);
        } else if (object == miAbout) {
            JDialog select = new AboutDialog(this);
            select.setLocation(50, 50);
            select.setVisible(true);
        } else if (object == miHelp) {
            loader.showDocument("help.html");
        } else if (object == miSmallgraphs) {
            loader.showDocument("smallgraphs.html");
        } else if (event.getActionCommand() == "miOpenProblem") {
            JDialog open=new OpenProblemDialog(this,
                    ((JMenuItem) event.getSource()).getText());
            open.setLocation(50, 50);
            open.setVisible(true);
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Object object = event.getSource();

        if (object == miDrawUnproper) {
            graphCanvas.setDrawUnproper(
                    ((JCheckBoxMenuItem) object).getState());
        }
    }
    
    
    public static class OptionSupport {
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
    
    
    public class SaveAction extends AbstractAction {
        JFileChooser chooser;

        public SaveAction() {
          super("Save...");
          chooser = null;
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

          URL url = view.getGraph2D().getURL();
          if (url != null && "file".equals(url.getProtocol())) {
            try {
              chooser.setSelectedFile(new File(new URI(url.toString())));
            } catch (URISyntaxException e1) {
              // ignore
            }
          }

          if (chooser.showSaveDialog(mainPan) == JFileChooser.APPROVE_OPTION) {
            String name = chooser.getSelectedFile().toString();
            if(!name.endsWith(".graphml")) {
              name += ".graphml";
            }
            IOHandler ioh = createGraphMLIOHandler();

            try {
              ioh.write(view.getGraph2D(), name);
            } catch (IOException ioe) {
              D.show(ioe);
            }
          }
        }
      }

      /**
       * Action that loads the current graph from a file in GraphML format.
       */
      public class LoadAction extends AbstractAction {
        JFileChooser chooser;

        public LoadAction() {
          super("Load...");
          chooser = null;
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
          if (chooser.showOpenDialog(mainPan) == JFileChooser.APPROVE_OPTION) {
            URL resource = null;
            try {
              resource = chooser.getSelectedFile().toURI().toURL();
            } catch (MalformedURLException urlex) {
              urlex.printStackTrace();
            }
            loadGraph(resource);
          }
        }
      }

      /**
       * Action that deletes the selected parts of the graph.
       */
      public static class DeleteSelection extends AbstractAction {
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
      public static class ExitAction extends AbstractAction {
    	    public ExitAction() {
    	      super("Exit");
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      System.exit(0);
    	    }
    	  }
      
      public class PrintAction extends AbstractAction {
    	    PageFormat pageFormat;

    	    OptionHandler printOptions;

    	    public PrintAction() {
    	      super("Print");

    	      // setup option handler
    	      printOptions = new OptionHandler("Print Options");
    	      printOptions.addInt("Poster Rows", 1);
    	      printOptions.addInt("Poster Columns", 1);
    	      printOptions.addBool("Add Poster Coords", false);
    	      final String[] area = {"View", "Graph"};
    	      printOptions.addEnum("Clip Area", area, 1);
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      Graph2DPrinter gprinter = new Graph2DPrinter(view);

    	      // show custom print dialog and adopt values
    	      if (!printOptions.showEditor(view.getFrame())) {
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

    	      // show default print dialogs
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

    	      // setup print job.
    	      // Graph2DPrinter is of type Printable
    	      printJob.setPrintable(gprinter, pageFormat);

    	      if (printJob.printDialog()) {
    	        try {
    	          printJob.print();
    	        } catch (Exception ex) {
    	          ex.printStackTrace();
    	        }
    	      }
    	    }
    	  }
      
      public static class CreateNewFolderNodeAction extends Graph2DViewActions.AbstractGroupingAction {

    	    public CreateNewFolderNodeAction() {
    	      this(null);
    	    }

    	    public CreateNewFolderNodeAction(final Graph2DView view) {
    	      super("CREATE_NEW_FOLDER_NODE", view);
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      final Graph2DView graph2DView = getView(e);
    	      if (graph2DView != null) {
    	        createFolderNode(graph2DView);
    	        graph2DView.getGraph2D().updateViews();
    	      }
    	    }

    	    /**
    	     * Create an empty folder node, assigns a name and sets the node bounds.
    	     */
    	    protected Node createFolderNode(Graph2DView view) {
    	      final Graph2D graph = view.getGraph2D();
    	      graph.firePreEvent();
    	      Node groupNode;
    	      try {
    	        groupNode = createFolderNodeImpl(graph);
    	        assignFolderName(groupNode, view);
    	        setFolderNodeBounds(view, graph, groupNode);
    	      } finally {
    	        graph.firePostEvent();
    	      }
    	      return groupNode;
    	    }

    	    protected Node createFolderNodeImpl(Graph2D graph) {
    	      return getHierarchyManager(graph).createFolderNode(graph);
    	    }

    	    protected void setFolderNodeBounds(Graph2DView view, Graph2D graph, Node folderNode) {
    	      double x = view.getCenter().getX();
    	      double y = view.getCenter().getY();
    	      graph.setCenter(folderNode, x, y);
    	    }

    	    protected void assignFolderName(Node groupNode, Graph2DView view) {
    	      NodeRealizer nr = view.getGraph2D().getRealizer(groupNode);
    	      if (nr instanceof ProxyShapeNodeRealizer) {
    	        ProxyShapeNodeRealizer pnr = (ProxyShapeNodeRealizer) nr;
    	        pnr.getRealizer(0).setLabelText(createGroupName(groupNode, view));
    	        pnr.getRealizer(1).setLabelText(createFolderName(groupNode, view));
    	      } else {
    	        nr.setLabelText(createGroupName(groupNode, view));
    	      }
    	    }

    	    protected String createFolderName(Node folderNode, Graph2DView view) {
    	      return "Folder";
    	    }

    	    protected String createGroupName(Node groupNode, Graph2DView view) {
    	      return "Group";
    	    }
    	  }
      
      public static class CreateNewGroupNodeAction extends Graph2DViewActions.AbstractGroupingAction {

    	    public CreateNewGroupNodeAction() {
    	      this(null);
    	    }

    	    public CreateNewGroupNodeAction(final Graph2DView view) {
    	      super("CREATE_NEW_GROUP_NODE", view);
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      final Graph2DView graph2DView = getView(e);
    	      if (graph2DView != null) {
    	        createGroupNode(graph2DView);
    	        graph2DView.getGraph2D().updateViews();
    	      }
    	    }

    	    /**
    	     * Create an empty group node, assigns a name and sets the node bounds.
    	     */
    	    protected Node createGroupNode(Graph2DView view) {
    	      final Graph2D graph = view.getGraph2D();
    	      graph.firePreEvent();
    	      Node groupNode;
    	      try {
    	        groupNode = createGroupNodeImpl(graph);
    	        assignGroupName(groupNode, view);
    	        setGroupNodeBounds(view, graph, groupNode);
    	      } finally {
    	        graph.firePostEvent();
    	      }
    	      return groupNode;
    	    }

    	    protected Node createGroupNodeImpl(Graph2D graph) {
    	      return getHierarchyManager(graph).createGroupNode(graph);
    	    }

    	    protected void setGroupNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
    	      double x = view.getCenter().getX();
    	      double y = view.getCenter().getY();
    	      graph.setCenter(groupNode, x, y);
    	    }

    	    protected void assignGroupName(Node groupNode, Graph2DView view) {
    	      NodeRealizer nr = view.getGraph2D().getRealizer(groupNode);
    	      if (nr instanceof ProxyShapeNodeRealizer) {
    	        ProxyShapeNodeRealizer pnr = (ProxyShapeNodeRealizer) nr;
    	        pnr.getRealizer(0).setLabelText(createGroupName(groupNode, view));
    	        pnr.getRealizer(1).setLabelText(createFolderName(groupNode, view));
    	      } else {
    	        nr.setLabelText(createGroupName(groupNode, view));
    	      }
    	    }

    	    protected String createFolderName(Node folderNode, Graph2DView view) {
    	      return "Folder";
    	    }

    	    protected String createGroupName(Node groupNode, Graph2DView view) {
    	      return "Group";
    	    }
    	  }
      
      class HierarchicPopupMode extends PopupMode {
    	    public JPopupMenu getPaperPopup(double x, double y) {
    	      JPopupMenu pm = new JPopupMenu();
    	      populateGroupingPopup(pm, x, y, null, false);
    	      return pm;
    	    }

    	    public JPopupMenu getNodePopup(Node v) {
    	      Graph2D graph = getGraph2D();
    	      JPopupMenu pm = new JPopupMenu();
    	      populateGroupingPopup(pm, graph.getCenterX(v), graph.getCenterY(v), v, true);
    	      return pm;
    	    }

    	    public JPopupMenu getSelectionPopup(double x, double y) {
    	      JPopupMenu pm = new JPopupMenu();
    	      populateGroupingPopup(pm, x, y, null, getGraph2D().selectedNodes().ok());
    	      return pm;
    	    }
    	  }
      
      private static final class RollOverViewMode extends ViewMode {
    	    /** Animation state constant */
    	    private static final int NONE = 0;
    	    /** Animation state constant */
    	    private static final int MARKED = 1;
    	    /** Animation state constant */
    	    private static final int UNMARK = 2;


    	    /** Preferred duration for roll over effect animations */
    	    private static final int PREFERRED_DURATION = 350;

    	    /** Scale factor for the roll over effect animations */
    	    private static final Value2D SCALE_FACTOR =
    	            DefaultMutableValue2D.create(2, 2);


    	    /** Stores the last node that was marked with the roll over effect */
    	    private Node lastHitNode;
    	    /** Stores the original size of nodes */
    	    private NodeMap size;
    	    /** Stores the animation state of nodes */
    	    private NodeMap state;

    	    private ViewAnimationFactory factory;
    	    private AnimationPlayer player;

    	    /**
    	     * Triggers a rollover effect for the first node at the specified location.
    	     */
    	    public void mouseMoved( final double x, final double y ) {
    	      final HitInfo hi = getHitInfo(x, y);
    	      if (hi.hasHitNodes()) {
    	        final Node node = (Node) hi.hitNodes().current();
    	        if (node != lastHitNode) {
    	          unmark(lastHitNode);
    	        }
    	        if (state.getInt(node) == NONE) {
    	          mark(node);
    	          lastHitNode = node;
    	        }
    	      } else {
    	        unmark(lastHitNode);
    	        lastHitNode = null;
    	      }
    	    }

    	    /**
    	     * Overwritten to initialize/dispose this <code>ViewMode</code>'s
    	     * helper data.
    	     */
    	    public void activate( final boolean b ) {
    	      if (b) {
    	        factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
    	        player = factory.createConfiguredPlayer();
    	        size = view.getGraph2D().createNodeMap();
    	        state = view.getGraph2D().createNodeMap();
    	      } else {
    	        view.getGraph2D().disposeNodeMap(state);
    	        view.getGraph2D().disposeNodeMap(size);
    	        state = null;
    	        size = null;
    	        player = null;
    	        factory = null;
    	      }
    	      super.activate(b);
    	    }

    	    /**
    	     * Overwritten to take only nodes into account for hit testing.
    	     */
    	    protected HitInfo getHitInfo( final double x, final double y ) {
    	      final HitInfo hi = new HitInfo(view, x, y, true, HitInfo.NODE);
    	      setLastHitInfo(hi);
    	      return hi;
    	    }

    	    /**
    	     * Triggers a <em>mark</em> animation for the specified node.
    	     * Sets the animation state of the given node to <em>MARKED</em>.
    	     */
    	    protected void mark( final Node node ) {
    	      // only start a mark animation if no other animation is playing
    	      // for the given node
    	    	if (check) {
    	      if (state.getInt(node) == NONE) {
    	        state.setInt(node, MARKED);

    	        final NodeRealizer nr = getGraph2D().getRealizer(node);
    	        nr.getLabel().setFontSize(12);
    	        size.set(node, DefaultMutableValue2D.create(nr.getWidth(), nr.getHeight()));
    	        final AnimationObject ao = factory.scale(
    	                nr,
    	                SCALE_FACTOR,
    	                ViewAnimationFactory.APPLY_EFFECT,
    	                PREFERRED_DURATION);
    	        player.animate(AnimationFactory.createEasedAnimation(ao));}
    	      }
    	    }

    	    /**
    	     * Triggers an <em>unmark</em> animation for the specified node.
    	     * Sets the animation state of the given node to <em>UNMARKED</em>.
    	     */
    	    protected void unmark( final Node node ) {
    	      if (node == null) {
    	        return;
    	      }

    	      // only start an unmark animation if the node is currently marked
    	      // (or in the process of being marked)
    	      
    	      if (state.getInt(node) == MARKED) {
    	        state.setInt(node, UNMARK);

    	        final Value2D oldSize = (Value2D) size.get(node);
    	        final NodeRealizer nr = getGraph2D().getRealizer(node);
    	        nr.getLabel().setFontSize(5);
    	        final AnimationObject ao = factory.resize(
    	                nr,
    	                oldSize,
    	                ViewAnimationFactory.APPLY_EFFECT,
    	                PREFERRED_DURATION);
    	        final AnimationObject eao = AnimationFactory.createEasedAnimation(ao);
    	        player.animate(new Reset(eao, node, nr, oldSize));
    	      }
    	    }

    	    /**
    	     * Custom animation object that resets node size and state upon disposal.
    	     */
    	    private final class Reset implements AnimationObject {
    	      private AnimationObject ao;
    	      private final Node node;
    	      private final NodeRealizer nr;
    	      private final Value2D oldSize;

    	      Reset(
    	              final AnimationObject ao,
    	              final Node node,
    	              final NodeRealizer nr,
    	              final Value2D size
    	      ) {
    	        this.ao = ao;
    	        this.node = node;
    	        this.nr = nr;
    	        this.oldSize = size;
    	      }

    	      public void initAnimation() {
    	        ao.initAnimation();
    	      }

    	      public void calcFrame( final double time ) {
    	        ao.calcFrame(time);
    	      }

    	      /**
    	       * Resets the target node to its original size and its animation state
    	       * to <em>NONE</em>.
    	       */
    	      public void disposeAnimation() {
    	        ao.disposeAnimation();
    	        nr.setSize(oldSize.getX(), oldSize.getY());
    	        size.set(node, null);
    	        state.setInt(node, NONE);
    	      }

    	      public long preferredDuration() {
    	        return ao.preferredDuration()/2;
    	      }
    	    }
    	  }
    
      class EditLabel extends AbstractAction {
    	    Edge e;

    	    EditLabel(Edge e) {
    	      super("Edit Preferred Length");
    	      this.e = e;
    	    }

    	    public void actionPerformed(ActionEvent ev) {

    	      final EdgeRealizer r = view.getGraph2D().getRealizer(e);
    	      final YLabel label = r.getLabel();

    	      view.openLabelEditor(label,
    	          label.getBox().getX(),
    	          label.getBox().getY(),
    	          null, true);
    	    }
    	  }
      
      class oLayoutAction extends AbstractAction {
    	    oLayoutAction() {
    	      super("Layout");
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      //update preferredEdgeLengthData before launching the module
    	      Graph2D graph = view.getGraph2D();
    	      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
    	        Edge edge = ec.edge();
    	        String eLabel = graph.getLabelText(edge);
    	        preferredEdgeLengthMap.set(edge, null);
    	        try {
    	          preferredEdgeLengthMap.setInt(edge, (int) Double.parseDouble(eLabel));
    	        }
    	        catch (Exception ex) {
    	        }
    	      }

    	      //start the module
    	      module.start(view.getGraph2D());
    	    }
    	  }
      
      class AssignLengthsAction extends AbstractAction {
    	    AssignLengthsAction() {
    	      super("Assign Preferred Length");
    	      putValue(Action.SHORT_DESCRIPTION, "Set the preferred length of each edge to its current geometric length");
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      Graph2D g = view.getGraph2D();
    	      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
    	        NodeRealizer snr = g.getRealizer(ec.edge().source());
    	        NodeRealizer tnr = g.getRealizer(ec.edge().target());
    	        double deltaX = snr.getCenterX() - tnr.getCenterX();
    	        double deltaY = snr.getCenterY() - tnr.getCenterY();
    	        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    	        EdgeRealizer er = g.getRealizer(ec.edge());
    	        er.getLabel().setText(Integer.toString((int) dist));
    	      }
    	      g.updateViews();
    	    }
    	  }
      
      class OptionAction extends AbstractAction {
    	    OptionAction() {
    	      super("Settings...");
    	    }

    	    public void actionPerformed(ActionEvent e) {
    	      OptionSupport.showDialog(module, view.getGraph2D(), false, view.getFrame());
    	    }
    	  }
}


/* EOF */
