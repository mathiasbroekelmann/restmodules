package org.restmodules.filter;


/**
 * A filter registrar contains filter definitions that are registered at a given filter registry.
 *
 * @author Mathias Broekelmann
 *
 * @since 26.01.2010
 *
 */
public interface FilterRegistrar {

    /**
     * Register servlet filter in the given filter registry.
     * 
     * @return a runnable which will unregister all filters.
     */
    void registerFilters(FilterRegistry registry);
}
