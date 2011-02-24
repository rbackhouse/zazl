/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var http = require('http');
var paperboy = require('paperboy');
var path = require('path');
var zazlhandler = require('./zazlhandler');
var fs = require('fs');
var jsoptimizer = require('jsoptimizer');

var appdir = process.argv.length > 2 ? process.argv[2] : process.cwd();
appdir = fs.realpathSync(appdir);

var dojodir = process.argv.length > 3 ? process.argv[3] : process.cwd()+"/dojo";
dojodir = fs.realpathSync(dojodir);

http.createServer(function (request, response) {
	var handled = zazlhandler.handle(request, response);
	if (!handled) {
		handled = jsoptimizer.handle(request, response);
	}
	if (!handled) {
		paperboy
	    .deliver(appdir, request, response)
	    .addHeader('Expires', 300)
	    .addHeader('X-PaperRoute', 'Node')
	    .error(function(statCode, msg) {
	    	response.writeHead(statCode, {'Content-Type': 'text/plain'});
	    	response.end("Error " + statCode);
	    })
	    .otherwise(function(statCode, msg) {
			paperboy
		    .deliver(dojodir, request, response)
		    .addHeader('Expires', 300)
		    .addHeader('X-PaperRoute', 'Node')
		    .error(function(statCode, msg) {
		    	response.writeHead(statCode, {'Content-Type': 'text/plain'});
		    	response.end("Error " + statCode);
		    })
		    .otherwise(function(err) {
		    	response.writeHead(404, {'Content-Type': 'text/plain'});
		    	response.end("Error 404: File not found");
		    });
	    });
	}
}).listen(8080);

console.log("Browse demos at \"http://localhost:8080/index.html\"");
