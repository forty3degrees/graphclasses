/*
 * The view of a connected graph.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;
import org.jgrapht.graph.SimpleDirectedGraph;
import teo.isgci.xml.GraphMLWriter;

/**
 * Display a graph in a GraphCanvas.
 */
public class GraphView<V,E> {
    /** The containing component (only GraphCanvas tested) */
    protected Component parent;
    protected SimpleDirectedGraph<V,E> graph;
    protected boolean drawUnproper;
    /** NodeViews */
    protected List<NodeView<V,E> > views;
    /** 'Extra' views: edges, virtual nodes */
    protected List<View<V,E> > eViews;


    /**
     * Create a new GraphView with nodes from DataSet.inclGraph.
     * @param parent the GraphCanvas that displays this view
     * @param nodes nodes the induce the graph to draw
     */
    public GraphView(Component parent,
            SimpleDirectedGraph<V,E> g) {
        this.parent = parent;

        graph = g;
        
        // Create the NodeViews; EdgeView are created when laying out.
        views = new ArrayList<NodeView<V,E> >();
        for (V v: g.vertexSet())
            views.add(new NodeView<V,E>(this, v));
        

        //layouter.layoutGraph();
        eViews = new ArrayList<View<V,E> >();

        // Create the EdgeViews
        for (E e : graph.edgeSet()) {
            if (!isVirtual(graph.getEdgeSource(e))) {
                EdgeView<V,E> v = new EdgeView<V,E>(this, e);
                if (parent instanceof GraphCanvas)
                    ((GraphCanvas) parent).setProperness(v);
                eViews.add(v);
            }
        }
    }

    public void setDrawUnproper(boolean b) {
        drawUnproper = b;
    }

    public boolean getDrawUnproper() {
        return drawUnproper;
    }

    /**
     * Return the view for the given node.
     */
    public NodeView<V,E> getView(V node) {
        for (NodeView v : views)
            if (v.getNode() == node)
                return v;
        for (View v : eViews)
            if (v instanceof NodeView  &&  ((NodeView) v).getNode() == node)
                return (NodeView<V,E>) v;
        return null;
    }

    /**
     * Return the graph this view shows.
     */
    public SimpleDirectedGraph<V,E> getGraph() {
        return graph;
    }

    /**
     * Return all nodeviews belonging to non-virtual nodes.
     */
    public List<NodeView<V,E> > getNodeViews() {
        return Collections.unmodifiableList(views);
    }

    /**
     * Return true if the given node is virtual.
     */
    public boolean isVirtual(V node) {
        return false;// layouter.getGDI(node).virt;
    }

    /**
     * Writes this to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        
        for (View v : views)
            v.write(w);
        for (View v : eViews)
            if (v instanceof EdgeView)
                v.write(w);
    }
}

/* EOF */
