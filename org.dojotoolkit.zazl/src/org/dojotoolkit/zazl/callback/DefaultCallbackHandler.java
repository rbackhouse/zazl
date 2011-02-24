/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.callback;

import java.util.ArrayList;
import java.util.List;

public class DefaultCallbackHandler implements ICallbackHandler {
	protected static final String[] callbackNames = new String[] {
        "contextRoot" 
    };

	protected String contextRoot = null;
	
	public DefaultCallbackHandler() {
		contextRoot = "";
	}
	
	public final String[] getCallbackNames() {
		List<String> callbackNameList = new ArrayList<String>();
		collectCallbackNames(callbackNameList);
		String[] names = new String[callbackNameList.size()];
		names = callbackNameList.toArray(names);
 		return names;
	}
	
	protected void collectCallbackNames(List<String> callbackNameList) {
		for (String callbackName : callbackNames) {
			callbackNameList.add(callbackName);
		}
	}

	public String contextRoot(String json) {
		return "{contextRoot: '"+contextRoot+"'}";
	}
}