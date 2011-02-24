/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.jetty.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.compressor.JSCompressorResourceLoader;
import org.dojotoolkit.zazl.contentprovider.ContentProvider;
import org.dojotoolkit.zazl.contentprovider.Util;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.resource.Resource;

public class JettyResourceLoader extends JSCompressorResourceLoader {
	private File root = null;
	protected List<ResourceHandler> resourceHandlerList = null;
	protected List<ContentProvider> contentProviderList = null;
	
	public JettyResourceLoader(File root, ResourceHandler[] resourceHandlers, JSCompressorFactory jsCompressorFactory, String[] ignoreList) {
		super(jsCompressorFactory, ignoreList);
		
		this.root = root;
		resourceHandlerList = new ArrayList<ResourceHandler>();
		for (ResourceHandler resourceHandler : resourceHandlers) {
			resourceHandlerList.add(resourceHandler);
		}
		contentProviderList = new ArrayList<ContentProvider>();

		File contentProvidersFile = new File(root, "ContentProviders.json");
		if (contentProvidersFile.exists()) {
			try {
				ContentProvider[] contentProviders = Util.loadContentProviders(contentProvidersFile.toURL());
				for (ContentProvider contentProvider : contentProviders) {
					contentProviderList.add(contentProvider);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public URL getResource(String path) throws IOException {
		return _getResource(normalizePath(path));
	}
	
	protected URL _getResource(String path) throws IOException {
		URL url = lookForResource(path, path);
		if (url == null && contentProviderList.size() > 0) {
			synchronized (contentProviderList) {
				for (ContentProvider contentProvider : contentProviderList) {
					url = lookForResource(contentProvider.base+path, path);
					if (url != null) {
						return url;
					}
					if (path.startsWith(contentProvider.alias)) {
						String moddedPath = path.substring(contentProvider.alias.length());
						url = lookForResource(contentProvider.base+moddedPath, path);
						if (url != null) {
							return url;
						}
					}
				}
			}
		}
		return url;
	}
	
	private URL lookForResource(String path, String originalPath) throws IOException {
		URL url = null;
		File f = new File(root, path);
		if (f.exists()) {
			timestampLookup.put(originalPath, f);
			url = f.toURL();
		}
		else {
			url = getClass().getClassLoader().getResource(path);
		}
		if (url == null) {
			synchronized (resourceHandlerList) {
				for (Handler handler : resourceHandlerList) {
					if (handler instanceof ResourceHandler) {
						Resource resource = ((ResourceHandler)handler).getResource(path);
						if (resource != null && resource.exists()) {
							url = resource.getURL();
							break;
						}
					}
				}
			}
		}
		return url;
	}
}
