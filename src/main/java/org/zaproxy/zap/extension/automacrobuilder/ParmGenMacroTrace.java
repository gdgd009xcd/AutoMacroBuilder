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

import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import org.zaproxy.zap.extension.automacrobuilder.GSONSaveObject.PRequestResponses;
import org.zaproxy.zap.extension.automacrobuilder.generated.MacroBuilderUI;
import org.zaproxy.zap.extension.automacrobuilder.mdepend.ClientDependent;

/** @author daike */
public class ParmGenMacroTrace extends ClientDependent {

    MacroBuilderUI ui = null;
    Charset charset = StandardCharsets.ISO_8859_1;
    private List<PRequestResponse> rlist = null; // マクロ実行後の全リクエストレスポンス
    private List<PRequestResponse> originalrlist = null; // オリジナルリクエストレスポンス

    // *** REMOVE*** private ArrayList<String> set_cookienames = null;//レスポンスのSet-Cookie値の名前リスト
    int selected_request = 0; // 現在選択しているカレントのリクエスト
    int stepno = -1; // 実行中のリクエスト番号
    PRequestResponse toolbaseline = null; // Repeater's baseline request.

    boolean MBCookieUpdate = false; // ==true Cookie更新
    boolean MBCookieFromJar = false; // ==true 開始時Cookie.jarから引き継ぐ
    boolean MBFinalResponse = false; // ==true 結果は最後に実行されたマクロのレスポンス
    boolean MBResetToOriginal = false; // ==true オリジナルリクエストを実行。
    boolean MBsettokencache = false; // 開始時tokenキャッシュ
    boolean MBreplaceCookie = false; // ==true Cookie引き継ぎ置き換え == false Cookie overwrite
    boolean MBmonitorofprocessing = false;
    boolean MBreplaceTrackingParam = false;

    int waittimer = 0; // 実行間隔(msec)

    ListIterator<PRequestResponse> oit = null; // オリジナル
    ListIterator<PRequestResponse> cit = null; // 実行
    ListIterator<ParmGenParser> pit = null;

    PRequestResponse postmacro_RequestResponse = null;

    int state = PMT_POSTMACRO_NULL; // 下記の値。

    public static final int PMT_PREMACRO_BEGIN = 0; // 前処理マクロ実行中
    public static final int PMT_PREMACRO_END = 1; // 前処理マクロ実行中
    public static final int PMT_CURRENT_BEGIN = 2; // カレントリクエスト開始
    public static final int PMT_CURRENT_END = 3; // カレントリクエスト終了。
    public static final int PMT_POSTMACRO_BEGIN = 4; // 後処理マクロ実行中
    public static final int PMT_POSTMACRO_END = 5; // 後処理マクロ終了。
    public static final int PMT_POSTMACRO_NULL = 6; // 後処理マクロレスポンスnull

    private FetchResponseVal fetchResVal = null; // token cache

    private ParmGenTWait TWaiter = null;

    private ParmGenCookieManager cookieMan = null; // cookie manager

    private boolean locked = false;
    private Queue<Long> tidlist = null;

    public String state_debugprint() {
        String msg = "PMT_UNKNOWN";
        switch (state) {
            case PMT_PREMACRO_BEGIN:
                msg = "PMT_PREMACRO_BEGIN";
                break;
            case PMT_PREMACRO_END:
                msg = "PMT_PREMACRO_END";
                break;
            case PMT_CURRENT_BEGIN:
                msg = "PMT_CURRENT_BEGIN";
                break;
            case PMT_CURRENT_END:
                msg = "PMT_CURRENT_END";
                break;
            case PMT_POSTMACRO_BEGIN:
                msg = "PMT_POSTMACRO_BEGIN";
                break;
            case PMT_POSTMACRO_END:
                msg = "PMT_POSTMACRO_END";
                break;
            case PMT_POSTMACRO_NULL:
                msg = "PMT_POSTMACRO_NULL";
                break;
            default:
                break;
        }

        return msg;
    }

    public ParmGenMacroTrace() {}

    //
    // setter
    //
    public void clear() {
        ParmGen.clearAll();
        tidlist = null;
        locked = false;
        macroEnded(true);
        rlist = null;
        originalrlist = null;
        // REMOVE set_cookienames = null;
        selected_request = 0;
        stepno = -1;
        oit = null;
        cit = null;
        pit = null;
        postmacro_RequestResponse = null;
        nullfetchResValAndCookieMan();
    }

