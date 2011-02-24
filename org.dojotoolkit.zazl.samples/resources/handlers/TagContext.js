(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.TagContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.TagContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "tag.dtl"),
	
	constructor: function(args, request) {
		this.contextRoot = request.contextRoot;
    }
});
})();
