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
 * Display a graph in a GraphCanvas.
 */
public class GraphView {
	
    protected SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph;
    /** Whether to include improper (non-strict) inclusions in the resulting graphML */
    protected boolean includeUnproper;
    
    /** NodeViews */
    protected List<NodeView> nodes;
    
    /** 'Extra' views: edges, virtual nodes */
    protected List<EdgeView> edges;


    /**
     * Create a new GraphView with nodes from DataSet.inclGraph.
     * @param g The graph
     */
    public GraphView(SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g) {

        graph = g;
        
        // Create the NodeViews; EdgeView are created when laying out.
        nodes = new ArrayList<NodeView>();
        for (Set<GraphClass> v: g.vertexSet())
            nodes.add(new NodeView(this, v));
        

        //layouter.layoutGraph();
        edges = new ArrayList<EdgeView>();

        // Create the EdgeViews
    	SimpleDirectedGraph<GraphClass, Inclusion> inclusionGraph = 
    			App.DataProvider.getInclusionGraph();
        for (DefaultEdge e : graph.edgeSet()) {
            EdgeView view = new EdgeView(this, e);
            
            setProperness(view, inclusionGraph);
            edges.add(view);
        }
    }

	private void setProperness(EdgeView view, 
			SimpleDirectedGraph<GraphClass, Inclusion> inclusionGraph) {
        List<Inclusion> path = GAlg.getPath(inclusionGraph,
                view.getFrom().iterator().next(),
                view.getTo().iterator().next());
        view.setProper(Algo.isPathProper(path)  ||
                Algo.isPathProper(Algo.makePathProper(path)));
		
	}

	/** Sets whether to include improper (non-strict) inclusions in the resulting graphML */
    public void setIncludeUnproper(boolean b) {
        includeUnproper = b;
    }

    /** Gets whether to include improper (non-strict) inclusions in the resulting graphML */
    public boolean getIncludeUnproper() {
        return includeUnproper;
    }

    /**
     * Return the view for the given node.
     */
    public NodeView getView(Set<GraphClass> node) {
        for (NodeView v : nodes)
            if (v.getGraphClasses() == node)
                return v;
        return null;
    }

    /**
     * Return the graph this view shows.
     */
    public SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> getGraph() {
        return graph;
    }

    /**
     * Return all nodeviews belonging to non-virtual nodes.
     */
    public List<NodeView> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

	public List<EdgeView> getEdges() {
		return Collections.unmodifiableList(this.edges);
	}

    /**
     * Writes this to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        
        for (NodeView v : nodes)
            v.write(w);
        for (EdgeView e : edges)
            e.write(w);
    }
}

/* EOF */
