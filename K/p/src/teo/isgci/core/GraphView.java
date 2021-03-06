/*
 * The view of a connected graph.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.core;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.data.db.Algo;
import teo.isgci.data.gc.GraphClass;
import teo.isgci.data.grapht.GAlg;
import teo.isgci.data.grapht.Inclusion;
import teo.isgci.data.xml.GraphMLWriter;

/**
 * Represents the view [model] for a graph.
 */
public class GraphView {
	
	/** The graph associated with this view. */
    protected SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph;
    
    /** Whether to include improper (non-strict) inclusions in the view. */
    protected boolean includeImproper;
    
    /** The nodes in this graph. */
    protected List<NodeView> nodes;
    
    /** The edges in this graph. */
    protected List<EdgeView> edges;


    /**
     * Create a new instance.
     * @param g The associated graph
     */
    public GraphView(SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g) {

        graph = g;
        
        /* Instantiate the collections */
        nodes = new ArrayList<NodeView>();
        edges = new ArrayList<EdgeView>();
        
        /* Add the nodes. */
        for (Set<GraphClass> v: g.vertexSet())
            nodes.add(new NodeView(this, v));
        
        /* Add the edges */
    	SimpleDirectedGraph<GraphClass, Inclusion> inclusionGraph = 
    			App.DataProvider.getInclusionGraph();
        for (DefaultEdge e : graph.edgeSet()) {
            EdgeView view = new EdgeView(this, e);
            
            setProperness(view, inclusionGraph);
            edges.add(view);
        }
    }

    /**
     * Sets the 'properness' of an edge (inclusion).
     * @param view				The edge.	
     * @param inclusionGraph	The inclusion graph.
     */
	private void setProperness(EdgeView view, 
			SimpleDirectedGraph<GraphClass, Inclusion> inclusionGraph) {
        List<Inclusion> path = GAlg.getPath(inclusionGraph,
                view.getFrom().iterator().next(),
                view.getTo().iterator().next());
        view.setProper(Algo.isPathProper(path)  ||
                Algo.isPathProper(Algo.makePathProper(path)));
		
	}

	/** Sets whether to include improper (non-strict) inclusions in the resulting graphML */
    public void setIncludeImproper(boolean b) {
        includeImproper = b;
    }

    /** Gets whether to include improper (non-strict) inclusions in the resulting graphML */
    public boolean getIncludeImproper() {
        return includeImproper;
    }

    /**
     * Return the view for the given set of graph classes.
     */
    public NodeView getView(Set<GraphClass> graphClasses) {
        for (NodeView v : nodes) {
            if (v.getGraphClasses() == graphClasses) {
                return v;
            }
        }
        return null;
    }

    /**
     * Return the graph this view represents.
     */
    public SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> getGraph() {
        return graph;
    }

    /**
     * Gets the nodes.
     */
    public List<NodeView> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Gets the edges.
     */
	public List<EdgeView> getEdges() {
		return Collections.unmodifiableList(this.edges);
	}

    /**
     * Writes this view in graphML format to the given writer.
     * 
     * @param w				The graphML writer.
     * @throws SAXException	Thrown if an error occurs when writing the graphML elements.
     */
    public void write(GraphMLWriter w) throws SAXException {
        /* Run through all the nodes and edges delegating the 
         * work to the NodeView.write(w) and EdgeView.write(w)
         * methods. */
        for (NodeView v : nodes)
            v.write(w);
        for (EdgeView e : edges)
            e.write(w);
    }
}

/* EOF */
