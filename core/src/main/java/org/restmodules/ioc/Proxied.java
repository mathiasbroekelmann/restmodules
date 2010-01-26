package org.restmodules.ioc;

/**
 * Providers which implement {@link Proxied} will be used to unfold the proxy instance so the container is able to
 * perform injection on the non proxied instance.
 * 
 * @author Mathias Broekelmann
 * 
 * @since 26.01.2010
 * 
 */
public interface Proxied {

    /**
     * Unproxy the given instance. If it is not a proxy just return the given instance.
     */
    <T> T unproxy(T instance);
}
