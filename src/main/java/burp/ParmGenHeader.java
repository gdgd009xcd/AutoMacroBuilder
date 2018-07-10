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
public class ParmGenHeader {
    private String name;
    private String value;
    private String key_uppername;
    
    ParmGenHeader(String _n, String _v){
        name = _n;
        value = _v;
        key_uppername = _n;
        if(name!=null){
            key_uppername = name.toUpperCase();
        }
        
    }
    
    public String getName(){
        return name;
    }
    
    public String getValue(){
        return value;
    }
    
    public String getKeyUpper(){
        return key_uppername;
    }
    
}