    public void setUI(MacroBuilderUI _ui) {
        ui = _ui;
    }

    public void setMBCookieFromJar(boolean b) {
        MBCookieFromJar = b;
    }

    public void setMBFinalResponse(boolean b) {
        MBFinalResponse = b;
    }

    public void setMBResetToOriginal(boolean b) {
        MBResetToOriginal = b;
    }

    public void setMBsettokencache(boolean b) {
        MBsettokencache = b;
    }

    public void setMBreplaceCookie(boolean b) {
        MBreplaceCookie = b;
    }

    public void setMBmonitorofprocessing(boolean b) {
        MBmonitorofprocessing = b;
    }

    public boolean isMBmonitorofprocessing() {
        return MBmonitorofprocessing;
    }

    public void setMBreplaceTrackingParam(boolean _b) {
        MBreplaceTrackingParam = _b;
    }

    public boolean isBaseLineMode() {
        return !MBreplaceTrackingParam;
    }

    boolean isOverWriteCurrentRequestTrackigParam() {
        return !MBreplaceTrackingParam && isCurrentRequest();
    }

    public void setWaitTimer(String msec) {
        try {
            waittimer = Integer.parseInt(msec); // msec
            if (waittimer <= 0) waittimer = 0;
        } catch (Exception e) {
            waittimer = 0;
        }
    }

    // ３）カレントリクエスト終了(レスポンス受信後)後に実行
    public void endAfterCurrentRequest(PRequestResponse pqrs) {
        if (rlist != null && selected_request < rlist.size() && selected_request >= 0) {
            pqrs.setComments(ParmVars.plog.getComments());
            pqrs.setError(ParmVars.plog.isError());
            rlist.set(selected_request, pqrs);
        }
        ui.updateCurrentReqRes();
        state = PMT_CURRENT_END;
    }

    /**
     * Set Current Request position number in PRequestResponse list(rlist)
     * 
     * @param _p position number(from 0 to rlist.size()-1)
     */
    public void setCurrentRequest(int _p) {
        if (rlist != null && rlist.size() > _p) {
            selected_request = _p;
            EnableRequest(_p); // カレントリクエストは強制
            ParmVars.plog.debuglog(
                    0, "selected_request:" + selected_request + " rlist.size=" + rlist.size());
        }
    }

    boolean isCurrentRequest(int _p) {
        if (selected_request == _p) {
            return true;
        }
        return false;
    }

    public boolean isCurrentRequest() {
        return isCurrentRequest(stepno);
    }

    public void EnableRequest(int _idx) {
        if (rlist != null && rlist.size() > _idx) {
            PRequestResponse prr = rlist.get(_idx);
            prr.Enable();
        }
    }

    public void DisableRequest(int _idx) {
        if (rlist != null && rlist.size() > _idx) {
            PRequestResponse prr = rlist.get(_idx);
            prr.Disable();
        }
    }

    boolean isDisabledRequest(int _idx) {
        if (rlist != null && rlist.size() > _idx) {
            PRequestResponse prr = rlist.get(_idx);
            return prr.isDisabled();
        }
        return false;
    }

    boolean isError(int _idx) {
        if (rlist != null && rlist.size() > _idx) {
            PRequestResponse prr = rlist.get(_idx);
            return prr.isError();
        }
        return false;
    }

    public int getRlistCount() {
        if (rlist == null) return 0;
        return rlist.size();
    }

    public void updateOriginalRequest(int idx, PRequest _request) {
        if (originalrlist != null && originalrlist.size() > 0) {
            PRequestResponse pqr = originalrlist.get(idx);
            pqr.updateRequest(_request);
            originalrlist.set(idx, pqr);
        }
    }

    public PRequestResponse getOriginalRequest(int idx) {
        if (originalrlist != null
                && originalrlist.size() > 0
                && idx > -1
                && idx < originalrlist.size()) {
            PRequestResponse pqr = originalrlist.get(idx);
            return pqr;
        }
        return null;
    }

    PRequestResponse getCurrentOriginalRequest() {
        return getOriginalRequest(getCurrentRequestPos());
    }

