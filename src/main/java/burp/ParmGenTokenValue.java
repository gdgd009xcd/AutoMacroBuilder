/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

/**
 *
 * @author chikara_1.daike
 */
public class ParmGenTokenValue {
    private String url;
    private String value;
    ParmGenTokenValue(String _url, String _value){
        url = _url;
        value = _value;
    }
    
    public String getValue(){
        return value;
    }
    
    public String getURL(){
        return url;
    }
    
}
