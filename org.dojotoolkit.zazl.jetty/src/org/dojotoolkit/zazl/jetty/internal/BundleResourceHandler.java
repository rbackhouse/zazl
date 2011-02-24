/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.jetty.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.resource.Resource;
import org.osgi.framework.Bundle;

public class BundleResourceHandler extends ResourceHandler {
	private Bundle bundle = null;
	private String prefix = null;
	
	public BundleResourceHandler(Bundle bundle, String prefix) {
		this.bundle = bundle;
		this.prefix = prefix;
	}
	
	public String getId() {
		return bundle.getSymbolicName();
	}
	
    public Resource getResource(String path) throws MalformedURLException {
    	try {
    		URL url = bundle.getResource(prefix+path);
    		if (url != null) {
    			return Resource.newResource(url);
    		}
    		else {
    			String strippedPath = stripFirstSegment(path);
    			url = bundle.getResource(prefix+strippedPath);
    			if (url != null) {
	    			return Resource.newResource(url);
	    		}
	    		else {
	    			return null;
	    		}
    		}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
    
	private String stripFirstSegment(String path) {
    	String s = path.substring(1);
    	if (s.indexOf('/') != -1) {
    		s = s.substring(s.indexOf('/'), s.length());
    	}
    	return s;
    }
}
