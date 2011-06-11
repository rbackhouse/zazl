/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.internal.DTLHandlerImpl;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.Main;
import org.mozilla.javascript.tools.debugger.ScopeProvider;

public class DTLHandlerFactoryImpl implements DTLHandlerFactory {
	public static final Object SCOPE_KEY = new Object();
	public static final ContextFactory debugContextFactory = new ContextFactory();
	
	private ResourceLoader dtlResourceHandler = null;
	private boolean useV8 = false;
	private boolean debug = false;
	private RhinoClassLoader rhinoClassLoader = null;
	
	public DTLHandlerFactoryImpl(ResourceLoader dtlResourceHandler, boolean useV8, boolean debug) {
		this(dtlResourceHandler, useV8, debug, new RhinoClassLoader(dtlResourceHandler, DTLHandlerFactoryImpl.class.getClassLoader()));
	}	
	
	public DTLHandlerFactoryImpl(ResourceLoader dtlResourceHandler, boolean useV8, boolean debug, RhinoClassLoader rhinoClassLoader) {
		this.dtlResourceHandler = dtlResourceHandler;
		this.useV8 = useV8;
		this.debug = debug;
		this.rhinoClassLoader = rhinoClassLoader;
		if (!useV8 && debug) {
			Main.mainEmbedded(debugContextFactory, new ScopeProvider() {
				public Scriptable getScope() {
					Context cx= Context.getCurrentContext();
					Scriptable scope= (Scriptable) cx.getThreadLocal(SCOPE_KEY);
					return scope;
				}
			}, "Rhino JavaScript Debugger"); //$NON-NLS-1$
		}
	}
	
	public DTLHandler createDTLHandler() {
		return new DTLHandlerImpl(dtlResourceHandler, rhinoClassLoader, useV8, debug, debugContextFactory);
	}
}
