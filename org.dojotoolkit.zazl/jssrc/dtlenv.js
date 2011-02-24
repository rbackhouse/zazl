/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var	window = this;

(function(){
	loadJS("/env.js");
	
	var obj_nodes = new java.util.HashMap();
	
	function makeNode(node){
		if ( node ) {
			if ( !obj_nodes.containsKey( node ) )
				obj_nodes.put( node, node.getNodeType() == 
					Packages.org.w3c.dom.Node.ELEMENT_NODE ?
						new DOMElement( node ) : new DOMNode( node ) );
			
			return obj_nodes.get(node);
		} else
			return null;
	}
	
	window.navigator.mimeTypes = {};
	window.navigator.appVersion = "";

	window.__defineSetter__("location", function(url){
	 	window.document = new DOMDocument(
							new java.io.ByteArrayInputStream(
								(new java.lang.String("<html><head><title></title></head><body><div id='test'></div></body></html>")).getBytes("UTF8")));	
	});
	
	window.__defineGetter__("location", function(url){
		return {
			get protocol(){
				return "file:";
			},
			get href(){
				return "file:///";
			},
			toString: function(){
				return this.href;
			},
			get pathname() {
				return "/";
			},
			get hash() {
				return "#";
			},
			get host() {
				return "";
			},
			get port() {
				return 80;
			},
			get hostname() {
				return "";
			},
			get search() {
				return "?";
			}
		};
	});
	
	DOMElement.prototype.removeChild = function(node){
		return makeNode((this._dom.removeChild( node._dom )));
	};
	
	DOMElement.prototype.replaceChild = function(newChild, oldChild) {
		return makeNode((this._dom.replaceChild( newChild._dom, oldChild._dom )));
	};
	
	DOMElement.prototype.hasChildNodes =  function() {
		return this._dom.hasChildNodes();
	};

	DOMElement.prototype.hasAttribute =  function(attribute) {
		return this._dom.hasAttribute(attribute);
	};
	
	DOMElement.prototype.__defineSetter__("innerHTML", function(html) {
		html = html.replace(/<\/?([A-Z]+)/g, function(m){
			return m.toLowerCase();
		});
		
		var nodes = this.ownerDocument.importNode(
			new DOMDocument( new java.io.ByteArrayInputStream(
				(new java.lang.String("<!DOCTYPE doc [<!ENTITY nbsp \"&#160;\">]><wrap>" + html + "</wrap>"))
					.getBytes("UTF8"))).documentElement, true).childNodes;
			
		while (this.firstChild)
			this.removeChild( this.firstChild );
		
		for ( var i = 0; i < nodes.length; i++ )
			this.appendChild( nodes[i] );
	});
	
	DOMDocument.prototype.__defineGetter__("firstChild", function() {
		return makeNode( this._dom.getFirstChild() );;
	});
	
	window.Image = function(width, height){};
	
	Image.prototype.__defineGetter__("border", function() {	return 0; });
	Image.prototype.__defineSetter__("border", function(border) {});
	Image.prototype.__defineGetter__("complete", function() {	return true; });
	Image.prototype.__defineSetter__("complete", function(complete) {});
	Image.prototype.__defineGetter__("height", function() {	return 0; });
	Image.prototype.__defineSetter__("height", function(height) {});
	Image.prototype.__defineGetter__("hspace", function() {	return 0; });
	Image.prototype.__defineSetter__("hspace", function(hspace) {});
	Image.prototype.__defineGetter__("lowsrc", function() {	return ""; });
	Image.prototype.__defineSetter__("lowsrc", function(lowsrc) {});
	Image.prototype.__defineGetter__("name", function() {	return ""; });
	Image.prototype.__defineSetter__("name", function(name) {});
	Image.prototype.__defineGetter__("src", function() {	return ""; });
	Image.prototype.__defineSetter__("src", function(src) {});
	Image.prototype.__defineGetter__("vspace", function() {	return 0; });
	Image.prototype.__defineSetter__("vspace", function(vspace) {});
	Image.prototype.__defineGetter__("width", function() {	return 0; });
	Image.prototype.__defineSetter__("width", function(width) {});
})();
