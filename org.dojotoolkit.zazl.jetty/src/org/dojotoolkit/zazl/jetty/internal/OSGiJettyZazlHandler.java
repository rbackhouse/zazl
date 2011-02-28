/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.jetty.internal;

import java.io.File;
import java.net.MalformedURLException;

import org.dojotoolkit.optimizer.JSOptimizer;
import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.servlet.IServletCallbackHandlerFactory;
import org.dojotoolkit.zazl.servlet.osgi.registry.CallbackHandlerRegistryListener;

public class OSGiJettyZazlHandler extends JettyZazlHandler implements CallbackHandlerRegistryListener {

	public OSGiJettyZazlHandler() {
		super();
	}
	
	protected void initialize(File root, ResourceLoader resourceLoader, RhinoClassLoader rhinoClassLoader, JSOptimizer jsOptimizer) {
		this.jsOptimizer = jsOptimizer;
		try {
			zazlHandler.initialize(Boolean.valueOf(System.getProperty("V8", "false")), 
					               Boolean.valueOf(System.getProperty("DEBUG", "false")), 
					               new File(root, "URLMap.json").toURI().toURL(), 
					               null, 
					               resourceLoader, 
					               rhinoClassLoader);
		} catch (MalformedURLException e) {
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
