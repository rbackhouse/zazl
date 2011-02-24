/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.contentprovider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dojotoolkit.json.JSONParser;

public class Util {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ContentProvider[] loadContentProviders(URL contentProvidersJsonUrl) {
		ContentProvider[] contentProviders = null;
		Reader r = null;
		InputStream is = null;
		List<ContentProvider> contentProviderList = new ArrayList<ContentProvider>();
		try {
			is = contentProvidersJsonUrl.openStream();
			r = new BufferedReader(new InputStreamReader(is));
			List contentProvidersJson = (List)JSONParser.parse(r);
			for (Iterator itr = contentProvidersJson.iterator(); itr.hasNext();) {
				Map<String, Object> contentProviderJson = (Map<String, Object>)itr.next();
				ContentProvider contentProvider = new ContentProvider();
				contentProvider.alias = (String)contentProviderJson.get("alias");
				contentProvider.base = (String)contentProviderJson.get("base");
				contentProviderList.add(contentProvider);
			}
			contentProviders = new ContentProvider[contentProviderList.size()];
			contentProviders = contentProviderList.toArray(contentProviders);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (r != null) { try { r.close(); } catch (IOException e){}}
			if (is != null) { try { is.close(); } catch (IOException e){}}
		}
		return contentProviders;
	}
}
