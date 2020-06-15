/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder.mdepend;

import burp.BurpExtender;
import burp.BurpIHttpService;
import burp.IHttpRequestResponse;
import burp.IScanQueueItem;
import java.io.File;
import java.nio.charset.Charset;
import org.zaproxy.zap.extension.automacrobuilder.Encode;
import org.zaproxy.zap.extension.automacrobuilder.PRequest;
import org.zaproxy.zap.extension.automacrobuilder.PRequestResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmVars;

/**
 *
 * @author daike
 */
public class ClientDependent {
    
    // below members does not need copy per thread. these parameter is temporary used.
    IScanQueueItem scanque = null;//scanner's queue
    byte[] originalrequest = null;// performAction's IHttpRequestResponse currentrequest bytes
    IHttpRequestResponse messageInfo = null;// performAction's IHttpRequestResponse
    
    private String comments = "";
    
    private boolean iserror = false;

    
    final public static String LOG4JXML_DIR = System.getProperty("user.home") + "/.BurpSuite";

    public ClientDependent(){
        scanQueNull();
    }
    
    public void burpSetCurrentOriginalRequest(byte[] b){
        originalrequest = b;
    }
    
    public byte[] burpGetCurrentOriginalRequest(){
        return originalrequest;
    }
    
    public void burpSetCurrentMessageInfo(IHttpRequestResponse messageInfo){
        this.messageInfo = messageInfo;
    }
    
    public IHttpRequestResponse burGetCurrentMessageInfo(){
        return this.messageInfo;
    }
    
    protected void burpSendToRepeater(String host, int port, boolean useHttps, byte[] messages, String tabtitle){
        BurpExtender.mCallbacks.sendToRepeater(
                host,
                port,
                useHttps,
                messages,
                tabtitle);
    }
    
    protected void burpDoActiveScan(String host, int port, boolean useHttps, byte[] messages){
        scanque =BurpExtender.mCallbacks.doActiveScan(
                host,
                port,
                useHttps,
                messages
            );
    }
    
    protected void burpSendToIntruder(String host, int port, boolean useHttps, byte[] messages){
        BurpExtender.mCallbacks.sendToIntruder(
                host,
                port,
                useHttps,
                messages
                );
    }
    
    protected PRequestResponse clientHttpRequest(PRequest request){
        PRequestResponse pqrs = null;
        if(request!=null){
            Encode enc_iso8859_1 = Encode.ISO_8859_1;
            Charset charset_iso8859_1 = enc_iso8859_1.getIANACharset();
            String noresponse = "";
            byte[] byterequest = request.getByteMessage();
            String host = request.getHost();
            int port = request.getPort();
            boolean isSSL = request.isSSL();
            Encode _pageenc = request.getPageEnc();
            BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
            //byte[] byteres = callbacks.makeHttpRequest(host,port, isSSL, byterequest);
            clearComments();
            setError(false);
            IHttpRequestResponse IHReqRes = BurpExtender.mCallbacks.makeHttpRequest(bserv, byterequest);
            byte[] bytereq = IHReqRes.getRequest();
            byte[] byteres = IHReqRes.getResponse();
            if(bytereq==null){//Impossible..
                bytereq = new String("").getBytes(charset_iso8859_1);//not NULL, length zero bytes.
            }
            if(byteres==null){
                byteres = new String("").getBytes(charset_iso8859_1);//not NULL, length zero bytes.
            }
            pqrs = new PRequestResponse(host, port, isSSL, bytereq, byteres, _pageenc);
            if(pqrs!=null){
                if(pqrs.response.getBodyContentLength()<=0){
                    noresponse = "\nNo Response(NULL)";
                }
                pqrs.setComments(getComments() + noresponse);
                pqrs.setError(isError());
            }
        }
        
        return pqrs;
    }
    
    public int getScanQuePercentage(){
        if(scanque!=null){
            byte b = scanque.getPercentageComplete();
            return b & 0xff;
        }
        return -1;
    }
    
    protected void scanQueNull(){
        scanque = null;
    }
    
    public void clearComments() {
        comments = ""; // no null
    }

    public void addComments(String _v) {
        comments += _v + "\n";
    }

    void setComments(String _v) {
        comments = _v;
    }

    public String getComments() {
        return comments;
    }

    public void setError(boolean _b) {
        iserror = _b;
    }

    public boolean isError() {
        return iserror;
    }
}
