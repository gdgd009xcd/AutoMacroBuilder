/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import com.google.gson.JsonElement;

/**
 *
 * @author daike
 */
public class GsonEntry {
    String key;
        JsonElement elem;
        boolean hasKey;
        
        GsonEntry(String k, JsonElement je){
            key = k;
            elem = je;
            hasKey = true;
        }
        
        GsonEntry(JsonElement je){
            key = null;
            elem = je;
            hasKey = false;
        }
        
        public boolean hasKey(){
            return hasKey;
        }
        
        public String getKey(){
            return key;
        }
        
        public JsonElement getJsonElement(){
            return elem;
        }
}
