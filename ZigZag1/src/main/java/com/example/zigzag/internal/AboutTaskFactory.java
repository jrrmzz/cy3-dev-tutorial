package com.example.zigzag.internal;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import org.osgi.framework.Version;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class AboutTaskFactory implements TaskFactory {
	private Version version;

	public AboutTaskFactory(Version version) {
		this.version = version;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new Task() {
			@Override
			public void run(final TaskMonitor monitor) {
				showAboutDialog();
			}

			@Override
			public void cancel() {
			}
		});
	}

	private void showAboutDialog() {
		// Tasks are run in their own thread. However, Swing
		// code needs to be run in the event dispatch thread.
		// We need to check whether we're in the right thread.
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showAboutDialog();
				}
			});
			return;
		}

		JOptionPane.showMessageDialog(null,
			"<html><h1>Zig Zag <small>" + version
				+ "</small></h1>"
				+ "An incredibly awesome app for "
				+ "finding paths of a node.</html>",
			"About Zig Zag",
			JOptionPane.INFORMATION_MESSAGE);
	}
}
