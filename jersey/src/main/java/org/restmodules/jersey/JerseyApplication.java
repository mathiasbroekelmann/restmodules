package org.restmodules.jersey;

import org.restmodules.RestmodulesApplication;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * @author Mathias Broekelmann
 *
 * @since 11.01.2010
 *
 */
public abstract class JerseyApplication extends RestmodulesApplication {

    /**
     * Optionally provide a custom {@link IoCComponentProviderFactory} instance which is used to instantiate defined
     * resource and provider classes.
     */
    public IoCComponentProviderFactory getComponentProviderFactory() {
        return null;
    }
}
