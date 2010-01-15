package org.restmodules.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public class FilterRegistryImpl implements FilterRegistry {

    private final IoCComponentProviderFactory _providerFactory;

    public FilterRegistryImpl(final IoCComponentProviderFactory providerFactory) {
        _providerFactory = providerFactory;
    }

    public FilterRegistryImpl() {
        this(null);
    }

    private final Collection<ServletFilter> _filters = new CopyOnWriteArrayList<ServletFilter>();

    public FilterBuilder filter(final String urlPattern, final String... moreUrlPatterns) {
        if (urlPattern == null) {
            throw new IllegalArgumentException("urlPattern must not be null.");
        }
        return new FilterBuilder() {
            public void through(final javax.servlet.Filter filter) {
                through(filter, Collections.<String, String> emptyMap());
            }

            public void through(final Filter filter, final Map<String, String> initParams) {
                if (filter == null) {
                    throw new IllegalArgumentException("Filter must not be null");
                }
                if (initParams == null) {
                    throw new IllegalArgumentException("Init params must not be null");
                }
                _filters.add(new ServletFilterWrapper(urlPattern, initParams, new Provider<Filter>() {
                    public Filter get() {
                        return filter;
                    }
                }, moreUrlPatterns));
            }

            public void through(final Class<Filter> filterClazz) {
                through(filterClazz, Collections.<String, String> emptyMap());
            }

            public void through(final Class<Filter> filterClazz, final Map<String, String> initParams) {
                if (filterClazz == null) {
                    throw new IllegalArgumentException("Filter class must not be null");
                }
                if (initParams == null) {
                    throw new IllegalArgumentException("Init params must not be null");
                }
                _filters.add(new ServletFilterWrapper(urlPattern,
                                                      initParams,
                                                      new ProviderImplementation(filterClazz),
                                                      moreUrlPatterns));
            }
        };
    }

    public Servlet filterServlet(final Servlet servlet) {
        final Servlet result;
        if (_filters.isEmpty()) {
            result = servlet;
        } else {
            result = wrapServlet(servlet);
        }
        return result;
    }

    private final Servlet wrapServlet(final Servlet servlet) {
        return new Servlet() {

            private ServletConfig _config;

            public void service(final ServletRequest request, final ServletResponse response) throws ServletException,
                IOException {
                new ServletFilterChainImplementation(servlet).doFilter(request, response);
            }

            public void init(final ServletConfig config) throws ServletException {
                _config = config;
                for (final ServletFilter filter : _filters) {
                    filter.init(config.getServletContext());
                }
                servlet.init(config);
            }

            public String getServletInfo() {
                return servlet.getServletInfo();
            }

            public ServletConfig getServletConfig() {
                return _config;
            }

            public void destroy() {
                servlet.destroy();
                for (final ServletFilter filter : _filters) {
                    filter.destroy();
                }
            }
        };
    }

    private final class ServletFilterChainImplementation implements ServletFilterChain {
        private final Servlet _servlet;
        private final Iterator<ServletFilter> _remainingFilters = _filters.iterator();

        private ServletFilterChainImplementation(final Servlet servlet) {
            _servlet = servlet;
        }

        public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException,
            ServletException {
            if (_remainingFilters.hasNext()) {
                _remainingFilters.next().doFilter(request, response, this);
            } else {
                _servlet.service(request, response);
            }
        }
    }

    private final class ProviderImplementation implements Provider<Filter> {
        private final Class<Filter> _filterClazz;

        private ProviderImplementation(final Class<Filter> filterClazz) {
            _filterClazz = filterClazz;
        }

        public Filter get() {
            final Filter filter;
            if (_providerFactory != null) {
                filter = (Filter) _providerFactory.getComponentProvider(_filterClazz).getInstance();
            } else {
                try {
                    filter = _filterClazz.newInstance();
                } catch (final InstantiationException e) {
                    throw new RuntimeException("Could not instantiate filter instance for " + _filterClazz, e);
                } catch (final IllegalAccessException e) {
                    throw new RuntimeException("Could not instantiate filter instance for " + _filterClazz, e);
                }
            }
            return filter;
        }
    }
}
