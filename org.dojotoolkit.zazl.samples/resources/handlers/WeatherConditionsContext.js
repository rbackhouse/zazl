(function() {
dojo.provide("org.dojotoolkit.zazl.samples.handlers.WeatherConditionsContext");

dojo.declare("org.dojotoolkit.zazl.samples.handlers.WeatherConditionsContext", null, {
    templatePath: dojo.moduleUrl("org.dojotoolkit.zazl.samples", "weatherconditions.dtl"),
	
	constructor: function(args, request, callback) {
		this.contextRoot = request.contextRoot;
		this.request = request;
		var self = this;
		if (request.parameters.icaoCode !== undefined) {
		    var bindArgs = {
		        url: "http://ws.geonames.org/weatherIcaoJSON?ICAO="+request.parameters.icaoCode[0],
		        handleAs: "json",
				sync: callback === undefined ? true : false,
		        handle: function(response, ioArgs) {
		            if (response instanceof Error) {
		            	self.errorMessage = response.toString(); 
		            }
		            else {
						self.weatherObservation = response.weatherObservation;
		            }
		            if (callback !== undefined) {
		            	callback(self);
		            }
		        }
		    };
		    var xhr = dojo.xhrGet(bindArgs);
		} else if (callback !== undefined) {
            callback(self);
		}
    }
});
})();
