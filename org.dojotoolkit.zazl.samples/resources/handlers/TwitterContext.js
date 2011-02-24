(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.TwitterContext");
dojo.require("dojo.io.script");
dojo.declare("org.dojotoolkit.zazl.samples.handlers.TwitterContext", null, {		
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "twitterExample.dtl"),

	constructor: function(args, request, callback) {
		this.request = request;
		this.contextRoot = request.contextRoot;
		this.tweets = null;
		var self = this;
		if ( args ){
			if(args.template && args.template === "refresh")
				this.templatePath = dojo.moduleUrl("org.dojotoolkit.zazl.samples", "twitterRefresh.dtl");
		}
	    var bindArgs = {
	        url: "http://search.twitter.com/search.json?q=Pirates",
	        handleAs: "json",
			sync: callback === undefined ? true : false,
	        handle: function(response, ioArgs) {
	            if (response instanceof Error) {
	            }
	            else {
					self.tweets = response.results;
	            }
	            if (callback !== undefined) {
	            	callback(self);
	            }
	        }
	    };
	    var xhr = dojo.xhrGet(bindArgs);
    }
});
})();