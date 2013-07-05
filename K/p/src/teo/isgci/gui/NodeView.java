/*
 * The view of a node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodeView.java,v 2.1 2011/09/29 08:52:48 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import teo.isgci.db.Algo;
import teo.isgci.problem.Problem;
import teo.isgci.problem.Complexity;
import teo.isgci.xml.GraphMLWriter;
import teo.isgci.util.Utility;
import teo.isgci.gc.GraphClass;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.*;
import java.util.Set;
import org.xml.sax.SAXException;

/**
 * Displays a node.
 */
public class NodeView<V,E> implements View {
    protected GraphView<V,E> parent;
    protected V node;
    /** The label that is displayed in the node, shortened automatically from
     * the fullname */
    protected String label;
    /** The fullname of the node (e.g. for identification) */
    protected String fullName;
    protected Color color;
    protected boolean marked;


    public NodeView(GraphView<V,E> parent, V node) {
        this.parent = parent;
        this.node = node;
        this.marked = false;
        this.color = Color.white;
        label = "";
        fullName = "";
    }


    /**
     * Get the node.
     */
    public V getNode() {
        return node;
    }


    /** Get the full name */
    public String getFullName() {
        return fullName;
    }

    /** Set the full name */
    protected void setFullName(String s) {
        fullName = s;
    }

    /** Get the depicted label */
    public String getLabel() {
        return label;
    }

    /** Set the depicted label */
    protected void setLabel(String s) {
        label = teo.isgci.util.Utility.getShortName(s);
    }

    /** Set the fullname to s and the label to the shortened form of s. */
    public void setNameAndLabel(String s) {
        setFullName(s);
        setLabel(s);
    }

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    public void setMark(boolean b) {
        marked = b;
    }

    /**
     * Writes this nodeview to w.
     */
    public void write(GraphMLWriter w) throws SAXException {
        w.writeNode(Integer.toString(parent.getNodeViews().indexOf(this)),
                getLabel(), getColor());
    }

    public String toString() {
        return "node: " + label;
    }
}

/* EOF */
