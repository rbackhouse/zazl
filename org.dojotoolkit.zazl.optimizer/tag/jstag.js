dojo.provide("org.dojotoolkit.zazl.optimizer.tag.jstag");

(function(){
	var dd = dojox.dtl;
	var odzot = org.dojotoolkit.zazl.optimizer.tag.jstag;

	odzot.JSTag = dojo.extend(function(modules, namespaces){
		this.modules = modules.split(',');
		this.namespaces = (namespaces === null) ? [] : namespaces.split(',');
	},
	{
		render: function(context, buffer) {
			var urls = dojo.fromJson(getModuleURLs(dojo.toJson({modules : this.modules, namespaces: this.namespaces})));
			for (var i = 0; i < urls.length; i++) {
				buffer.append("<script type=\"text/javascript\" src=\"");
				buffer.append(urls[i]);
				buffer.append("\"></script>\n");
			}
			return buffer;
		},
		unrender: function(context, buffer) {
			return buffer;
		},
		clone: function(){
			return this;
		}
	});
		
	dojo.mixin(odzot, {
		jslinks: function(parser, token) {
			var parts = token.contents.split();
			parts.shift();
			if(parts.length === 0){
				throw new Error("jstag takes at least one argument: a list of javascript module ids");
			}
			var modules = parts.shift();
			var namespaces = null;
			if (parts.length > 0) {
				namespaces = parts.shift();
			}
			return new odzot.JSTag(modules, namespaces);
		}
	});

	dd.register.tags("org.dojotoolkit.zazl.optimizer.tag", {
		"jstag": ["jslinks"]
	});
})();