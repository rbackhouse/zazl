/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.compressor.JSCompressorResourceLoader;
import org.dojotoolkit.zazl.contentprovider.ContentProvider;

public class ServletDTLResourceHandler extends JSCompressorResourceLoader {
	private ServletContext servletContext = null;
	private ContentProvider[] contentProviders = null;

	public ServletDTLResourceHandler(ServletContext servletContext, ContentProvider[] contentProviders, JSCompressorFactory jsCompressorFactory, String[] ignoreList) {
		super(jsCompressorFactory, ignoreList);
		this.servletContext = servletContext;
		this.contentProviders = contentProviders;
	}

	public URL getResource(String path) throws IOException {
		return _getResource(normalizePath(path));
	}
	
	protected URL _getResource(String path) throws IOException {
		URL url = internalGetResource(path, "/jssrc");
		return url;
	}
	
	private URL internalGetResource(String path, String jsSrcPrefix) throws IOException {
		URL url = searchPaths(path, jsSrcPrefix, path);
		if (url != null) {
			return url;
		}
		
		if (contentProviders != null) {
			for (ContentProvider contentProvider : contentProviders) {
				url = searchPaths(contentProvider.base+path, jsSrcPrefix, path);
				if (url != null) {
					return url;
				}
				if (path.startsWith(contentProvider.alias)) {
					String moddedPath = path.substring(contentProvider.alias.length());
					url = searchPaths(contentProvider.base+moddedPath, jsSrcPrefix, path);
					if (url != null) {
						return url;
					}
				}
			}
		}
		return url;
	}
	
	private URL searchPaths(String path, String jsSrcPrefix, String originalPath) throws IOException {
		URL url = servletContext.getResource(path);
		if (url != null) {
			String realPath = servletContext.getRealPath(path);
			if (realPath != null) {
				File file = new File(realPath);
				timestampLookup.put(originalPath, file);
			}
			return url;
		}
			
		url = getClass().getClassLoader().getResource(path);
		if (url != null) {
			return url;
		}
		
		url = getClass().getClassLoader().getResource(path.substring(1));
		if (url != null) {
			return url;
		}
		
		url = getClass().getClassLoader().getResource(jsSrcPrefix+path);
		if (url != null) {
			return url;
		}
		
		return getClass().getClassLoader().getResource((jsSrcPrefix+path).substring(1));
	}
}