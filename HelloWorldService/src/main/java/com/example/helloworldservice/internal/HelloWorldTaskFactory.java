package com.example.helloworldservice.internal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldTaskFactory implements TaskFactory {
	@Override
	public boolean isReady() {
		// This method lets the factory do its own sanity checks to verify
		// it's ready to create and run its tasks.
		return true;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new HelloWorldConsoleTask(),
								new HelloWorldSwingTask());
	}
}

class HelloWorldConsoleTask implements Task {
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Saying hello on the console...");
		
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Hello, world!");
		
		taskMonitor.setProgress(1.0);
	}
	
	@Override
	public void cancel() {
	}
}

class HelloWorldSwingTask extends AbstractTask {
	@Override
	public void run(final TaskMonitor taskMonitor) {
		taskMonitor.setStatusMessage("Saying hello via Swing...");
		sayHello();
		taskMonitor.setProgress(1.0);
	}

	private void sayHello() {
		// The run() method is typically executed in its own thread.
		// If we want to make Swing calls, we need to run that code
		// in the event dispatch thread.
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					sayHello();
				}
			});
			return;
		}
		
		JOptionPane.showMessageDialog(null, "Hello, world!");
	}
}