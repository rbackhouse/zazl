/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.jetty.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.optimizer.JSOptimizer;
import org.dojotoolkit.optimizer.JSOptimizerFactory;
import org.dojotoolkit.optimizer.servlet.JSHandler;
import org.dojotoolkit.optimizer.servlet.SyncLoaderJSHandler;
import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class JSContentHandler extends AbstractHandler {
	private JSHandler jsHandler = null;
	
	public JSContentHandler(ResourceLoader resourceLoader, JSOptimizerFactory jsOptimizerFactory, RhinoClassLoader rhinoClassLoader, boolean javaChecksum, JSCompressorFactory jsCompressorFactory) {
		jsHandler = new SyncLoaderJSHandler();
		jsHandler.initialize(resourceLoader, rhinoClassLoader, javaChecksum, jsOptimizerFactory, jsCompressorFactory);
	}

	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
		if (target.startsWith("/_javascript")) {
			if (jsHandler.handle(request, response)) {
				Request jettyRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
				jettyRequest.setHandled(true);
			}
		}
	}
	
	public JSOptimizer getJSOptimizer() {
		return jsHandler.getJSOptimizer();
	}
}
