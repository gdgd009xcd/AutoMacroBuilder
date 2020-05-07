/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.HashMap;
import static org.zaproxy.zap.extension.automacrobuilder.CastUtils.castToType;


/**
 *
 * @author daike
 *
 */
class HashMapDeepCopy{
    
   /**
    * HashMap<K extends DeepClone(has public clone()), V extends DeepClone> deep copy
    * @param <K>
    * @param <V>
    * @param src
    * @param dest
    * @return HashMap<K,V>
    */
    private  static <K extends DeepClone, V extends DeepClone> HashMap<K,V> hashMapDeepCopyKVClone(HashMap<K,V>src, HashMap<K,V>dest){
        
        if(src!=null&&dest!=null){
            src.entrySet().forEach(ent -> {
                dest.put(castToType(ent.getKey().clone()),  castToType(ent.getValue().clone()));
            });
        }
       
        return dest;
    }
    
    /**
    * HashMap<K extends DeepClone, V> deep copy
    * @param <K>
    * @param <V>
    * @param src copy from
    * @param dest copy to
    * @return HashMap<K,V>
    */
    private static <K extends DeepClone, V> HashMap<K,V> hashMapDeepCopyKClone(HashMap<K,V>src, HashMap<K,V>dest){
        if(src!=null&&dest!=null){
            src.entrySet().forEach(ent -> {
                dest.put(castToType(ent.getKey().clone()), ent.getValue());
            });
        }
        return dest;
    }
    
    /**
    * HashMap<K, V extends DeepClone> deep copy
    * @param <K>
    * @param <V>
    * @param src copy from
    * @param dest copy to
    * @return HashMap<K,V>
    */
    private static <K, V extends DeepClone> HashMap<K,V> hashMapDeepCopyVClone(HashMap<K,V>src, HashMap<K,V>dest){
        if(src!=null&&dest!=null){
            src.entrySet().forEach(ent -> {
                dest.put(ent.getKey(), castToType(ent.getValue().clone()));
            });
        }
        return dest;
    }
    
    /**
    * HashMap<K, V>  copy(Both K and V param is Primitive or no DeepClone(which has No Cloneable method) variants.)
    * @param <K>
    * @param <V>
    * @param src copy from
    * @param dest copy to
    * @return HashMap<K,V>
    */
    private static <K, V> HashMap<K,V> hashMapDeepCopyPrimitive(HashMap<K,V>src, HashMap<K,V>dest){

        if(src!=null&&dest!=null){
            src.entrySet().forEach(ent -> {
                dest.put(ent.getKey(), ent.getValue());
            });
        }
        return dest;
    }
    
    /**
    * HashMap<String, V extends DeepClone> deep copy
    * @param String
    * @param <V>
    * @param src copy from
    * @param dest to which copy src
    * @return HashMap<String,V extends DeepClone>
    */
    private static <String, V extends DeepClone> HashMap<String, V> hashMapDeepCopyStrK(HashMap<String, V> src, HashMap<String, V>dest){
        return hashMapDeepCopyVClone(src, dest);
    }
    
    /**
    * HashMap<String, String> copy( that is same as HashMap<String,String>.clone() . because String is final fixed value.)
    * @param String
    * @param <V>
    * @param src copy from
    * @return HashMap<String,String> dest to which copy src
    */
    public static HashMap<String, String> hashMapDeepCopyStrKStrV(HashMap<String, String> src){
        HashMap<String,String> dest = new HashMap<>();
        return hashMapDeepCopyPrimitive(src, dest);
    }
    
    public static HashMap<String, ParmGenHeader>  hashMapDeepCopyStrKParmGenHeaderV(HashMap<String, ParmGenHeader> src){
        HashMap<String, ParmGenHeader> dest = new HashMap<String, ParmGenHeader>();
        return hashMapDeepCopyStrK(src, dest);
    }
}
