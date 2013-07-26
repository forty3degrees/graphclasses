/*
 * The view of a node.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodeView.java,v 2.1 2011/09/29 08:52:48 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.core;

import teo.isgci.core.util.Latex2Html;
import teo.isgci.data.gc.GraphClass;
import teo.isgci.data.problem.Complexity;
import teo.isgci.data.problem.Problem;
import teo.isgci.data.xml.GraphMLWriter;
import teo.isgci.view.gui.SColor;

import java.awt.Color;
import java.util.Set;
import org.xml.sax.SAXException;

/**
 * A view [model] representing a node.
 */
public class NodeView {
	
	/** The parent graph. */
    private GraphView parent;
    
    /** The associated graph classes. */
    private Set<GraphClass> graphClasses;
    
    /** The default graph class used for naming. */
    private GraphClass defaultClass;
    
    /** The label that is displayed in the node, shortened automatically from
     * the full name */
    private String label;
    
    /** The full name of the node (e.g. for identification) */
    private String fullName;
    
    /** The background colour of the node. */
    private Color color;
    
    /** Colour for linear complexity. */
    private static Color COLOR_LIN = Color.green;
    /** Colour for polynomial complexity. */
    private static Color COLOR_P = Color.green.darker();
    /** Colour for NP-complete complexity. */
    private static Color COLOR_NPC = Color.red;
    /** Colour for GI-complete complexity. */
    private static Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    /** Colour for unknown complexity. */
    private static Color COLOR_UNKNOWN = Color.white;
    
	/**
	 * Latex to HTML converter for the HTML node labels.
	 */
	private static Latex2Html latexConverter = new Latex2Html("images/");

	/**
	 * Creates a new instance.
	 * @param parent		The parent graph view.
	 * @param graphClasses	The associated graph classes.
	 */
    public NodeView(GraphView parent, Set<GraphClass> graphClasses) {
        this.parent = parent;
        this.graphClasses = graphClasses;
        this.color = Color.white;
    }

    /**
     * Sets the node colouring.
     * @param lin	The colour for linear complexity.
     * @param p		The colour for polynomial complexity.
     * @param i		The colour for NP-complete complexity.
     * @param npc	The colour for GI-complete complexity.
     * @param u		The colour for unknown complexity.
     */
    public static void setColoring(Color lin, Color p, Color i, Color npc, Color u) {
       	COLOR_LIN = lin;
       	COLOR_P = p;
       	COLOR_INTERMEDIATE = i;
       	COLOR_NPC = npc;
       	COLOR_UNKNOWN = u;
    }
    
    /** Gets the graph classes associated with this node. */
    public Set<GraphClass> getGraphClasses() {
        return graphClasses;
    }

    /** Get the default class. */
    public GraphClass getDefaultClass() {
        return defaultClass;
    }

    /** Set the default class. */
    public void setDefaultClass(GraphClass g) {
    	/* Make sure only non-null values are used */
    	if (g == null) {
    		throw new NullPointerException("g cannot be null");
    	}
    	defaultClass = g;
        this.setNameAndLabel(defaultClass.toString());
    }

    /** Get the full name. */
    public String getFullName() {
        return fullName;
    }

    /** Set the full name. */
    private void setFullName(String s) {
        fullName = s;
    }

    /** Get the depicted label. */
    public String getLabel() {
        return label;
    }
    
    /** Set the depicted label. */
    private void setLabel(String s) {
        label = teo.isgci.core.util.Utility.getShortName(s);
    }

    /** Get the depicted label as HTML.
     *  The conversion from latex is done on-demand so 
     *  the returned value should be cached. */
    public String getHtmlLabel() {
        return "<html>" + latexConverter.html(label) + "</html>";
    }
    
    /** Set the full name to s and the label to the shortened form of s. */
    private void setNameAndLabel(String s) {
        setFullName(s);
        setLabel(s);
    }

    /** Gets the current node colour. */
    public Color getColor() {
        return color;
    }

    /**
     * Update the nodes colour considering its complexity for the given
     * problem.
     */
    public void setColor(Problem problem) {
        if (problem == null) {
        	color = COLOR_UNKNOWN;
        }
        else {
        	Complexity complexity= problem.getComplexity(graphClasses.iterator().next());
	        if (/*complexity == null  ||*/  complexity.isUnknown()) {
	            color = COLOR_UNKNOWN;
	        }
	        else if (complexity.betterOrEqual(Complexity.LINEAR)) {
	        	color = COLOR_LIN;
	        }
	        else if (complexity.betterOrEqual(Complexity.P)) {
	        	color = COLOR_P;
	        }
	        else if (complexity.equals(Complexity.GIC)) {
	        	color = COLOR_INTERMEDIATE;
	        }
	        else if (complexity.likelyNotP()) {
	        	color = COLOR_NPC;
	        }
	        else {
	        	color = COLOR_UNKNOWN;
	        }
        }
        
    }
    
    /**
     * Writes this node in graphML format to the given writer.
     * 
     * @param w				The graphML writer.
     * @throws SAXException	Thrown if an error occurs when writing the graphML elements.
     */
    public void write(GraphMLWriter w) throws SAXException {
        w.writeNode(Integer.toString(parent.getNodes().indexOf(this)),
                getLabel(), getColor());
    }

    /**
     * Gets a string representation of this node.
     */
    public String toString() {
        return "node: " + label;
    }
}

/* EOF */
