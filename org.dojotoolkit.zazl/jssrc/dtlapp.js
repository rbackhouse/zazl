/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var global = this;
var window = this;

dtlapp = function(env, templateMapping){
	function getMapping(path, mappings){
		var regex;
		var isValidURL = false;
		for (var i = 0; i < mappings.length; i++) {
			if (mappings[i].urlPattern instanceof Array || typeof mappings[i].urlPattern == "array") {
				for (var j = 0; j < mappings[i].urlPattern.length; j++) {
					regex = new RegExp(mappings[i].urlPattern[j]);
					if (regex.test(path)) {
						return mappings[i];
					}
				}
			}
			else {
				regex = new RegExp(mappings[i].urlPattern);
				if (regex.test(path)) {
					return mappings[i];
				}
			}
		}
		return null;
	}
	
	var mapping = templateMapping;
	
	if (mapping === undefined) {
		var mappings = eval("(" + readText("URLMap.json") + ")");
		mapping = getMapping(env["URL_PATH"], mappings);
	}
	
	if (mapping === null && env["URL_PATH"].match(/\.dtl/) === null) {
		return null;
	}
	
	if (global["net"] !== undefined) {
		var _net = net;
		delete net;
	}
	if (global["com"] !== undefined) {
		var _com = com;
		delete com;
	}
	if (global["org"] !== undefined) {
		var _org = org;
		delete org;
	}
	
	djConfig = {
		isDebug: false,
		usePlainJson: true,
		baseUrl: "/dojo/"
	};
	
	if (env["jsengine"] === "v8") {
		window.XMLHttpRequest = function(){
			this.headers = {};
			this.responseHeaders = {};
		};
	}
	else {
		loadJS("/dtlenv.js");
	}
	loadJS("/XMLHttpRequest.js");
	
	window.location = env["URL_PATH"];
	
	loadJS("dojo/_base/_loader/bootstrap.js");
	dojo._hasResource = {};
	loadJS("dojo/_base/_loader/loader.js");
	
	if (env["jsengine"] === "v8") {
		loadJS("/hostenv_v8.js");
	}
	else if (env["jsengine"] === "commonjs") {
		loadJS("/hostenv_commonjs.js");
	}
	else {
		loadJS("/hostenv_dtl.js");
	}
	
	loadJS("dojo/_base.js");
	dojo.require("dojo._base.xhr");
	
	dojo.require("dojox.dtl");
	dojo.require("dojox.dtl.Context");
	dojo.require("dojox.serverdtl.Request");
	dojo.require("dojox.serverdtl.util");
	
	var request = new dojox.serverdtl.Request(env);
	
	dojo.locale = djConfig.locale = request.locale;

	if (mapping !== null) {
		dojo["require"](mapping.callback);
		var contextType = dojo.getObject(mapping.callback);
		if (contextType === null) {
			throw new Error("Failed to locate Context type ["+mapping.callback+"]");
		}
		try {
			var context = new contextType(mapping.parameters, request);
		}
		catch (exc) {
			throw new Error("Failed to instantiate instance of Context type ["+mapping.callback+"]["+exc.message+"]");
		}
	}
	
	if (context === null || request.urlPath.match(/\.dtl/)) {
		var templatePath = request.urlPath;
		context = {};
		context.request = request;
	}
	else {
		templatePath = context.templatePath;
	}
	
	var templateString = readText(templatePath);
	if (templateString == null) {
		throw new Error("Failed to locate template ["+templatePath+"]");
	}
	try {
		var template = new dojox.dtl.Template(templateString);
		var renderedResponse = template.render(new dojox.dtl.Context(context));
	}
	catch (exc) {
		throw new Error("Failed to render template ["+context.templatePath+"]["+exc.message+"]");
	}
	
	var status = 200;
	if (context.getStatus !== undefined && dojo.isFunction(context.getStatus)) {
		status = context.getStatus();
	}
	
	var responseHeaders = {"content-type" : "text/html"};
	if (context.getResponseHeaders !== undefined && dojo.isFunction(context.getResponseHeaders)) {
		responseHeaders = context.getResponseHeaders();
	}
	
	if (_net !== null) {
		net = _net;
	}
	if (_com !== null) {
		com = _com;
	}
	if (_org !== null) {
		org = _org;
	}
	return dojo.toJson({status : status, headers : responseHeaders, renderedResponse : renderedResponse});
};
