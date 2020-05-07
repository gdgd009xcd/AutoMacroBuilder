/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import com.google.gson.JsonElement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author daike
 */
public class GsonIterator {
    enum ElmType {
        ARRAY, OBJECT, PRIMITIVE
    }
    
    String keyname;//this Array's or Ojbect's keyname
    Iterator<Map.Entry<String, JsonElement>> objit;
    Iterator<JsonElement> arrit;

    GsonIterator(String k, Set<Map.Entry<String, JsonElement>> eset){
        keyname = k;
        objit = eset.iterator();
        arrit = null;
    }

    GsonIterator(String k, Iterator<JsonElement> it){
        keyname = k;
        arrit = it;
        objit = null;
    }

    public ElmType getElmType(){
        return (arrit!=null?ElmType.ARRAY:ElmType.OBJECT);
    }

    public boolean hasNext(){
        return arrit!=null?(arrit.hasNext()):(objit.hasNext());
    }

    public GsonEntry next(){
        GsonEntry jent = null;
        if(hasNext()){
            if(arrit!=null){
                JsonElement nelm = arrit.next();
                jent = new GsonEntry(nelm);
            }else{
                Map.Entry<String, JsonElement> ment = objit.next();
                String k = ment.getKey();
                JsonElement nelm = ment.getValue();
                jent = new GsonEntry(k, nelm);
            }
        }

        return jent;
    }
    
    public String getKeyName(){
        return keyname;
    }
}
