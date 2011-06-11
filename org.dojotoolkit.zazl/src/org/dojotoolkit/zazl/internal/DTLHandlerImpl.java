/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.internal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.DTLContext;
import org.dojotoolkit.zazl.DTLHandler;
import org.mozilla.javascript.ContextFactory;

public class DTLHandlerImpl implements DTLHandler {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.zazl");
	private ResourceLoader dtlResourceHandler = null;
	private RhinoClassLoader rhinoClassLoader = null;
	private boolean useV8 = false;
	private boolean debug = false;
	private ContextFactory debugContextFactory = null;
	
	public DTLHandlerImpl(ResourceLoader dtlResourceHandler,
						  RhinoClassLoader rhinoClassLoader,
			              boolean useV8,
			              boolean debug,
			              ContextFactory debugContextFactory) {
		this.dtlResourceHandler = dtlResourceHandler;
		this.rhinoClassLoader = rhinoClassLoader;
		this.useV8 = useV8;
		this.debug = debug;
		this.debugContextFactory = debugContextFactory;
		logger.logp(Level.FINER, DTLHandlerImpl.class.getName(), "DTLHandlerImpl", "useV8 = "+useV8 + " debug = "+debug+" v8Available = "+V8DTLHandler.v8Available);
	}
	
	public boolean handle(DTLContext dtlContext) throws IOException {
		boolean handled = false;
		
		if (useV8 && V8DTLHandler.v8Available) {
			V8DTLHandler v8DTLHandler = new V8DTLHandler(dtlResourceHandler, dtlContext.getResponseHandler(), dtlContext.getCallbackHandler(), dtlContext.getCallbackNames()); 
			handled = v8DTLHandler.runDTLScript(dtlContext.getEnv(), dtlContext.getTemplateMapping());
		}
		else {
			RhinoDTLHandler rhinoDTLHandler = new RhinoDTLHandler(dtlContext.getResponseHandler(), dtlResourceHandler, debug, dtlContext.getCallbackHandler(), dtlContext.getCallbackNames(), rhinoClassLoader, debugContextFactory);
			handled = rhinoDTLHandler.runDTLScript(dtlContext.getEnv(), dtlContext.getTemplateMapping());
		}
		return handled;
	}
}
