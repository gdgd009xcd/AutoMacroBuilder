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
public class CastUtils {
 
    /**
     * 
     * @param <T>
     * @param obj
     * @return 
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T castToType(Object obj) {
        return (T) obj;
    }
    
    
}
