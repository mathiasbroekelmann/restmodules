package org.restmodules.jersey;

import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.ws.rs.core.Application;

import org.osgi.service.http.HttpContext;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.restmodules.filter.DefaultFilterRegistry;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.restmodules.AbstractApplicationRegistrar;
import org.restmodules.ApplicationProvider;
import org.restmodules.ApplicationRegistrar;
import org.restmodules.filter.Provider;

/**
 * Jersey specific implementation for an {@link ApplicationRegistrar}.
 *
 * @author Mathias Broekelmann
 *
 * @since 11.01.2010
 *
 */
public class JerseyApplicationRegistrar extends AbstractApplicationRegistrar {

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
        return servlet;
    }

    @Override
    protected DefaultFilterRegistry filterRegistry() {
        return new DefaultFilterRegistry() {

            @Override
            protected Provider<Filter> createFilterProvider(Class<Filter> filterClazz) {
                Application app = getApplication();
                if (app instanceof JerseyApplication) {
                    IoCComponentProviderFactory cpf = ((JerseyApplication) app).getComponentProviderFactory();
                    if (cpf != null) {
                        final IoCComponentProvider provider = cpf.getComponentProvider(filterClazz);
                        if (provider != null) {
                            return (Provider<Filter>) new Provider<Filter>() {

                                public Filter get() {
                                    return (Filter) provider.getInstance();
                                }
                            };
                        }
                    }
                }
                return super.createFilterProvider(filterClazz);
            }
        };
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
