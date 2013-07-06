/*
 * The view of an edge.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/EdgeView.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
//import teo.isgci.gc.GraphClass;
//import teo.isgci.db.Algo;
//import teo.isgci.db.DataSet;
import teo.isgci.xml.GraphMLWriter;
import org.xml.sax.SAXException;

public class EdgeView<V,E> implements View {
    protected GraphView<V,E> parent;
    protected V from, to;
    protected boolean isProper;
    protected boolean marked;


    /**
     * Create a new edge view that can span virtual nodes. e.from must be
     * non-virtual. If e.to is virtual, it will be followed until a non-virtual
     * node is reached.
     */
    public EdgeView(GraphView<V,E> parent, E e) {
        this.parent = parent;
        from = parent.getGraph().getEdgeSource(e);

        to = parent.getGraph().getEdgeTarget(e);
        marked = false;
        isProper = false;
    }


    public void setMark(boolean b) {
        marked = b;
    }

    public void setProper(boolean b) {
        isProper = b;
    }

    public V getFrom() {
        return from;
    }
    public V getTo() {
        return to;
    }

    /**
     * Writes this to w.
     */
    public void write(GraphMLWriter w)
            throws SAXException {
        w.writeEdge(
            Integer.toString(
                parent.getNodeViews().indexOf(parent.getView(this.from))),
            Integer.toString(
                parent.getNodeViews().indexOf(parent.getView(this.to))),
                isProper);
    }
}

/* EOF */
