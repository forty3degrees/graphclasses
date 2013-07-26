/*
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIGraphCanvas.java,v 2.1 2012/10/28 16:00:51 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.xml.sax.SAXException;

import teo.isgci.data.db.Algo;
import teo.isgci.data.gc.GraphClass;
import teo.isgci.data.grapht.GAlg;
import teo.isgci.data.problem.Problem;
import teo.isgci.data.xml.GraphMLWriter;

/**
 * View manager for a graph (specific to a single Frame).
 */
public class ViewManager {
	
	/** The registered drawing services. */
    private List<IDrawingService> drawingServices = new ArrayList<IDrawingService>();
    /**
     * Attaches a drawing service to the view manager in order for the view to invoke 
     * the service when the view is updated.
     * @param service	The service to attach.
     */
    public void attachDrawingService(IDrawingService service) {
    	drawingServices.add(service);
    }

    /** Whether or not the view manager is still loading */
	private boolean isLoading = false;
	
	/** The currently selected problem. */
	private Problem problem;
	
	/** The current graphs. */
	private List<GraphView> graphs;
	
	/** Whether or not to draw improper inclusions. */
	private boolean drawImproper;

	/** The current naming preference for nodes. */
	private Algo.NamePref namingPref = Algo.NamePref.BASIC;
	
	/** 
	 * Holds the root classes initially loaded into the graph. 
	 * This is used to make sure the default names are set
	 * properly for these classes.
	 */
	private Collection<GraphClass> rootClasses;

	/**
	 * Creates a new instance.
	 */
    public ViewManager() {
        super();
        problem = null;
        graphs = new ArrayList<GraphView>();
        drawImproper = true;
    }

    public void load(Collection<GraphClass> graphClasses, boolean doSuper, boolean doSub) {
    	this.isLoading = true;
    	this.rootClasses = graphClasses;
    	
    	/* Load the hierarchy */
    	graphClasses = App.DataProvider.getGraphClasses(graphClasses, doSuper, doSub);
    	
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> directedGraph =
            Algo.createHierarchySubgraph(graphClasses);

        List<SimpleDirectedGraph<Set<GraphClass>,DefaultEdge>> list =
                GAlg.split(directedGraph, DefaultEdge.class);
        
        try {
        	/* Clear the current graphs and add the new ones */
            graphs.clear();
            for (SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g : list) {
                addGraph(g);
            }
        } catch (Error e) {

            if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
            	graphs.clear();
            } else {
                throw(e);
            }
        }

