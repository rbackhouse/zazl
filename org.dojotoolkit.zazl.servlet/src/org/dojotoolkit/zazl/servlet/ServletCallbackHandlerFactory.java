/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.zazl.callback.DefaultCallbackHandlerFactory;
import org.dojotoolkit.zazl.callback.ICallbackHandler;

public class ServletCallbackHandlerFactory extends DefaultCallbackHandlerFactory implements IServletCallbackHandlerFactory {

	public ICallbackHandler createCallbackHandler(HttpServletRequest request) {
		return new ServletCallbackHandler(request);
	}
}
