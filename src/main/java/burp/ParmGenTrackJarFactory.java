/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author daike
 */
public class ParmGenTrackJarFactory {
    
    static HashMap<Integer, ParmGenTrackingParam> trackjar = null;
    static Integer keymax = -1;
    

    
    static void clear(){
        keymax = 0;
        trackjar = new HashMap<Integer,ParmGenTrackingParam>();
    }
    
    static int create(){
        ParmGenTrackingParam tkparam = new ParmGenTrackingParam();
        trackjar.put(keymax, tkparam);
        return keymax++;
    }
    
    
    
    static void put(Integer key, ParmGenTrackingParam tkparam){
        trackjar.put(key, tkparam);
        //ParmVars.plog.debuglog(0, "TrackJar put key:" + key);
    }
    
   
    static ParmGenTrackingParam get(Integer key){
        //ParmVars.plog.debuglog(0, "TrackJar get key:" + key);
        return trackjar.get(key);
        
    }
    
    static void remove(Integer key){
        trackjar.remove(key);
    }
    
    
    
    static {
        clear();
    }
    
}
