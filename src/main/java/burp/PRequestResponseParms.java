/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;


public class PRequestResponseParms extends PRequestResponse {

    public PRequestResponseParms(String _request_string, String _response_string) {
        super(_request_string, _response_string);
    }

    public PRequestResponseParms(String h, int p, boolean ssl, byte[] _binrequest, String _response_string) {
        super(h, p, ssl, _binrequest, _response_string);
    }
    
}
