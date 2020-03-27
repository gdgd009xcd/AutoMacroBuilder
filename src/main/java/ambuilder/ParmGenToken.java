/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import ambuilder.ParmGenTokenKey;
import ambuilder.ParmGenTokenValue;
import java.util.Objects;

/**
 *
 * @author youtube
 */
public class ParmGenToken  {

    ParmGenTokenKey ptk;
    ParmGenTokenValue ptv;
    Boolean enabled = false;
    
ParmGenToken(AppValue.TokenTypeNames _tokentype, String url, String name, String value, Boolean _b, int fcnt){
    ptk = new ParmGenTokenKey(_tokentype, name, fcnt);
    ptv = new ParmGenTokenValue(url, value, _b);
}

ParmGenToken(ParmGenTokenKey tkey, ParmGenTokenValue tval){
    ptk = tkey;
    ptv = tval;
}

ParmGenToken(ParmGenToken tkn){
	ptk = new ParmGenTokenKey(tkn.ptk);
	ptv = new ParmGenTokenValue(tkn.ptv);
}

public ParmGenTokenKey getTokenKey(){
    return ptk;
}

public ParmGenTokenValue getTokenValue(){
    return ptv;
}

Boolean isEnabled() {
    return enabled;
}

void setEnabled(Boolean _enabled){
    enabled = _enabled;
}

@Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenToken) {
            ParmGenToken tkn = (ParmGenToken) obj;
            //name is case-sensitive.
            return this.ptk.equals(tkn.ptk) && this.ptv.equals(tkn.ptv) && Objects.equals(this.enabled, tkn.enabled);
        } else {
            return false;
        }
    }
    
}