    // １）前処理マクロ開始
    public void startBeforePreMacro() {
        macroStarted();

        if (waittimer > 0) {
            TWaiter = new ParmGenTWait(waittimer);
        } else {
            TWaiter = null;
        }

        initFetchResponseVal();
        initCookieManager();

        if (!MBsettokencache) {
            if (fetchResVal != null) {
                fetchResVal.clearCachedLocVal();
            }
        }
        if (!MBCookieFromJar) {
            if (cookieMan != null) {
                cookieMan.removeAll();
            }
        }
        if (fetchResVal != null) {
            fetchResVal.clearDistances();
        }
        state = PMT_PREMACRO_BEGIN;
        ParmVars.plog.debuglog(0, "BEGIN PreMacro");

        // 1)synchronized preMacroLock
        // 単一のスレッドが running = trueにセット only one thread set running =true then preMacroLock method
        // end.
        // 2）2番目に実行したスレッドはpreMacroLock内でrunning = true時、wait(); second excuting thread wait,because
        // running == true.
        //	他のスレッドは、2番目スレッドが終了するまで、synchronizedのため、待機。 the other threads wait until second executing
        // thread complete preMacroLock.
        // 3）共有storeからスレッド毎のローカルのFetchResponseVal, Cookieストアを生成。	local FetchResponseVal, Cookie
        // store create from shared store.

        oit = null;
        cit = null;

        stepno = 0;

        try {
            if (rlist != null && selected_request >= 0 && rlist.size() > selected_request) {
                oit = originalrlist.listIterator();
                cit = rlist.listIterator();
                int n = 0;
                if (TWaiter != null) {
                    TWaiter.TWait();
                }
                while (cit.hasNext() && oit.hasNext()) {
                    PRequestResponse ppr = cit.next();
                    PRequestResponse opr = oit.next();
                    stepno = n;
                    if (n++ >= selected_request) {
                        break;
                    }

                    if (ppr.isDisabled()) {
                        continue;
                    }

                    if (MBResetToOriginal) {
                        ppr = opr; // オリジナルにリセット
                    }

                    // Set Cookie Value from CookieStore.
                    ppr.request.setCookiesFromCookieMan(cookieMan);

                    String noresponse = "";
                    /*
                    String host = ppr.request.getHost();
                    int port = ppr.request.getPort();
                    boolean isSSL = ppr.request.isSSL();
                    Encode _pageenc = ppr.request.getPageEnc();
                    BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
                    */
                    ParmVars.plog.debuglog(
                            0,
                            "PreMacro StepNo:"
                                    + stepno
                                    + " "
                                    + ppr.request.getHost()
                                    + " "
                                    + ppr.request.method
                                    + " "
                                    + ppr.request.url);
                    // byte[] byteres = callbacks.makeHttpRequest(host,port, isSSL, byterequest);

                    /*
                    ParmVars.plog.clearComments();
                    ParmVars.plog.setError(false);
                    IHttpRequestResponse IHReqRes = callbacks.makeHttpRequest(bserv, byterequest);
                    byte[] bytereq = IHReqRes.getRequest();
                    byte[] byteres = IHReqRes.getResponse();
                    */
                    PRequestResponse pqrs = clientHttpRequest(ppr.request);

                    if (pqrs != null) {
                        cit.set(pqrs); // 更新
                    }

                    if (TWaiter != null) {
                        TWaiter.TWait();
                    }
                }
            }
        } catch (Exception e) {
            ParmVars.plog.printException(e);
        }
        ParmVars.plog.debuglog(0, "END PreMacro");
        state = PMT_PREMACRO_END;
    }

