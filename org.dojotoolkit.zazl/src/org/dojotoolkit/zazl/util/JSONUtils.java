/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.dojotoolkit.json.JSONParser;
import org.dojotoolkit.json.JSONSerializer;

public class JSONUtils {
	public static Object fromJson(String json) {
		Object o = null;
		try {
			o = JSONParser.parse(new StringReader(json));
		} catch (IOException e) {
		}
		return o;
	}
	
	public static Object fromJson(Reader reader) {
		Object o = null;
		try {
			o = JSONParser.parse(reader);
		} catch (IOException e) {
		}
		return o;
	}
	
	public static String toJson(Object o) {
		return toJson(o, false);
	}

	public static String toJson(Object o, boolean verbose) {
		StringWriter sw = new StringWriter();
		try {
			JSONSerializer.serialize(sw, o, verbose);
		} catch (IOException e) {
		}
		return sw.toString();
	}
	
	public static void toJson(Writer writer, Object o) throws IOException {
		JSONSerializer.serialize(writer, o);
	}
}
