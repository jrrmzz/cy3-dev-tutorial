package com.example.zigzag.internal;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import org.osgi.framework.Version;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

class AboutTaskFactory implements TaskFactory {
	private Version version;

	public AboutTaskFactory(Version version) {
		this.version = version;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new Task() {
			@Override
			public void run(TaskMonitor monitor) {
				/*
				 * We have to wrap this in an "invokeLater" so that the task
				 * dialog doesn't show up in the background of our dialog.
				 */
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(null,
								"<html><h1>Zig Zag <small>" + version
										+ "</small></h1>"
										+ "An incredibly awesome app for "
										+ "finding paths of a node.</html>",
								"About Zig Zag",
								JOptionPane.INFORMATION_MESSAGE);
					}
				});
			}

			@Override
			public void cancel() {
			}
		});
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
