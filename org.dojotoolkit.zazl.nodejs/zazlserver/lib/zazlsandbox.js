/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var resourceloader = require('zazlutil').resourceloader;
var jsoptimizer = require('jsoptimizer');
var path = require('path');

dojo.require("dojo._base.xhr");
dojo.require("dojox.dtl");
dojo.require("dojox.dtl.Context");
dojo.require("dojox.serverdtl.Request");
dojo.require("dojox.serverdtl.util");

getModuleURLs = function(jsonIn) {
	var moduleDetails = JSON.parse(jsonIn);
	var debug = request.parameters.debug === undefined ? false : true;
	var analysisData = jsoptimizer.getAnalysisData(moduleDetails.modules);
	var urls = []
	if (debug) {
		urls.push(request.contextRoot+"/_javascript?debug=true");
		if (jsoptimizer.config.type === 'syncloader') {
			for (var i = 0; i < analysisData.dependencyList.length; i++) {
				urls.push(request.contextRoot+path.normalize(analysisData.dependencyList[i]));
			}
		}
	} else {
		var url = request.contextRoot + "/_javascript?modules=";
		for (i = 0; i < moduleDetails.modules.length; i++) {
			url += moduleDetails.modules[i];
		}
		url += "&namespaces=";
		for (i = 0; i < moduleDetails.namespaces.length; i++) {
			url += moduleDetails.namespaces[i];
			url += ',';
		}
		url += "&version=";
		url += analysisData.checksum;
		url += "&locale=";
		url += request.locale;
		urls.push(url);
	}
	return JSON.stringify(urls);
}

writeResponse = function(context) {
	var templateString = resourceloader.readText(context.templatePath);
	if (templateString == null) {
	    response.writeHead(500, {'Content-Type': 'text/plain'}); 
	    response.end("Failed to locate template ["+templatePath+"]");
		return;
	}
	
	try {
		var template = new dojox.dtl.Template(templateString);
		var renderedResponse = template.render(new dojox.dtl.Context(context));
	}
	catch (exc) {
	    response.writeHead(500, {'Content-Type': 'text/plain'}); 
	    response.end("Failed to render template ["+context.templatePath+"]["+exc.message+"]");
		return;
	}
	
	var status = 200;
	if (context.getStatus !== undefined && dojo.isFunction(context.getStatus)) {
		status = context.getStatus();
	}
	
	var responseHeaders = {"content-type" : "text/html"};
	if (context.getResponseHeaders !== undefined && dojo.isFunction(context.getResponseHeaders)) {
		responseHeaders = context.getResponseHeaders();
	}
	
    response.writeHead(status, responseHeaders); 
    response.end(renderedResponse);
};

async = mapping.async || false;

request = new dojox.serverdtl.Request(zazlenv);

dojo.locale = djConfig.locale = request.locale;

dojo["require"](mapping.callback);
var contextType = dojo.getObject(mapping.callback);
if (contextType === null) {
    response.writeHead(500, {'Content-Type': 'text/plain'}); 
    response.end("Failed to locate Context type ["+mapping.callback+"]");
} else {
	try {
		readText = resourceloader.readText;
		if (async) {
			new contextType(mapping.parameters, request, function(context) {
				writeResponse(context);
			});
		} else {
			writeResponse(new contextType(mapping.parameters, request));
		}
	} catch (exc) {
	    response.writeHead(500, {'Content-Type': 'text/plain'}); 
	    response.end("Failed to instantiate instance of Context type ["+mapping.callback+"]["+exc.message+"]");
	}
}

