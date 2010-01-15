package org.restmodules;

/**
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public interface ApplicationRegistrarFactory {

    /**
     * Create a {@link ApplicationRegistrar} for the given context.
     */
    ApplicationRegistrar create(ApplicationProvider context);
}
