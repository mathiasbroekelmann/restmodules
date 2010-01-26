package org.restmodules.filter;

import java.util.Map;

import javax.servlet.Filter;

import org.restmodules.ioc.Provider;

/**
 * A FilterRegistry allows defining a set of servlet filters. Implementation is loosely inspired by google guice.
 *
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public interface FilterRegistry {

    /**
     * Start building a filter definition by defining the url pattern for which request the filter should apply.
     *
     * @param urlPattern the required url pattern
     * @param moreUrlPatterns optionally define more url patterns for the filter.
     */
    FilterBuilder filter(String urlPattern, String... moreUrlPatterns);

    /**
     * The filter builder recives the filter instance optionally with some init parameters.
     *
     * @author Mathias Broekelmann
     *
     * @since 13.01.2010
     *
     */
    public interface FilterBuilder {

        /**
         * define a filter without any init params.
         *
         * @param filter the filter instance to define.
         */
        void through(final Filter filter);

        /**
         * define a filter by supplying init parameters.
         *
         * @param filter the filter instance to define.
         * @param initParams the init parameters to apply to the {@link Filter#init(javax.servlet.FilterConfig)} method.
         */
        void through(final Filter filter, final Map<String, String> initParams);

        /**
         * define a filter without any init params.
         *
         * @param filter the filter type to define.
         */
        void through(final Class<Filter> filterClazz);

        /**
         * define a filter by supplying init parameters.
         *
         * @param filter the filter type to define.
         * @param initParams the init parameters to apply to the {@link Filter#init(javax.servlet.FilterConfig)} method.
         */
        void through(final Class<Filter> filterClazz, final Map<String, String> initParams);

        /**
         * define a filter without any init params.
         *
         * @param provider a provider for the filter to define.
         */
        void through(final Provider<Filter> provider);

        /**
         * define a filter by supplying init parameters.
         * 
         * @param provider a provider for the filter to define.
         * @param initParams the init parameters to apply to the {@link Filter#init(javax.servlet.FilterConfig)} method.
         */
        void through(final Provider<Filter> provider, final Map<String, String> initParams);
    }
}
