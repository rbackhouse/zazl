/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dojotoolkit.json.JSONParser;
import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.DTLHandler;
import org.dojotoolkit.zazl.DTLHandlerFactory;
import org.dojotoolkit.zazl.DTLHandlerFactoryImpl;

public class ZazlHandler {
	protected DTLHandlerFactory dtlHandlerFactory = null;
	protected boolean useV8 = false;
	protected boolean debug = false;
	protected ResourceLoader resourceHandler = null;
	protected RhinoClassLoader rhinoClassLoader = null;
	protected List<Pattern> urlPatterns = null;
	protected Map<String, Map<String, Object>> urlMapLookup = null;
	protected Map<String, IServletCallbackHandlerFactory> callBackHandlerLookup = null;
	protected String defaultCallbackHandlerId = ServletCallbackHandlerFactory.class.getName();
	private Object lock = new Object();
	
	public ZazlHandler() {
		callBackHandlerLookup = new HashMap<String, IServletCallbackHandlerFactory>();
	}
	
	public void initialize(boolean useV8,
			               boolean debug,
			               URL urlMapJsonUrl, 
			               URL callbackConfigJsonUrl, 
			               ResourceLoader resourceHandler, 
			               RhinoClassLoader rhinoClassLoader) {
		this.useV8 = useV8;
		this.debug = debug;
		this.resourceHandler = resourceHandler;
		this.rhinoClassLoader = rhinoClassLoader;
		IServletCallbackHandlerFactory callbackHandlerFactory = new ServletCallbackHandlerFactory();
		callBackHandlerLookup.put(callbackHandlerFactory.getClass().getName(), callbackHandlerFactory);
		if (callbackConfigJsonUrl != null) {
			List<String> callbackHandlerFactoryClassNames = loadCallbackConfig(callbackConfigJsonUrl);
			for (String callbackHandlerFactoryClassName : callbackHandlerFactoryClassNames) {
				try {
					callbackHandlerFactory = (IServletCallbackHandlerFactory)Class.forName(callbackHandlerFactoryClassName).newInstance();
					callBackHandlerLookup.put(callbackHandlerFactory.getClass().getName(), callbackHandlerFactory);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (urlMapJsonUrl != null) {
			loadURLPatterns(urlMapJsonUrl);
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		synchronized (lock) {
	    	if (dtlHandlerFactory == null) {
	    		dtlHandlerFactory = new DTLHandlerFactoryImpl(resourceHandler, useV8, debug, rhinoClassLoader);
	    	}
		}
		String target = request.getPathInfo();
    	boolean matchFound = false;
    	String requestPath = request.getPathInfo();
    	if (requestPath.charAt(0) == '/') {
    		requestPath = requestPath.substring(1);
    	}
    	Matcher matcher = null;
    	for (Pattern urlPattern : urlPatterns) {
    		matcher = urlPattern.matcher(requestPath);
    		if (matcher.lookingAt()) {
    			matchFound = true;
    			break;
    		}
    		matcher = urlPattern.matcher(request.getPathInfo());
    		if (matcher.lookingAt()) {
    			matchFound = true;
    			break;
    		}
    	}
    	boolean handled = false;
    	if (matchFound) {
	    	Map<String, Object> env = createEnvObject(target, request);
	   		DTLHandler dtlHandler = dtlHandlerFactory.createDTLHandler(!isDebugEnabled(request));
	   		Map<String, Object> urlMapping = urlMapLookup.get(matcher.pattern().pattern());
	   		String callbackHandlerId = (String)urlMapping.get("callbackHandlerId");
	   		if (callbackHandlerId == null) {
	   			callbackHandlerId = defaultCallbackHandlerId;
	   		}
	   		IServletCallbackHandlerFactory callbackHandlerFactory = callBackHandlerLookup.get(callbackHandlerId);
	   		String contextId = (String)urlMapping.get("callback");
	   		Map<String, Object> parameters = (Map<String, Object>)urlMapping.get("parameters");
			handled = dtlHandler.handle(new ServletDTLContext(contextId, env, new ServletDTLResponseHandler(response), callbackHandlerFactory.createCallbackHandler(request), parameters));
    	}
    	
		return handled;
	}

	public void addCallbackHandlerFactory(IServletCallbackHandlerFactory callbackHandlerFactory) {
		synchronized (callBackHandlerLookup) {
			callBackHandlerLookup.put(callbackHandlerFactory.getClass().getName(), callbackHandlerFactory);
		}
	}
	
	public void removeCallbackHandlerFactory(String callbackHandlerFactoryClassName) {
		synchronized (callBackHandlerLookup) {
			callBackHandlerLookup.remove(callbackHandlerFactoryClassName);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Map<String, Object> createEnvObject(String path, HttpServletRequest request) {
    	Map<String, Object> env = new HashMap<String, Object>();
    	
    	env.put("REQUEST_METHOD", request.getMethod());
    	env.put("URL_PATH", path);
    	env.put("PATH_INFO", request.getPathInfo() == null ? "" : request.getPathInfo());
    	env.put("QUERY_STRING", request.getQueryString() == null ? "" : request.getQueryString());
    	env.put("CONTENT_TYPE", request.getContentType() == null ? "" : request.getContentType());
    	env.put("CONTENT_LENGTH", String.valueOf(request.getContentLength()));
    	env.put("SERVER_NAME", request.getServerName());
    	env.put("SERVER_PORT", String.valueOf(request.getServerPort()));
    	env.put("SERVER_PROTOCOL", request.getProtocol());
    	env.put("GATEWAY_INTERFACE", "CGI/1.1");
    	env.put("SERVER_SOFTWARE", "Jetty DTL/0.1");
    	env.put("PATH_TRANSLATED", request.getPathTranslated() == null ? "" : request.getPathTranslated());
    	env.put("REMOTE_HOST", request.getRemoteHost());
    	env.put("REMOTE_ADDR", request.getRemoteAddr());
    	env.put("REMOTE_USER", request.getRemoteUser() == null ? "" : request.getRemoteUser());
    	env.put("AUTH_TYPE", request.getAuthType() == null ? "" : request.getAuthType());
    	env.put("LOCALE", request.getLocale().toString().toLowerCase().replace('_', '-'));
		for (Enumeration<?> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
			String headerName = (String)headerNames.nextElement();
			headerName = headerName.replace("-", "_").toUpperCase();
			if (!headerName.equals("CONTENT_TYPE") && !headerName.equals("CONTENT_LENGTH")) {
				String headerValue = "";
				for (Enumeration<?> headers = request.getHeaders(headerName); headers.hasMoreElements();) {
					headerValue += (String)headers.nextElement();
				}
				env.put("HTTP_"+headerName, headerValue);
			}
		}
		Map parameterMap = request.getParameterMap();
		if (parameterMap.size() > 0) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			env.put("PARAMETERS", parameters);
			for (Iterator itr = parameterMap.keySet().iterator(); itr.hasNext();) {
				String parameterName = (String)itr.next();
				String[] parameterValues = (String[])parameterMap.get(parameterName);
				List<String> parameterValueList = new ArrayList<String>();
				for (String parameterValue : parameterValues) {
					parameterValueList.add(parameterValue);
				}
				parameters.put(parameterName, parameterValueList);
			}
		}
		return env;
    }
	
	private static boolean isDebugEnabled(HttpServletRequest request) {
		return Boolean.valueOf(request.getParameter("debug")).booleanValue(); //$NON-NLS-1$
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadURLPatterns(URL urlMapJsonUrl) {
		urlPatterns = new ArrayList<Pattern>();
		urlMapLookup = new HashMap<String, Map<String, Object>>();
		Reader r = null;
		InputStream is = null;
	
		try {
			is = urlMapJsonUrl.openStream();
			r = new BufferedReader(new InputStreamReader(is));
			List urlMapJson = (List)JSONParser.parse(r);
			for (Iterator itr = urlMapJson.iterator(); itr.hasNext();) {
				Map<String, Object> urlMapping = (Map<String, Object>)itr.next();
				Object urlPattern = urlMapping.get("urlPattern");
				if (urlPattern instanceof String) {
					Pattern pattern = Pattern.compile((String)urlPattern);
					urlPatterns.add(pattern);
					urlMapLookup.put((String)urlPattern, urlMapping);
				}
				else if (urlPattern instanceof List) {
					List patternList = (List)urlPattern;
					for (Iterator patternListIterator = patternList.iterator(); patternListIterator.hasNext();) {
						String patternString = (String)patternListIterator.next();
						Pattern pattern = Pattern.compile(patternString);
						urlPatterns.add(pattern);
						urlMapLookup.put(patternString, urlMapping);
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (r != null) { try { r.close(); } catch (IOException e){}}
			if (is != null) { try { is.close(); } catch (IOException e){}}
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<String> loadCallbackConfig(URL callbackConfigJsonUrl) {
		List<String> classNameList = null;
		Reader r = null;
		InputStream is = null;

		try {
			is = callbackConfigJsonUrl.openStream();
			r = new BufferedReader(new InputStreamReader(is));
			classNameList = (List<String>)JSONParser.parse(r);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (r != null) { try { r.close(); } catch (IOException e){}}
			if (is != null) { try { is.close(); } catch (IOException e){}}
		}
		return classNameList;
	}
}
