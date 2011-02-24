var file = require("file");
var util = require("util");
var Jack = require("jack");
var http = require("http");
var uri = require("uri");

var Request = require("jack/request").Request;
var Response = require("jack/response").Response;
var fileHandler = new Jack.File(file.cwd());
var zazlFileHandler = new Jack.File(file.path(module.path).resolve("./"));

loadJS = exports.load = function(moduleName) {
    if (moduleName.charAt(0) == '/') {
        moduleName = moduleName.substring(1);
    }
    if (moduleName.lastIndexOf(".js") != -1) {
        moduleName = moduleName.substring(0, moduleName.lastIndexOf(".js"));
    }
    moduleName = file.normal(moduleName);
    try {
        var loadedModule = require(moduleName);
    }
    catch (e) {
        loadedModule = require(file.cwd()+"/"+moduleName);
    }
    //print("require ["+moduleName+"] loadedModule = "+loadedModule);
    return loadedModule;
}

readText = exports.readText = function(path) {
    var filePath = file.join(file.cwd(), path);
    try {
        if (file.isFile(filePath) && file.isReadable(filePath)) {
            var contents = file.read(path, { mode : "t" });
            if (contents) {
                return contents;
            }
        }
    } catch(e) {
        print("readFile error: " + e);
    }
    print("Failed to read text for ["+filePath+"]");
    return null;
}

xhrRequest = exports.xhrRequest = function(jsonIn) {
	var params = eval("("+jsonIn+")");
	var url = uri.parse(params.url);
	if (url.scheme == "") {
		url = "http://localhost:8080"+params.url;
	}
	var response = http.read(url);
	return JSON.encode({status : 200, statusText : "OK", responseText : response.decodeToString(), headers : {}});
}

exports.load("dtlapp.js");

exports.handle = function(env) {
    var request = new Request(env);
    var response = new Response();

    var zazlenv = {};

    zazlenv["REQUEST_METHOD"] = env["REQUEST_METHOD"];
    zazlenv["URL_PATH"] = env["PATH_INFO"];
    zazlenv["PATH_INFO"] = env["PATH_INFO"];
    zazlenv["QUERY_STRING"] = env["QUERY_STRING"] || "";
    zazlenv["CONTENT_TYPE"] = env["CONTENT_TYPE"] || "";
    zazlenv["CONTENT_LENGTH"] = env["CONTENT_LENGTH"] || "";
    zazlenv["SERVER_NAME"] = env["SERVER_NAME"] || "";
    zazlenv["SERVER_PORT"] = env["SERVER_PORT"] || "";
    zazlenv["SERVER_PROTOCOL"] = env["SERVER_PROTOCOL"] || "";
    zazlenv["GATEWAY_INTERFACE"] = "CGI/1.1";
    zazlenv["SERVER_SOFTWARE"] = "Jack/Zazl/0.1";
    zazlenv["PATH_TRANSLATED"] = env["PATH_INFO"] || "";
    zazlenv["REMOTE_HOST"] = env["REMOTE_ADDR"] || "";
    zazlenv["REMOTE_ADDR"] = env["REMOTE_ADDR"] || "";
    zazlenv["REMOTE_USER"] = "Unknown";
    zazlenv["AUTH_TYPE"] = "Unknown";
    zazlenv["jsengine"] = "commonjs";

    for (var i in env) {
        if (i.indexOf("HTTP_") == 0) {
        	zazlenv[i] = env[i];
        }
    }

    zazlenv["PARAMETERS"] = request.params();

    var responseJson = dtlapp(zazlenv);
    if (responseJson != null) {
        var responseData = eval("("+responseJson+")");
        response.status = responseData.status;
        for (var headerName in responseData.headers) {
        	response.setHeader(headerName, responseData.headers[headerName]);
        }
        response.write(responseData.renderedResponse);
        return response.finish();
    }
    else {
        var fileResponse = fileHandler(env);
        if (fileResponse.status == 404) {
            fileResponse = zazlFileHandler(env);
        }
        return fileResponse;
    }
}
