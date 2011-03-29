/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var fs = require('fs');
var path = require('path');
var jsp = require("uglify-js").parser;
var pro = require("uglify-js").uglify;

var providerPaths = [];
var ignoreList = [/dojo\/_base\/html.js/];

exports.addProvider = function(providerPath) {
	//console.log("provider path ["+providerPath+"] added");
	providerPaths.push(providerPath);
};

exports.addProvider(path.dirname(module.filename));

exports.readText = function(filePath, compress) {
	if (compress === undefined) {
		compress = true;
	}
    var contents = null;
    for (var i = 0; i < providerPaths.length; i++) {
    	contents = readTextFile(filePath, providerPaths[i]);
        if (contents !== null) {
        	if (doCompress(filePath, compress)) {
        		var ast = jsp.parse(contents);
        		ast = pro.ast_mangle(ast);
        		ast = pro.ast_squeeze(ast, {make_seqs: false});
        		contents = pro.gen_code(ast);
                if (contents.charAt(contents.length-1) === ')') {
                	contents += ";";
                }
        	}
        	break;
        }
    }
    //console.log("readText : ["+filePath+"] "+ ((contents === null) ? "false" : "true"));
    return contents;
};

doCompress = function(filePath, compressFlag) {
	var compress = false;
	var path = String(filePath);
	if (compressFlag && path.match(".js$")) {
		var ignore = false;
		for (var i = 0; i < ignoreList.length; i++) {
			if (path.match(ignoreList[i])) {
				ignore = true;
				break;
			}
		}
		compress = !ignore;
	}
	return compress;
}

readTextFile = function(filePath, root) {
    filePath = path.join(root, String(filePath));
    try {
    	return fs.readFileSync(filePath, 'utf8');
    } catch(e) {
        return null;
    }
};
