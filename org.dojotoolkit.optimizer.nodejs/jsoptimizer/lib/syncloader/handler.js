/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var resourceloader = require('zazlutil').resourceloader;
var utils = require('zazlutil').utils;
var sandbox = require('zazlutil').sandbox;
var jsdom = require('jsdom');
var path = require('path');

var NAMESPACE_PREFIX = "dojo.registerModulePath('";
var NAMESPACE_MIDDLE = "', '";
var NAMESPACE_SUFFIX = "');\n";

Handler = function(config) {
	this.config = config;
};

Handler.prototype = {
	handle: function(params, analysisData, request, response) {
		var namespaces = [];
		var i;
		if (params.namespace !== undefined) {
			var namespaceArray = params.namespace.split(',');
			for (i = 0; i < namespaceArray.length; i++) {
				var bits = namespaceArray[i].split(':');
				namespaces.push({namespace: bits[0], prefix: bits[1]});
			}
		}
		for (i = 0; i < namespaces.length; i++) {
			response.write(NAMESPACE_PREFIX+namespaces[i].namespace+NAMESPACE_MIDDLE+namespaces[i].prefix+NAMESPACE_SUFFIX);
		}
		
		if (params.modules !== undefined) {
			this.writeLocalizations(response, analysisData.localizations, utils.getBestFitLocale(request.headers["accept-language"]));
			
			for (i = 0; i < analysisData.dependencyList.length; i++) {
				response.write(resourceloader.readText(path.normalize(analysisData.dependencyList[i]), true));
			}
		}
	},
	
	getAnalysisData: function(modules) {
		var dojoSandbox = {
			document: jsdom.jsdom("<html><head></head><body>hello world</body></html>"),
			resourceloader: resourceloader,
			modules: modules,
			hostfile: "optimizer/syncloader/hostenv_optimizer.js"
		};
		var sb = sandbox.createSandbox(dojoSandbox);
		sb.loadJS("dojosandbox.js");
		sb.loadJS("optimizer/syncloader/module.js");
		sb.loadJS("optimizer/syncloader/map.js");
		sb.loadJS("optimizer/syncloader/analyzer.js");
		return sb.loadJS("analyzersandbox.js");
	},

	writeLocalizations: function(response, localizations, locale) {
		response.write(resourceloader.readText("optimizer/syncloader/localization.js"));
		var intermediateLocale = null;
		if (locale.indexOf('-') !== -1) {
			intermediateLocale = locale.split('-')[0];
		}
		var lineSeparator = /\n/g;
		for (var i = 0; i < localizations.length; i++) {
			var rootModule = path.normalize(localizations[i].modpath+'/'+localizations[i].bundlename+".js");
			var fullModule = path.normalize(localizations[i].modpath+'/'+locale+'/'+localizations[i].bundlename+".js");
			if (intermediateLocale !== null) {
				var intermediateModule = path.normalize(localizations[i].modpath+'/'+intermediateLocale+'/'+localizations[i].bundlename+".js");
			}
			var langId = (intermediateLocale === null) ? null : "'"+intermediateLocale+"'";
			var root = resourceloader.readText(rootModule);
			if (root === null) {
				root = "null";
			} else {
				root = root.replace(lineSeparator, " ");
				root = "'"+root+"'";
			}
			var lang = (intermediateModule === null) ? null : resourceloader.readText(intermediateModule);
			if (lang === null) {
				lang = "null";
			} else {
				lang = lang.replace(lineSeparator, " ");
				lang = "'"+lang+"'";
			}
			var langCountry = resourceloader.readText(fullModule);
			if (langCountry === null) {
				langCountry = "null";
			} else {
				langCountry = langCountry.replace(lineSeparator, " ");
				langCountry = "'"+langCountry+"'";
			}
			response.write("dojo.optimizer.localization.load('"+localizations[i].bundlepackage+"', "+langId+", '"+locale+"', "+root+", "+lang+", "+langCountry+");\n");
		}
	}
};

exports.createHandler = function(config) {
	return new Handler(config);
};

