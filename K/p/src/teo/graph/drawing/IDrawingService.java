package teo.graph.drawing;

import java.util.List;

import teo.graph.view.GraphView;

public interface IDrawingService {
	void initializeView(List<GraphView> graphs);
	void updateView(List<GraphView> graphs);
}
