package org.restmodules.tests;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartup;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.compendiumProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.vmOption;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.webProfile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.restmodules.AbstractApplicationRegistrar;
import org.restmodules.filter.FilterRegistry;
import org.restmodules.jersey.JerseyApplication;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.sun.jersey.api.uri.UriBuilderImpl;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;

/**
 * @author Mathias Broekelmann
 *
 * @since 09.01.2010
 *
 */
@RunWith(JUnit4TestRunner.class)
public class ApplicationRegistrationTest {

    @Inject
    private BundleContext bundleContext;
    private Application _app;

    @Configuration
    public static Option[] configuration() {
        return options(provision(mavenBundle().groupId("org.restmodules")
                                              .artifactId("restmodules-core")
                                              .versionAsInProject(),
                                 mavenBundle().groupId("org.restmodules")
                                              .artifactId("restmodules-jersey")
                                              .versionAsInProject(),
                                 mavenBundle().groupId("com.sun.jersey.osgi")
                                              .artifactId("jersey-core")
                                              .versionAsInProject(),
                                 mavenBundle().groupId("com.sun.jersey.osgi")
                                              .artifactId("jersey-server")
                                              .versionAsInProject(),
                                 mavenBundle().groupId("com.sun.jersey.osgi")
                                              .artifactId("jsr311-api")
                                              .versionAsInProject(),
                                 mavenBundle().groupId("com.google.inject").artifactId("guice").versionAsInProject(),
                                 mavenBundle().groupId("org.aopalliance")
                                              .artifactId("com.springsource.org.aopalliance")
                                              .versionAsInProject(),
                                 mavenBundle().groupId("commons-io").artifactId("commons-io").versionAsInProject()),
                       vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"),
                       waitForFrameworkStartup(),
                       compendiumProfile(),
                       webProfile());
    }

    @Test
    public void testBundleContextIsAvailable() {
        assertThat(bundleContext, is(notNullValue()));
    }

    @Test
    public void testRuntimeDelegateIsRegistered() {
        final RuntimeDelegate runtime = RuntimeDelegate.getInstance();
        final UriBuilder uriBuilder = runtime.createUriBuilder();
        assertThat("jersey runtime delegate was not defined.", uriBuilder, is(instanceOf(UriBuilderImpl.class)));
    }

    @Before
    public void setUp() throws Exception {
        _app = new JerseyApplicationExtension("test");
    }

    @Test
    public void testApplicationRegistrationWithServicePropertyDefinedAlias() throws Exception {
        final Hashtable params = new Hashtable();
        params.put(AbstractApplicationRegistrar.ALIAS_SERVICE_PROPERTY, "foo");
        final Application application = new Application() {

            @Override
            public Set<Class<?>> getClasses() {
                return new HashSet<Class<?>>(asList(TestResource.class));
            }
        };
        final ServiceRegistration registration = registerApplication(application, params);
        final URL url = new URL("http://localhost:8080/foo/hello");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertThat(connection.getResponseCode(), is(200));
        assertThat(IOUtils.toString(connection.getInputStream()), is("Hello World!"));
        registration.unregister();
    }

    private ServiceRegistration registerApplication(final Application application, final Hashtable params)
        throws InterruptedException {
        final ServiceRegistration registration = bundleContext.registerService(Application.class.getName(),
                                                                               application,
                                                                               params);
        waitForApplicationRegistration();
        return registration;
    }

    private void waitForApplicationRegistration() throws InterruptedException {
        Thread.sleep(5000);
    }

    @Test
    public void testApplicationRegistration() throws Exception {
        final ServiceRegistration registration = registerApplication(_app, null);
        final URL url = new URL("http://localhost:8080/test/hello");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertThat(connection.getResponseCode(), is(200));
        assertThat(IOUtils.toString(connection.getInputStream()), is("Hello World!"));
        registration.unregister();
    }

    @Test
    public void testApplicationFilterRegistration() throws Exception {
        final TestFilter filter = new TestFilter();
        final JerseyApplicationExtension app = new JerseyApplicationExtension("test") {

            @Override
            public void registerFilters(final FilterRegistry registry) {
                registry.filter("/*").through(filter);
            }
        };
        final ServiceRegistration registration = registerApplication(app, null);
        final URL url = new URL("http://localhost:8080/test/hello");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertThat(connection.getResponseCode(), is(200));
        assertThat(IOUtils.toString(connection.getInputStream()), is("Hello World!"));
        assertThat("filter.init call not valid", filter._initCalled, is(1));
        assertThat("filter.doFilter call not valid", filter._doFilterCalled, is(1));
        registration.unregister();
        assertThat("filter.destroy call not valid", filter._destroyCalled, is(1));
        assertThat(connection.getHeaderField("foo"), is("bar"));
    }

