/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.optimizer;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.zazl.callback.DefaultCallbackHandlerFactory;
import org.dojotoolkit.zazl.callback.ICallbackHandler;
import org.dojotoolkit.zazl.servlet.IServletCallbackHandlerFactory;

public class OptimizerCallbackHandlerFactory extends DefaultCallbackHandlerFactory implements IServletCallbackHandlerFactory {

	public ICallbackHandler createCallbackHandler() {
		return new OptimizerCallbackHandler();
	}

	public ICallbackHandler createCallbackHandler(HttpServletRequest request) {
		return new OptimizerCallbackHandler(request);
	}
}
