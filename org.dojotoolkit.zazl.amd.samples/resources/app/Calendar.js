define([
	'dojo',
	'dijit',
	'dijit/Calendar'
], function (dojo, dijit) {
    var calendar = new dijit.Calendar({}, dojo.byId("calendarNode"));
    return calendar;
});
