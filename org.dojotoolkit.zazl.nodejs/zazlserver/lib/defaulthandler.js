/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var url = require("url");
var path = require("path");  
var fs = require("fs");  
var resourceloader = require('zazlutil').resourceloader;
   
expoxt.handle = function(request, response) {  
	var uri = url.parse(request.url).pathname;  
	var filename = path.join(process.cwd(), uri);  
	path.exists(filename, function(exists) {  
		if(!exists) {  
			response.sendHeader(404, {"Content-Type": "text/plain"});  
			response.write("404 Not Found\n");  
			response.close();  
			return;  
		}  

		fs.readFile(filename, "binary", function(err, file) {  
			if(err) {  
				response.sendHeader(500, {"Content-Type": "text/plain"});  
				response.write(err + "\n");  
				response.close();  
				return;  
			}  

			response.sendHeader(200);  
			response.write(file, "binary");  
			response.close();  
		});  
	});  
}