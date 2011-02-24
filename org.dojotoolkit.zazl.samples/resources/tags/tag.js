dojo.provide("org.dojotoolkit.zazl.samples.tags.tag");

(function(){
	var odzstt = org.dojotoolkit.zazl.samples.tags.tag;
	
	odzstt.ExampleTag = dojo.extend(function(params){
		this.params = params;
	},
	{
		render: function(context, buffer) {
			buffer.append("Hello from exampletag");
			return buffer;
		},
		unrender: function(context, buffer) {
			return buffer;
		},
		clone: function(){
			return this;
		}
	});
	
	dojo.mixin(odzstt, {
		exampletag: function(parser, token) {
			var parts = token.contents.split();
			return new odzstt.ExampleTag(parts);
		},
	});
	
	dojox.dtl.register.tags("org.dojotoolkit.zazl.samples.tags", {
		"tag": ["exampletag"]
	});
})();