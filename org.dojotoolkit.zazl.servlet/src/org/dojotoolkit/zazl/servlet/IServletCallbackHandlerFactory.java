package org.dojotoolkit.zazl.servlet;

import javax.servlet.http.HttpServletRequest;

import org.dojotoolkit.zazl.callback.ICallbackHandler;
import org.dojotoolkit.zazl.callback.ICallbackHandlerFactory;

public interface IServletCallbackHandlerFactory extends ICallbackHandlerFactory {
	public ICallbackHandler createCallbackHandler(HttpServletRequest request);

}
