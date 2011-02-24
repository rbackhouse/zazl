/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.jetty.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.zazl.servlet.osgi.registry.CallbackHandlerRegistryReader;
import org.dojotoolkit.zazl.servlet.osgi.registry.ContentProviderRegistryReader;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, BundleListener, ServiceTrackerCustomizer {
	private static String[] ignoreList = new String[] {
		"/dojo/dojo.js", 
		"^/optimizer/", 
		"^/uglifyjs/", 
		"^/jssrc/", 
		"/dtlapp.js", 
		"/dtlenv.js", 
		"/env.js",
		".*/nls/.*"
	};
	
	private ZazlServer dtlServer = null;
	private BundleContext context = null;
	private BundleResourceHandler[] bundleHandlers = null;
	private String[] bundleIds = null;
	private IExtensionRegistry extensionRegistry = null;
	private ServiceTracker registryTracker = null;
	private CallbackHandlerRegistryReader callbackHandlerRegistryReader = null;
	private ContentProviderRegistryReader contentProviderRegistryReader = null;
	private ServiceReference compressorServiceReference = null;

	public void start(BundleContext context) throws Exception {
		this.context = context;
        contentProviderRegistryReader = new ContentProviderRegistryReader();
        callbackHandlerRegistryReader = new CallbackHandlerRegistryReader();
		
		List<String> bundleIdList = new ArrayList<String>();
		String bundleIdsString = System.getProperty("searchBundleIds");
		if (bundleIdsString != null) {
			StringTokenizer st = new StringTokenizer(bundleIdsString, ",");
			while (st.hasMoreTokens()) {
				bundleIdList.add(st.nextToken().trim());
			}
		}
		bundleIds = new String[bundleIdList.size()];
		bundleIds = bundleIdList.toArray(bundleIds);
		bundleHandlers = new BundleResourceHandler[bundleIds.length+1];
		Bundle serverDTLBundle = findBundle("org.dojotoolkit.zazl");
		if (serverDTLBundle != null) {
			bundleHandlers[0] = new BundleResourceHandler(serverDTLBundle, "/jssrc");
		}
		int i = 0;
		for (String bundleId : bundleIds) {
			Bundle bundle = findBundle(bundleId);
			++i;
			if (bundle != null) {
				bundleHandlers[i] = new BundleResourceHandler(bundle, "/");
			}
		}
		registryTracker = new ServiceTracker(context, IExtensionRegistry.class.getName(), this);
		registryTracker.open();
		
		context.addBundleListener(this);
	}

	public void stop(BundleContext context) throws Exception {
		registryTracker.close();
		context.removeBundleListener(this);
    	if (compressorServiceReference != null) {
    		context.ungetService(compressorServiceReference);
    	}
		context = null;
		if (dtlServer != null) {
			dtlServer.stop();
		}
	}
	
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STARTED) {
			String name = event.getBundle().getSymbolicName();
			boolean interestingBundle = false;
			if (name.equals("org.dojotoolkit.zazl")) {
				bundleHandlers[0] = new BundleResourceHandler(event.getBundle(), "/jssrc");
				interestingBundle = true;
			}
			else {
				int i = 0;
				for (String bundleId : bundleIds) {
					++i;
					if (name.equals(bundleId)) {
						bundleHandlers[i] = new BundleResourceHandler(event.getBundle(), "/");
						interestingBundle = true;
					}
				}
			}
			if (interestingBundle) {
				startJetty();
			}
		}
	}
	
	public Object addingService(ServiceReference reference) {
        Object service = context.getService(reference);

        if (service instanceof IExtensionRegistry && extensionRegistry == null) {
        	extensionRegistry = (IExtensionRegistry)service;
        	startJetty();
        }

        return service;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
        if (service instanceof IExtensionRegistry) {
        	callbackHandlerRegistryReader.stop();
        	contentProviderRegistryReader.stop();
        	extensionRegistry = null;
        }
	}
	
	private synchronized void startJetty() {
		boolean ready = true;
		for (BundleResourceHandler bundleHandler : bundleHandlers) {
			if (bundleHandler == null) {
				ready = false;
				break;
			}
		}
		if (ready && dtlServer == null && extensionRegistry != null) {
			String rootPath = System.getProperty("org.dojotoolkit.zazl.jetty.ROOT_PATH");
			if (rootPath != null) {
				File root = new File(rootPath);
				try {
					boolean useV8 = Boolean.valueOf(System.getProperty("V8", "false"));
					String compressorType = System.getProperty("compressorType");
					JSCompressorFactory jsCompressorFactory = getJSCompressorFactory(useV8, compressorType);
					
					OSGiJettyResourceLoader resourceLoader = new OSGiJettyResourceLoader(root, bundleHandlers, context, jsCompressorFactory, ignoreList);
					OSGiJettyZazlHandler zazlHandler = new OSGiJettyZazlHandler();  

					dtlServer = new ZazlServer(root, bundleHandlers);
					contentProviderRegistryReader.start(extensionRegistry, resourceLoader);
					callbackHandlerRegistryReader.start(extensionRegistry, zazlHandler);
					dtlServer.start(zazlHandler, resourceLoader);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("No org.dojotoolkit.zazl.jetty.ROOT_PATH system property provided");
			}
		}
	}
	
	private JSCompressorFactory getJSCompressorFactory(boolean useV8, String compressorType) {
		JSCompressorFactory jsCompressorFactory = null;
		StringBuffer filter = new StringBuffer();
		if (compressorType != null) {
			if (compressorType.equals("shrinksafe")) {
				filter.append("(dojoServiceId=ShrinksafeJSCompressor)");
			} else if (compressorType.equals("uglifyjs")) {
				if (useV8) {
					filter.append("(dojoServiceId=V8UglifyJSCompressor)");
				} else {
					filter.append("(dojoServiceId=RhinoUglifyJSCompressor)");
				}
			}
		}
		if (filter.length() > 0) {
			try {
				ServiceReference[] srs = context.getServiceReferences(JSCompressorFactory.class.getName(), filter.toString());
				if (srs != null) {
					jsCompressorFactory = (JSCompressorFactory)context.getService(srs[0]);
					if (jsCompressorFactory != null) {
						compressorServiceReference = srs[0];
					}
				}
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
		}
		return jsCompressorFactory;
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
