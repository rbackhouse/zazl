/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.compressor.JSCompressorResourceLoader;
import org.dojotoolkit.zazl.contentprovider.ContentProvider;
import org.dojotoolkit.zazl.servlet.osgi.registry.ContentProviderRegistryListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class OSGiResourceLoader extends JSCompressorResourceLoader implements ContentProviderRegistryListener {
	private Bundle serverDTLBundle = null;
	private Bundle[] bundles = null;
	private String[] bundleIds = null;
	private BundleContext context = null;
	private List<ContentProvider> contentProviderList = null;
	
	public OSGiResourceLoader(BundleContext context, 
			                  Bundle serverDTLBundle,
			                  String[] bundleIds,
			                  JSCompressorFactory jsCompressorFactory) {
		super(jsCompressorFactory);
		this.context = context;
		this.bundleIds = bundleIds;
		this.serverDTLBundle = serverDTLBundle; 
		this.contentProviderList = new ArrayList<ContentProvider>();
	}
	
	protected URL _getResource(String path) throws IOException {
		if (bundles == null) {
			bundles = new Bundle[bundleIds.length];
			int i = 0;
			for (String bundleId : bundleIds) {
				bundles[i] = findBundle(bundleId);
				if (bundles[i] == null) {
					throw new RuntimeException("Bundle ["+bundleId+"] cannot be located");
				}
				i++;
			}
		}
		URL url = null;
		synchronized (contentProviderList) {
			for (ContentProvider contentProvider : contentProviderList) {
				Bundle b = findBundle(contentProvider.id);
				if (b != null) {
					url = b.getResource(contentProvider.base+path);
					if (url == null && path.startsWith(contentProvider.alias)) {
						String moddedPath = path.substring(contentProvider.alias.length());
						url = b.getResource(contentProvider.base+moddedPath);
					}
				}
				if (url != null) {
					break;
				}
			}
		}
		
		if (url == null) {
			url = serverDTLBundle.getResource("/jssrc"+path);
		}
		if (url == null) {
			for (Bundle bundle : bundles) {
				url = bundle.getResource(path);
				if (url != null) {
					break;
				}
			}
		}
		return url;
	}
	
	private Bundle findBundle(String symbolicName) {
		Bundle[] bundles = context.getBundles();
		Bundle bundle = null;
		
		for (Bundle b : bundles) {
			if (b.getSymbolicName().equals(symbolicName)) {
				bundle = b;
				break;
			}
		}
		return bundle;
	}

	public void addContentProvider(ContentProvider contentProvider) {
		synchronized (contentProviderList) {
			contentProviderList.add(contentProvider);
		}
	}
	
	public void removeContentProvider(String contentProviderId) {
		synchronized (contentProviderList) {
			for (ContentProvider contentProvider : contentProviderList) {
				if (contentProvider.id.equals(contentProviderId)) {
					contentProviderList.remove(contentProvider);
					break;
				}
			}
		}
	}
}
