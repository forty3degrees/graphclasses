package teo.isgci.core;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import teo.isgci.data.gc.GraphClass;
import teo.isgci.view.gui.PSGraphics;

public interface IDrawingService {
	/**
	 * @return
	 */
	JComponent getCanvas();
	
	/**
	 * @param graphs
	 * @param initialNames
	 */
	void initializeView(List<GraphView> graphs);
	/**
	 * @param graphs
	 * @param initialNames
	 */
	void updateView(List<GraphView> graphs);
	/**
	 * 
	 */
	void updateColors();
	/**
	 * 
	 */
	void updateLabel(NodeView node);
	/**
	 * 
	 */
	void updateImproperInclusions(List<GraphView> graphs);
	/**
	 * 
	 */
	void refreshView();
	/**
	 * 
	 */
	void doLayout();
	/**
	 * 
	 */
	void clearView();
	/**
	 * 
	 */
	void invertSelection();

	/**
	 * @param gmlString
	 */
	void loadGraphML(String gmlString);
	/**
	 * @param resource
	 */
	void loadGraphML(URL resource);
	
	/**
	 * @param g
	 */
	void exportGraphics(PSGraphics g);
	/**
	 * @param file
	 * @throws IOException
	 */
	void exportPS(String file) throws IOException;
	/**
	 * @param file
	 * @throws IOException
	 */
	void exportSVG(String file) throws IOException;
	/**
	 * @param file
	 * @throws IOException
	 */
	void exportGML(String file) throws IOException;
	
	/**
	 * 
	 */
	void selectNeighbors();
    /**
     * 
     */
    void selectSuperClasses();
    /**
     * 
     */
    void selectSubClasses();
    /**
     * @return
     */
    Collection<GraphClass> getSelection();
    
    /**
     * @param view
     */
    void search(NodeView view);
    /**
     * 
     */
    void print();
    
	/**
	 * @return
	 */
	URL getCurrentFile();

    
}
