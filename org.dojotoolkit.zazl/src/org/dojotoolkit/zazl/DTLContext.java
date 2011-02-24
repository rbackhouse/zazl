/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl;

import java.util.Map;

public interface DTLContext {
	public Map<String, Object> getEnv();
	public Map<String, Object> getTemplateMapping();
	public DTLResponseHandler getResponseHandler();
	public Object getCallbackHandler();
	public String[] getCallbackNames();
}
