/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.optimizer.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.optimizer.JSAnalysisData;
import org.dojotoolkit.optimizer.Localization;
import org.dojotoolkit.optimizer.Util;

public class AMDJSHandler extends JSHandler {
	
	public AMDJSHandler(String configFileName) {
		super(configFileName);
	}
	
	protected void customHandle(HttpServletRequest request, Writer writer, JSAnalysisData analysisData) throws ServletException, IOException {
		if (analysisData != null) {	
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> implicitDependencies = (List<Map<String, Object>>)config.get("implicitDependencies"); 
			if (implicitDependencies != null) {
				for (Map<String, Object> implicitDependency : implicitDependencies) {
					String uri = (String)implicitDependency.get("uri");
					String id = (String)implicitDependency.get("id");
					String implicitDependencyContent = resourceLoader.readResource(Util.normalizePath(uri));
					if (implicitDependencyContent == null) {
						throw new IOException("Unable to load implicit dependency ["+implicitDependency+"]");
					}
					int missingNameIndex = lookForMissingName(uri, analysisData.getModulesMissingNames());
					if (missingNameIndex != -1) {
						StringBuffer modifiedSrc = new StringBuffer(implicitDependencyContent.substring(0, missingNameIndex));
						modifiedSrc.append("'"+id+"', ");
						modifiedSrc.append(implicitDependencyContent.substring(missingNameIndex));
						implicitDependencyContent = modifiedSrc.toString();
					}
					writer.write(implicitDependencyContent);
				}
			}
			String suffixCode = (String)config.get("suffixCode");
			if (suffixCode != null) {
				writer.write(suffixCode);
			}
			
			List<Localization> localizations = analysisData.getLocalizations();
			if (localizations.size() > 0) {
				Util.writeAMDLocalizations(resourceLoader, writer, localizations, request.getLocale());
			}
			
			for (String textDependency : analysisData.getTextDependencies()) {
				String textContent = resourceLoader.readResource(Util.normalizePath(textDependency));
				writer.write("define('text!");
				writer.write(textDependency);
				writer.write("', function () { return ");
				writer.write(escapeString(textContent));
				writer.write(";});");
				writer.write("\n");
			}
			Map<String, Object> aliases = (Map<String, Object>)config.get("aliases");
			for (String dependency : analysisData.getDependencies()) {
				String content = resourceLoader.readResource(Util.normalizePath(dependency));
				if (content != null) {
					String id = dependency.substring(0, dependency.indexOf(".js"));
					int missingNameIndex = lookForMissingName(id, analysisData.getModulesMissingNames());
					if (missingNameIndex != -1) {
		                for (Iterator<String> itr = aliases.keySet().iterator(); itr.hasNext();) {
		                	String aliasKey = itr.next();
		                	String alias = (String)aliases.get(aliasKey);
		                	if (alias.equals(id)) {
		                		id = aliasKey;
		                		break;
		                	}
		                }
						StringBuffer modifiedSrc = new StringBuffer(content.substring(0, missingNameIndex));
						modifiedSrc.append("'"+id+"', ");
						modifiedSrc.append(content.substring(missingNameIndex));
						content = modifiedSrc.toString();
					}
					
					writer.write(content);
				}
			}
		}
	}
	
	private int lookForMissingName(String uri, List<Map<String, Object>> modulesMissingNamesList) {
		int index = -1;
		for (Map<String, Object> modulesMissingNames : modulesMissingNamesList) {
			if (modulesMissingNames.get("uri").equals(uri)) {
				index = ((Long)modulesMissingNames.get("nameIndex")).intValue();
				break;
			}
		}
		return index;
	}
	
	private String escapeString(String str) {
		return "\"" + str.replace("\"", "\\\"").replaceAll("[\f]", "\\f").replaceAll("[\b]", "\\b").replaceAll("[\n]", "\\n").replaceAll("[\t]", "\\t").replaceAll("[\r]", "\\r")+"\"";
	}	
}