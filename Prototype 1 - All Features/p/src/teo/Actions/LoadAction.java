package teo.Actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import teo.isgci.gui.ISGCIMainFrame;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.util.D;

/**
 * Action that loads the current graph from a file in GraphML format.
 */
public class LoadAction extends AbstractAction {
  JFileChooser chooser;
  ISGCIMainFrame parent;

  public LoadAction(ISGCIMainFrame p) {
    super("Load...");
    chooser = null;
    parent = p;
  }

  public void actionPerformed(ActionEvent e) {
    if (chooser == null) {
      chooser = new JFileChooser();
      chooser.setAcceptAllFileFilterUsed(false);
      chooser.addChoosableFileFilter(new FileFilter() {
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().endsWith(".graphml");
        }

        public String getDescription() {
          return "GraphML Format (.graphml)";
        }
      });
    }
    if (chooser.showOpenDialog(parent.mainPan) == JFileChooser.APPROVE_OPTION) {
      URL resource = null;
      try {
        resource = chooser.getSelectedFile().toURI().toURL();
      } catch (MalformedURLException urlex) {
        urlex.printStackTrace();
      }
      loadGraph(resource);
    }
  }
  
  protected GraphMLIOHandler createGraphMLIOHandler() {
      return new GraphMLIOHandler();
    }
  
  public void loadGraph(URL resource) {
      if (resource == null) {
        String message = "Resource \"" + resource + "\" not found in classpath";
        D.showError(message);
        throw new RuntimeException(message);
      }

      try {
        IOHandler ioh = null;
        ioh = createGraphMLIOHandler();
        parent.view.getGraph2D().clear();
        ioh.read(parent.view.getGraph2D(), resource);
      } catch (IOException e) {
        String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
        D.bug(message);
        throw new RuntimeException(message, e);
      }
      parent.view.getGraph2D().setURL(resource);
      parent.view.fitContent();
      parent.view.updateView();
    }
  
  protected void loadGraph(Class aClass, String resourceString) {
      try {
  	URL resource = new URL("File:///D:/" + resourceString);
      if (resource == null) {
        String message = "Resource \"" + resourceString + "\" not found in classpath of " + aClass;
        D.showError(message);
        
        throw new RuntimeException(message);
      }
      loadGraph(resource);
      } catch (Exception e) {
      	
      }
    }
  
  public void loadGraph(String resourceString) {
      loadGraph(getClass(), resourceString);
    }
  
}
