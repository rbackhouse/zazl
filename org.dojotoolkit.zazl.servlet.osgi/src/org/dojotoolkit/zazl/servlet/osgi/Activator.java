/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet.osgi;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.dojotoolkit.compressor.JSCompressorFactory;
import org.dojotoolkit.json.JSONParser;
import org.dojotoolkit.optimizer.JSOptimizerFactory;
import org.dojotoolkit.optimizer.servlet.JSHandler;
import org.dojotoolkit.optimizer.servlet.JSServlet;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.servlet.osgi.registry.CallbackHandlerRegistryReader;
import org.dojotoolkit.zazl.servlet.osgi.registry.ContentProviderRegistryReader;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, ServiceTrackerCustomizer, BundleListener  {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.zazl.servlet.osgi");
    private HttpService httpService = null;
    private ServiceTracker httpServiceTracker = null;
	private IExtensionRegistry extensionRegistry = null;
	private ServiceTracker registryTracker = null;
    private BundleContext context = null;
	private Bundle serverDTLBundle = null;
	private boolean initialized = false;
	private Object lock = new Object();
	private ContentProviderRegistryReader contentProviderRegistryReader = null;
	private CallbackHandlerRegistryReader callbackHandlerRegistryReader = null;
	private OSGiResourceLoader resourceHandler = null;
	private JSCompressorFactory jsCompressorFactory = null;
	private JSOptimizerFactory jsOptimizerFactory = null;
	private ServiceTracker jsCompressorFactoryTracker = null;
	private ServiceTracker jsOptimizerFactoryServiceTracker = null;

	public void start(BundleContext context) throws Exception {
        this.context = context;
        contentProviderRegistryReader = new ContentProviderRegistryReader();
        callbackHandlerRegistryReader = new CallbackHandlerRegistryReader();
		serverDTLBundle = findBundle("org.dojotoolkit.zazl");
        
		context.addBundleListener(this);
        httpServiceTracker = new ServiceTracker(context, HttpService.class.getName(), this);
        httpServiceTracker.open();
		registryTracker = new ServiceTracker(context, IExtensionRegistry.class.getName(), this);
		registryTracker.open();
		boolean useV8 = Boolean.valueOf(System.getProperty("V8", "false"));
		jsCompressorFactoryTracker = new JSCompressorFactoryServiceTracker(context, useV8, System.getProperty("compressorType"));
		jsCompressorFactoryTracker.open();
		jsOptimizerFactoryServiceTracker = new JSOptimizerFactoryServiceTracker(context, useV8, System.getProperty("jsHandlerType"));
		jsOptimizerFactoryServiceTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		jsCompressorFactoryTracker.close();
		jsOptimizerFactoryServiceTracker.close();
		registryTracker.close();
        httpServiceTracker.close();
	}

	public Object addingService(ServiceReference reference) {
        Object service = context.getService(reference);

        if (service instanceof HttpService && httpService == null) {
            httpService = (HttpService)service;
            initialize();
        }
        else if (service instanceof IExtensionRegistry && extensionRegistry == null) {
        	extensionRegistry = (IExtensionRegistry)service;
            initialize();
        }

        return service;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
        if (service instanceof HttpService) {
			logger.logp(Level.CONFIG, getClass().getName(), "removedService", "Unregistering jsServlet");
        	httpService.unregister("/_javascript");
			logger.logp(Level.CONFIG, getClass().getName(), "removedService", "Unregistering zazlServlet");
        	httpService.unregister("/");
        	httpService = null;
        }
        else if (service instanceof IExtensionRegistry) {
        	callbackHandlerRegistryReader.stop();
        	contentProviderRegistryReader.stop();
        	extensionRegistry = null;
        }
	}
	
	@SuppressWarnings("unchecked")
	private void initialize() {
		synchronized (lock) {
			if (!initialized) {
				if (httpService != null && 
					extensionRegistry != null && 
					jsCompressorFactory != null && 
					jsOptimizerFactory != null && 
					serverDTLBundle != null) {
					boolean javaChecksum = Boolean.valueOf(System.getProperty("javaChecksum", "false"));
					List<String> bundleIdList = new ArrayList<String>();
					String bundleIdsString = System.getProperty("searchBundleIds");
					if (bundleIdsString != null) {
						StringTokenizer st = new StringTokenizer(bundleIdsString, ",");
						while (st.hasMoreTokens()) {
							bundleIdList.add(st.nextToken().trim());
						}
					}
					String[] bundleIds = new String[bundleIdList.size()];
					bundleIds = bundleIdList.toArray(bundleIds);
					resourceHandler = new OSGiResourceLoader(context, serverDTLBundle, bundleIds, jsCompressorFactory);
					contentProviderRegistryReader.start(extensionRegistry, resourceHandler);
					RhinoClassLoader rhinoClassLoader = new RhinoClassLoader(resourceHandler);

		            OSGiZazlServlet zazlServlet = new OSGiZazlServlet(resourceHandler, rhinoClassLoader);
					callbackHandlerRegistryReader.start(extensionRegistry, zazlServlet);

					String stringWarmupValues = System.getProperty("optimizerWarmup");
					List<List<String>> warmupValues = null;
					if (stringWarmupValues != null) {
						try {
							warmupValues = (List<List<String>>)JSONParser.parse(new StringReader(stringWarmupValues));
						} catch (IOException e) {
							logger.logp(Level.SEVERE, getClass().getName(), "init", "IOException while parsing warmup values", e);
						}
					}
		            JSServlet jsServlet = new JSServlet(resourceHandler, jsOptimizerFactory, rhinoClassLoader, javaChecksum, System.getProperty("jsHandlerType"), warmupValues);
		            try {
		    			logger.logp(Level.CONFIG, getClass().getName(), "initialize", "Registering zazlServlet");
						httpService.registerServlet("/", zazlServlet, null, ZazlHttpContext.getSingleton(httpService.createDefaultHttpContext()));
		    			logger.logp(Level.CONFIG, getClass().getName(), "initialize", "Registering jsServlet");
						httpService.registerServlet("/_javascript", jsServlet, null, ZazlHttpContext.getSingleton(httpService.createDefaultHttpContext()));
					} catch (ServletException e) {
						e.printStackTrace();
					} catch (NamespaceException e) {
						e.printStackTrace();
					}
					initialized = true;
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

	public void bundleChanged(BundleEvent event) {
		String name = event.getBundle().getSymbolicName();
		boolean doIniitialize = false;
		if (name.equals("org.dojotoolkit.zazl")) {
			serverDTLBundle = event.getBundle();
			doIniitialize = true;
		}
		if (doIniitialize){
			initialize();
		}
	}
	
	
	private class JSCompressorFactoryServiceTracker extends ServiceTracker {
		private boolean useV8 = false; 
		private String compressorType = null;
		
		public JSCompressorFactoryServiceTracker(BundleContext context, boolean useV8, String compressorType) {
			super(context, JSCompressorFactory.class.getName(), null);
			this.useV8 = useV8;
			this.compressorType = compressorType;
		}
		
		public Object addingService(ServiceReference reference) {
			String dojoServiceId = null;
			if (compressorType != null) {
				if (compressorType.equals("shrinksafe")) {
					dojoServiceId = "ShrinksafeJSCompressor";
				} else if (compressorType.equals("uglifyjs")) {
					if (useV8) {
						dojoServiceId = "V8UglifyJSCompressor";
					} else {
						dojoServiceId = "RhinoUglifyJSCompressor";
					}
				}
			}
			if (dojoServiceId != null && reference.getProperty("dojoServiceId").equals(dojoServiceId)) {
				jsCompressorFactory = (JSCompressorFactory)context.getService(reference);
				initialize();
			}
			return context.getService(reference);
		}
	}
	
	private class JSOptimizerFactoryServiceTracker extends ServiceTracker {
		private boolean useV8 = false;
		private String jsHandlerType = null;
		
		public JSOptimizerFactoryServiceTracker(BundleContext context, boolean useV8, String jsHandlerType) {
			super(context, JSOptimizerFactory.class.getName(), null);
			this.useV8 = useV8;
			this.jsHandlerType = jsHandlerType;
		}
		
		public Object addingService(ServiceReference reference) {
			String dojoServiceId = null;
			if (jsHandlerType.equals(JSHandler.AMD_HANDLER_TYPE)) {
				if (useV8) {
					dojoServiceId = "AMDV8JSOptimizer";
				} else {
					dojoServiceId = "AMDRhinoJSOptimizer";
				}
			} else {
				if (useV8) {
					dojoServiceId = "V8JSOptimizer";
				} else {
					dojoServiceId = "RhinoJSOptimizer";
				}
			}
			if (dojoServiceId != null && reference.getProperty("dojoServiceId").equals(dojoServiceId)) {
				jsOptimizerFactory = (JSOptimizerFactory)context.getService(reference);
				initialize();
			}
			return context.getService(reference);
		}
	}
}
