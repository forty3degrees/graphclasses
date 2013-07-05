/*
 * Displays graphs.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphCanvas.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.*;
import java.util.Collection;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.VertexFactory;
import teo.isgci.xml.GraphMLWriter;
import teo.isgci.util.IntFunction;

/**
 * A canvas that can display a graph.
 */
public class GraphCanvas<V,E> extends JPanel {

    protected Component parent;
    protected List<GraphView<V,E> > graphs;
    protected View markedView;
    protected boolean dragInProcess;
    protected boolean drawUnproper;
    protected VertexFactory<V> vertexFactory;    // Internal


    public GraphCanvas(Component parent,
            LatexGraphics latexgraphics,
            VertexFactory<V> vertexFactory,
            IntFunction<V> widthFunc) {
        super();
        this.parent = parent;
        this.vertexFactory = vertexFactory;
        graphs = new ArrayList<GraphView<V,E> >();
        markedView = null;
        dragInProcess = false;
        drawUnproper = true;
    }


    /**
     * Remove all graphs from the canvas.
     */
    public void clearGraphs() {
        graphs.clear();
    }


    /**
     * Add the given graph to this canvas.
     */
    protected GraphView<V,E> addGraph(SimpleDirectedGraph<V,E> g) {
        GraphView<V,E> gv = new GraphView<V,E>(this, g);
        gv.setDrawUnproper(drawUnproper);
        graphs.add(gv);
        return gv;
    }
    
    
    /**
     * Return the NodeView for the given node.
     */
    public NodeView<V,E> getView(V node) {
        for (GraphView<V,E> gv : graphs) {
            NodeView<V,E> view = gv.getView(node);
            if (view != null)
                return view;
        }
        return null;
    }


    /**
     * Clear the canvas and draw the given graphs.
     */
    public void drawGraphs(Collection<SimpleDirectedGraph<V,E> > graphs) {
        try {
            /*long t, s = System.currentTimeMillis();*/
            clearGraphs();
            /*s = t;
            t = System.currentTimeMillis();
            System.out.println("split "+(t-s));*/
            for (SimpleDirectedGraph<V,E> g : graphs)
                addGraph(g);
        } catch (Error e) {
            //e.printStackTrace();
            if (e instanceof OutOfMemoryError ||
                    e instanceof StackOverflowError) {
                clearGraphs();
                MessageDialog.error(parent,
                        "Not enough memory to draw this many graph classes");
            } else
                throw(e);
        }
    }


    public void setDrawUnproper(boolean b) {
        drawUnproper = b;
        for (GraphView<V,E> gv : graphs)
            gv.setDrawUnproper(b);
    }

    public boolean getDrawUnproper() {
        return drawUnproper;
    }

    /**
     * Bit of a hack to get all ISGCI stuff in one place:
     * Set the appropriate properness of the given edgeview.
     */
    protected void setProperness(EdgeView<V,E> view) {
    }


    /**
     * Write this to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        for (GraphView gv : graphs)
            gv.write(w);
    }
    

}

/* EOF */
