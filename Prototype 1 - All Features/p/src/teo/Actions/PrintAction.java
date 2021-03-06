package teo.Actions;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;

import teo.isgci.gui.ISGCIMainFrame;
import y.option.OptionHandler;
import y.view.Graph2DPrinter;

public class PrintAction extends AbstractAction {
    PageFormat pageFormat;

    OptionHandler printOptions;
    ISGCIMainFrame parent;

    public PrintAction(ISGCIMainFrame p) {
      super("Print");
      parent = p;

      // setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.addInt("Poster Rows", 1);
      printOptions.addInt("Poster Columns", 1);
      printOptions.addBool("Add Poster Coords", false);
      final String[] area = {"View", "Graph"};
      printOptions.addEnum("Clip Area", area, 1);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DPrinter gprinter = new Graph2DPrinter(parent.view);

      // show custom print dialog and adopt values
      if (!printOptions.showEditor(parent.view.getFrame())) {
        return;
      }
      gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
      gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
      gprinter.setPrintPosterCoords(printOptions.getBool("Add Poster Coords"));
      if ("Graph".equals(printOptions.get("Clip Area"))) {
        gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
      } else {
        gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
      }

      // show default print dialogs
      PrinterJob printJob = PrinterJob.getPrinterJob();
      if (pageFormat == null) {
        pageFormat = printJob.defaultPage();
      }
      PageFormat pf = printJob.pageDialog(pageFormat);
      if (pf == pageFormat) {
        return;
      } else {
        pageFormat = pf;
      }

      // setup print job.
      // Graph2DPrinter is of type Printable
      printJob.setPrintable(gprinter, pageFormat);

      if (printJob.printDialog()) {
        try {
          printJob.print();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }
