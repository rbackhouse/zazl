/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.zazl.servlet.osgi.registry;

import org.dojotoolkit.zazl.contentprovider.ContentProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;

public class ContentProviderRegistryReader implements IRegistryEventListener {
	private static final String NAMESPACE = "org.dojotoolkit.zazl.servlet.osgi"; //$NON-NLS-1$
    private static final String EXT_PT_ID = "contentProvider"; //$NON-NLS-1$
    private static final String ELEMENT_NAME = "contentProvider"; //$NON-NLS-1$
    private static final String BASE_ATTRIBUTE_NAME = "base"; //$NON-NLS-1$
    private static final String ALIAS_ATTRIBUTE_NAME = "alias"; //$NON-NLS-1$
	private IExtensionRegistry extensionRegistry = null;
	private ContentProviderRegistryListener listener = null;
    private Object lock = new Object();

	public ContentProviderRegistryReader() {}
	
	public void start(IExtensionRegistry extensionRegistry, ContentProviderRegistryListener listener) {
		this.extensionRegistry = extensionRegistry;
		this.listener = listener;
		synchronized (lock) {
			extensionRegistry.addListener(this, NAMESPACE);
			IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(NAMESPACE+'.'+EXT_PT_ID);
			if (extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				for (IExtension extension : extensions) {
					addContentProvider(extension);
				}
			}
		}
	}
	
	public void stop() {
		if (extensionRegistry != null) {
			extensionRegistry.removeListener(this);
			extensionRegistry = null;
		}
	}
	
	public void added(IExtension[] extensions) {
		synchronized (lock) {
			for (IExtension extension : extensions) {
				if (extension.getExtensionPointUniqueIdentifier().equals(NAMESPACE+'.'+EXT_PT_ID)) {
					addContentProvider(extension);
				}
			}
		}
	}

	public void removed(IExtension[] extensions) {
		synchronized (lock) {
			for (IExtension extension : extensions) {
				if (extension.getExtensionPointUniqueIdentifier().equals(NAMESPACE+'.'+EXT_PT_ID)) {
					removeContentProvider(extension);
				}
			}
		}
	}

	public void added(IExtensionPoint[] extensionPoints) {
	}

	public void removed(IExtensionPoint[] extensionPoints) {
	}
	
	private void addContentProvider(IExtension extension) {
		IConfigurationElement[] configurationElements = extension.getConfigurationElements();
		ContentProvider contentProvider = new ContentProvider();
		contentProvider.id = extension.getNamespaceIdentifier();
		for (int i = 0; i < configurationElements.length; i++) {
			if (configurationElements[i].getName().equals(ELEMENT_NAME)) {
				contentProvider.base = configurationElements[i].getAttribute(BASE_ATTRIBUTE_NAME);
				contentProvider.alias = configurationElements[i].getAttribute(ALIAS_ATTRIBUTE_NAME);
				break;
			}
		}
		listener.addContentProvider(contentProvider);
	}

	private void removeContentProvider(IExtension extension) {
		listener.removeContentProvider(extension.getNamespaceIdentifier());
	}
}
