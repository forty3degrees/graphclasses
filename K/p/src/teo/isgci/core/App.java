package teo.isgci.core;

import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import teo.isgci.data.XmlDataProvider;
import teo.isgci.view.gui.ISGCIMainFrame;
import teo.isgci.yfiles.YFilesDrawingService;

/**
 * Class to manage the application data and drawing services. Core
 * part of the application logic layer.
 * 
 * @author Calum *
 */
public class App {
	
	/** The data provider for the entire application. */
	public static final IDataProvider DataProvider = new XmlDataProvider();
	
	/** A map of drawing services and their respective frames. */
	private static final Map<Frame, IDrawingService> drawingServices = 
			new HashMap<Frame, IDrawingService>();
	/** A map of view managers and their respective frames. */
	private static final Map<Frame, ViewManager> viewManagers = 
			new HashMap<Frame, ViewManager>();
		
	/**
	 * Creates the drawing service and view manager for the given frame. 
	 * @param parent	The frame.
	 */
	private static void createServices(ISGCIMainFrame parent) {

		ViewManager viewManager = new ViewManager();
		IDrawingService drawingService = new YFilesDrawingService(parent);
		viewManager.attachDrawingService(drawingService);

		App.viewManagers.put(parent, viewManager);
		App.drawingServices.put(parent, drawingService);
	}

	/**
	 * Gets a drawing service given the associated frame.
	 * @param parent	The frame.
	 * @return			The associated drawing service.
	 */
	public static IDrawingService getDrawingService(ISGCIMainFrame parent) {
		if (!App.drawingServices.containsKey(parent)) {
			createServices(parent);
		}
		System.out.println("Getting drawing service");
		return App.drawingServices.get(parent);
	}

	/**
	 * Gets a view manager given the associated frame.
	 * @param parent	The frame.
	 * @return			The associated view manager.
	 */
	public static ViewManager getViewManager(ISGCIMainFrame parent) {
		if (!App.viewManagers.containsKey(parent)) {
			createServices(parent);
		}
		System.out.println("Getting view manager");
		return App.viewManagers.get(parent);
	}
	
}
