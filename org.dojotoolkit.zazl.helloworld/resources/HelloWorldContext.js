(function() {
dojo.provide("org.dojotoolkit.zazl.helloworld.HelloWorldContext");

dojo.declare("org.dojotoolkit.zazl.helloworld.HelloWorldContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.helloworld", "helloworld.dtl"),
	
	constructor: function(args, request) {
		this.contextRoot = request.contextRoot;
		var helloWorldHolder = dojox.serverdtl.util.invokeCallback(helloWorld, "{}");
		this.helloWorld = helloWorldHolder.helloWorldMsg;
    }
});
})();
