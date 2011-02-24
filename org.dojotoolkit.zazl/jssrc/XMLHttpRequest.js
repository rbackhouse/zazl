/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
(function(){
	XMLHttpRequest.prototype = {
		open: function(method, url, async, user, password){ 
			this.readyState = 1;
			if (async) {
				this.async = true;
			}
			this.method = method || "GET";
			this.url = url;
			this.onreadystatechange();
		},
		setRequestHeader: function(header, value){
			this.headers[header] = value;
		},
		getResponseHeader: function(header){ },
		send: function(data){
			var hdrs = (this.headers === undefined ? [] : this.headers);
			var raw = xhrRequest(dojo.toJson({ url : this.url, method: this.method, headers : []}));
			var response = eval('('+raw+')');
			this.readyState = 4;
			this.status = response.status;
			this.statusText = response.statusText;
			this.responseText = response.responseText;
		},
		abort: function(){},
		onreadystatechange: function(){},
		getResponseHeader: function(header){
			if (this.readyState < 3) {
				throw new Error("INVALID_STATE_ERR");
			}
			else {
				var returnedHeaders = [];
				for (var rHeader in this.responseHeaders) {
					if (rHeader.match(new Regexp(header, "i"))) {
						returnedHeaders.push(this.responseHeaders[rHeader]);
					}
				}
				
				if (returnedHeaders.length) {
					return returnedHeaders.join(", ");
				}
			}
			
			return null;
		},
		getAllResponseHeaders: function(header){
			if (this.readyState < 3) {
				throw new Error("INVALID_STATE_ERR");
			}
			else {
				var returnedHeaders = [];
				
				for (var hdr in this.responseHeaders) {
					returnedHeaders.push(hdr + ": " + this.responseHeaders[hdr]);
				}
				
				return returnedHeaders.join("\r\n");
			}
		},
		async: false,
		readyState: 0,
		responseText: "",
		status: 0
	};
})();
