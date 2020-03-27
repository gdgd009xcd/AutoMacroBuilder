/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import java.util.Objects;

/**
 *
 * @author daike
 */
public class CookieKey implements Comparable<CookieKey> {
    private String domain;
    private String name;

    
    public CookieKey(String _domain, String _name){
        if(_domain!=null)
            this.domain = _domain.toLowerCase();
        if(_name!=null)
            this.name = _name.toLowerCase();
        
    }
    
    String getDomain(){
        return domain;
    }
    
    String getName(){
        return name;
    }
    
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CookieKey ) {
            CookieKey key = (CookieKey) obj;
            if(key.domain!=null&&key.name!=null&&this.domain!=null&&this.name!=null){
                if(this.domain.equals(key.domain.toLowerCase()) && 
                    this.name.equals(key.name.toLowerCase())){
                    
                    return true;
                
                }
            }else if(key.domain==null&&key.name==null&&this.domain==null&&this.name==null){
                return true;//all String member is null
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, name);
    }
    
    @Override
    public int compareTo(CookieKey obj) {
        if(obj == null){
            throw new NullPointerException();
        }
        //
        // this > obj : > 0
        // this ==obj : =0
        // this < obj : < 0
        //
        if (this.domain != null && obj.domain == null) {
            return 1;
        }
        
        if (this.domain == null && obj.domain != null) {
            return -1;
        }

        if (this.domain != null && obj.domain != null) {
            int stringDiff = this.domain.compareTo(obj.domain);
            if (stringDiff > 0) {
                return 1;
            }
            if (stringDiff < 0) {
                return -1;
            }
        }
        
        if (this.name != null && obj.name == null) {
            return 1;
        }
        
        if (this.name == null && obj.name != null) {
            return -1;
        }

        if (this.name != null && obj.name != null) {
            int stringDiff = this.name.compareTo(obj.name);
            if (stringDiff > 0) {
                return 1;
            }
            if (stringDiff < 0) {
                return -1;
            }
        }
        return 0;
    }
}
