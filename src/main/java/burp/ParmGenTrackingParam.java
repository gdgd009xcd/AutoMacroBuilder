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
public class ParmGenTrackingParam {
    private String cachevalue = null;
    private int responseStepNo = -1;
    
    ParmGenTrackingParam(){
        cachevalue = null;
        responseStepNo = -1;
    }
    
    public void init(){
        cachevalue = null;
        responseStepNo = -1;
    }
    
    void setValue(String _v){
        cachevalue = _v;
    }
    
    void setResponseStepNo(int _r){
        responseStepNo = _r;
    }
    
    public String getValue(){
        return cachevalue;
    }
    
    public int getResponseStepNo(){
        return responseStepNo;
    }

}
