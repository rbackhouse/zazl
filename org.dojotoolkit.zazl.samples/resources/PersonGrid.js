(function() {
dojo.provide("org.dojotoolkit.zazl.samples.PersonGrid");
dojo.require("dojox.grid.DataGrid");
dojo.require("dojo.data.ItemFileReadStore");
dojo.require("dojo.parser");
dojo.requireLocalization("dijit", "loading");

	console.debug("org.dojotoolkit.zazl.samples.PersonGrid loaded");
	var messages = dojo.i18n.getLocalization("dijit", "loading");
	for (var id in messages) {
		console.debug(id+":"+messages[id]);
	}
})();