        /* Notify any listeners that the view was initialised */
        this.initializeView();
    	this.isLoading = false;
    }
	
    /**
     * Add the given graph to the current view.
     */
    private GraphView addGraph(SimpleDirectedGraph<Set<GraphClass>,DefaultEdge> g) {
    	GraphView gv = new GraphView(g);
    	
        gv.setIncludeImproper(drawImproper);
        graphs.add(gv);
        
        for (NodeView nv : gv.getNodes()) {
        	/* Set the colour */
            nv.setColor(this.problem); 
            
            /* Check if the node contains one of the root classes and
             * set this to the default if so. Do the for-loop on the
             * root classes collection as this will usually be smaller. */
            Set<GraphClass> graphClasses = nv.getGraphClasses();
            for (GraphClass gc : this.rootClasses) {
            	if (graphClasses.contains(gc)) {
            		nv.setDefaultClass(gc);
            		break;
            	}
            }
            
            /* Make sure we have set a class */
            if (nv.getDefaultClass() == null) {
            	nv.setDefaultClass(graphClasses.iterator().next());
            }
        }
        
        return gv;
    }

    /**
     * Adds the given graph classes to the current view.
     * @param graphClasses		The classes to add.
     */
    public void add(Collection<GraphClass> graphClasses) {
    	
    	/* Collect all the current graph classes and add them to 
    	 * the collection passed.
    	 */
    	for (GraphView gv : graphs) {
    		for (Set<GraphClass> l : gv.getGraph().vertexSet()) {
    			for (GraphClass gc : l) {
    				graphClasses.add(gc);
    			}
    		}
    	}   
    	System.out.println(graphClasses.size());
    	
    	/* Clear the current view */
    	graphs.clear();
    	
    	/* Check that we have something to draw */
    	if (graphClasses.isEmpty()) {
    		return;
    	}
    	
    	/* Create and add a new graph for the new collection of classes */
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> directedGraph =
            Algo.createHierarchySubgraph(graphClasses);

        try {
        	addGraph(directedGraph);
        } catch (Error e) {

            if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
            	graphs.clear();
            } else {
                throw(e);
            }
        }      

        /* Notify any listeners that the view was updated */
        this.refresh();
    }

    /**
     * Deletes the given graph classes from the current view.
     * @param graphClasses		The classes to delete.
     */
	public void delete(Collection<GraphClass> graphClasses) {
	
		/* Run through the current graph view building a collection
		 * of the remaining nodes (current - parameter = newNodes). */
		Collection<GraphClass> newNodes = new ArrayList<GraphClass>();
		Collection<GraphClass> done = new ArrayList<GraphClass>();
		for (GraphView gv : graphs) {
			for (Set<GraphClass> l : gv.getGraph().vertexSet()) {
				for (GraphClass gc : l) {
					boolean contained = false;
					for (GraphClass mc : Algo.equNodes(gc)) {
						if (graphClasses.contains(mc) || done.contains(mc)) {
							contained = true;
							break;
						}
					}
					
					if (!contained) {
						done.addAll(Algo.equNodes(gc));
						newNodes.add(gc);
					}
				}
			}
		}   
		
		/* Clear the current view */
		graphs.clear();
		
		/* Make sure we have something to draw. If not we simply clear
		 * the current view. */
		if (newNodes.isEmpty()) {
			for (IDrawingService ds : drawingServices) {
	            ds.clearView();
	        }
			return;
		}
		
		/* Use the newNodes collection to refresh the view. */
	    SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> directedGraph =
	        Algo.createHierarchySubgraph(newNodes);
	
	    List<SimpleDirectedGraph<Set<GraphClass>,DefaultEdge>> list =
	            GAlg.split(directedGraph, DefaultEdge.class);
	    
	    System.out.println(list.size());
	    
	    try {
	    	
	    	addGraph(directedGraph);
	    	
	    } catch (Error e) {
	
	        if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
	        	graphs.clear();
	        } else {
	            throw(e);
	        }
	    }

        /* Notify any listeners that the view was updated */
        this.refresh();
	}

    /**
     * Calls IDrawingService.InitializeView(g) for all services.
     */
	private void initializeView() {
		for (IDrawingService ds : drawingServices) {
            ds.initializeView(this.graphs);
        }
	}
	
	/**
	 * Refreshes the current view. Adding or removing nodes and
	 * edges as necessary.
	 */
    public void refresh() {
        if (!isLoading) {
	        /* Notify any attached services that the view should be refreshed */
	        for (IDrawingService ds : drawingServices) {
	            ds.updateView(this.graphs);
	        }
        }
    }

    /** Gets a list of all the current views */
    public Collection<GraphView> getCurrentViews() {
    	return Collections.unmodifiableCollection(this.graphs);
    }
    
    /** Gets a flat list of all the nodes in the current views */
    public Collection<NodeView> getCurrentNodes() {
    	
    	/* Get a list of all the nodes in all graph views */
    	ArrayList<NodeView> result = new ArrayList<NodeView>();
    	for (GraphView gv : graphs) {
    		for (NodeView node : gv.nodes) {
        		if (!result.contains(node)) {
        			result.add(node);
        		}        			
        	}
    	}
    	return result;
    }

    /**
     * Find the NodeView for the given graph class or null if not found
     */
    public NodeView findNode(GraphClass gc) {
        for (GraphView gv : graphs) {
            for (NodeView v : gv.getNodes())
                if (v.getGraphClasses().contains(gc))
                    return v;
        }
        return null;
    }
    
    /**
     * Return the NodeView for the given set of graph classes.
     */
    public NodeView getView(Set<GraphClass> graphClassess) {
        for (GraphView gv : graphs) {
            NodeView view = gv.getView(graphClassess);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    /** Gets whether to draw improper inclusions. */
    public boolean getDrawImproper() {
        return drawImproper;
    }

    /** Sets whether to draw improper inclusions. */
    public void setDrawImproper(boolean b) {
        drawImproper = b;
        for (GraphView gv : graphs) {
            gv.setIncludeImproper(b);
        }
        if (!isLoading) {
	        /* Notify any attached services that the view should be refreshed */
	        for (IDrawingService ds : drawingServices) {
	            ds.updateImproperInclusions(this.graphs);
	        }
        }
    }

    /** Gets the currently selected problem. */
    public Problem getProblem() {
        return problem;
    }

    /** Sets the currently selected problem. */
    public void setProblem(Problem p) {
        if (problem != p) {
            problem = p;
            
            /* Set the colours for this problem */
            setComplexityColors();
        }
    }

    /** Gets the naming preference. */
    public Algo.NamePref getNamingPref() {
        return namingPref;
    }

    /** Sets the naming preference. */
    public void setNamingPref(Algo.NamePref pref) {
        namingPref = pref;
        setPreferedNames();
    }

    /**
     * Set all nodes to their preferred names.
     */
    public void setPreferedNames() {
        for (GraphView gv : graphs) {
            for (NodeView v : gv.getNodes()) {
            	v.setDefaultClass(Algo.getDefaultClass(v.getGraphClasses(), namingPref)); 
            }
        }
        this.refresh();
    }

    /**
     * Set all nodes to the proper complexity colour.
     */
    public void setComplexityColors() {
    	/* Update the colours for each node */
        for (GraphView gv : graphs) {
            for (NodeView v : gv.getNodes()) {
               v.setColor(this.problem);
            }
        }
        
        /* Update the colours in the UI */
        if (!isLoading) {
	        for (IDrawingService ds : drawingServices) {
	            ds.updateColors();
	        }
        }
    }
    
    /**
     * Exports the current view as graphML to the specified file.
     * @param file	The path to the destination file.
     * @return		True if successful, otherwise false.
     */
    public boolean export(String file) {
    	boolean res = true;
    	FileOutputStream f;
    	try {
    		File  myFile = new File(file);
    		f = new FileOutputStream(myFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	
	    try {
	    	exportGML(f);            
	    } catch (Exception e) {
	        res = false;
	        e.printStackTrace();
	    }
	    return res;
    }

	/**
	 * Exports the current view as GraphML to a file stream.
	 * @param f				The destination stream.
	 * @throws Exception	Thrown if the XML could not be generated 
	 * 						or the file would not be written to
	 */
	protected void exportGML(FileOutputStream f) throws Exception {
		Exception res = null;
	    Writer out = null;
	    
	    try {
	        out = new OutputStreamWriter(f, "UTF-8");
	        GraphMLWriter w = new GraphMLWriter(out,GraphMLWriter.MODE_YED,
	               this.getDrawImproper(),true);
	
	        w.startDocument();
	        this.write(w);
	        w.endDocument();
	    } catch (IOException ex)  {
	        res = ex;
	    } finally {
	        out.close();
	    }
	    
	    if (res != null)
	        throw res;
	}

    /**
     * Gets the graphML for the current view.
     * @return					The graphML.
     * @throws SAXException		Thrown if there is an error writing the XML elements.
     */
    public String getCurrentGraphML() throws SAXException {
    	StringWriter writer = null;
    	try {
    		
    		/* Create a new writer and then delegate the work to the 
    		 * 'write' method. */
    		writer = new StringWriter();
            GraphMLWriter graphMLWriter = new GraphMLWriter(writer, 
            		GraphMLWriter.MODE_YED, this.getDrawImproper(), true);

            graphMLWriter.startDocument();
            this.write(graphMLWriter);
            graphMLWriter.endDocument();

        	return writer.toString();
        } finally {
            try {
				writer.close();
			} catch (IOException e) {
				/* Ignore this, it won't have any effect */
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Writes the current view in graphML format to the given writer.
     * 
     * @param w				The graphML writer.
     * @throws SAXException	Thrown if an error occurs when writing the graphML elements.
     */
    public void write(GraphMLWriter w) throws SAXException {
        for (GraphView gv : graphs)
            gv.write(w);
    }


}

/* EOF */
