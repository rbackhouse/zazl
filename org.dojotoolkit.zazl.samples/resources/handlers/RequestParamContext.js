(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.RequestParamContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.RequestParamContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "requestparam.dtl"),
	
	constructor: function(args, request) {
		this.contextRoot = request.contextRoot;
		this.request = request;
    }
});
})();
