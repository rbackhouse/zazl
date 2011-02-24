(function() {
dojo.provide("handlers.TestTemplateContext");

dojo.declare("handlers.TestTemplateContext", null, {	
    templatePath: "testTemplate.dtl",
	
	constructor: function(args) {
		this.parameters = [];
		for (var param in args) {
			this.parameters.push({name : param, value : args[param]});
		}
    }
});
})();