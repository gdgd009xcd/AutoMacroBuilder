/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

/**
 *
 * @author daike
 */
public class CookiePathValue {
    private String path;
    private String value;
    
    CookiePathValue(String _path, String _value){
        path = _path;
        value = _value;
    }
    
    String getPath(){
        return path;
    }
    
    String getValue(){
        return value;
    }
    
    
    
}

