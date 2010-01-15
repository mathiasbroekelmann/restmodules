package org.restmodules;

import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpService;

/**
 * An {@link ApplicationRegistrar} is responsible to register an {@link Application} at a given {@link HttpService}.
 *
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public interface ApplicationRegistrar {

    /**
     * Register this application at the given http service. The returned runnable is used to unregister the
     * application from that http service. The returned runnable may be executed more than once.
     *
     * @param httpService the http service to register the application.
     *
     * @return a runnable to unregister the application at the given http service. Null if registration could not be performed.
     */
    Runnable registerAt(HttpService httpService);
}
