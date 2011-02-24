/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet.osgi.registry;

import org.dojotoolkit.zazl.servlet.IServletCallbackHandlerFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;

public class CallbackHandlerRegistryReader implements IRegistryEventListener {
	private static final String NAMESPACE = "org.dojotoolkit.zazl.servlet.osgi"; //$NON-NLS-1$
    private static final String EXT_PT_ID = "callbackHandler"; //$NON-NLS-1$
    private static final String ELEMENT_NAME = "callbackHandler"; //$NON-NLS-1$
    private static final String CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	private IExtensionRegistry extensionRegistry = null;
	private CallbackHandlerRegistryListener listener = null;
    private Object lock = new Object();

	public CallbackHandlerRegistryReader() {}
	
	public void start(IExtensionRegistry extensionRegistry, CallbackHandlerRegistryListener listener) {
		this.extensionRegistry = extensionRegistry;
		this.listener = listener;
		synchronized (lock) {
			extensionRegistry.addListener(this, NAMESPACE);
			IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(NAMESPACE+'.'+EXT_PT_ID);
			if (extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				for (IExtension extension : extensions) {
					addCallbackHandler(extension);
				}
			}
		}
	}
	
	public void stop() {
		extensionRegistry.removeListener(this);
		extensionRegistry = null;
	}
	

	public void added(IExtension[] extensions) {
		synchronized (lock) {
			for (IExtension extension : extensions) {
				if (extension.getExtensionPointUniqueIdentifier().equals(NAMESPACE+'.'+EXT_PT_ID)) {
					addCallbackHandler(extension);
				}
			}
		}
	}

	public void removed(IExtension[] extensions) {
		synchronized (lock) {
			for (IExtension extension : extensions) {
				if (extension.getExtensionPointUniqueIdentifier().equals(NAMESPACE+'.'+EXT_PT_ID)) {
					removeCallbackHandler(extension);
				}
			}
		}
	}

	public void added(IExtensionPoint[] extensionPoints) {
	}

	public void removed(IExtensionPoint[] extensionPoints) {
	}
	
	private void addCallbackHandler(IExtension extension) {
		IConfigurationElement[] configurationElements = extension.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			if (configurationElements[i].getName().equals(ELEMENT_NAME)) {
				try {
					IServletCallbackHandlerFactory callbackHandlerFactory = (IServletCallbackHandlerFactory)configurationElements[i].createExecutableExtension(CLASS_ATTRIBUTE_NAME);
					listener.addCallbackHandlerFactory(callbackHandlerFactory);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void removeCallbackHandler(IExtension extension) {
		IConfigurationElement[] configurationElements = extension.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			if (configurationElements[i].getName().equals(ELEMENT_NAME)) {
				listener.removeCallbackHandlerFactory(configurationElements[i].getAttribute(CLASS_ATTRIBUTE_NAME));
			}
		}
	}
}
