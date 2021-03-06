package teo.isgci.view.gui;
import javax.swing.JComponent;
import javax.swing.RepaintManager;

public class TracingRepaintManager extends RepaintManager {
	@Override
	public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
		StringBuffer sb = new StringBuffer();
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int count = 0;
		for (StackTraceElement stackEntry : stack) {
			// if (count++ > 25)
			// break;
			sb.append("\t");
			sb.append(stackEntry.getClassName() + ".");
			sb.append(stackEntry.getMethodName() + " [");
			sb.append(stackEntry.getLineNumber() + "]");
			sb.append("\n");
		}
		System.err.println("**** Repaint stack ****");
		System.err.println(sb.toString());

		super.addDirtyRegion(c, x, y, w, h);
	}
}
