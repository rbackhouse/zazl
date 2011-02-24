/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet.osgi;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.servlet.IServletCallbackHandlerFactory;
import org.dojotoolkit.zazl.servlet.ZazlServlet;
import org.dojotoolkit.zazl.servlet.osgi.registry.CallbackHandlerRegistryListener;

public class OSGiZazlServlet extends ZazlServlet implements CallbackHandlerRegistryListener {
	private static final long serialVersionUID = 1L;
	
	private ResourceLoader resourceLoaderOverride = null;
	private RhinoClassLoader rhinoClassLoaderOverride = null;
	
	public OSGiZazlServlet(ResourceLoader resourceLoader, RhinoClassLoader rhinoClassLoader) {
		resourceLoaderOverride = resourceLoader;
		rhinoClassLoaderOverride = rhinoClassLoader;
	}
	
	public void init(ServletConfig config) throws ServletException {
		_init(config);
		resourceLoader = resourceLoaderOverride;
		boolean useV8 = Boolean.valueOf(System.getProperty("V8", "false"));
		boolean debug = Boolean.valueOf(System.getProperty("DEBUG", "false"));
		try {
			zazlHandler.initialize(useV8,
					               debug,
					               resourceLoader.getResource("/URLMap.json"), 
					               null, 
					               resourceLoader, 
					               rhinoClassLoaderOverride);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void addCallbackHandlerFactory(IServletCallbackHandlerFactory callbackHandlerFactory) {
		zazlHandler.addCallbackHandlerFactory(callbackHandlerFactory);
	}
	
	public void removeCallbackHandlerFactory(String callbackHandlerFactoryClassName) {
		zazlHandler.removeCallbackHandlerFactory(callbackHandlerFactoryClassName);
	}
	
}
