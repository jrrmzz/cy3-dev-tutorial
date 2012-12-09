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

class FindPathsNodeViewTaskFactory implements NodeViewTaskFactory {
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

		return new TaskIterator(createTask(nodeView, networkView));
	}

	private Task createTask(final View<CyNode> nodeView,
		final CyNetworkView networkView) {

		return new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				Set<CyEdge> edges = findPaths(nodeView, networkView);
				taskMonitor.setProgress(0.33);

				if (cancelled) {
					return;
				}

				createReachableColumn(networkView);
				resetReachableColumn(networkView);
				updateReachableColumn(networkView, edges);
				taskMonitor.setProgress(0.67);

				VisualStyle style = getVisualStyle(networkView);
				style.apply(networkView);
				taskMonitor.setProgress(1.0);

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
		};
	}

	private void createReachableColumn(CyNetworkView networkView) {
		CyNetwork network = networkView.getModel();
		createReachableColumn(network.getDefaultNodeTable());
		createReachableColumn(network.getDefaultEdgeTable());
	}

	private void createReachableColumn(CyTable table) {
		if (table.getColumn(REACHABLE) != null) {
			return;
		}

		boolean isImmutable = false;
		table.createColumn(REACHABLE, Boolean.class, isImmutable);
	}

	private void resetReachableColumn(CyNetworkView networkView) {
		CyNetwork network = networkView.getModel();
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			row.set(REACHABLE, false);
		}

		for (CyEdge edge : network.getEdgeList()) {
			CyRow row = network.getRow(edge);
			row.set(REACHABLE, false);
		}
	}

	private void updateReachableColumn(CyNetworkView networkView,
		Set<CyEdge> edges) {

		CyNetwork network = networkView.getModel();
		for (CyEdge edge : edges) {
			network.getRow(edge).set(REACHABLE, true);
			network.getRow(edge.getSource()).set(REACHABLE, true);
			network.getRow(edge.getTarget()).set(REACHABLE, true);
		}
	}

	private VisualStyle getVisualStyle(CyNetworkView networkView) {
		// If we already created the ZigZag style, don't create it again.
		VisualStyle style = getVisualStyleByTitle(ZIGZAG_VISUAL_STYLE);
		if (style != null) {
			return style;
		}

		style = visualStyleFactory.createVisualStyle(ZIGZAG_VISUAL_STYLE);

		DiscreteMapping<Boolean, Double> edgeMapping =
			(DiscreteMapping<Boolean, Double>) discreteMappingFactory
				.createVisualMappingFunction(REACHABLE, Boolean.class,
					BasicVisualLexicon.EDGE_WIDTH);
		edgeMapping.putMapValue(true, 9.0);
		style.addVisualMappingFunction(edgeMapping);

		DiscreteMapping<Boolean, Double> nodeMapping =
			(DiscreteMapping<Boolean, Double>) discreteMappingFactory
				.createVisualMappingFunction(REACHABLE, Boolean.class,
					BasicVisualLexicon.NODE_BORDER_WIDTH);
		nodeMapping.putMapValue(true, 9.0);
		style.addVisualMappingFunction(nodeMapping);

		visualMappingManager.addVisualStyle(style);
		visualMappingManager.setVisualStyle(style, networkView);

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
