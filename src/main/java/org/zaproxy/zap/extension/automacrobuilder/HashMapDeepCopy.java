/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import static org.zaproxy.zap.extension.automacrobuilder.CastUtils.castToType;

import java.util.HashMap;
import java.util.UUID;

/** @author daike */
class HashMapDeepCopy {

    /**
     * HashMap<K extends DeepClone(has public clone()), V extends DeepClone> deep copy
     *
     * @param <K>
     * @param <V>
     * @param src
     * @param dest
     * @return HashMap<K,V>
     */
    private static <K extends DeepClone, V extends DeepClone> HashMap<K, V> hashMapDeepCopyKVClone(
            HashMap<K, V> src, HashMap<K, V> dest) {

        if (src != null && dest != null) {
            src.entrySet()
                    .forEach(
                            ent -> {
                                dest.put(
                                        castToType(ent.getKey().clone()),
                                        castToType(ent.getValue().clone()));
                            });
        }

        return dest;
    }

    /**
     * HashMap<K extends DeepClone, V> deep copy
     *
     * @param <K>
     * @param <V>
     * @param src copy from
     * @param dest copy to
     * @return HashMap<K,V>
     */
    private static <K extends DeepClone, V> HashMap<K, V> hashMapDeepCopyKClone(
            HashMap<K, V> src, HashMap<K, V> dest) {
        if (src != null && dest != null) {
            src.entrySet()
                    .forEach(
                            ent -> {
                                dest.put(castToType(ent.getKey().clone()), ent.getValue());
                            });
        }
        return dest;
    }

    /**
     * HashMap<K, V extends DeepClone> deep copy
     *
     * @param <K>
     * @param <V>
     * @param src copy from
     * @param dest copy to
     * @return HashMap<K,V>
     */
    private static <K, V extends DeepClone> HashMap<K, V> hashMapDeepCopyVClone(
            HashMap<K, V> src, HashMap<K, V> dest) {
        if (src != null && dest != null) {
            src.entrySet()
                    .forEach(
                            ent -> {
                                dest.put(ent.getKey(), castToType(ent.getValue().clone()));
                            });
        }
        return dest;
    }

    /**
     * HashMap<K, V> copy(Both K and V param is Primitive or no DeepClone(which has No Cloneable
     * method) variants.)
     *
     * @param <K>
     * @param <V>
     * @param src copy from
     * @param dest copy to
     * @return HashMap<K,V>
     */
    private static <K, V> HashMap<K, V> hashMapDeepCopyPrimitive(
            HashMap<K, V> src, HashMap<K, V> dest) {

        if (src != null && dest != null) {
            src.entrySet()
                    .forEach(
                            ent -> {
                                dest.put(ent.getKey(), ent.getValue());
                            });
        }
        return dest;
    }

    /**
     * HashMap<String, V extends DeepClone> deep copy
     *
     * @param String
     * @param <V>
     * @param src copy from
     * @param dest to which copy src
     * @return HashMap<String,V extends DeepClone>
     */
    private static <String, V extends DeepClone> HashMap<String, V> hashMapDeepCopyStrK(
            HashMap<String, V> src, HashMap<String, V> dest) {
        return hashMapDeepCopyVClone(src, dest);
    }

    /**
     * HashMap<String, String> copy( that is same as HashMap<String,String>.clone() . because String
     * is immutable. In other words String is "final fixed(unchangable)" object.)
     *
     * @param String
     * @param <V>
     * @param src copy from
     * @return HashMap<String,String> dest to which copy src
     */
    public static HashMap<String, String> hashMapDeepCopyStrKStrV(HashMap<String, String> src) {
        if(src == null) return null;
        HashMap<String, String> dest = new HashMap<>();
        return hashMapDeepCopyPrimitive(src, dest);
    }

    /**
     * Copy HashMap<String, ParmGenHeader>
     *      String is immutable
     *      ParmGenHeader has clone()
     * 
     * @param src
     * @return 
     */
    public static HashMap<String, ParmGenHeader> hashMapDeepCopyStrKParmGenHeaderV(
            HashMap<String, ParmGenHeader> src) {
        if(src == null) return null;
        HashMap<String, ParmGenHeader> dest = new HashMap<String, ParmGenHeader>();
        return hashMapDeepCopyStrK(src, dest);
    }
    /**
     * Copy HashMap<UUID, ParmGenTrackParam>
     *      UUID is immutable
     *      ParmGenTrackParam has clone()
     * 
     * @param src
     * @return 
     */
    public static HashMap<UUID, ParmGenTrackingParam> hashMapDeepCopyUuidKParmGenTrackingParamV(HashMap<UUID, ParmGenTrackingParam> src){
        if(src == null) return null;
        HashMap<UUID, ParmGenTrackingParam> dest = new HashMap<>();
        return hashMapDeepCopyVClone(src, dest);
    }
    
    /**
     * Copy HashMap<ParmGenTokenKey,Integer>  
     *      ParmGenTokenKey has clone()
     *      Integer is immutable.
     * 
     * @param src
     * @return 
     */
    public static HashMap<ParmGenTokenKey, Integer> hashMapDeepCopyParmGenTokenKeyKIntegerV(HashMap<ParmGenTokenKey, Integer> src){
        if(src == null) return null;
        HashMap<ParmGenTokenKey, Integer> dest = new HashMap<>();
        return hashMapDeepCopyKClone(src, dest);
    }
    
    /**
     * copy HashMap<ParmGenTokenKey, ParmGenTokenValue>
     * 
     * @param src
     * @return 
     */
    public static HashMap<ParmGenTokenKey, ParmGenTokenValue> hashMapDeepCopyParmGenHashMapSuper(HashMap<ParmGenTokenKey, ParmGenTokenValue> src){
        if(src == null) return null;
        HashMap<ParmGenTokenKey, ParmGenTokenValue> dest = new HashMap<>();
        return hashMapDeepCopyKVClone(src, dest);
    }
    
 }
