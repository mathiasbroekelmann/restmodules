package org.restmodules;

import static java.lang.String.format;

import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.restmodules.filter.DefaultFilterRegistry;

/**
 * Abstract base class for {@link ApplicationRegistrar} implementations.
 *
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public abstract class AbstractApplicationRegistrar implements ApplicationRegistrar {

    public static final String ALIAS_SERVICE_PROPERTY = "javax.ws.rs.core.application.alias";
    private final ApplicationProvider _provider;

    public AbstractApplicationRegistrar(final ApplicationProvider provider) {
        _provider = provider;
    }

    public final ApplicationProvider getProvider() {
        return _provider;
    }

    public final Application getApplication() {
        return _provider.getApplication();
    }

    public final ServiceReference getServiceReference() {
        return _provider.getServiceReference();
    }

    /**
     * register the application at the given http service.
     */
    public final Runnable registerAt(final HttpService httpService) {
        final HttpContext context = httpContext();
        final String alias = validAlias(alias());
        final Servlet servlet = filtered(servlet());
        final Dictionary initParams = servletInitParams();
        try {
            httpService.registerServlet(alias, servlet, initParams, context);
        } catch (final ServletException e) {
            throw new RuntimeException(format("Could not register servlet %s for application %s at alias %s",
                                              servlet,
                                              getApplication().toString(),
                                              alias), e);
        } catch (final NamespaceException e) {
            throw new RuntimeException(format("Could not register servlet %s for application %s at alias %s",
                                              servlet,
                                              getApplication().toString(),
                                              alias), e);
        }
        return new Runnable() {
            private volatile boolean _registered = true;

            public synchronized void run() {
                if (_registered) {
                    httpService.unregister(alias);
                    _registered = false;
                }
            }
        };
    }

    protected DefaultFilterRegistry filterRegistry() {
        return new DefaultFilterRegistry();
    }

    private String validAlias(final String alias) {
        final String validAlias;
        if (alias == null) {
            validAlias = "/";
        } else if (!alias.startsWith("/")) {
            validAlias = "/" + alias;
        } else {
            validAlias = alias;
        }
        return validAlias;
    }

    /**
     * create the jsr311 capable servlet instance to register at the http service.
     */
    protected abstract Servlet servlet();

    /**
     * Optionally provide the servlet init parameters which are used to register the servlet instance.
     */
    protected Dictionary servletInitParams() {
        return null;
    }

    /**
     * Optionally provide the http context to use when registering the servlet at the http service.
     */
    protected HttpContext httpContext() {
        return null;
    }

    /**
     * Provide the alias to use for registering the servlet.
     */
    protected String alias() {
        String basePath = null;
        if (basePath == null) {
            basePath = (String) getServiceReference().getProperty(ALIAS_SERVICE_PROPERTY);
        }
        if (basePath == null) {
            basePath = "/";
        }
        return basePath;
    }

    protected Servlet filtered(Servlet servlet) {
        final Servlet filteredServlet;
        Application app = getApplication();
        if(app instanceof RestmodulesApplication) {
            RestmodulesApplication restModulesApp = (RestmodulesApplication) app;
            DefaultFilterRegistry registry = filterRegistry();
            restModulesApp.registerFilters(registry);
            filteredServlet = registry.filterServlet(servlet);
        } else {
            filteredServlet = servlet;
        }
        return filteredServlet;
    }

}