/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.ArrayList;
import java.util.List;
import static org.zaproxy.zap.extension.automacrobuilder.CastUtils.castToType;

/**
 *
 * @author daike
 */
public class ListDeepCopy {
    
    private static <V extends DeepClone> List<V> listDeepCopyVClone(List<V> src, List<V> dest){
        
        if(src!=null&&dest!=null){
            src.forEach(v -> {dest.add(castToType(v.clone()));});
        }
        
        return dest;
        
    }
    
    public static List<ParmGenBeen> listDeepCopy(List<ParmGenBeen> src){
        List<ParmGenBeen> dest = new ArrayList<>();
        
        return listDeepCopyVClone(src, dest);
        
    }
    
}
