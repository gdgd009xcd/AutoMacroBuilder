/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.Date;

/**
 *
 * @author daike
 */
public class BurpICookie implements ICookie{
    String domain;
    String path;
    String name;
    String value;
    Date expdate;
    
    BurpICookie(String _dom, String _path, String _name, String _val, Date _expdate){
        domain = _dom;
        path = _path;
        name = _name;
        value = _val;
        expdate = _expdate;
    }
    
    BurpICookie(ICookie ic, boolean isdelete){
        domain = ic.getDomain();
        path = ic.getPath();
        name = ic.getName();
        if(isdelete){
            value = null;
        }else{
            value = ic.getValue();
        }
        expdate = ic.getExpiration();
    }
    
    public String getDomain(){
        return domain;
    }
    public String getPath(){
        return path;
    }
    
    public String getName(){
        return name;
    }
    public String getValue(){
        return value;
    }
    
    public Date getExpiration(){
        return expdate;
    }
}
