package com.example.helloworldservice.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		Properties properties = new Properties();
		properties.put(ServiceProperties.TITLE, "Say Hello");
		properties.put(ServiceProperties.PREFERRED_MENU, ServiceProperties.APPS_MENU);
		properties.put(ServiceProperties.ENABLE_FOR, "networkAndView");
		
		registerService(context,
				new HelloWorldTaskFactory(),
				TaskFactory.class,
				properties);
	}
}
