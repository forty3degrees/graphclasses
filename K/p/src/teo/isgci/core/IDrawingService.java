package teo.isgci.core;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import teo.isgci.data.gc.GraphClass;
import teo.isgci.view.gui.PSGraphics;

/**
 * Drawing interface. This provides abstraction for the actual drawing
 * of the graphs.
 * 
 * @author Calum
 *
 */
public interface IDrawingService {
	/**
	 * Gets the top-level UI component from the drawing service.
	 * 
	 * @return The JComponent containing the drawing canvas that should
	 * be added to the main window.
	 */
	JComponent getCanvas();
	
	/**
	 * Initialises the graph view. This will clear any current
	 * nodes and edges.
	 * @param graphs	The graphs to draw.
	 */
	void initializeView(List<GraphView> graphs);
	
	/**
	 * Updates an existing graph. Removing unnecessary nodes and
	 * edges, and adding any new ones.
	 * @param graphs	The new graphs to be drawn.
	 */
	void updateView(List<GraphView> graphs);
	
	/**
	 * Updates the colours of the nodes using the colour set
	 * in the respective NodeView.
	 */
	void updateColors();
		
	/**
	 * Updates the display of any improper inclusions in the current graph.
	 */
	void updateImproperInclusions(List<GraphView> graphs);
	
	/**
	 * Refreshes the current view, updating the node size and fitting
	 * the view to the current canvas size.
	 */
	void refreshView();
	
	/**
	 * Performs a layout pass on the current view.
	 */
	void doLayout();
	
	/**
	 * Clears the current view.
	 */
	void clearView();

	/**
	 * Loads a graphML string, replacing the current view.
	 * @param gmlString The graphML.
	 */
	void loadGraphML(String gmlString);
	/**
	 * Loads a graphML resource, replacing the current view.
	 * @param resource The URL of the graphML resource.
	 */
	void loadGraphML(URL resource);
	
	/**
	 * Exports the current view into the post-script graphics object passed.
	 * @param g The post-script graphics object.
	 */
	void exportGraphics(PSGraphics g);
	
	/**
	 * Exports the current view to a post-script file.
	 * @param file 			The post-script file path.
	 * @throws IOException	If there is a problem accessing the file. 
	 */
	void exportPS(String file) throws IOException;
	
	/**
	 * Exports the current view to an SVG file
	 * @param file			The SVG file path.
	 * @throws IOException	If there is a problem accessing the file.
	 */
	void exportSVG(String file) throws IOException;
	
	/**
	 * Exports the current view to a graphML file
	 * @param file			The graphML file path.
	 * @throws IOException	If there is a problem accessing the file.
	 */
	void exportGML(String file) throws IOException;
	
	/**
	 * Selects the neighbours (super- and sub-classes) of all the 
	 * currently selected nodes.
	 */
	void selectNeighbors();
	
    /**
     * Selects the super-classes for the currently selected nodes.
     */
    void selectSuperClasses();
    
    /**
     * Selects the sub-classes for the currently selected nodes.
     */
    void selectSubClasses();
    
    /**
     * Gets the currently selected nodes.
     * @return The selected nodes.
     */
    Collection<GraphClass> getSelection();
	
	/**
	 * Inverts the current node selection.
	 */
	void invertSelection();
    
    /**
     * Searches for a node in the current view.
     * @param view The NodeView associated with the node being searched for.
     */
    void search(NodeView view);
    
    /**
     * Prints the current view.
     */
    void print();
    
	/**
	 * Gets the URL of the currently loaded file (if any).
	 * @return The URL.
	 */
	URL getCurrentFile();

    
}
