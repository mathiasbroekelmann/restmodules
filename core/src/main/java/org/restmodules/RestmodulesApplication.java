/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.restmodules;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpContext;
import org.restmodules.filter.FilterRegistry;

/**
 * @author Mathias Broekelmann
 */
public abstract class RestmodulesApplication extends Application {
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
}
