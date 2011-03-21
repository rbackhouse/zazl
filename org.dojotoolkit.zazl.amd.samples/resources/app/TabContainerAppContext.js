(function() {
dojo.provide("app.TabContainerAppContext");

dojo.declare("app.TabContainerAppContext", null, {
    templatePath: dojo.moduleUrl("app", "tabcontainerapp.dtl"),

	constructor: function(args, request, callback) {
		this.request = request;
		this.contextRoot = request.contextRoot;
    }
});
})();
