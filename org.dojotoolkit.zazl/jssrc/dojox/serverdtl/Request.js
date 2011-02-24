/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
(function() {
dojo.provide("dojox.serverdtl.Request");

dojo.declare("dojox.serverdtl.Request", null, {
    constructor: function(env) {
        this.requestMethod = env["REQUEST_METHOD"];
		this.urlPath = env["URL_PATH"];
    	this.pathInfo = env["PATH_INFO"];
    	this.queryString = env["QUERY_STRING"];
    	this.contentType = env["CONTENT_TYPE"];
    	this.contentLength = env["CONTENT_LENGTH"];
    	this.serverName = env["SERVER_NAME"];
    	this.serverPort = env["SERVER_PORT"];
    	this.serverProtocol = env["SERVER_PROTOCOL"];
		this.gatewayInterface = env["GATEWAY_INTERFACE"];
		this.serverSoftware = env["SERVER_SOFTWARE"];
		this.pathTranslated = env["PATH_TRANSLATED"];
		this.remoteHost = env["REMOTE_HOST"];
		this.remoteAddr = env["REMOTE_ADDR"];
		this.remoteUser = env["REMOTE_USER"];
		this.authType = env["AUTH_TYPE"];
		this.locale = env["LOCALE"];
		
		this.headers = [];
		for (var name in env) {
            if (name.search(/^HTTP_*?/) != -1) {
				var header = {};
				header.name = name;
				header.value = env[name];
				this.headers.push(header);
			}	
		}
		if (env.PARAMETERS !== undefined) {
			this.parameters = env.PARAMETERS;
		}
		else {
			this.parameters = {};
		}
		
		try {
			var contextRootHolder = dojox.serverdtl.util.invokeCallback(contextRoot, "{}");
			this.contextRoot = contextRootHolder.contextRoot;
		} catch (exc) {
		}
		
		if (this.contextRoot === undefined) {	
			this.contextRoot = "";
		}
    }
});

})();