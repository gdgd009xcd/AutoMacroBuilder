/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

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
    
}
