package org.dojotoolkit.zazl.servlet.osgi;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

public class ZazlHttpContext implements HttpContext {
	private HttpContext defaultHttpContext = null;
	private static ZazlHttpContext INSTANCE = null;
	private ZazlHttpContext(HttpContext defaultHttpContext) {
		this.defaultHttpContext = defaultHttpContext;
	}
	
	public static ZazlHttpContext getSingleton(HttpContext defaultHttpContext) {
		if (INSTANCE == null) {
			INSTANCE = new ZazlHttpContext(defaultHttpContext);
		}
		return INSTANCE;
	}
	
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return defaultHttpContext.handleSecurity(request, response);
	}

	public URL getResource(String name) {
		return defaultHttpContext.getResource(name);
	}

	public String getMimeType(String name) {
		return defaultHttpContext.getMimeType(name);
	}
}
