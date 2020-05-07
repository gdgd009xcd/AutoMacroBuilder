/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.Objects;

/**
 *
 * @author daike
 */
public class ParmGenTokenValue {
    private String url;//url is LOCATION header's URL "VALUE". It is Not a key value.  LOCATION: http://brah.com/xxx...
    private String value;
    private Boolean b;
    
    ParmGenTokenValue(String _url, String _value, Boolean _b){
        url = _url;
        value = _value;
        b = _b;
    }
    
    ParmGenTokenValue(ParmGenTokenValue tv){
    	url = new String(tv.url);
    	value = new String(tv.value);
        b = tv.b;
    }
    
    public String getValue(){
        return value;
    }
    
    public String getURL(){
        return url;
    }
    
    public Boolean getBoolean(){
        return b;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenTokenValue) {
            ParmGenTokenValue v = (ParmGenTokenValue) obj;
            //name is case-sensitive.
            return this.url.equals(v.url) && this.value.equals(v.value) && Objects.equals(this.b, v.b);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash =  Objects.hash(this.url, this.value, this.b);
        return hash;
    }
    
}
