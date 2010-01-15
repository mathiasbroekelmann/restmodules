package org.restmodules;

import javax.ws.rs.core.Application;

import org.osgi.framework.ServiceReference;

/**
 * Provides the {@link Application}.
 *
 * @author Mathias Broekelmann
 * 
 * @since 13.01.2010
 * 
 */
public interface ApplicationProvider {

    /**
     * Provides the service reference of the application.
     */
    ServiceReference getServiceReference();

    /**
     * Provides the application instance.
     */
    Application getApplication();
}
