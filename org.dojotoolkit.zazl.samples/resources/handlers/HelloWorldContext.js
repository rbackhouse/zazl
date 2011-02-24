(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.HelloWorldContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.HelloWorldContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "helloworld.dtl"),
	
	constructor: function(args, request) {
		this.contextRoot = request.contextRoot;
		this.helloWorld = "<b>Hello, World</b>";
    }
});
})();
