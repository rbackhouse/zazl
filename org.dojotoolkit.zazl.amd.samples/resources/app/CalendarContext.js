(function() {
dojo.provide("app.CalendarContext");

dojo.declare("app.CalendarContext", null, {
    templatePath: dojo.moduleUrl("app", "calendar.dtl"),

	constructor: function(args, request, callback) {
		this.request = request;
		this.contextRoot = request.contextRoot;
    }
});
})();
