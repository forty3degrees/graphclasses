package teo.isgci.view.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import teo.isgci.core.App;
import teo.isgci.core.IDrawingService;
import teo.isgci.view.gui.ISGCIMainFrame;

public class SaveAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
    JFileChooser chooser;
    ISGCIMainFrame parent;
    public SaveAction(ISGCIMainFrame p) {
      super("Save...");
      parent = p;
      chooser = null;
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

      IDrawingService drawingService = App.getDrawingService(parent);
      URL url = drawingService.getCurrentFile();
      if (url != null && "file".equals(url.getProtocol())) {
        try {
          chooser.setSelectedFile(new File(new URI(url.toString())));
        } catch (URISyntaxException e1) {
          // ignore
        }
      }

      if (chooser.showSaveDialog(parent.mainPan) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        try {
        	drawingService.exportGML(name);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      }
    }
    
    
    
    
  }