(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.FilterContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.FilterContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "filter.dtl"),
	
	constructor: function(args, request) {
		this.contextRoot = request.contextRoot;
		this.helloWorld = "Hello, World";
    }
});
})();
