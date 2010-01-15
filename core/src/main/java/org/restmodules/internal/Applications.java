package org.restmodules.internal;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.restmodules.ApplicationProvider;
import org.restmodules.ApplicationRegistrar;
import org.restmodules.ApplicationRegistrarFactory;
import org.restmodules.jersey.JerseyApplicationRegistrar;

/**
 * Helper class to register servlets in {@link HttpService} for detected {@link Application} services.
 *
 * @author Mathias Broekelmann
 *
 * @since 11.01.2010
 *
 */
public class Applications {
    private final ConcurrentMap<ApplicationRegistrar, Collection<Runnable>> _registeredApplications = new ConcurrentHashMap<ApplicationRegistrar, Collection<Runnable>>();

    private final ConcurrentMap<HttpService, Collection<Runnable>> _registrationsAtHttpService = new ConcurrentHashMap<HttpService, Collection<Runnable>>();

    private final BundleContext _bundleContext;

    private final ServiceTracker _applicationTracker;

    private final ServiceTracker _httpServiceTracker;

    private final ServiceTracker _applicationRegistrarFactoryTracker;

    private final ServiceTrackerCustomizer _applicationListener = new ServiceTrackerCustomizer() {

        public Object addingService(final ServiceReference reference) {
            // registers the application at an http service.
            final ApplicationRegistrar registrar = createApplicationRegistrar(reference);
            // register application for already known http services.
            if (_httpServiceTracker != null) {
                for (final Object httpService : in(_httpServiceTracker.getServices())) {
                    registerApplication((HttpService) httpService, registrar);
                }
            }
            return registrar;
        }

        public void modifiedService(final ServiceReference reference, final Object service) {
            // TODO: track update to the basepath and unregister/register application
        }

        public void removedService(final ServiceReference reference, final Object service) {
            unregister(registeredApplications((ApplicationRegistrar) service));
        }
    };

    private static final <E, T extends Iterable<E>> T in(final T iterable, final T onNull) {
        return iterable == null ? onNull : iterable;
    }

    private static final <E> Collection<E> in(final Collection<E> iterable) {
        return iterable == null ? Collections.<E> emptyList() : iterable;
    }

    private final ServiceTrackerCustomizer _httpServiceListener = new ServiceTrackerCustomizer() {

        /**
         * unregister all applications which where registered at the given http service.
         */
        public void removedService(final ServiceReference reference, final Object service) {
            unregister(in(registeredApplications((HttpService) service)));
        }

        public void modifiedService(final ServiceReference reference, final Object service) {
        }

        public Object addingService(final ServiceReference reference) {
            final HttpService httpService = (HttpService) _bundleContext.getService(reference);
            // register applications that are already known.
            if (_applicationTracker != null) {
                for (final Object service : in(_applicationTracker.getServices())) {
                    registerApplication(httpService, (ApplicationRegistrar) service);
                }
            }
            return httpService;
        }
    };

    private Applications(final BundleContext bundleContext) {
        _bundleContext = bundleContext;
        _applicationTracker = new ServiceTracker(bundleContext, Application.class.getName(), _applicationListener);
        _httpServiceTracker = new ServiceTracker(bundleContext,
                                                 "org.osgi.service.http.HttpService",
                                                 _httpServiceListener);
        _applicationRegistrarFactoryTracker = new ServiceTracker(_bundleContext,
                                                                 ApplicationRegistrarFactory.class.getName(),
                                                                 null);
    }

    private static <T> Iterable<T> in(final T... elements) {
        return elements == null ? Collections.<T> emptyList() : asList(elements);
    }

    /**
     * Start the registration listeners for the given bundle context.
     */
    public static Runnable start(final BundleContext bundleContext) {
        final Applications registrar = new Applications(bundleContext);
        registrar.start();
        return new Runnable() {
            public void run() {
                registrar.stop();
            }
        };
    }

    public void start() {
        _applicationRegistrarFactoryTracker.open();
        _httpServiceTracker.open();
        _applicationTracker.open();
    }

    public void stop() {
        _applicationRegistrarFactoryTracker.close();
        _applicationTracker.close();
        _httpServiceTracker.close();
    }

    private final Collection<Runnable> registeredApplications(final ApplicationRegistrar registration) {
        if (!_registeredApplications.containsKey(registration)) {
            _registeredApplications.putIfAbsent(registration, new CopyOnWriteArrayList<Runnable>());
        }
        return _registeredApplications.get(registration);
    }

    private final Collection<Runnable> registeredApplications(final HttpService httpService) {
        if (!_registrationsAtHttpService.containsKey(httpService)) {
            _registrationsAtHttpService.putIfAbsent(httpService, new CopyOnWriteArrayList<Runnable>());
        }
        return _registrationsAtHttpService.get(httpService);
    }

    private void registerApplication(final HttpService httpService, final ApplicationRegistrar registrar) {
        final Runnable unregistrar = registrar.registerAt(httpService);
        registeredApplications(registrar).add(unregistrar);
        registeredApplications(httpService).add(unregistrar);
    }

    private void unregister(final Collection<Runnable> unregistrations) {
        for (final Runnable runnable : unregistrations) {
            runnable.run();
        }
        unregistrations.clear();
    }

    private ApplicationRegistrar createApplicationRegistrar(final ServiceReference applicationReference) {
        final Object factory = _applicationRegistrarFactoryTracker.getService();
        final ApplicationProvider provider = new ApplicationProvider() {
            public ServiceReference getServiceReference() {
                return applicationReference;
            }

            public Application getApplication() {
                return (Application) _bundleContext.getService(applicationReference);
            }
        };
        final ApplicationRegistrar registrar;
        if (factory instanceof ApplicationRegistrarFactory) {
            registrar = ((ApplicationRegistrarFactory) factory).create(provider);
        } else {
            registrar = new JerseyApplicationRegistrar(provider);
        }
        return registrar;
    }
}