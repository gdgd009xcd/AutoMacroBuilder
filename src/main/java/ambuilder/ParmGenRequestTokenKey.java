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
public class ParmGenRequestTokenKey {
    
    private int fcnt;
    private String name;
    
    enum RequestParamType {
        Query,
        X_www_form_urlencoded,
        Json,
        Form_data,
        Header,
        Nop
    }
    
    enum RequestParamSubType {
        Default,
        Cookie,
        Bearer,
    }
    
    private RequestParamType rptype;
    private RequestParamSubType subtype;
   
    ParmGenRequestTokenKey(RequestParamType _rptype, RequestParamSubType _subtype, String _name, int _fcnt){
        rptype = _rptype;
        subtype = _subtype;
        name = _name;
        fcnt = _fcnt;
    }
    
    ParmGenRequestTokenKey(ParmGenRequestTokenKey tk){
    	rptype = tk.rptype;
        subtype = tk.subtype;
    	name = tk.name;
    	fcnt = tk.fcnt;
    }
    
    public String getName(){
        return name;
    }
    
    
    
    public int getFcnt(){
        return fcnt;
    }
    
    public RequestParamType getRequestParamType(){
        return rptype;
    }
    
    public RequestParamSubType getRequestParamSubType(){
        return subtype;
    }
    
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenRequestTokenKey) {
            ParmGenRequestTokenKey key = (ParmGenRequestTokenKey) obj;
            //name is case-sensitive.
            return this.rptype == key.rptype && this.subtype == key.subtype && this.name.equals(key.name) && this.fcnt == key.fcnt;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //name is case-sensitive.
        return Objects.hash(rptype, subtype ,name ,fcnt);
    }
}
