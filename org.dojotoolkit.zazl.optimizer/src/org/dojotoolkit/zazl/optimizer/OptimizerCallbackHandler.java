/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.optimizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.optimizer.JSAnalysisData;
import org.dojotoolkit.optimizer.JSOptimizer;
import org.dojotoolkit.zazl.servlet.ServletCallbackHandler;
import org.dojotoolkit.zazl.util.JSONUtils;

public class OptimizerCallbackHandler extends ServletCallbackHandler {
	private static final String[] callbackNames = { 
    	"getModuleURLs"//$NON-NLS-1$
    };
	
	private JSOptimizer jsOptimizer = null;
	
	public OptimizerCallbackHandler() {
		super();
	}

	public OptimizerCallbackHandler(HttpServletRequest request) {
		super(request);
		jsOptimizer = (JSOptimizer)request.getAttribute("org.dojotoolkit.optimizer.JSOptimizer");
	}

	protected void collectCallbackNames(List<String> callbackNameList) {
		super.collectCallbackNames(callbackNameList);
		for (String callbackName : callbackNames) {
			callbackNameList.add(callbackName);
		}
	}
	
	public String getModuleURLs(String json) {
		Map<String, Object> params = (Map<String, Object>)JSONUtils.fromJson(json);
		List<String> moduleList = (List<String>)params.get("modules");
		List<String> namespaceList = (List<String>)params.get("namespaces");
		String[] modules = new String[moduleList.size()];
		modules = moduleList.toArray(modules);
		boolean debug = (request.getParameter("debug") == null) ? false : Boolean.valueOf(request.getParameter("debug"));
		try {
			List<String> jsonUrls = new ArrayList<String>();
			JSAnalysisData analysisData = jsOptimizer.getAnalysisData(modules);

			if (debug) {
				jsonUrls.add(contextRoot+"/_javascript?debug=true");
				String[] dependencies = analysisData.getDependencies();

				for (String dependency : dependencies) {
					jsonUrls.add(contextRoot+dependency);
				}
			} else {
				String checksum = analysisData.getChecksum();
				StringBuffer url = new StringBuffer(); 
				url.append(contextRoot);
				url.append("/_javascript?modules=");
				for (String module : moduleList) {
					url.append(module);
					url.append(',');
				}
				url.append("&namespaces=");
				for (String namespace: namespaceList) {
					url.append(namespace);
					url.append(',');
				}
				url.append("&version=");
				url.append(checksum);
				url.append("&locale=");
				url.append(request.getLocale().toString());
				jsonUrls.add(url.toString());
			}
			return JSONUtils.toJson(jsonUrls);
		} catch (IOException e) {
			e.printStackTrace();
			return "[]";
		}
	}
}
