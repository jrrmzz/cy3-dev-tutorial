package com.example.zigzag.internal;

import static org.cytoscape.work.ServiceProperties.APPS_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * {@code CyActivator} is a class that is a starting point for OSGi bundles.
 * 
 * A quick overview of OSGi: The common currency of OSGi is the <i>service</i>.
 * A service is merely a Java interface, along with objects that implement the
 * interface. OSGi establishes a system of <i>bundles</i>. Most bundles import
 * services. Some bundles export services. Some do both. When a bundle exports a
 * service, it provides an implementation to the service's interface. Bundles
 * import a service by asking OSGi for an implementation. The implementation is
 * provided by some other bundle.
 * 
 * When OSGi starts your bundle, it will invoke {@CyActivator}'s
 * {@code start} method. So, the {@code start} method is where you put in all
 * your code that sets up your app. This is where you import and export
 * services.
 * 
 * Your bundle's {@code Bundle-Activator} manifest entry has a fully-qualified
 * path to this class. It's not necessary to inherit from
 * {@code AbstractCyActivator}. However, we provide this class as a convenience
 * to make it easier to work with OSGi.
 * 
 * Note: AbstractCyActivator already provides its own {@code stop} method, which
 * {@code unget}s any services we fetch using getService().
 */
public class CyActivator extends AbstractCyActivator {
	/**
	 * This is the {@code start} method, which sets up your app. The
	 * {@code BundleContext} object allows you to communicate with the OSGi
	 * environment. You use {@code BundleContext} to import services or ask OSGi
	 * about the status of some service.
	 */
	@Override
	public void start(BundleContext context) {
		registerAboutTaskFactory(context);
		registerFindPathsTaskFactory(context);
	}

	private void registerAboutTaskFactory(BundleContext context) {
		// Configure the service properties first.
		final Properties properties = new Properties();
		properties.put(TITLE, "About Zig Zag");
		properties.put(PREFERRED_MENU, APPS_MENU);

		// Create the service implementation.
		Version version = context.getBundle().getVersion();
		TaskFactory taskFactory = new AboutTaskFactory(version);

		registerService(context,
			taskFactory,
			TaskFactory.class,
			properties);
	}

	private void registerFindPathsTaskFactory(BundleContext context) {
		// Configure the service properties first.
		final Properties properties = new Properties();
		properties.setProperty(IN_MENU_BAR, "false");
		properties.setProperty(TITLE, "Zig Zag: Find paths from here");

		// Create the service implementation.
		FindPathsNodeViewTaskFactory taskFactory =
			new FindPathsNodeViewTaskFactory();

		registerService(context,
			taskFactory,
			NodeViewTaskFactory.class,
			properties);
	}
}