    PRequest configureRequest(PRequest preq) {

        if (isRunning()) { // MacroBuilder list > 0 && state is Running.
            // ここでリクエストのCookieをCookie.jarで更新する。
            String domain_req = preq.getHost().toLowerCase();
            String path_req = preq.getPath();
            boolean isSSL_req = preq.isSSL();
            List<HttpCookie> cklist = cookieMan.get(domain_req, path_req, isSSL_req);
            HashMap<CookieKey, ArrayList<CookiePathValue>> cookiemap =
                    new HashMap<CookieKey, ArrayList<CookiePathValue>>();
            for (HttpCookie cookie : cklist) {
                String domain = cookie.getDomain();
                if (domain == null || domain.isEmpty()) {
                    domain = domain_req;
                }
                domain = domain.toLowerCase();
                if (!domain.equals(domain_req)) { // domain:.test.com != domain_req:www.test.com
                    if (domain_req.endsWith(
                            domain)) { // domain_req is belong to domain's subdomain.
                        domain = domain_req;
                    }
                }
                String name = cookie.getName();
                if (name == null) name = "";
                String path = cookie.getPath();
                if (path == null) path = "";
                String value = cookie.getValue();
                if (value == null) value = "";
                CookieKey cikey = new CookieKey(domain, name);
                ParmVars.plog.debuglog(0, "Cookiekey domain:" + domain + " name=" + name);
                CookiePathValue cpvalue = new CookiePathValue(path, value);
                ArrayList<CookiePathValue> cpvlist = cookiemap.get(cikey);
                if (cpvlist == null) {
                    cpvlist = new ArrayList<CookiePathValue>();
                }

                cpvlist.add(cpvalue);

                cookiemap.put(cikey, cpvlist);
            }

            boolean ReplaceCookieflg = true;
            if (isCurrentRequest()) {
                ReplaceCookieflg = MBreplaceCookie;
            }

            if (preq.setCookies(cookiemap, ReplaceCookieflg)) {
                return preq;
            }
        }

        return null;
    }

    // ４）後処理マクロの開始
    public void startPostMacro() {
        state = PMT_POSTMACRO_BEGIN;
        postmacro_RequestResponse = null;
        if (isMBFinalResponse()) {
            // 後処理マクロ　selected_request+1 ～最後まで実行。
            stepno = selected_request + 1;
            ParmVars.plog.debuglog(0, "BEGIN PostMacro");
            try {
                if (cit != null && oit != null) {
                    int n = stepno;
                    while (cit.hasNext() && oit.hasNext()) {
                        stepno = n;
                        if (TWaiter != null) {
                            TWaiter.TWait();
                        }
                        n++;

                        PRequestResponse ppr = cit.next();
                        PRequestResponse opr = oit.next();
                        if (ppr.isDisabled()) {
                            continue;
                        }
                        postmacro_RequestResponse = null;
                        if (MBResetToOriginal) {
                            ppr = opr;
                        }

                        /*
                        byte[] byterequest = ppr.request.getByteMessage();
                        String host = ppr.request.getHost();
                        int port = ppr.request.getPort();
                        boolean isSSL = ppr.request.isSSL();
                        Encode _pageenc = ppr.request.getPageEnc();
                        BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
                        */
                        ParmVars.plog.debuglog(
                                0,
                                "PostMacro StepNo:"
                                        + stepno
                                        + " "
                                        + ppr.request.getHost()
                                        + " "
                                        + ppr.request.method
                                        + " "
                                        + ppr.request.url);
                        /*
                        ParmVars.plog.clearComments();
                        ParmVars.plog.setError(false);
                        postmacro_RequestResponse = callbacks.makeHttpRequest(bserv, byterequest);
                        byte[] bytereq = postmacro_RequestResponse.getRequest();
                        byte[] byteres = postmacro_RequestResponse.getResponse();
                        if(bytereq==null){
                            bytereq = new String("").getBytes();
                        }
                        if(byteres == null){
                            byteres = new String("").getBytes();
                        }
                        PRequestResponse pqrs = new PRequestResponse(host, port, isSSL, bytereq, byteres, _pageenc);
                        */
                        PRequestResponse pqrs = clientHttpRequest(ppr.request);
                        if (pqrs != null) {
                            postmacro_RequestResponse = pqrs;
                            cit.set(pqrs); // 更新
                        }
                    }
                }
            } catch (Exception ex) {
                ParmVars.plog.printException(ex);
            }
        }
        cit = null;
        if (postmacro_RequestResponse != null) {
            state = PMT_POSTMACRO_END;
        } else {
            state = PMT_POSTMACRO_NULL;
        }

        ParmVars.plog.debuglog(0, "END PostMacro");
    }

    byte[] getPostMacroRequest() {
        if (postmacro_RequestResponse != null) {
            return postmacro_RequestResponse.request.getByteMessage();
        }
        return null;
    }

    public byte[] getPostMacroResponse() {
        if (postmacro_RequestResponse != null) {
            return postmacro_RequestResponse.response.getByteMessage();
        }
        return null;
    }

