/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.contentprovider;

public class ContentProvider {
	public String id = null;
	public String alias = null;
	public String base = null;
	
	public String toString() {
		return "ContentProvider("+id+","+alias+","+base+")";
	}
}
