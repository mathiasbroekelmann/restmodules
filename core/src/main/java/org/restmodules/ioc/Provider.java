package org.restmodules.ioc;

/**
 * A provider encapsulates a value.
 *
 * Implement {@link Scoped} to indicate the scope of the provided value. Implement {@link Proxied} to unwrap a proxy for
 * injection.
 *
 * @author mathias.broekelmann
 */
public interface Provider<T> {

    /**
     * Get the value of the provider.
     */
    T get();
}
