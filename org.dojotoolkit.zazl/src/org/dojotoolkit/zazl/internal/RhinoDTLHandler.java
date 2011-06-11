/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.internal;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.json.JSONParser;
import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.server.util.rhino.RhinoJSMethods;
import org.dojotoolkit.zazl.DTLResponseHandler;
import org.dojotoolkit.zazl.util.JSONUtils;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class RhinoDTLHandler {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.zazl");
	private static final String XHR_REQUEST = "xhrRequest"; //$NON-NLS-1$
	private static final String CALLBACK_HANDLER = "callbackHandler"; //$NON-NLS-1$
	private static final String RESPONSE_HANDLER = "responseHandler"; //$NON-NLS-1$
	private static final String EXTERNAL = "external"; //$NON-NLS-1$
	
	private DTLResponseHandler dtlResponseHandler = null;
	private ResourceLoader dtlResourceHandler = null;
	private RhinoClassLoader rhinoClassLoader = null;
	private boolean debug = false;
	private Object external = null;
	private String[] callbacks = null;
	private ContextFactory debugContextFactory = null;
	
	public RhinoDTLHandler(DTLResponseHandler dtlResponseHandler, 
			               ResourceLoader dtlResourceHandler, 
			               boolean debug,
			               Object external,
			               String[] callbacks,
			               RhinoClassLoader rhinoClassLoader,
			               ContextFactory debugContextFactory) {
		this.dtlResponseHandler = dtlResponseHandler;
		this.dtlResourceHandler = dtlResourceHandler;
		this.debug = debug;
		this.external = external;
		this.callbacks = callbacks;
		this.rhinoClassLoader = rhinoClassLoader;
		this.debugContextFactory = debugContextFactory;
	}
	
	public boolean runDTLScript(Map<String, Object> env) throws IOException {
		return runDTLScript(env, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean runDTLScript(Map<String, Object> env, Map<String, Object> templateMapping) throws IOException {
		boolean handled = false;
		try {
			long start = System.currentTimeMillis();
			
			Context ctx = null;
			if (debug) {
				ctx = debugContextFactory.enterContext();
				ctx.setGeneratingDebug(true);
				ctx.setGeneratingSource(true);
				ctx.setOptimizationLevel(-1);
			}
			else {
				ctx = Context.enter();
			}
			
			ScriptableObject scope = ctx.initStandardObjects();
			initScope(scope);
	    	
			StringBuffer sb = new StringBuffer();
	        sb.append("loadJS('dtlapp.js');\n"); //$NON-NLS-1$
	        env.put("jsengine", "rhino");
			sb.append("var env = ");
			sb.append(JSONUtils.toJson(env));
			sb.append("; ");
			if (templateMapping != null) {
				sb.append("var templateMapping = ");
				sb.append(JSONUtils.toJson(templateMapping));
				sb.append("; ");
				sb.append("dtlapp(env, templateMapping);");
			}
			else {
				sb.append("dtlapp(env);");
			}
			logger.logp(Level.FINER, getClass().getName(), "runDTLScript", "["+sb.toString()+"]");
			String responseString = (String)ctx.evaluateString(scope, sb.toString(), "calldtlapp.js", 1, null);//$NON-NLS-1$
			if (responseString != null) {
				Object responseObject = JSONParser.parse(new StringReader(responseString));
				if (responseObject != null && responseObject instanceof Map) {
					Map<String, Object> response = (Map<String, Object>)responseObject;
					Number status = (Number)response.get("status");
					
					try {
			    		logger.logp(Level.FINER, RhinoDTLHandler.class.getName(), "runDTLScript", "Setting status to ["+status+"]");
						dtlResponseHandler.setStatus(status.intValue());
					} catch (NumberFormatException e) {
						dtlResponseHandler.setStatus(500);
					}
					Map<String, String> headers = (Map<String, String>)response.get("headers");
					for (Iterator<String> itr = headers.keySet().iterator(); itr.hasNext(); ) {
						String headerName = itr.next();
						String headerValue = headers.get(headerName);
			    		logger.logp(Level.FINER, RhinoDTLHandler.class.getName(), "runDTLScript", "Setting header ["+headerName+"] to ["+headerValue+"]");
						dtlResponseHandler.setHeader(headerName, headerValue);
					}
					
					String renderedResponse = (String) response.get("renderedResponse");
					dtlResponseHandler.getWriter().write(renderedResponse);
					
					long end = System.currentTimeMillis();
					logger.logp(Level.FINE, getClass().getName(), "runDTLScript", "time : "+(end-start)+" ms for ["+env.get("URL_PATH")+"]");
					handled = true;
				}
			}
		}
		catch (Throwable e){
			logger.logp(Level.SEVERE, RhinoDTLHandler.class.getName(), "runDTLScript", "Exception on request for ["+env.get("URL_PATH")+"]", e);
			dtlResponseHandler.setErrorStatus(500, "Exception on request for ["+env.get("URL_PATH")+"] : "+e.getMessage());
			throw new IOException("Exception on request for ["+env.get("URL_PATH")+"] : "+e.getMessage());
		}
		finally {
			Context.exit();
		}
		return handled;
	}
	
	private void initScope(ScriptableObject scope) {
		RhinoJSMethods.initScope(scope, dtlResourceHandler, rhinoClassLoader, debug);
    	Method[] methods = getClass().getMethods();
    	for (int i = 0; i < methods.length; i++) {
    		if (methods[i].getName().equals(XHR_REQUEST)) {
    			FunctionObject f = new FunctionObject(XHR_REQUEST, methods[i], scope);
    			scope.defineProperty(XHR_REQUEST, f, ScriptableObject.DONTENUM);
    		}
    		else if (methods[i].getName().equals(CALLBACK_HANDLER)) {
    			if (external != null && callbacks != null) {
    				for (String callback : callbacks) {
	        			FunctionObject f = new FunctionObject(callback, methods[i], scope);
	        			scope.defineProperty(callback, f, ScriptableObject.DONTENUM);
    				}
    			}
    		}
    	}
	    scope.associateValue(RESPONSE_HANDLER, dtlResponseHandler);
	    if (external != null && callbacks != null) {
	    	scope.associateValue(EXTERNAL, external);
	    }
	}
	
	public static Object xhrRequest(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		return XMLHttpRequestUtils.xhrRequest((String)args[0]);
	}
	
	public static Object callbackHandler(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		Object returnValue = null;
    	Object external = ((ScriptableObject)thisObj).getAssociatedValue(EXTERNAL);
    	if (external != null) {
    		String functionName = ((BaseFunction)funObj).getFunctionName();
    		logger.logp(Level.FINER, RhinoDTLHandler.class.getName(), "callbackHandler", "calling callback ["+functionName+"] on ["+external.getClass().getName()+"]");
    		try {
				Method m = external.getClass().getMethod(functionName, new Class[] {String.class});
				if (m != null) {
					returnValue = m.invoke(external, new Object[] {args[0]});
				}
			} catch (SecurityException e) {
				logger.logp(Level.SEVERE, RhinoDTLHandler.class.getName(), "callbackHandler", "SecurityException while calling function ["+functionName+"]", e);
			} catch (NoSuchMethodException e) {
				logger.logp(Level.SEVERE, RhinoDTLHandler.class.getName(), "callbackHandler", "NoSuchMethodException while calling function ["+functionName+"]", e);
			} catch (InvocationTargetException e) {
				logger.logp(Level.SEVERE, RhinoDTLHandler.class.getName(), "callbackHandler", "InvocationTargetException while calling function ["+functionName+"]", e);
			} catch (IllegalAccessException e) {
				logger.logp(Level.SEVERE, RhinoDTLHandler.class.getName(), "callbackHandler", "IllegalAccessException while calling function ["+functionName+"]", e);
			}
    	}
   		return returnValue;
	}
}
