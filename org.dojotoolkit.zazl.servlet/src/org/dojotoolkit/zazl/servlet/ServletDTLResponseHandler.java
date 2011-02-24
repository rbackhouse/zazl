/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.dojotoolkit.zazl.DTLResponseHandler;

public class ServletDTLResponseHandler implements DTLResponseHandler {
	private HttpServletResponse response = null;
	
	public ServletDTLResponseHandler(HttpServletResponse response) {
		this.response = response;
	}
	
	public Writer getWriter() throws IOException {
		return response.getWriter();
	}

	public void setStatus(int status) {
		response.setStatus(status);
	}
	
	public void setErrorStatus(int status, String errorMessage) {
		try {
			response.sendError(status, errorMessage);
		} catch (IOException e) {
		}
	}
	
	public void setHeader(String headerName, String headerValue) {
		response.setHeader(headerName, headerValue);
	}
}
