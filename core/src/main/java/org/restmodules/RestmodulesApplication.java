/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.restmodules;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpContext;
import org.restmodules.filter.FilterRegistrar;
import org.restmodules.filter.FilterRegistry;
import org.restmodules.ioc.Provider;
import org.restmodules.ioc.Scoped;

/**
 * @author Mathias Broekelmann
 */
public abstract class RestmodulesApplication extends Application implements FilterRegistrar {
    /**
     * Optionally provide the http base path for this application.
     */
    public String getAlias() {
        return null;
    }

    /**
     * Optionally provide an http context that is used to register the application in an http service.
     */
    public HttpContext getHttpContext() {
        return null;
    }

    /**
     * Call back template. overwrite this method to register any {@link Filter servlet filters} to apply to this
     * application.
     */
    public void registerFilters(final FilterRegistry registry) {
    }

    /**
     * call back template to register an update function. Use the function to notify the container that the application
     * has changed and needs to get updated.
     */
    public void updateCallback(final Runnable update) {
    }

    /**
     * Determine the provider which resolves instances of the given type. The default implementation always returns null
     * which means that the container is responsible to create and manage the instance for the type.
     * 
     * The returned provider may implement {@link Scoped} to provide the scope of the instance to the container.
     * 
     * @param <T> the type of the value
     * @param clazz the class type for the value
     * @return the provider or null if no such provider is available.
     */
    public <T> Provider<T> getProvider(final Class<T> clazz) {
        return null;
    }
}
