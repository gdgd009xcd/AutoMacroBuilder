/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.Objects;

/**
 *
 * @author daike
 */
public class ParmGenTrackingToken {
    private ParmGenToken RToken;//Token value from HTTP Response
    private ParmGenRequestToken QToken;//Token value from HTTP reQuest
    private String regex = null;//option regex


    public ParmGenTrackingToken(ParmGenRequestToken _qtoken, ParmGenToken _rtoken, String optregex){
        QToken = _qtoken;
        RToken = _rtoken;
        regex = optregex;
    }
    
    public ParmGenToken getResponseToken(){
        return RToken;
    }
    
    public ParmGenRequestToken getRequestToken(){
        return QToken;
    }
    
    public String getRegex(){
        return regex;
    }
    
     
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenTrackingToken) {
            ParmGenTrackingToken that = (ParmGenTrackingToken)obj;
            ParmGenRequestToken that_qtoken = that.getRequestToken();
            ParmGenRequestToken this_qtoken = this.getRequestToken();
            
            return this_qtoken.equals(that_qtoken);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        
        ParmGenRequestToken this_qtoken = this.getRequestToken();
        return this_qtoken.hashCode();
    }
    
}