    public int getCurrentRequestPos() {
        return selected_request;
    }

    boolean isRunning() {
        if (rlist != null && rlist.size() > 0) return state < PMT_POSTMACRO_END ? true : false;
        return false;
    }

    boolean CurrentRequestIsTrackFromTarget(AppParmsIni pini) {
        int FromStepNo = pini.getTrackFromStep();
        if (FromStepNo < 0) {
            return true;
        } else if (FromStepNo == stepno) {
            return true;
        }
        return false;
    }

    boolean CurrentRequestIsSetToTarget(AppParmsIni pini) {
        int ToStepNo = pini.getSetToStep();
        int ToStepBase = ParmVars.TOSTEPANY;

        if (ToStepNo == ToStepBase) {
            return true;
        } else if (ToStepNo == stepno) {
            return true;
        }
        // ParmVars.plog.debuglog(0, "!!!!!!!!!!!!!!!!! failed CurrentRequestIsSetToTarget: stepno="
        // + stepno + " ToStepNo=" + ToStepNo + " ToStepBase=" + ToStepBase) ;
        return false;
    }

    public void setRecords(List<PRequestResponse> _rlist) {
        // rlist = new ArrayList <PRequestResponse> (_rlist);//copy
        if (rlist == null) {
            rlist = _rlist; // reference共有
            originalrlist = new ArrayList<PRequestResponse>(_rlist); // copy
        } else {
            originalrlist.addAll(new ArrayList<PRequestResponse>(_rlist));
        }
        ParmVars.plog.debuglog(0, "setRecords:" + rlist.size() + "/" + originalrlist.size());
    }

    void macroStarted() {
        ParmVars.plog.debuglog(0, "<--Macro Started.-->");
        macroLock();
    }

    public void macroEnded(boolean clrtidlist) {
        nullState();
        macroUnLock();
        if (clrtidlist) {
            tidlist = null;
        }
        ParmVars.plog.debuglog(0, "<--Macro Complete Ended.-->");
    }

    public synchronized void macroLock() {
        long tid = Thread.currentThread().getId();
        if (tidlist == null) {
            tidlist = new ArrayDeque<>();
        }
        tidlist.add(tid);
        ParmVars.plog.debuglog(0, "macroLock Start. tid=" + tid);
        while (locked == true) {
            try {
                ParmVars.plog.debuglog(0, "macroLock wait In.  tid=" + tid);
                wait(); // availableがtrueの間、wait
                ParmVars.plog.debuglog(0, "macroLock wait Out. tid=" + tid);
            } catch (InterruptedException e) {
            }
        }
        // workAreaに値をセットする処理
        locked = true;

        ParmVars.plog.debuglog(0, "macroLock  Done. tid=" + tid);
    }

    public synchronized void macroUnLock() {
        // lockedにfalseを代入した後wait状態のスレッドを解除
        locked = false;
        notifyAll();
        Long tid = new Long(-1);
        try {
            if (tidlist != null) {
                tid = tidlist.remove();
            }
        } catch (Exception e) {

        }
        ParmVars.plog.debuglog(0, "macroUnLock Done. tid=" + tid);
    }

    private void nullState() {
        state = PMT_POSTMACRO_NULL;
        stepno = -1;
        scanQueNull();
    }

    public void setToolBaseLine(PRequestResponse _baseline) {

        toolbaseline = _baseline;
    }

    public void setState(int st) {
        state = st;
    }

    //
    // getter
    //
    public int getState() {
        return state;
    }

    List<PRequestResponse> getRecords() {
        return rlist;
    }

    public boolean isMBFinalResponse() {
        return MBFinalResponse;
    }

    public int getStepNo() {
        return stepno;
    }

    public PRequestResponse getToolBaseline() {
        return toolbaseline;
    }

    public PRequestResponse getRequestResponse(int pos) {
        if (rlist != null && rlist.size() > 0) {
            PRequestResponse pqr = rlist.get(pos);
            if (MBResetToOriginal) {
                pqr = originalrlist.get(pos);
            }

            return pqr;
        }
        return null;
    }
    
