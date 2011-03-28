(function() {
dojo.provide("app.DeclarativeContext");

dojo.declare("app.DeclarativeContext", null, {
    templatePath: dojo.moduleUrl("app", "declarative.dtl"),

	constructor: function(args, request, callback) {
		this.request = request;
		this.contextRoot = request.contextRoot;
    }
});
})();
