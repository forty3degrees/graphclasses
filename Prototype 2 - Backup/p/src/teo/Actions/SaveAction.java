package teo.Actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import teo.isgci.gui.ISGCIMainFrame;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.util.D;

public class SaveAction extends AbstractAction {
    JFileChooser chooser;
    ISGCIMainFrame parent;
    public SaveAction(ISGCIMainFrame p) {
      super("Save...");
      parent = p;
      chooser = null;
    }

    protected GraphMLIOHandler createGraphMLIOHandler() {
        return new GraphMLIOHandler();
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

      URL url = parent.view.getGraph2D().getURL();
      if (url != null && "file".equals(url.getProtocol())) {
        try {
          chooser.setSelectedFile(new File(new URI(url.toString())));
        } catch (URISyntaxException e1) {
          // ignore
        }
      }

      if (chooser.showSaveDialog(parent.mainPan) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        if(!name.endsWith(".graphml")) {
          name += ".graphml";
        }
        IOHandler ioh = createGraphMLIOHandler();

        try {
          ioh.write(parent.view.getGraph2D(), name);
        } catch (IOException ioe) {
          D.show(ioe);
        }
      }
    }
    
    
    
    
  }