/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.restmodules.ioc;

/**
 * Identifies a scope
 *
 * @author mathias.broekelmann
 */
public enum Scope {
    /**
     * scope singleton means that the instance is only get once for the livetime of the application
     */
    Singleton,

    /**
     * scope request will get one instance for each request if needed.
     */
    Request,

    /**
     * scope session will get one instance for each session if needed.
     */
    // Session,

    /**
     * scope none will get an instance for each usage which might be multiple times during a request.
     */
    None
}
