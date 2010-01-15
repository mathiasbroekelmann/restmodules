package org.restmodules.jersey;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpContext;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.restmodules.RestmodulesApplication;
import org.restmodules.filter.FilterRegistry;

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
