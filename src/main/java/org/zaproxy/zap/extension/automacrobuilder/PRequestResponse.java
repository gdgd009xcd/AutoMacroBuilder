/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;


/**
 *
 * @author tms783
 */
public class PRequestResponse {
    public PRequest request;
    public PResponse response;
    String comments;
    Boolean disable = false;//==true no execute.
    boolean iserror = false;
    int macropos = -1;
    
    //public PRequestResponse(byte[] brequest, byte[] bresponse , Encode _pageenc){
    //    request = new PRequest(brequest, _pageenc);
    //    response = new PResponse(bresponse, _pageenc);
    //    comments = null;
    //    disable = false;
    //}
    
    public PRequestResponse(String h, int p, boolean ssl, byte[] _binrequest, byte[] _binresponse, Encode _pageenc){
        request = new PRequest(h, p, ssl,_binrequest, _pageenc);
        response = new PResponse(_binresponse, _pageenc);
        comments = null;
        disable = false;
    }
    
    void updateRequest(PRequest _req){
        request = _req;
    }
    
    public void setComments(String _v){
        comments = _v;
    }
    
    void Disable(){
        disable = true;
    }
    
    void Enable(){
        disable = false;
    }
    
    boolean isDisabled(){
        return disable;
    }
    
    void addComments(String _v){
        comments = comments + _v;
    }
    
    public String getComments(){
        return comments;
    }
    
    boolean isError(){
        return iserror;
    }
    public void setError(boolean b){
        iserror = b;
    }
    
    public void setMacroPos(int _p){
        macropos = _p;
    }
    
    int getMacroPos(){
        return macropos;
    }
}
