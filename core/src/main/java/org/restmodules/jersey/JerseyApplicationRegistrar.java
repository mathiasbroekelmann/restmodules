package org.restmodules.jersey;

import javax.servlet.Servlet;
import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpContext;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.restmodules.filter.FilterRegistryImpl;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.restmodules.AbstractApplicationRegistrar;
import org.restmodules.ApplicationProvider;
import org.restmodules.ApplicationRegistrar;

/**
 * Jersey specific implementation for an {@link ApplicationRegistrar}.
 *
 * @author Mathias Broekelmann
 *
 * @since 11.01.2010
 *
 */
public class JerseyApplicationRegistrar extends AbstractApplicationRegistrar implements ApplicationRegistrar {

    public JerseyApplicationRegistrar(final ApplicationProvider provider) {
        super(provider);
    }

    @Override
    protected Servlet servlet() {
        final Application application = getApplication();
        final Servlet servlet = new ServletContainer(application) {
            @Override
            protected void initiate(final ResourceConfig rc, final WebApplication wa) {
                if (application instanceof JerseyApplication) {
                    final IoCComponentProviderFactory providerFactory = ((JerseyApplication) application).getComponentProviderFactory();
                    wa.initiate(rc, providerFactory);
                } else {
                    super.initiate(rc, wa);
                }
            }
        };
        final Servlet filteredServlet;
        if (application instanceof JerseyApplication) {
            final FilterRegistryImpl registry = new FilterRegistryImpl(((JerseyApplication) application).getComponentProviderFactory());
            ((JerseyApplication) application).registerFilters(registry);
            filteredServlet = registry.filterServlet(servlet);
        } else {
            filteredServlet = servlet;
        }
        return filteredServlet;
    }

    @Override
    protected HttpContext httpContext() {
        final Application application = getApplication();
        final HttpContext httpContext;
        if (application instanceof JerseyApplication) {
            httpContext = ((JerseyApplication) application).getHttpContext();
        } else {
            httpContext = super.httpContext();
        }
        return httpContext;
    }

    @Override
    protected String alias() {
        final Application application = getApplication();
        String alias = null;
        if (application instanceof JerseyApplication) {
            alias = ((JerseyApplication) application).getAlias();
        }
        if (alias == null) {
            alias = super.alias();
        }
        return alias;
    }
}