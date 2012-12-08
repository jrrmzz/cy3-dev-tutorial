package com.example.helloworld.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	final static Logger logger = LoggerFactory.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info("Hello, world!");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("Goodbye!");
	}
}
