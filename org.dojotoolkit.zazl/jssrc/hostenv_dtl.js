/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
if(dojo.config["baseUrl"]){
    dojo.baseUrl = dojo.config["baseUrl"];
}else{
    dojo.baseUrl = "/";
}

dojo.locale = dojo.locale || String(java.util.Locale.getDefault().toString().replace('_','-').toLowerCase());
dojo._name = 'dtl';
dojo.isRhino = false;

if(typeof print == "function"){
    console.debug = print;
}

if(typeof dojo["byId"] == "undefined"){
    dojo.byId = function(id, doc){
        if(id && (typeof id == "string" || id instanceof String)){
            if(!doc){ doc = document; }
            return doc.getElementById(id);
        }
        return id; // assume it's a node
    };
}

dojo._loadUri = function(uri, cb) {
	try{
		if(cb){
			cb(loadJS(uri));
		}else{
			loadJS(uri);
		}
		return true;
	}catch(e){
		print("load for ('" + uri + "') failed. Exception: " + e.name + " : [" + e.message +"] at line "+e.lineNumber);
		return false;
	}
};

dojo._getText = function(/*URI*/ uri, /*Boolean*/ fail_ok) {
	try{
		var contents = readText(uri);
		if (contents === null) {
			print("Contents of ["+uri+"] is empty");
		}
		return contents;
	}catch(e){
		print("getText('" + uri + "') failed. Exception: " + e);
		if(fail_ok){ return null; }
		throw e;
	}
};

alert = function(msg) {
	print("alert : "+msg);
};
		
dojo.exit = function(exitcode){
    quit(exitcode);
}

dojo._rhinoCurrentScriptViaJava = function(depth){
    var optLevel = Packages.org.mozilla.javascript.Context.getCurrentContext().getOptimizationLevel();
    var caw = new java.io.CharArrayWriter();
    var pw = new java.io.PrintWriter(caw);
    var exc = new java.lang.Exception();
    var s = caw.toString();
    // we have to exclude the ones with or without line numbers because they put double entries in:
    //   at org.mozilla.javascript.gen.c3._c4(/Users/mda/Sites/burstproject/burst/Runtime.js:56)
    //   at org.mozilla.javascript.gen.c3.call(/Users/mda/Sites/burstproject/burst/Runtime.js)
    var matches = s.match(/[^\(]*\.js\)/gi);
    if(!matches){
        throw Error("cannot parse printStackTrace output: " + s);
    }

    // matches[0] is entire string, matches[1] is this function, matches[2] is caller, ...
    var fname = ((typeof depth != 'undefined')&&(depth)) ? matches[depth + 1] : matches[matches.length - 1];
    var fname = matches[3];
    if(!fname){ fname = matches[1]; }
    // print("got fname '" + fname + "' from stack string '" + s + "'");
    if (!fname){ throw Error("could not find js file in printStackTrace output: " + s); }
    //print("Rhino getCurrentScriptURI returning '" + fname + "' from: " + s);
    return fname;
};

// call this now because later we may not be on the top of the stack
if(
    (!dojo.config.libraryScriptUri)||
    (!dojo.config.libraryScriptUri.length)
){
    try{
        dojo.config.libraryScriptUri = dojo._rhinoCurrentScriptViaJava(1);
    }catch(e){
        // otherwise just fake it
        if(dojo.config["isDebug"]){
            print("\n");
            print("we have no idea where Dojo is located.");
            print("Please try loading rhino in a non-interpreted mode or set a");
            print("\n\tdjConfig.libraryScriptUri\n");
            print("Setting the dojo path to './'");
            print("This is probably wrong!");
            print("\n");
            print("Dojo will try to load anyway");
        }
        dojo.config.libraryScriptUri = "./";
    }
}

// summary:
//      return the document object associated with the dojo.global
dojo.doc = typeof(document) != "undefined" ? document : null;

dojo.body = function(){
    return document.body;
};

function setTimeout(func, delay){
    // summary: provides timed callbacks using Java threads

    var def={
        sleepTime:delay,
        hasSlept:false,

        run:function(){
            if (!this.hasSlept){
                this.hasSlept=true;
                java.lang.Thread.currentThread().sleep(this.sleepTime);
            }
            try {
                func();
            } catch(e){print("Error running setTimeout thread:" + e);}
        }
    };

    var runnable=new java.lang.Runnable(def);
    var thread=new java.lang.Thread(runnable);
    thread.start();
}

dojo.requireIf((dojo.config["isDebug"] || dojo.config["debugAtAllCosts"]), "dojo.debug");

	
dojo._xhrObj = function(){
	return new XMLHttpRequest();
};

dojo._isDocumentOk = function(http){
	var stat = http.status || 0;
	return (stat >= 200 && stat < 300) || 	// Boolean
		stat == 304 || 						// allow any 2XX response code
		stat == 1223 || 						// get it out of the cache
		(!stat && (location.protocol=="file:" || location.protocol=="chrome:") ); // Internet Explorer mangled the status code
};

dojo.addOnWindowUnload = function() {};
