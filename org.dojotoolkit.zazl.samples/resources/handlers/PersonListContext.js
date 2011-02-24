(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.PersonListContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.PersonListContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "personlist.dtl"),

	constructor: function(args, request, callback) {
		this.request = request;
		this.contextRoot = request.contextRoot;
		this.people = [];
		var self = this;
		if ( args ) {
			if(args.template && args.template === "grid")
				this.templatePath = dojo.moduleUrl("org.dojotoolkit.zazl.samples", "persongrid.dtl");
		}
	    var bindArgs = {
	        url: this.contextRoot+"/data/people.json",
	        handleAs: "json",
			sync: callback === undefined ? true : false,
	        handle: function(response, ioArgs) {
	            if (response instanceof Error) {
	            }
	            else {
					self.people = response;
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
