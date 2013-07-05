/*
 * Something that can be displayed on a GraphCanvas.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/View.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.*;
import teo.isgci.xml.GraphMLWriter;

/*
 * An element that can be displayed on a GraphCanvas.
 */
public interface View<V,E> {
    /** Mark/unmark it */
    public void setMark(boolean b);
    /** Write this to w */
    public void write(GraphMLWriter w) throws org.xml.sax.SAXException;
}

/* EOF */
