/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 *
 * @author daike
 */
public class GsonParser {
    
    enum EventType {
        NONE,
        START_OBJECT,
        END_OBJECT,
        START_ARRAY,
        END_ARRAY,
        BOOLEAN,
        NUMBER,
        STRING,
        NULL,
    }
    
    
    
    public static void elementLoopParser(JsonElement element, GsonParserListener listener){
        int level = 0;
        ParmGenStack<GsonIterator> itstack = new ParmGenStack<>();
        String kname = null;
        
        GsonIterator.ElmType currentelmtype = GsonIterator.ElmType.PRIMITIVE;
        GsonIterator git = null;
        
        do{
            try{
                if(element!=null){
                    
                    if(element.isJsonArray()){
                        JsonArray jarray = element.getAsJsonArray();
                        git = new GsonIterator(kname, jarray.iterator());
                        itstack.push(git);
                        level++;
                        listener.receiver(git, GsonParser.EventType.START_ARRAY, kname, null, level);
                        
                    }else if(element.isJsonObject()){
                        JsonObject jobj = element.getAsJsonObject();
                        git = new GsonIterator(kname,jobj.entrySet());
                        itstack.push(git);
                        level++;
                        listener.receiver(git, GsonParser.EventType.START_OBJECT, kname, null, level);
                        
                    }else if(element.isJsonNull()){
                        
                        listener.receiver(git, GsonParser.EventType.NULL, kname, null, level);
                    }else if(element.isJsonPrimitive()){
                        JsonPrimitive jprim = element.getAsJsonPrimitive();
                        if(jprim.isBoolean()){
                          Boolean b = jprim.getAsBoolean();
                          listener.receiver(git, GsonParser.EventType.BOOLEAN, kname, b,level);
                          
                        }else if (jprim.isNumber()){
                            Number numval = jprim.getAsNumber();
                            listener.receiver(git, GsonParser.EventType.NUMBER, kname, numval, level);
                            
                        }else if (jprim.isString()){
                            String s = jprim.getAsString();
                            listener.receiver(git, GsonParser.EventType.STRING, kname, s, level);
                            
                        }
                    }
                }
                git = itstack.getCurrent();
                if(git!=null){
                    if(!git.hasNext()){//end of array or object list
                        
                        GsonParser.EventType etype = GsonParser.EventType.NONE;
                        if(currentelmtype==GsonIterator.ElmType.ARRAY){
                            etype = GsonParser.EventType.END_ARRAY;
                        }else{
                            etype = GsonParser.EventType.END_OBJECT;
                        }
                        listener.receiver(git, etype, git.getKeyName(), null, level);
                        itstack.pop();
                        git = itstack.getCurrent();
                        if(git!=null){
                            currentelmtype = git.getElmType();
                        }
                        level--;
                    }
                    if(git!=null&&git.hasNext()){
                        currentelmtype = git.getElmType();
                        GsonEntry jent = git.next();
                        if(jent.hasKey()){
                            kname = jent.getKey();
                        }else{
                            kname = null;
                        }
                        element = jent.getJsonElement();
                    }else{
                        kname = null;
                        element = null;
                    }
                }
            }catch(Exception e){
                
            }
        }while(itstack.size()>0);
        
    }
}
