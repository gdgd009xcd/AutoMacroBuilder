/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import burp.IHttpService;

/**
 *
 * @author daike
 */
public class BurpIHttpService implements IHttpService {

    String host;
    int port;
    String proto;
    
    BurpIHttpService(String _host, int _port, String _proto){
        host = _host;
        port = _port;
        proto = _proto;
    }
    
    public BurpIHttpService(String _host, int _port, boolean ssl){
        host = _host;
        port = _port;
        proto = (ssl==true?"https":"http");
    }
    
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getProtocol() {
        return proto;
    }
    
}
