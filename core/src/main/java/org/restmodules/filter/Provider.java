package org.restmodules.filter;

/**
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
public interface Provider<T> {
    T get();
}
