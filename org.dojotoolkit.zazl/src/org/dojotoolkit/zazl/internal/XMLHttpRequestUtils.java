/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dojotoolkit.json.JSONParser;
import org.dojotoolkit.json.JSONSerializer;

public class XMLHttpRequestUtils {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.zazl");
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String xhrRequest(String shrDataString) {
    	InputStream is = null;
    	String json = null;
    	
    	try {
			logger.logp(Level.FINER, XMLHttpRequestUtils.class.getName(), "xhrRequest", "shrDataString ["+shrDataString+"]");
    		
			Map<String, Object> xhrData = (Map<String, Object>)JSONParser.parse(new StringReader(shrDataString));
			String url = (String)xhrData.get("url");
			String method = (String)xhrData.get("method");
			List headers = (List)xhrData.get("headers");
    		URL requestURL = createURL(url);
    		URI uri = new URI(requestURL.toString());
			
			HashMap httpMethods = new HashMap(7);
			httpMethods.put("DELETE", new HttpDelete(uri));
			httpMethods.put("GET", new HttpGet(uri));
			httpMethods.put("HEAD", new HttpHead(uri));
			httpMethods.put("OPTIONS", new HttpOptions(uri));
			httpMethods.put("POST", new HttpPost(uri));
			httpMethods.put("PUT", new HttpPut(uri));
			httpMethods.put("TRACE", new HttpTrace(uri));
			HttpUriRequest request = (HttpUriRequest) httpMethods.get(method.toUpperCase());
			
			if ( request.equals(null)){
				throw new Error("SYNTAX_ERR");
			}
			
			for (Object header: headers) {
	    		StringTokenizer st = new StringTokenizer((String)header, ":"); 
	    		String name = st.nextToken();
	    		String value = st.nextToken();
	    		request.addHeader(name, value);
	    	}
			
			HttpClient client =  new DefaultHttpClient();
			
			HttpResponse response = client.execute(request);
			Map headerMap = new HashMap();
			
			HeaderIterator headerIter = response.headerIterator();
			
			while(headerIter.hasNext()){
				Header header = headerIter.nextHeader();
				headerMap.put(header.getName(), header.getValue());
			}
			
			int status = response.getStatusLine().getStatusCode();
			String statusText = response.getStatusLine().toString();
			
			is = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    	String line = null;
	    	StringBuffer sb = new StringBuffer();
	    	
	    	while ((line = br.readLine()) != null) {
    			sb.append(line);
    			sb.append('\n');
	    	}
    		Map m = new HashMap();
    		m.put("status", new Integer(status));
    		m.put("statusText", statusText);
    		m.put("responseText", sb.toString());
    		m.put("headers", headerMap.toString());
    		StringWriter w = new StringWriter();
    		JSONSerializer.serialize(w, m);
    		json = w.toString();
			logger.logp(Level.FINER, XMLHttpRequestUtils.class.getName(), "xhrRequest", "json ["+json+"]");
		} catch (Throwable e) {
			logger.logp(Level.SEVERE, XMLHttpRequestUtils.class.getName(), "xhrRequest", "Failed request for ["+shrDataString+"]", e);
		}
		finally {
			if (is != null) {try {is.close();}catch(IOException e){}}
		}
    	return json;
	}
	
	private static URL createURL( String url ){
		URL requestURL;
		try {
			requestURL = new URL(url);
			return requestURL;
		} catch (MalformedURLException e) {
			try {
				requestURL = new URL("http://localhost:8080"+url);
				return requestURL;
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
	}
}
