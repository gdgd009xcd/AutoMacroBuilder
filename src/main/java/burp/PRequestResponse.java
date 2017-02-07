/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

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
    
    public PRequestResponse(String _request_string, String _response_string){
        request = new PRequest(_request_string);
        response = new PResponse(_response_string);
        comments = null;
        disable = false;
    }
    
    public PRequestResponse(String h, int p, boolean ssl, byte[] _binrequest, String _response_string){
        request = new PRequest(h, p, ssl,_binrequest);
        response = new PResponse(_response_string);
        comments = null;
        disable = false;
    }
    
    void setComments(String _v){
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
    
    String getComments(){
        return comments;
    }
    
    boolean isError(){
        return iserror;
    }
    void setError(boolean b){
        iserror = b;
    }
}
