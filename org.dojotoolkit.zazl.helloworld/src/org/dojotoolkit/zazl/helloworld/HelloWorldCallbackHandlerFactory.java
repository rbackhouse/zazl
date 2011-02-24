package org.dojotoolkit.zazl.helloworld;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.zazl.callback.ICallbackHandler;
import org.dojotoolkit.zazl.servlet.IServletCallbackHandlerFactory;

public class HelloWorldCallbackHandlerFactory implements IServletCallbackHandlerFactory {

	public ICallbackHandler createCallbackHandler(HttpServletRequest request) {
		return new HelloWorldCallbackHandler(request);
	}

	public ICallbackHandler createCallbackHandler() {
		return new HelloWorldCallbackHandler();
	}
}
