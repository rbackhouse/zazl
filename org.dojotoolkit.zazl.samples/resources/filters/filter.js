dojo.provide("org.dojotoolkit.zazl.samples.filters.filter");

(function(){
	dojo.mixin(org.dojotoolkit.zazl.samples.filters.filter, {
		bold: function(value){
			return dojox.dtl._base.safe("<b>"+value+"</b>");
		}
	});
	
	dojox.dtl.register.filters("org.dojotoolkit.zazl.samples.filters", {
		"filter": ["bold"]
	});
})();