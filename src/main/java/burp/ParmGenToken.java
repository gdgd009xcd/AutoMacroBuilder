/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;
import java.util.Objects;

/**
 *
 * @author chikara_1.daike
 */
public class ParmGenToken {
    private int tokentype;
    private String name;
    
   
    ParmGenToken(int _tokentype, String _name){
        tokentype = _tokentype;
        name = new String(_name);
    }
    
    public String GetName(){
        return name;
    }
    
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenToken) {
            ParmGenToken key = (ParmGenToken) obj;
            return this.tokentype == key.tokentype && this.name.equals(key.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokentype, name);
    }
}
