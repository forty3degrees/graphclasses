/*
 * The view of an edge.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/EdgeView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.core;

import java.util.Set;

import teo.isgci.data.gc.GraphClass;
import teo.isgci.data.xml.GraphMLWriter;
//import teo.isgci.gc.GraphClass;
//import teo.isgci.db.Algo;
//import teo.isgci.db.DataSet;

import org.jgrapht.graph.DefaultEdge;
import org.xml.sax.SAXException;

/**
 * Represents a view [model] for an edge.
 * 
 * @author Calum *
 */
public class EdgeView {
	
	/** The parent graph view. */
    private GraphView parent;

    /** The set of graph classes associated with this edges source node. */
    private Set<GraphClass> from;
    
    /** The set of graph classes associated with this edges destination node. */
    private Set<GraphClass> to;
    
    /** Whether this edge represents a proper inclusion. */
    private boolean isProper;


    /**
     * Create a new edge view.
     * @param parent The parent graph view.	
     * @param e The edge associated with this view.
     */
    public EdgeView(GraphView parent, DefaultEdge e) {
        this.parent = parent;
        from = parent.getGraph().getEdgeSource(e);

        to = parent.getGraph().getEdgeTarget(e);
        isProper = false;
    }

    /**
     * Gets whether this edge represents a proper inclusion or not.
     * @return Whether this node represents a proper inclusion.
     */
    public boolean getProper() {
        return isProper;
    }
    
    /**
     * Sets whether this edge represents a proper inclusion or not.
     * @param b	Whether this node represents a proper inclusion.
     */
    public void setProper(boolean b) {
        isProper = b;
    }

    /**
     * Gets the set of graph classes associated with this edges source node.
     * @return The graph classes for the source node.
     */
    public Set<GraphClass> getFrom() {
        return from;
    }
    
    /**
     * Gets the set of graph classes associated with this edges destination node.
     * @return The graph classes for the destination node.
     */
    public Set<GraphClass> getTo() {
        return to;
    }

    /**
     * Gets the source node for this edge.
     * @return The source node.
     */
    public NodeView getFromNode() {
        return parent.getView(this.from);
    }
    
    /**
     * Gets the destination node for this edge.
     * @return The destination node.
     */
    public NodeView getToNode() {
        return parent.getView(this.to);
    }

    /**
     * Writes this edge in graphML format to the given writer.
     * 
     * @param w				The graphML writer.
     * @throws SAXException	Thrown if an error occurs when writing the graphML elements.
     */
    public void write(GraphMLWriter w)
            throws SAXException {
        w.writeEdge(
            Integer.toString(
                parent.getNodes().indexOf(parent.getView(this.from))),
            Integer.toString(
                parent.getNodes().indexOf(parent.getView(this.to))),
                isProper);
    }
}

/* EOF */
