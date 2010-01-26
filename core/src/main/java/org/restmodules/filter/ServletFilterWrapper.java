package org.restmodules.filter;

import static java.util.Collections.enumeration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.restmodules.ioc.Provider;

/**
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public final class ServletFilterWrapper implements ServletFilter {
    private final Map<String, String> _initParams;
    private final Provider<Filter> _filter;
    private volatile Filter _filterInstance;
    private final Iterable<PatternMatcher> _uriPatterns;

    public ServletFilterWrapper(final String urlPattern,
                                final Map<String, String> initParams,
                                final Provider<Filter> filter,
                                final String... moreUrlPatterns) {
        _initParams = initParams;
        _filter = filter;
        _uriPatterns = createPatterns(urlPattern, moreUrlPatterns);
    }

    private Filter getFilter() {
        if (_filterInstance == null) {
            synchronized (this) {
                if (_filterInstance == null) {
                    _filterInstance = _filter.get();
                }
            }
        }
        return _filterInstance;
    }

    private boolean matches(final ServletRequest request) {
        final boolean result;
        if (request instanceof HttpServletRequest) {
            result = matches(((HttpServletRequest) request).getServletPath());
        } else {
            result = false;
        }
        return result;
    }

    private Iterable<PatternMatcher> createPatterns(final String urlPattern, final String... moreUrlPatterns) {
        final Collection<PatternMatcher> result = new ArrayList<PatternMatcher>();
        result.add(urlPatternMatcher(urlPattern));
        if (moreUrlPatterns != null) {
            for (final String pattern : moreUrlPatterns) {
                result.add(urlPatternMatcher(pattern));
            }
        }
        return result;
    }

    private PatternMatcher urlPatternMatcher(final String urlPattern) {
        final PatternMatcher matcher;
        if (urlPattern.startsWith("*")) {
            matcher = new PatternMatcher() {
                public boolean matches(final String path) {
                    return path.endsWith(urlPattern.substring(1));
                }
            };
        } else if (urlPattern.endsWith("*")) {
            matcher = new PatternMatcher() {
                public boolean matches(final String path) {
                    return path.startsWith(urlPattern.substring(0, urlPattern.length() - 1));
                }
            };
        } else {
            matcher = new PatternMatcher() {
                public boolean matches(final String path) {
                    return path.equals(urlPattern);
                }
            };
        }
        return matcher;
    }

    private boolean matches(final String path) {
        boolean matches = false;
        for (final PatternMatcher pattern : _uriPatterns) {
            matches = pattern.matches(path);
            if (matches) {
                break;
            }
        }
        return matches;
    }

    public void destroy() {
        getFilter().destroy();
    }

    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final ServletFilterChain chain) throws IOException, ServletException {
        if (matches(request)) {
            getFilter().doFilter(request, response, new FilterChain() {
                public void doFilter(final ServletRequest servletrequest, final ServletResponse servletresponse)
                    throws IOException, ServletException {
                    chain.doFilter(request, response);
                }
            });
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(final ServletContext context) throws ServletException {
        getFilter().init(new FilterConfig() {

            public ServletContext getServletContext() {
                return context;
            }

            public Enumeration<String> getInitParameterNames() {
                return enumeration(_initParams.keySet());
            }

            public String getInitParameter(final String name) {
                return _initParams.get(name);
            }

            public String getFilterName() {
                return _filter.toString();
            }
        });
    }

    interface PatternMatcher {
        boolean matches(String path);
    }

}