package teo.isgci.core;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;

import teo.isgci.data.gc.GraphClass;
import teo.isgci.view.gui.PSGraphics;

public interface IDrawingService {
	JComponent getCanvas();
	
	void initializeView(List<GraphView> graphs, HashMap<String, String> initialNames);
	void updateView(List<GraphView> graphs, HashMap<String, String> initialNames);
	void updateColors();
	void refreshView();
	void doLayout();
	void clearView();
	void invertSelection();

	void loadGraphML(String gmlString);
	void loadGraphML(URL resource);
	
	void exportGraphics(PSGraphics g);
	void exportPS(String file) throws IOException;
	void exportSVG(String file) throws IOException;
	void exportGML(String file) throws IOException;
	
	void selectNeighbors();
    void selectSuperClasses();
    void selectSubClasses();
    Collection<GraphClass> getSelection();
    
    void search(NodeView view);
    void print();
    
	URL getCurrentFile();
    
}
