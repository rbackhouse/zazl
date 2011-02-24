/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultDTLContext implements DTLContext, DTLResponseHandler {
	protected Object callbackHandler = null;
	protected String[] callbackNames = null;
	protected Map<String, Object> env = null;
	protected Map<String, Object> templateMapping = null;
	protected StringWriter sw = new StringWriter();
	
	public DefaultDTLContext(String target, Object callbackHandler, String[] callbackNames) {
		this.callbackHandler = callbackHandler;
		this.callbackNames = callbackNames;
		env = createEnvObject(target);
	}

	public DefaultDTLContext(String callback) {
		this(callback, new HashMap<String, Object>());
	}
	
	public DefaultDTLContext(String callback, Map<String, Object> parameters) {
		this(callback, new HashMap<String, Object>(), null, null);
	}
		
	public DefaultDTLContext(String callback, Map<String, Object> parameters, Object callbackHandler, String[] callbackNames) {
		this.callbackHandler = callbackHandler;
		this.callbackNames = callbackNames;
		env = createEnvObject("processTemplate");
		templateMapping = new HashMap<String, Object>();
    	templateMapping.put("urlPattern", "processTemplate");
    	templateMapping.put("callback", callback);
    	templateMapping.put("parameters", parameters);
	}
	
	public Map<String, Object> getEnv() {
		return env;
	}

	public Map<String, Object> getTemplateMapping() {
		return templateMapping;
	}

	public DTLResponseHandler getResponseHandler() {
		return this;
	}

	public Object getCallbackHandler() {
		return callbackHandler;
	}

	public String[] getCallbackNames() {
		return callbackNames;
	}
	
    private Map<String, Object> createEnvObject(String path) {
    	Map<String, Object> env = new HashMap<String, Object>();
    	
    	env.put("REQUEST_METHOD", "GET");
    	env.put("URL_PATH", path);
    	env.put("PATH_INFO", path);
    	env.put("QUERY_STRING", "");
    	env.put("CONTENT_TYPE", "");
    	env.put("CONTENT_LENGTH", String.valueOf(-1));
    	env.put("SERVER_NAME", "localhost");
    	env.put("SERVER_PORT", String.valueOf(8080));
    	env.put("SERVER_PROTOCOL", "HTTP/1.1");
    	env.put("GATEWAY_INTERFACE", "CGI/1.1");
    	env.put("SERVER_SOFTWARE", "DTL/0.1");
    	env.put("PATH_TRANSLATED", "");
    	env.put("REMOTE_HOST", "127.0.0.1");
    	env.put("REMOTE_ADDR", "127.0.0.1");
    	env.put("REMOTE_USER", "");
    	env.put("AUTH_TYPE", "");
    	env.put("LOCALE", Locale.getDefault().toString().toLowerCase().replace('_', '-'));
		return env;
    }
    
	public Writer getWriter() throws IOException {
		return sw;
	}

	public void setHeader(String headerName, String headerValue) {
	}

	public void setStatus(int status) {
	}
	
	public void setErrorStatus(int status, String errorMessage) {
	}
	
	public String getResponse() {
		return sw.toString();
	}
}
