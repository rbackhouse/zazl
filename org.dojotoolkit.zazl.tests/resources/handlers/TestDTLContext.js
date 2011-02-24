(function() {
dojo.provide("handlers.TestDTLContext");

dojo.declare("handlers.TestDTLContext", null, {	
    templatePath: "test.dtl",
	
	constructor: function(args) {
		testCallback("{}");
		this.people = eval("(" + readText("people.json") + ")");
    }
});
})();