package com.example.helloworldservice.internal;

import java.io.File;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for Tasks that greet the user.
 */
public class HelloWorldTaskFactory implements TaskFactory {
	@Override
	public boolean isReady() {
		// This method lets the factory do its own sanity checks to verify
		// it's ready to create and run its tasks.
		return true;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// Our factory creates a sequence of three tasks.
		return new TaskIterator(
			new HelloWorldTask(),
			new TunableHelloWorldTask(),
			new TunableGalleryTask()
		);
	}

	/**
	 * A basic Task implementation that says "hello" on the log console.
	 */
	public static class HelloWorldTask implements Task {
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// The setStatusMessage() method lets the user know what's
			// happening. In the Cy3 desktop application, this message
			// only gets shown if the task takes longer than half a
			// second to complete.
			taskMonitor.setStatusMessage("Saying hello on the console...");

			Logger logger = LoggerFactory.getLogger(getClass());
			logger.info("Hello, world!");

			// The setProgress() method tells the TaskMonitor how close we
			// are to task completion, where 1.0 means we're done. As before,
			// the progress meter is only shown by the Cy3 desktop application
			// if the task takes longer than half a second.
			taskMonitor.setProgress(1.0);
		}

		@Override
		public void cancel() {
		}
	}

	/**
	 * A parameterized Task implementation that allows customization of who is
	 * greeted and how. Parameterization is done by annotating fields and
	 * methods as @Tunable.  The Cy3 desktop application scans for these
	 * annotations and generates a UI for configuring the Task before it gets
	 * executed.
	 */
	public static class TunableHelloWorldTask implements Task {
		// Tunable fields and their enclosing class(es) need to be public.
		// The "description" attribute is used as a label in the generated UI
		// for this Task.
		@Tunable(description = "Name")
		public String name = "Bob";

		@Tunable(description = "Greeting")
		public ListSingleSelection<String> greetings =
			new ListSingleSelection<String>(
				"Hello",
				"Good morning",
				"Good evening");

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setTitle("Hello World Task");
			taskMonitor.setStatusMessage("Saying hello on the console...");

			String greeting = greetings.getSelectedValue();
			String message = String.format("%s, %s!", greeting, name);
			
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.warn(message);
			taskMonitor.setProgress(0.5);
			
			// Wait for 2000 ms just so we can see the
			// status message
			Thread.sleep(2000);
			taskMonitor.setStatusMessage(message);
			taskMonitor.setProgress(1.0);
		}

		@Override
		public void cancel() {
		}
	}
	
	/**
	 * Another parameterized Task implementation demonstrating other types
	 * of Tunable parameters.
	 */
	public static class TunableGalleryTask implements Task {
		@Tunable(description = "File to load",
			     params = "fileCategory=network;input=true")
		public File fileToLoad;
		
		@Tunable(description = "File to save",
			     params = "fileCategory=table;input=false")
		public File fileToSave;
		
		@Tunable(description = "Disable bugs")
		public boolean disableBugs = true;
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setStatusMessage(String.format("Loading %s...",
				                                       fileToLoad.getPath()));
			
			taskMonitor.setStatusMessage(String.format("Saving %s...",
				                                       fileToSave.getPath()));
			
			taskMonitor.setStatusMessage(String.format("Bugs %s",
				                                       disableBugs ? "enabled" : "disabled"));
		}
		
		@Override
		public void cancel() {
		}
	}
}