/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.jetty.internal;

import java.io.File;

import org.dojotoolkit.zazl.contentprovider.ContentProvider;
import org.dojotoolkit.zazl.servlet.osgi.registry.ContentProviderRegistryListener;
import org.mortbay.jetty.handler.ResourceHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class OSGiJettyResourceLoader extends JettyResourceLoader implements ContentProviderRegistryListener {
	private BundleContext context = null;
	
	public OSGiJettyResourceLoader(File root, ResourceHandler[] resourceHandlers, BundleContext context) {
		super(root, resourceHandlers);
		this.context = context;
	}

	public void addContentProvider(ContentProvider contentProvider) {
		synchronized (contentProviderList) {
			contentProviderList.add(contentProvider);
		}
		synchronized (resourceHandlerList) {
			Bundle b = findBundle(contentProvider.id);
			BundleResourceHandler resourceHandler = new BundleResourceHandler(b, "");
			resourceHandlerList.add(resourceHandler);
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
		synchronized (resourceHandlerList) {
			for (ResourceHandler resourceHandler: resourceHandlerList) {
				if (resourceHandler instanceof BundleResourceHandler && ((BundleResourceHandler)resourceHandler).getId().equals(contentProviderId)) {
					contentProviderList.remove(resourceHandler);
				}
			}
		}
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
}
