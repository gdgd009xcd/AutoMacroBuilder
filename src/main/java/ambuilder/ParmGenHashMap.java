/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import ambuilder.ParmGenTokenKey;
import ambuilder.ParmGenTokenValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author youtube
 */
public  class ParmGenHashMap extends HashMap<ParmGenTokenKey,ParmGenTokenValue> implements InterfaceCollection<Map.Entry<ParmGenTokenKey,ParmGenTokenValue>>{

    
    ParmGenHashMap(){

    }
    
    
    
    public int size(){
        return super.size();
    }
    
    public void addToken(AppValue.TokenTypeNames _tokentype, String url, String name, String value, Boolean b, int fcnt){
        ParmGenTokenKey tk = new ParmGenTokenKey(_tokentype, name, fcnt);
        ParmGenTokenValue tv = new ParmGenTokenValue(url, value, b);
        super.put(tk, tv);
    }

    @Override
    public Iterator<Entry<ParmGenTokenKey, ParmGenTokenValue>> iterator() {
        return entrySet().iterator();
    }

    
}
