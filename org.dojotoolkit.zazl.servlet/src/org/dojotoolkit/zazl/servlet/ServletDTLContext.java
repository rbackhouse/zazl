/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import java.util.HashMap;
import java.util.Map;

import org.dojotoolkit.zazl.DTLContext;
import org.dojotoolkit.zazl.DTLResponseHandler;
import org.dojotoolkit.zazl.callback.ICallbackHandler;

public class ServletDTLContext implements DTLContext {
	private Map<String, Object> env = null;
	private DTLResponseHandler responseHandler = null;
	private ICallbackHandler callbackHandler = null;
	private Map<String, Object> templateMapping = null;
	
	public ServletDTLContext(String contextId, Map<String, Object> env, DTLResponseHandler responseHandler, ICallbackHandler callbackHandler, Map<String, Object> parameters) {
		this.env = env;
		this.responseHandler = responseHandler;
		this.callbackHandler = callbackHandler;
		templateMapping = new HashMap<String, Object>();
    	templateMapping.put("urlPattern", "processTemplate");
    	templateMapping.put("callback", contextId);
    	if (parameters != null) {
    		templateMapping.put("parameters", parameters);
    	} else {
    		templateMapping.put("parameters", new HashMap<String, Object>());
    	}
	}
	
	public Map<String, Object> getEnv() {
		return env;
	}

	public Map<String, Object> getTemplateMapping() {
		return templateMapping;
	}

	public DTLResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public Object getCallbackHandler() {
		return callbackHandler;
	}

	public String[] getCallbackNames() {
		return callbackHandler.getCallbackNames();
	}
}
