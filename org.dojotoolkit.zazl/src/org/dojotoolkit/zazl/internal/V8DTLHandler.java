/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dojotoolkit.rt.v8.V8Exception;
import org.dojotoolkit.rt.v8.V8JavaBridge;
import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.zazl.DTLResponseHandler;
import org.dojotoolkit.zazl.util.JSONUtils;

public class V8DTLHandler extends V8JavaBridge {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.zazl");
	private static final String jsSrc = "loadJS('/dtlapp.js'); dtlapp(env);";
	private static final String jsSrcWithMapping = "loadJS('/dtlapp.js'); dtlapp(env, templateMapping);";
	
	private ResourceLoader dtlResourceHandler = null;
	private DTLResponseHandler dtlResponseHandler = null;
	private Object external = null;
	private String[] callbacks = null;
	
	public V8DTLHandler(ResourceLoader dtlResourceHandler,  
			            DTLResponseHandler dtlResponseHandler,
			            Object external,
			            String[] callbacks,
			            boolean useCache) {
		super(useCache);
		this.dtlResourceHandler = dtlResourceHandler;
		this.dtlResponseHandler = dtlResponseHandler;
		this.external = external;
		this.callbacks = callbacks;
	}
	
	public boolean runDTLScript(Map<String, Object> env) throws IOException {
		return runDTLScript(env, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean runDTLScript(Map<String, Object> env, Map<String, Object> templateMapping) throws IOException {
		boolean handled = false;
		
		long start = System.currentTimeMillis();
		StringBuffer sb = new StringBuffer();
        env.put("jsengine", "v8");
		sb.append("var env = ");
		sb.append(JSONUtils.toJson(env));
		sb.append("; ");
		if (templateMapping != null) {
			sb.append("var templateMapping = ");
			sb.append(JSONUtils.toJson(templateMapping));
			sb.append("; ");
			sb.append(jsSrcWithMapping);
		}
		else {
			sb.append(jsSrc);
		}
		try {
			Object responseObject = null;
			logger.logp(Level.FINER, getClass().getName(), "runDTLScript", "["+sb.toString()+"]");
			
			if (external != null && callbacks != null) {
				String[] allCallbacks = new String[1+callbacks.length];
				int i = 1;
				allCallbacks[0] = "xhrRequest";
				for (String callback : callbacks) {
					allCallbacks[i++] = callback;
				}
				responseObject = runScript(sb.toString(), allCallbacks, external);
			}
			else {
				responseObject = runScript(sb.toString(), new String[]{"xhrRequest"});
			}
			if (responseObject != null && responseObject instanceof Map) {
				Map<String, Object> response = (Map<String, Object>)responseObject;
				Number status = (Number)response.get("status");
				dtlResponseHandler.setStatus(status.intValue());
	    		logger.logp(Level.FINER, getClass().getName(), "runDTLScript", "Setting status to ["+status+"]");
				Map<String, String> headers = (Map<String, String>)response.get("headers");
				for (Iterator<String> itr = headers.keySet().iterator(); itr.hasNext(); ) {
					String headerName = itr.next();
					String headerValue = headers.get(headerName);
		    		logger.logp(Level.FINER, getClass().getName(), "runDTLScript", "Setting header ["+headerName+"] to ["+headerValue+"]");
					dtlResponseHandler.setHeader(headerName, headerValue);
				}
				String renderedResponse = (String)response.get("renderedResponse");
				dtlResponseHandler.getWriter().write(renderedResponse);
				long end = System.currentTimeMillis();
				logger.logp(Level.FINE, getClass().getName(), "runDTLScript", "time : "+(end-start)+" ms for ["+env.get("URL_PATH")+"]");
				handled = true;
			}
		}
		catch (V8Exception e) {
			if (compileErrors.size() > 0) {
				for (Throwable t : compileErrors) {
					logger.logp(Level.SEVERE, getClass().getName(), "runDTLScript", "IOException on request for ["+env.get("URL_PATH")+"]", t);
				}
			}
			logger.logp(Level.SEVERE, getClass().getName(), "runDTLScript", "Exception on request for ["+env.get("URL_PATH")+"]", e);
			dtlResponseHandler.setErrorStatus(500, "Exception on request for ["+env.get("URL_PATH")+"] : "+e.getMessage());
			throw new IOException("Exception on request for ["+env.get("URL_PATH")+"] : "+e.getMessage());
		}
		return handled;
	}
	
	@Override
	public String readResource(String path, boolean useCache) throws IOException {
		try {
			logger.logp(Level.FINER, getClass().getName(), "getResource", "Path ["+path+"]");
			URI uri = new URI(path);
			path = uri.normalize().getPath();
			if (path.charAt(0) != '/') {
				path = '/'+path;
			}
			logger.logp(Level.FINER, getClass().getName(), "getResource", "Normalized path ["+path+"]");
			return dtlResourceHandler.readResource(path, useCache);
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	public String xhrRequest(String shrDataString) {
		return XMLHttpRequestUtils.xhrRequest(shrDataString);
	}
}
