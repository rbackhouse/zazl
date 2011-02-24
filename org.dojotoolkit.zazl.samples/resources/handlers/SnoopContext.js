(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.SnoopContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.SnoopContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "snoop.dtl"),
	
	constructor: function(args, request) {
		this.request = request;
		this.contextRoot = request.contextRoot;
		this.parameterList = [];
		for (var param in request.parameters) {
			this.parameterList.push({"name": param, "value" : request.parameters[param]});
		}
    }
});
})();
