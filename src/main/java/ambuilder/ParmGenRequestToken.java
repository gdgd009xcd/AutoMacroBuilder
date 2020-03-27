/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import ambuilder.ParmGenRequestTokenKey;
import ambuilder.ParmGenToken;
import java.util.Objects;

/**
 *
 * @author daike
 */
public class ParmGenRequestToken {
    
    private ParmGenRequestTokenKey key = null;
    private String value = null;
    
    ParmGenRequestToken(ParmGenRequestTokenKey.RequestParamType _rptype, ParmGenRequestTokenKey.RequestParamSubType _subtype,
            String _name, String _value, int _fcnt){
        key = new ParmGenRequestTokenKey(_rptype, _subtype, _name, _fcnt);
        value = _value;

    }
    
    ParmGenRequestToken(ParmGenToken tkn){
        if(tkn!=null){//  Is tkn convertable?
            switch(tkn.getTokenKey().GetTokenType()){
                case JSON:
                    key = new ParmGenRequestTokenKey(ParmGenRequestTokenKey.RequestParamType.Json,ParmGenRequestTokenKey.RequestParamSubType.Default,
                    tkn.getTokenKey().getName(), tkn.getTokenKey().getFcnt());
                    value = tkn.getTokenValue().getValue();
                    break;
                default:
                    tkn = null;
                    break;
            }
        }
        
        if(tkn==null){//We cannot convert tkn's key to ParmGenRequestTokenKey.
            key = new ParmGenRequestTokenKey(ParmGenRequestTokenKey.RequestParamType.Nop, ParmGenRequestTokenKey.RequestParamSubType.Default,
            "", 0);
            value = "";
        }
    }
    
    public String getValue(){
        return value;
    }
    
    public ParmGenRequestTokenKey getKey(){
        return key;
    }
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenRequestToken) {
            ParmGenRequestToken that = (ParmGenRequestToken)obj;
            ParmGenRequestTokenKey that_key = that.getKey();
            ParmGenRequestTokenKey this_key = this.getKey();
            
            return this_key.equals(that_key);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        
        ParmGenRequestTokenKey this_key = this.getKey();
        return this_key.hashCode();
    }
    
}
