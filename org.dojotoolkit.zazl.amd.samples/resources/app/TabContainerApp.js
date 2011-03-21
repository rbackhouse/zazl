define([
	'dijit/layout/TabContainer',
	'dijit/layout/ContentPane',
	'dijit/Calendar'
], function (TabContainer, ContentPane, Calendar, Slider) {
	var tc = new TabContainer({
        style: "height: 100%; width: 100%;"
    },
    "tabContainerNode");

    var cp = new ContentPane({
        title: "Tab Number 1",
        content: "Some Content for Tab Number 1"
    });
    tc.addChild(cp);

    tc.addChild(new Calendar({title: "Calendar"}));
    tc.startup();
    return tc;
});
