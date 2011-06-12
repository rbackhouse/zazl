/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.contentprovider.ContentProvider;
import org.dojotoolkit.zazl.contentprovider.Util;

public class ZazlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected ZazlHandler zazlHandler = null;
	protected ResourceLoader resourceLoader = null;
	
	public ZazlServlet() {
		zazlHandler = new ZazlHandler();
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		boolean useV8 = false;
		String v8 = getServletContext().getInitParameter("V8");
		if (v8 != null) {
			useV8 = Boolean.valueOf(v8);
		}
		boolean debug = false;
		String debugEnabled = getServletContext().getInitParameter("debug");
		if (debugEnabled != null) {
			debug = Boolean.valueOf(debugEnabled);
		}
		
		resourceLoader = (ResourceLoader)getServletContext().getAttribute("org.dojotoolkit.ResourceLoader");
		if (resourceLoader == null) {
			try {
				ContentProvider[] contentProviders = new ContentProvider[0];
				URL contentProvidersJsonUrl = getServletContext().getResource("/ContentProviders.json");
				if (contentProvidersJsonUrl != null) {
					contentProviders = Util.loadContentProviders(contentProvidersJsonUrl);
				}
				resourceLoader = new ServletDTLResourceHandler(getServletContext(), contentProviders);
				getServletContext().setAttribute("org.dojotoolkit.ResourceLoader", resourceLoader);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			throw new ServletException("A Resource Loader has already been added to the servlet context. Ensure the ZazlServlet is set to initilize first");
		}
		
		RhinoClassLoader rhinoClassLoader = (RhinoClassLoader)getServletContext().getAttribute("org.dojotoolkit.RhinoClassLoader");
		if (rhinoClassLoader == null) {
			rhinoClassLoader = new RhinoClassLoader(resourceLoader);
			getServletContext().setAttribute("org.dojotoolkit.RhinoClassLoader", rhinoClassLoader);
		} else {
			throw new ServletException("A Rhino ClassLoader has already been added to the servlet context. Ensure the ZazlServlet is set to initilize first");
		}
		
		try {
			zazlHandler.initialize(useV8,
					               debug,
					               config.getServletContext().getResource("/URLMap.json"), 
					               config.getServletContext().getResource("/CallbackConfig.json"), 
					               resourceLoader, 
					               rhinoClassLoader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
	}
	
	public void _init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		for (Enumeration<?> e = getServletContext().getAttributeNames(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			Object value = getServletContext().getAttribute(name);
			request.setAttribute(name, value);
		}
		String target = request.getPathInfo();
		boolean handled = zazlHandler.handle(request, response);
		if (!handled) {
			URL url = resourceLoader.getResource(target);
			if (url != null) {
				String mimeType = getServletContext().getMimeType(target);
				if (mimeType == null) {
					mimeType = "text/plain";
				}
				response.setContentType(mimeType);
				InputStream is = null;
				URLConnection urlConnection = null;
				ServletOutputStream os = response.getOutputStream();
				try {
					urlConnection = url.openConnection();
					long lastModifed = urlConnection.getLastModified();
					if (lastModifed > 0) {
					    String ifNoneMatch = request.getHeader("If-None-Match");
						
					    if (ifNoneMatch != null && ifNoneMatch.equals(Long.toString(lastModifed))) {
					    	response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					        return;
					    }

			 			response.setHeader("ETag", Long.toString(lastModifed));
					}
					is = urlConnection.getInputStream();
					byte[] buffer = new byte[4096];
					int len = 0;
					while((len = is.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
				}
				finally {
					if (is != null) {try{is.close();}catch(IOException e){}}
				}
			}
			else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "path ["+target+"] not found");
			}
		}
	}
}
