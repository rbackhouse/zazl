/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var http = require('http');
var url = require('url');
var path = require('path');
var fs = require('fs');
var jsdom = require('jsdom');
var qs = require('querystring');
var resourceloader = require('zazlutil').resourceloader;
var sandbox = require('zazlutil').sandbox;
var jsoptimizer = require('jsoptimizer');
var utils = require('zazlutil').utils;

var appdir = process.argv.length > 2 ? process.argv[2] : process.cwd();
appdir = fs.realpathSync(appdir);
resourceloader.addProvider(appdir);

var dojodir = process.argv.length > 3 ? process.argv[3] : process.cwd()+"/dojo";
dojodir = fs.realpathSync(dojodir);
resourceloader.addProvider(dojodir);

resourceloader.addProvider(path.dirname(module.filename));

var mappings = eval("(" + resourceloader.readText("URLMap.json") + ")");

getMapping = function(path){
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
};

exports.handle = function(request, response) {
	mapping = getMapping(request.url);
	
	if (mapping === null) {
		return false;
	}
	if (request.method === "POST") {
		var postData = "";
		request.on("data", function (data) {
			postData += data;
        });
		request.on("end", function () {
			var zazlenv = buildEnvObj(request, qs.parse(postData));
			handleZazlRequest(request, response, mapping, zazlenv);
        });
	} else {
		var zazlenv = buildEnvObj(request);
		handleZazlRequest(request, response, mapping, zazlenv);
	}
    return true;
};

handleZazlRequest = function(request, response, mapping, zazlenv) {
	var dojoSandbox = {
		document: jsdom.jsdom("<html><head></head><body>hello world</body></html>"),
		require: require,
		response: response,
		mapping: mapping,
		zazlenv: zazlenv,
		hostfile: "hostenv_nodejs.js"
	};
	var sb = sandbox.createSandbox(dojoSandbox);
	sb.loadJS("dojosandbox.js");
	sb.loadJS("zazlsandbox.js");
};

buildEnvObj = function(request, postData) {
    var zazlenv = {};
    
    zazlenv["REQUEST_METHOD"] = request.method;
    zazlenv["URL_PATH"] = request.url;
    zazlenv["PATH_INFO"] = request.url;
    zazlenv["QUERY_STRING"] = url.parse(request.url).query;
    zazlenv["CONTENT_TYPE"] = "";
    zazlenv["CONTENT_LENGTH"] = -1;
    zazlenv["SERVER_NAME"] = (request.headers['host'].indexOf(':') !== -1) ? request.headers['host'].substring(0, request.headers['host'].indexOf(':')) : request.headers['host'];
    zazlenv["SERVER_PORT"] = (request.headers['host'].indexOf(':') !== -1) ? request.headers['host'].substring(0, request.headers['host'].indexOf(':')) : "";
    zazlenv["SERVER_PROTOCOL"] = "HTTP/"+request.httpVersion;
    zazlenv["GATEWAY_INTERFACE"] = "CGI/1.1";
    zazlenv["SERVER_SOFTWARE"] = "NodeJS/Zazl/0.1";
    zazlenv["PATH_TRANSLATED"] = request.url;
    zazlenv["REMOTE_HOST"] = request.connection.remoteAddress;
    zazlenv["REMOTE_ADDR"] = request.connection.remoteAddress;
    zazlenv["REMOTE_USER"] = "Unknown";
    zazlenv["AUTH_TYPE"] = "Unknown";
    zazlenv["LOCALE"] = utils.getBestFitLocale(request.headers["accept-language"]);
    zazlenv["jsengine"] = "commonjs";
    
    for (var headerName in request.headers) {
        zazlenv["HTTP_"+headerName] = request.headers[headerName];
    }
    zazlenv["PARAMETERS"] = {};
    var params = url.parse(request.url, true).query;
    for (var paramName in params) {
    	zazlenv["PARAMETERS"][paramName] = [params[paramName]];
    }
    if (postData !== undefined) {
        for (var paramName in postData) {
        	zazlenv["PARAMETERS"][paramName] = [postData[paramName]];
        }
    }
    return zazlenv;
};
