(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.ShowSourceContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.ShowSourceContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "showsource.dtl"),
	
	constructor: function(args, request) {
		this.contextRoot = request.contextRoot;
		this.sourcePath = "";
		this.source = "";
		var namespace = "org.dojotoolkit.zazl.samples";
		if (request.parameters.namespace !== undefined) {
			namespace = request.parameters.namespace[0];
		}
		if (request.parameters.sourcePath !== undefined) {
			this.sourcePath = request.parameters.sourcePath;
			this.source = readText(dojo.moduleUrl(namespace, this.sourcePath[0]));
		}
    }
});
})();
