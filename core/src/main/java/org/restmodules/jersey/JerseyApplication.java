package org.restmodules.jersey;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpContext;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.restmodules.filter.FilterRegistry;

/**
 * @author Mathias Broekelmann
 *
 * @since 11.01.2010
 *
 */
public abstract class JerseyApplication extends Application {

    /**
     * Optionally provide the http base path for this application.
     */
    public String getAlias() {
        return null;
    }

    /**
     * Optionally provide a custom {@link IoCComponentProviderFactory} instance which is used to instantiate defined
     * resource and provider classes.
     */
    public IoCComponentProviderFactory getComponentProviderFactory() {
        return null;
    }

    /**
     * Optionally provide an http context that is used to register the application in an http service.
     */
    public HttpContext getHttpContext() {
        return null;
    }

    /**
     * Callback template. overwrite this method to register any {@link Filter servlet filters} to apply to this
     * application.
     */
    public void registerFilters(final FilterRegistry registry) {
    }
}
