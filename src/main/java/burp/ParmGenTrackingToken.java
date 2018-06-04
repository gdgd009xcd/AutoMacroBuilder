/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

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
        Nop
    }
    
    private RequestParamType rptype;
    
    ParmGenTrackingToken(ParmGenToken _qtoken, ParmGenToken _rtoken, RequestParamType _rptype){
        QToken = _qtoken;
        RToken = _rtoken;
        rptype = _rptype;
        
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
    
}
