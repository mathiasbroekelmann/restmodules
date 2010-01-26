/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.restmodules.ioc;

/**
 * Identifies a scoped instance.
 * 
 * @author mathias.broekelmann
 */
public interface Scoped {

    /**
     * @return the scope. returning null means Scope.None
     */
    Scope getScope();
}
