/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.zazl.callback.DefaultCallbackHandler;

public class ServletCallbackHandler extends DefaultCallbackHandler {
	protected HttpServletRequest request = null;

	public ServletCallbackHandler(HttpServletRequest request) {
		this.request = request;
		contextRoot = (request.getContextPath() == null) ? "" : request.getContextPath();
	}
	
	public ServletCallbackHandler() {
		super();
	}
}