    /**
     * Get current selected PRequestResponse object from PRequstResponse list.
     * 
     * @return PRequestResponse
     */
    public PRequestResponse getCurrentRequestResponse(){
        if(this.selected_request > -1){
            return getRequestResponse(this.selected_request);
        }
        return null;
    }

    public void sendToRepeater(int pos) {
        PRequestResponse pqr = null;
        if ((pqr = getRequestResponse(pos)) != null) {
            setToolBaseLine(pqr);
            String host = pqr.request.getHost();
            int port = pqr.request.getPort();
            boolean useHttps = pqr.request.isSSL();
            burpSendToRepeater(
                    host, port, useHttps, pqr.request.getByteMessage(), "MacroBuilder:" + pos);
        }
    }

    public void sendToScanner(int pos) {
        PRequestResponse pqr = null;
        if ((pqr = getRequestResponse(pos)) != null) {
            setToolBaseLine(null);
            String host = pqr.request.getHost();
            int port = pqr.request.getPort();
            boolean useHttps = pqr.request.isSSL();
            burpDoActiveScan(host, port, useHttps, pqr.request.getByteMessage());
        }
    }

    public void sendToIntruder(int pos) {
        PRequestResponse pqr = null;
        if ((pqr = getRequestResponse(pos)) != null) {
            setToolBaseLine(null);
            String host = pqr.request.getHost();
            int port = pqr.request.getPort();
            boolean useHttps = pqr.request.isSSL();
            burpSendToIntruder(host, port, useHttps, pqr.request.getByteMessage());
        }
    }

    void GSONSave(GSONSaveObject gsonsaveobj) {
        if (gsonsaveobj != null) {
            if (originalrlist != null) {
                gsonsaveobj.CurrentRequest = getCurrentRequestPos();

                for (PRequestResponse pqr : originalrlist) {
                    PRequestResponses preqresobj = new PRequestResponses();
                    byte[] qbin = pqr.request.getByteMessage();
                    byte[] rbin = pqr.response.getByteMessage();
                    // byte[] encodedBytes = Base64.encodeBase64(qbin);
                    String qbase64 =
                            Base64.getEncoder()
                                    .encodeToString(qbin); // same as new String(encode(src),
                    // StandardCharsets.ISO_8859_1)
                    /*
                    try {
                        qbase64 = new String(encodedBytes,"ISO-8859-1");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ParmGenMacroTrace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    */
                    // encodedBytes = Base64.encodeBase64(rbin);
                    String rbase64 = Base64.getEncoder().encodeToString(rbin);
                    /*
                    try {
                        rbase64 = new String(encodedBytes, "ISO-8859-1");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ParmGenMacroTrace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    */
                    preqresobj.PRequest = qbase64;
                    preqresobj.PResponse = rbase64;

                    String host = pqr.request.getHost();
                    int port = pqr.request.getPort();
                    boolean ssl = pqr.request.isSSL();
                    String comments = pqr.getComments();
                    boolean isdisabled = pqr.isDisabled();
                    boolean iserror = pqr.isError();
                    preqresobj.Host = host;
                    preqresobj.Port = port;
                    preqresobj.SSL = ssl;
                    preqresobj.Comments = comments == null ? "" : comments;
                    preqresobj.Disabled = isdisabled;
                    preqresobj.Error = iserror;

                    gsonsaveobj.PRequestResponse.add(preqresobj);
                }
            }
        }
    }

    public void initFetchResponseVal() {
        if (fetchResVal == null) {
            fetchResVal = new FetchResponseVal();
        }
    }

    public FetchResponseVal getFetchResponseVal() {
        return fetchResVal;
    }

    public void nullfetchResValAndCookieMan() {
        fetchResVal = null;
        cookieMan = null;
    }

    public void initCookieManager() {
        if (cookieMan == null) {
            cookieMan = new ParmGenCookieManager();
        }
    }

    public List<PRequestResponse> getOriginalrlist() {
        return originalrlist;
    }

    public void parseSetCookie(PRequestResponse pqrs) {
        // カレントリクエストのset-cookie値をcookie.jarに保管

        List<String> setcookieheaders = pqrs.response.getSetCookieHeaders();
        for (String headerval : setcookieheaders) {
            String cheader = "Set-Cookie: " + headerval;
            String domain = pqrs.request.getHost();
            String path = "/"; // default root path
            cookieMan.parse(domain, path, cheader);
        }
    }
}