    @Test
    public void testGuiceIntegration() throws Exception {
        final Injector injector = Guice.createInjector(new MyTestModule());
        final Application app = new JerseyApplication() {

            @Override
            public String getAlias() {
                return "test";
            }

            @Override
            public Set<Class<?>> getClasses() {
                return new HashSet<Class<?>>(Arrays.<Class<?>> asList(UriInfoResolver.class, GuiceTestResource.class));
            }

            @Override
            public IoCComponentProviderFactory getComponentProviderFactory() {
                return new IoCComponentProviderFactory() {
                    public IoCComponentProvider getComponentProvider(final ComponentContext cc, final Class<?> c) {
                        if (injector.getBindings().containsKey(Key.get(c))) {
                            return new IoCInstantiatedComponentProvider() {
                                public Object getInstance() {
                                    return injector.getInstance(c);
                                }

                                public Object getInjectableInstance(final Object o) {
                                    return o;
                                }
                            };
                        }
                        return null;
                    }

                    public IoCComponentProvider getComponentProvider(final Class<?> c) {
                        return getComponentProvider(null, c);
                    }
                };
            }

        };
        final ServiceRegistration registration = registerApplication(app, null);
        final URL url = new URL("http://localhost:8080/test/guice");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertThat(connection.getResponseCode(), is(200));
        assertThat(IOUtils.toString(connection.getInputStream()), is("Success!!"));
        registration.unregister();
    }

    @Path("guice")
    public static class GuiceTestResource {
        private final Provider<UriInfo> _uriInfoProvider;

        @Context
        private UriInfo _uriInfo;

        @com.google.inject.Inject
        public GuiceTestResource(final Provider<UriInfo> uriInfoProvider) {
            _uriInfoProvider = uriInfoProvider;
        }

        @GET
        public String assertUriInfoFromGuice() {
            assertThat(_uriInfoProvider.get().getAbsolutePath(), is(_uriInfo.getAbsolutePath()));
            return "Success!!";
        }
    }

    private static class MyTestModule extends AbstractModule {

        @Provides
        public UriInfo resolveUriInfo(final UriInfoResolver resolver) {
            return resolver.getContext(getClass());
        }

        @Override
        protected void configure() {
            bind(UriInfoResolver.class).in(Scopes.SINGLETON);
            bind(GuiceTestResource.class);
        }
    }

    @javax.ws.rs.ext.Provider
    public static class UriInfoResolver implements ContextResolver<UriInfo> {

        private UriInfo _uriInfo;

        @Context
        public void setUriInfo(final UriInfo uriInfo) {
            _uriInfo = uriInfo;
        }

        public UriInfo getContext(final Class<?> type) {
            return _uriInfo;
        }

    }

    /**
     * @author Mathias Broekelmann
     *
     * @since 13.01.2010
     *
     */
    private class JerseyApplicationExtension extends JerseyApplication {

        private final String _alias;

        public JerseyApplicationExtension(final String alias) {
            _alias = alias;
        }

        @Override
        public Set<Class<?>> getClasses() {
            return new HashSet<Class<?>>(asList(TestResource.class));
        }

        @Override
        public String getAlias() {
            return _alias;
        }
    }

    @Path("hello")
    public static class TestResource {

        @GET
        @Produces("text/plain")
        public String getMessage() {
            return "Hello World!";
        }
    }

    /**
     * @author Mathias Broekelmann
     *
     * @since 13.01.2010
     *
     */
    public static class TestFilter implements Filter {

        private int _destroyCalled;
        private int _doFilterCalled;
        private int _initCalled;

        public void destroy() {
            _destroyCalled++;
        }

        public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
            _doFilterCalled++;
            ((HttpServletResponse) response).setHeader("foo", "bar");
            chain.doFilter(request, response);
        }

        public void init(final FilterConfig arg0) throws ServletException {
            _initCalled++;
        }
    }
}
