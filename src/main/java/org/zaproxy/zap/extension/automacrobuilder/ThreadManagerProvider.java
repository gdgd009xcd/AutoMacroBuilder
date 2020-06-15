/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

/**
 *
 * @author daike
 */
public class ThreadManagerProvider {
    static private ThreadManager tm = null;
    
    public static ThreadManager getThreadManager(){
        if( tm == null) {
            tm = new ThreadManager();
        }
        return tm;
    }
    
}
