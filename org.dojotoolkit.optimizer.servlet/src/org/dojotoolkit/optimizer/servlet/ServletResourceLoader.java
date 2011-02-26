/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.optimizer.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.compressor.JSCompressorResourceLoader;

public class ServletResourceLoader extends JSCompressorResourceLoader {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.optimizer");
	private ServletContext servletContext = null;

	public ServletResourceLoader(ServletContext servletContext, JSCompressorFactory jsCompressorFactory, String[] ignoreList) {
		super(jsCompressorFactory, ignoreList);
		this.servletContext = servletContext;
	}
	
	public URL getResource(String path) throws IOException {
		return _getResource(normalizePath(path));
	}

	protected URL _getResource(String path) throws IOException {
		URL url = servletContext.getResource(path);
		if (url != null) {
			String realPath = servletContext.getRealPath(path);
			if (realPath != null) {
				File file = new File(realPath);
				timestampLookup.put(path, file);
			}
			logger.logp(Level.FINE, getClass().getName(), "_getResource", "["+path+"] ["+url+"]");
			return url;
		}
		url = getClass().getClassLoader().getResource(path);	
		if (url != null) {
			logger.logp(Level.FINE, getClass().getName(), "_getResource", "["+path+"] ["+url+"]");
			return url;
		}
		url = getClass().getClassLoader().getResource(path.substring(1));	
		logger.logp(Level.FINE, getClass().getName(), "_getResource", "["+path.substring(1)+"] ["+url+"]");
		return url;
	}
}