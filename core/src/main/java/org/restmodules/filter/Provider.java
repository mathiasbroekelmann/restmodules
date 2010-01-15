package org.restmodules.filter;

/**
 * @author Mathias Broekelmann
 *
 * @since 13.01.2010
 *
 */
interface Provider<T> {
    T get();
}
