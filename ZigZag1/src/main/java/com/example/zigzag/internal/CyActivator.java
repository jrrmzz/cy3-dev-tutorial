package com.example.zigzag.internal;

import java.util.Properties;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.cytoscape.service.util.AbstractCyActivator;
import static org.cytoscape.work.ServiceProperties.*;

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
 * When OSGi first loads your bundle, it will invoke {@CyActivator}'s
 * {@code start} method. So, the {@code start} method is where
 * you put in all your code that sets up your app. This is where you import and
 * export services.
 * 
 * Your bundle's {@code Bundle-Activator} manifest entry has a fully-qualified
 * path to this class. You don't really have to write a class that extends
 * {@code AbstractCyActivator}. However, we provide this class as a convenience
 * to make it easier to work with OSGi.
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
		Version version = context.getBundle().getVersion();

		/*
		 * Here, we'll export a TaskFactory service. When exporting a service,
		 * you can include a Properties object, which is just a String-to-String
		 * map with additional information about the service you're exporting.
		 * If given the right set of properties, Cytoscape creates a menu item
		 * for the TaskFactory we're exporting. The Properties object specifies
		 * the names of the menu and the menu item for our TaskFactory.
		 * 
		 * This TaskFactory presents a "About" dialog for our app. It puts it in
		 * the "Apps" menu.
		 */
		final Properties aboutProps = new Properties();
		aboutProps.put(TITLE, "About Zig Zag");
		aboutProps.put(PREFERRED_MENU, APPS_MENU);
		registerService(context, new AboutTaskFactory(version), TaskFactory.class,
				aboutProps);

		final Properties findPathsProps = new Properties();
		findPathsProps.setProperty(IN_MENU_BAR, "false");
		findPathsProps.setProperty(TITLE, "Zig Zag: Find paths from here");
		registerService(context, new FindPathsNodeViewTaskFactory(),
				NodeViewTaskFactory.class, findPathsProps);
	}
}
