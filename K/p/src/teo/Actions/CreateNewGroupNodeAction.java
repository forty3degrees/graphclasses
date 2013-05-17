package teo.Actions;

import java.awt.event.ActionEvent;

import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.NodeRealizer;
import y.view.ProxyShapeNodeRealizer;

public class CreateNewGroupNodeAction extends Graph2DViewActions.AbstractGroupingAction {

    public CreateNewGroupNodeAction() {
      this(null);
    }

    public CreateNewGroupNodeAction(final Graph2DView view) {
      super("CREATE_NEW_GROUP_NODE", view);
    }

    public void actionPerformed(ActionEvent e) {
      final Graph2DView graph2DView = getView(e);
      if (graph2DView != null) {
        createGroupNode(graph2DView);
        graph2DView.getGraph2D().updateViews();
        
      }
    }

    /**
     * Create an empty group node, assigns a name and sets the node bounds.
     */
    protected Node createGroupNode(Graph2DView view) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      Node groupNode;
      try {
        groupNode = createGroupNodeImpl(graph);
        assignGroupName(groupNode, view);
        setGroupNodeBounds(view, graph, groupNode);
      } finally {
        graph.firePostEvent();
      }
      return groupNode;
    }

    protected Node createGroupNodeImpl(Graph2D graph) {
      return getHierarchyManager(graph).createGroupNode(graph);
    }

    protected void setGroupNodeBounds(Graph2DView view, Graph2D graph, Node groupNode) {
      double x = view.getCenter().getX();
      double y = view.getCenter().getY();
      graph.setCenter(groupNode, x, y);
    }

    protected void assignGroupName(Node groupNode, Graph2DView view) {
      NodeRealizer nr = view.getGraph2D().getRealizer(groupNode);
      if (nr instanceof ProxyShapeNodeRealizer) {
        ProxyShapeNodeRealizer pnr = (ProxyShapeNodeRealizer) nr;
        pnr.getRealizer(0).setLabelText(createGroupName(groupNode, view));
        pnr.getRealizer(1).setLabelText(createFolderName(groupNode, view));
      } else {
        nr.setLabelText(createGroupName(groupNode, view));
      }
    }

    protected String createFolderName(Node folderNode, Graph2DView view) {
      return "Folder";
    }

    protected String createGroupName(Node groupNode, Graph2DView view) {
      return "Group";
    }
  }
