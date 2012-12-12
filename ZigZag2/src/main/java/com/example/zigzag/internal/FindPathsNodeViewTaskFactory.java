package com.example.zigzag.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class FindPathsNodeViewTaskFactory implements NodeViewTaskFactory {
	private static final String REACHABLE = "reachable";

	private static final String ZIGZAG_VISUAL_STYLE = "ZigZag";

	private VisualMappingManager visualMappingManager;
	private VisualStyleFactory visualStyleFactory;
	private VisualMappingFunctionFactory discreteMappingFactory;

	public FindPathsNodeViewTaskFactory(VisualStyleFactory visualStyleFactory,
		VisualMappingFunctionFactory discreteMappingFactory,
		VisualMappingManager visualMappingManager) {

		this.visualStyleFactory = visualStyleFactory;
		this.discreteMappingFactory = discreteMappingFactory;
		this.visualMappingManager = visualMappingManager;
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return nodeView != null && networkView != null;
	}

	@Override
	public TaskIterator createTaskIterator(final View<CyNode> nodeView,
		final CyNetworkView networkView) {

		Task task = new FindPathsTask(networkView, nodeView);
		return new TaskIterator(task);
	}

	// AbstractTask implements the cancel() method for us. All we need to do
	// is poll AbstractTask.cancelled to see if we need to abort.
	public class FindPathsTask extends AbstractTask {
		private final CyNetworkView networkView;
		private final View<CyNode> nodeView;

		private FindPathsTask(CyNetworkView networkView,
			View<CyNode> nodeView) {
			this.networkView = networkView;
			this.nodeView = nodeView;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			Set<CyEdge> edges = findPaths(nodeView, networkView);
			taskMonitor.setProgress(0.25);

			if (cancelled) {
				return;
			}

			ensureReachableColumnsExist(networkView);
			resetReachableColumn(networkView);
			taskMonitor.setProgress(0.5);

			updateReachableColumn(networkView, edges);
			taskMonitor.setProgress(0.75);

			VisualStyle style = getZigZagVisualStyle();

			// Assign the ZigZag style to the network view.
			visualMappingManager.setVisualStyle(style, networkView);

			// Apply the VisualProperties computed by the VisualStyle to
			// the network view.
			style.apply(networkView);
			taskMonitor.setProgress(1.0);

			// Tell the network view it needs to update itself.
			networkView.updateView();
		}

		private Set<CyEdge> findPaths(View<CyNode> nodeView,
			CyNetworkView networkView) {
			// Use breadth-first search algorithm to find all connected
			// nodes and edges.
			final Set<CyNode> nodes = new HashSet<CyNode>();
			final Set<CyEdge> edges = new HashSet<CyEdge>();
			final LinkedList<CyNode> pending = new LinkedList<CyNode>();

			final CyNetwork network = networkView.getModel();
			final CyNode startingNode = nodeView.getModel();
			pending.push(startingNode);

			while (!pending.isEmpty()) {
				if (cancelled) {
					return Collections.emptySet();
				}

				final CyNode node = pending.pop();
				if (nodes.contains(node)) {
					continue;
				}

				pending.addAll(network.getNeighborList(node,
					CyEdge.Type.OUTGOING));
				edges.addAll(network.getAdjacentEdgeList(node,
					CyEdge.Type.OUTGOING));
				nodes.add(node);
			}
			return edges;
		}

		private void ensureReachableColumnsExist(CyNetworkView networkView) {
			CyNetwork network = networkView.getModel();

			// Ensure the column REACHABLE exists in the node table.
			ensureReachableColumnExists(network.getDefaultNodeTable());

			// ...as well as the edge table.
			ensureReachableColumnExists(network.getDefaultEdgeTable());
		}

		private void ensureReachableColumnExists(CyTable table) {
			if (table.getColumn(REACHABLE) != null) {
				// The column REACHABLE already exists in this table so we
				// can return early.
				return;
			}

			boolean isImmutable = false;
			table.createColumn(REACHABLE, Boolean.class, isImmutable);
		}

		private void resetReachableColumn(CyNetworkView networkView) {
			// Set the REACHABLE column in all node table rows to false.
			CyNetwork network = networkView.getModel();
			for (CyNode node : network.getNodeList()) {
				CyRow row = network.getRow(node);
				row.set(REACHABLE, false);
			}

			// Set the REACHABLE column in all edge table rows to false.
			for (CyEdge edge : network.getEdgeList()) {
				CyRow row = network.getRow(edge);
				row.set(REACHABLE, false);
			}
		}

		private void updateReachableColumn(CyNetworkView networkView,
			Set<CyEdge> edges) {

			// Set the REACHABLE column of the rows for the given edges
			// (and their associated nodes) to true.
			CyNetwork network = networkView.getModel();
			for (CyEdge edge : edges) {
				network.getRow(edge).set(REACHABLE, true);
				network.getRow(edge.getSource()).set(REACHABLE, true);
				network.getRow(edge.getTarget()).set(REACHABLE, true);
			}
		}

		private VisualStyle getZigZagVisualStyle() {
			// If we already created the ZigZag style, don't create it again.
			VisualStyle style = getVisualStyleByTitle(ZIGZAG_VISUAL_STYLE);
			if (style != null) {
				return style;
			}

			style = visualStyleFactory.createVisualStyle(ZIGZAG_VISUAL_STYLE);

			// Create a mapping for edge table column, REACHABLE, and
			// the EDGE_WIDTH visual property. We need to cast to
			// DiscreteMapping to set the mapping values.
			DiscreteMapping<Boolean, Double> edgeMapping =
				(DiscreteMapping<Boolean, Double>) discreteMappingFactory
					.createVisualMappingFunction(REACHABLE, Boolean.class,
						BasicVisualLexicon.EDGE_WIDTH);
			edgeMapping.putMapValue(true, 9.0);
			style.addVisualMappingFunction(edgeMapping);

			// Create a mapping for node table column, REACHABLE, and
			// the NODE_BORDER_WIDTH visual property. We need to cast to
			// DiscreteMapping to set the mapping values.
			DiscreteMapping<Boolean, Double> nodeMapping =
				(DiscreteMapping<Boolean, Double>) discreteMappingFactory
					.createVisualMappingFunction(REACHABLE, Boolean.class,
						BasicVisualLexicon.NODE_BORDER_WIDTH);
			nodeMapping.putMapValue(true, 9.0);
			style.addVisualMappingFunction(nodeMapping);

			// Register the new VisualStyle with the VisualMappingManager.
			visualMappingManager.addVisualStyle(style);

			return style;
		}

		private VisualStyle getVisualStyleByTitle(String title) {
			for (VisualStyle style : visualMappingManager.getAllVisualStyles()) {
				if (style.getTitle().equals(title)) {
					return style;
				}
			}
			return null;
		}
	}
}
