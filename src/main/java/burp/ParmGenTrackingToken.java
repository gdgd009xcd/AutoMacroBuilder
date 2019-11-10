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
public class ParmGenTrackingToken {
    private ParmGenToken RToken;//レスポンスから抽出したトークン
    private ParmGenToken QToken;//リクエストから抽出したトークン
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
    
    ParmGenTrackingToken(ParmGenToken _qtoken, ParmGenToken _rtoken, RequestParamType _rptype, RequestParamSubType _stype){
        QToken = _qtoken;
        RToken = _rtoken;
        rptype = _rptype;
        subtype = _stype;
    }
    
    public ParmGenToken getResponseToken(){
        return RToken;
    }
    
    public ParmGenToken getRequestToken(){
        return QToken;
    }
    
    public RequestParamType getParamType(){
        return rptype;
    }
    
    public RequestParamSubType getParamSubType(){
        return subtype;
    }
    
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenTrackingToken) {
            ParmGenTrackingToken target = (ParmGenTrackingToken)obj;
            ParmGenToken this_token = this.getRequestToken();
            ParmGenToken target_token = target.getRequestToken();
            boolean matched = false;
            if(this_token==null&&target_token==null){
                matched = true;
            }else if(this_token!=null&&target_token!=null){
                ParmGenTokenKey this_tkey = this_token.getTokenKey();
                ParmGenTokenKey target_tkey = target_token.getTokenKey();
                if(this_tkey.GetName().toLowerCase().equals(target_tkey.GetName().toLowerCase()) &&
                        this_tkey.GetFcnt() == target_tkey.GetFcnt()){
                    matched = true;
                }
            }
            return this.rptype == target.rptype && this.subtype == target.subtype && matched;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        ParmGenToken this_token = this.getRequestToken();
        String name = null;int fcnt = 0;
        if(this_token!=null){
            ParmGenTokenKey this_tkey = this_token.getTokenKey();
            name = this_tkey.GetName().toLowerCase();
            fcnt = this_tkey.GetFcnt();
        }
        return Objects.hash(rptype, subtype, name,fcnt);
    }
    
}
