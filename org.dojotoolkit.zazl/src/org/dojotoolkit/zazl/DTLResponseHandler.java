/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl;

import java.io.IOException;
import java.io.Writer;

public interface DTLResponseHandler {
	public Writer getWriter() throws IOException;
	public void setStatus(int status);
	public void setErrorStatus(int status, String errorMessage);
	public void setHeader(String headerName, String headerValue);
}
