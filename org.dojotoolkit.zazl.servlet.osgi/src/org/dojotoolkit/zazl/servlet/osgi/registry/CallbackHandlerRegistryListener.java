/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet.osgi.registry;

import org.dojotoolkit.zazl.servlet.IServletCallbackHandlerFactory;

public interface CallbackHandlerRegistryListener {
	public void addCallbackHandlerFactory(IServletCallbackHandlerFactory callbackHandlerFactory);
	public void removeCallbackHandlerFactory(String callbackHandlerFactoryClassName);
}
