/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dojotoolkit.server.util.resource.ResourceLoader;
import org.dojotoolkit.server.util.rhino.RhinoClassLoader;
import org.dojotoolkit.zazl.DTLHandler;
import org.dojotoolkit.zazl.DTLHandlerFactory;
import org.dojotoolkit.zazl.DTLHandlerFactoryImpl;
import org.dojotoolkit.zazl.DefaultDTLContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class Activator implements BundleActivator, ResourceLoader, BundleListener {
	private BundleContext context = null;
	private Bundle dojoBundle = null;
	private Bundle serverDTLBundle = null;
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		dojoBundle = findBundle("org.dojotoolkit.dojo");
		serverDTLBundle = findBundle("org.dojotoolkit.zazl");
		runTest();
	}

	public void stop(BundleContext context) throws Exception {
		this.context = null;
	}

	public URL getResource(String path) throws IOException {
		URL url = context.getBundle().getResource("/resources"+path);
		if (url == null) {
			url = serverDTLBundle.getResource("/jssrc"+path);
			if (url == null) {
				url = dojoBundle.getResource(path);
			}
		}
		return url;
	}

	public long getTimestamp(String path) {
		return -1;
	}

	public String readResource(String path) throws IOException {
		URL url = getResource(path);
		if (url != null) {
			String resource = null;
			InputStream is = null;

			try {
				is = url.openStream();
				BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line = "";
				while((line = r.readLine()) != null){
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
				resource = sb.toString();
			} finally {
	            if (is != null) {
	                try {is.close();}catch (IOException e) {}
	            }
			}
			
			return resource;
		} else {
			return null;
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
	
	public String testCallback(String json) {
		System.out.println("testCallback called");
		String returnString = "{}";
		return returnString;
	}

	public void bundleChanged(BundleEvent event) {
		String name = event.getBundle().getSymbolicName();
		boolean interestingBundle = false;
		if (name.equals("org.dojotoolkit.dojo")) {
			dojoBundle = event.getBundle();
			interestingBundle = true;
		}
		else if (name.equals("org.dojotoolkit.zazl")) {
			serverDTLBundle = event.getBundle();
			interestingBundle = true;
		}
		if (interestingBundle) {
			runTest();
		}
	}

	private void runTest() {
		if (dojoBundle != null && serverDTLBundle != null) {
			DTLHandlerFactory dtlHandlerFactory = new DTLHandlerFactoryImpl(this, false, false, new RhinoClassLoader(this));		
	    	DTLHandler dtlHandler = dtlHandlerFactory.createDTLHandler();
	    	
	    	try {
	    		DefaultDTLContext dtlContext = new DefaultDTLContext("testDTL", this, new String[] {"testCallback"});
	    		dtlHandler.handle(dtlContext);
		    	System.out.println("Rhino processTarget["+dtlContext.getResponse()+"]");
		    	//dtlHandler.processTarget("failDTL");
	    		dtlContext = new DefaultDTLContext("handlers.TestDTLContext", this, new String[] {"testCallback"});
	    		dtlHandler.handle(dtlContext);
		    	System.out.println("Rhino processTemplate["+dtlContext.getResponse()+"]");
		    	Map<String, Object> parameters = new HashMap<String, Object>();
		    	parameters.put("param1", "value1");
		    	parameters.put("param2", new Integer(1));
		    	parameters.put("param3", new Boolean(true));
	    		dtlContext = new DefaultDTLContext("handlers.TestTemplateContext", parameters, this, new String[] {"testCallback"});
	    		dtlHandler.handle(dtlContext);
		    	System.out.println("Rhino processTemplate with params["+dtlContext.getResponse()+"]");
			} catch (IOException e) {
				e.printStackTrace();
			}
			dtlHandlerFactory = new DTLHandlerFactoryImpl(this, true, false);		
	    	dtlHandler = dtlHandlerFactory.createDTLHandler();
	    	try {
	    		DefaultDTLContext dtlContext = new DefaultDTLContext("testDTL", this, new String[] {"testCallback"});
	    		dtlHandler.handle(dtlContext);
		    	System.out.println("V8 processTarget["+dtlContext.getResponse()+"]");
		    	//dtlHandler.processTarget("failDTL");
	    		dtlContext = new DefaultDTLContext("handlers.TestDTLContext", this, new String[] {"testCallback"});
	    		dtlHandler.handle(dtlContext);
		    	System.out.println("V8 processTemplate["+dtlContext.getResponse()+"]");
		    	Map<String, Object> parameters = new HashMap<String, Object>();
		    	parameters.put("param1", "value1");
		    	parameters.put("param2", new Integer(1));
		    	parameters.put("param3", new Boolean(true));
	    		dtlContext = new DefaultDTLContext("handlers.TestTemplateContext", parameters, this, new String[] {"testCallback"});
	    		dtlHandler.handle(dtlContext);
		    	System.out.println("V8 processTemplate with params["+dtlContext.getResponse()+"]");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
