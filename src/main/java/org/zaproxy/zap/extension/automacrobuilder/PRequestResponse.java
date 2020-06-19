/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import org.zaproxy.zap.extension.automacrobuilder.mdepend.ClientDependMessageContainer;

/** @author tms783 */
public class PRequestResponse {
    public PRequest request;
    public PResponse response;
    String comments;
    Boolean disable = false; // ==true no execute.
    boolean iserror = false;
    int macropos = -1;
    private ClientDependMessageContainer cdmc = null;

    // public PRequestResponse(byte[] brequest, byte[] bresponse , Encode _pageenc){
    //    request = new PRequest(brequest, _pageenc);
    //    response = new PResponse(bresponse, _pageenc);
    //    comments = null;
    //    disable = false;
    // }

    private void init(
            String h,
            int p,
            boolean ssl,
            byte[] binrequest,
            byte[] binresponse,
            Encode reqpageenc,
            Encode respageenc) {
        request = new PRequest(h, p, ssl, binrequest, reqpageenc);
        response = new PResponse(binresponse, respageenc);
        comments = null;
        disable = false;
    }

    public PRequestResponse(
            String h, int p, boolean ssl, byte[] binrequest, byte[] binresponse, Encode pageenc) {
        cdmc = null;
        init(h, p, ssl, binrequest, binresponse, pageenc, pageenc);
    }

    public PRequestResponse(ClientDependMessageContainer cdmc) {
        this.cdmc = cdmc;
        init(
                cdmc.getHost(),
                cdmc.getPort(),
                cdmc.isSSL(),
                cdmc.getRequestByte(),
                cdmc.getResponseByte(),
                cdmc.getRequestEncode(),
                cdmc.getResponseEncode());
    }

    public void setClientDependMessageContainer(ClientDependMessageContainer cdmc){
        this.cdmc = cdmc;
    }
    
    public ClientDependMessageContainer getClientDependMessageContainer() {
        return this.cdmc;
    }

    void updateRequest(PRequest _req) {
        request = _req;
    }

    public void setComments(String _v) {
        comments = _v;
    }

    void Disable() {
        disable = true;
    }

    void Enable() {
        disable = false;
    }

    boolean isDisabled() {
        return disable;
    }

    void addComments(String _v) {
        comments = comments + _v;
    }

    public String getComments() {
        return comments;
    }

    boolean isError() {
        return iserror;
    }

    public void setError(boolean b) {
        iserror = b;
    }

    public void setMacroPos(int _p) {
        macropos = _p;
    }

    public int getMacroPos() {
        return macropos;
    }
}