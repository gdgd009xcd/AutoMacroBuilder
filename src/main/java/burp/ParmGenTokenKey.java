/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;
import java.util.Objects;

/**
 *
 * @author daike
 */
public class ParmGenTokenKey {
    private AppValue.TokenTypeNames tokentype;
    private int fcnt;
    private String name;
    
   
    ParmGenTokenKey(AppValue.TokenTypeNames _tokentype, String _name, int _fcnt){
        tokentype = _tokentype;
        name = new String(_name);
        fcnt = _fcnt;
    }
    
    ParmGenTokenKey(ParmGenTokenKey tk){
    	tokentype = tk.tokentype;
    	name = new String(tk.name);
    	fcnt = tk.fcnt;
    }
    
    public String getName(){
        return name;
    }
    
    public AppValue.TokenTypeNames GetTokenType(){
        return tokentype;
    }
    
    public int getFcnt(){
        return fcnt;
    }
    
    public void setTokenType(AppValue.TokenTypeNames _tktype){
    	tokentype = _tktype;
    }
    
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenTokenKey) {
            ParmGenTokenKey key = (ParmGenTokenKey) obj;
            //name is case-sensitive.
            return this.tokentype == key.tokentype && this.name.equals(key.name) && this.fcnt == key.fcnt;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //name is case-sensitive.
        return Objects.hash(tokentype, name,fcnt);
    }
}
