/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
(function() {
dojo.provide("dojox.serverdtl.util");

dojox.serverdtl.util.invokeCallback = function(/*Function*/cb, /*String*/jsonInput) {
	var result = dojo.fromJson(cb(dojo.toJson(jsonInput)));
	if (result._exceptionThrown !== undefined) {
		throw new Error("Exception thrown while calling callback : "+ result._exceptionThrown);
	}
	return result;
};

})();