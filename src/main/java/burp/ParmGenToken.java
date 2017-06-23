/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

/**
 *
 * @author youtube
 */
public class ParmGenToken  {

    ParmGenTokenKey ptk;
    ParmGenTokenValue ptv;
    
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
    
}
