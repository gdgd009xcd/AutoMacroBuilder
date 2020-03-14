/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import burp.GSONSaveObject.PRequestResponses;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.Base64;
import java.util.Queue;
import java.util.Stack;

/**
 *
 * @author daike
 */
public class ParmGenMacroTrace {

    MacroBuilderUI ui = null;
    IBurpExtenderCallbacks callbacks;
    Charset charset = StandardCharsets.ISO_8859_1;
    private ArrayList <PRequestResponse> rlist = null;//マクロ実行後の全リクエストレスポンス
    private ArrayList <PRequestResponse> originalrlist = null; //オリジナルリクエストレスポンス

    // *** REMOVE*** private ArrayList<String> set_cookienames = null;//レスポンスのSet-Cookie値の名前リスト
    int selected_request = 0;//現在選択しているカレントのリクエスト
    int stepno = -1;//実行中のリクエスト番号
    PRequestResponse toolbaseline = null;// Repeater's baseline request.
    
    boolean MBCookieUpdate = false;//==true Cookie更新
    boolean MBCookieFromJar = false;//==true 開始時Cookie.jarから引き継ぐ
    boolean MBFinalResponse = false;//==true 結果は最後に実行されたマクロのレスポンス
    boolean MBResetToOriginal =false;//==true オリジナルリクエストを実行。
    boolean MBsettokencache = false;//開始時tokenキャッシュ
    boolean MBreplaceCookie = false;//==true Cookie引き継ぎ置き換え == false Cookie overwrite
    boolean MBmonitorofprocessing = false;
    boolean MBreplaceTrackingParam = false;
    IScanQueueItem scanque = null;//scanner's queue
    

    
    int waittimer = 0;//実行間隔(msec)

    ListIterator<PRequestResponse> oit = null;//オリジナル
    ListIterator<PRequestResponse> cit = null;//実行
    ListIterator<ParmGenParser> pit = null;

    IHttpRequestResponse postmacro_RequestResponse = null;


    int state = PMT_POSTMACRO_NULL;//下記の値。

    public static final int PMT_PREMACRO_BEGIN = 0;//前処理マクロ実行中
    public static final int PMT_PREMACRO_END = 1;//前処理マクロ実行中
    public static final int PMT_CURRENT_BEGIN = 2;//カレントリクエスト開始
    public static final int PMT_CURRENT_END = 3;//カレントリクエスト終了。
    public static final int PMT_POSTMACRO_BEGIN = 4;//後処理マクロ実行中
    public static final int PMT_POSTMACRO_END = 5;//後処理マクロ終了。
    public static final int PMT_POSTMACRO_NULL = 6; //後処理マクロレスポンスnull

    private FetchResponseVal fetchResVal = null;//token cache
    
    private ParmGenTWait TWaiter = null;
    
    private ParmGenCookieManager cookieMan = null;//cookie manager
    
    private boolean locked = false;
    private Queue<Long> tidlist = null;
    
    String state_debugprint(){
        String msg = "PMT_UNKNOWN";
        switch(state){
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
    
    ParmGenMacroTrace(IBurpExtenderCallbacks _callbacks){
        callbacks = _callbacks;
    }

    
    //
    // setter
    //
    void clear(){
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

    void setUI(MacroBuilderUI _ui){
        ui = _ui;
    }

   
    
    void setMBCookieFromJar(boolean b){
        MBCookieFromJar = b;
    }
    
    void setMBFinalResponse(boolean b){
        MBFinalResponse = b;
    }
    void setMBResetToOriginal(boolean b){
        MBResetToOriginal = b;
    }

    void setMBsettokencache(boolean b){
        MBsettokencache = b;
    }
    void setMBreplaceCookie(boolean b){
        MBreplaceCookie = b;
    }
    
    void setMBmonitorofprocessing(boolean b){
        MBmonitorofprocessing =b;
    }
    
    public boolean isMBmonitorofprocessing(){
        return MBmonitorofprocessing;
    }
    
    void setMBreplaceTrackingParam(boolean _b){
        MBreplaceTrackingParam = _b;
    }
    
    
    boolean isBaseLineMode(){
        return !MBreplaceTrackingParam;
    }

    boolean isOverWriteCurrentRequestTrackigParam(){
        return !MBreplaceTrackingParam && isCurrentRequest();
    }
    
    void setWaitTimer(String msec){
        try{
            waittimer = Integer.parseInt(msec);//msec
            if(waittimer <= 0) waittimer = 0;
        }catch(Exception e){
            waittimer = 0;
        }
    }

    void startCurrentRequest(IHttpRequestResponse currentRequest){
        ParmVars.plog.clearComments();
        ParmVars.plog.setError(false);
        state = PMT_CURRENT_BEGIN;
        ParmGen pgen = new ParmGen(this);
        IHttpService iserv = currentRequest.getHttpService();
        String host = iserv.getHost();
        int port = iserv.getPort();
        boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
        ParmVars.plog.debuglog(0, "Current StepNo:" + stepno + " "+ host );
        byte[] retval = pgen.Run(host, port, isSSL, currentRequest.getRequest());
        if ( retval != null){
                currentRequest.setRequest(retval);

        }
    }
    
    
     //３）カレントリクエスト終了(レスポンス受信後)後に実行
    void endAfterCurrentRequest(PRequestResponse pqrs){
        if(rlist!=null && selected_request < rlist.size() && selected_request >=0 ){
            pqrs.setComments(ParmVars.plog.getComments());
            pqrs.setError(ParmVars.plog.isError());
            rlist.set(selected_request, pqrs);
            
            /*** REMOVE
            //カレントリクエストのset-cookie値をcookie.jarに保管
            
            List<String> setcookieheaders = pqrs.response.getSetCookieHeaders();
            for(String headerval: setcookieheaders) {
                String cheader = "Set-Cookie: " +  headerval;
                String domain = pqrs.request.getHost();
                String path = "/";//default root path

                
                //BurpICookie bicookie = new BurpICookie(domain, path, name, value, null);// update cookie
                //callbacks.updateCookieJar(bicookie);
                cookieMan.parse(domain, path, cheader);
            }
            * *****/
        }
        ui.updateCurrentReqRes();
        state = PMT_CURRENT_END;
    }

    void setCurrentRequest(int _p){
        if(rlist!=null&& rlist.size() > _p){
            selected_request = _p;
            EnableRequest(_p);//カレントリクエストは強制
            ParmVars.plog.debuglog(0, "selected_request:" + selected_request + " rlist.size=" + rlist.size());
        }
    }

    boolean isCurrentRequest(int _p){
        if(selected_request == _p){
            return true;
        }
        return false;
    }

    boolean isCurrentRequest(){
        return isCurrentRequest(stepno);
    }
    
    void EnableRequest(int _idx){
        if(rlist!=null&&rlist.size() > _idx){
            PRequestResponse prr = rlist.get(_idx);
            prr.Enable();
        }
    }
    void DisableRequest(int _idx){
        if(rlist!=null&&rlist.size() > _idx){
            PRequestResponse prr = rlist.get(_idx);
            prr.Disable();
        }
    }
    boolean isDisabledRequest(int _idx){
        if(rlist!=null&&rlist.size() > _idx){
            PRequestResponse prr = rlist.get(_idx);
            return prr.isDisabled();
        }
        return false;
    }

    boolean isError(int _idx){
        if(rlist!=null&&rlist.size() > _idx){
            PRequestResponse prr = rlist.get(_idx);
            return prr.isError();
        }
        return false;
    }

    int getRlistCount(){
        if(rlist==null) return 0;
        return rlist.size();
    }
    
    


    void updateOriginalRequest(int idx, PRequest _request){
        if(originalrlist!=null&&originalrlist.size()>0){
            PRequestResponse pqr = originalrlist.get(idx);
            pqr.updateRequest(_request);
            originalrlist.set(idx, pqr);
        }
    }
    
    PRequestResponse getOriginalRequest(int idx){
        if(originalrlist!=null&&originalrlist.size()>0&&idx>-1&&idx<originalrlist.size()){
            PRequestResponse pqr = originalrlist.get(idx);
            return pqr;
        }
        return null;
    }
    
    PRequestResponse getCurrentOriginalRequest(){
        return getOriginalRequest(getCurrentRequestPos());
    }
    
    //１）前処理マクロ開始
    void  startBeforePreMacro(){
        macroStarted();
        
        if(waittimer>0){
            TWaiter = new ParmGenTWait(waittimer);
        }else{
            TWaiter = null;
        }
        

        initFetchResponseVal();
        initCookieManager();

        
        if(!MBsettokencache){
            if(fetchResVal!=null){
                    fetchResVal.clearCachedLocVal();
            }
        }
        if(!MBCookieFromJar){
            if(cookieMan!=null){
                cookieMan.removeAll();
            }
        }
        if(fetchResVal!=null){
            fetchResVal.clearDistances();
        }
        state = PMT_PREMACRO_BEGIN;
        ParmVars.plog.debuglog(0, "BEGIN PreMacro");
        


            //1)synchronized preMacroLock	
            //単一のスレッドが running = trueにセット only one thread set running =true then preMacroLock method end.
            //2）2番目に実行したスレッドはpreMacroLock内でrunning = true時、wait(); second excuting thread wait,because running == true.	
            //	他のスレッドは、2番目スレッドが終了するまで、synchronizedのため、待機。 the other threads wait until second executing thread complete preMacroLock.
            //3）共有storeからスレッド毎のローカルのFetchResponseVal, Cookieストアを生成。	local FetchResponseVal, Cookie store create from shared store.

        
        //前処理マクロの0～selected_request-1まで実行。
        //開始時Cookieを参照しない。...cookie.jarから全削除
        //List<ICookie> iclist = callbacks.getCookieJarContents();//Burp's cookie.jar
        //if(!MBCookieFromJar){
        //    for(ICookie cookie:iclist){
        //        BurpICookie bicookie = new BurpICookie(cookie, true);// delete cookie.
        //        callbacks.updateCookieJar(bicookie);
        //   }
        //    iclist = callbacks.getCookieJarContents();
        //}
        oit = null;
        cit = null;

        stepno = 0;

        try{
            if(rlist!=null&&selected_request>=0&& rlist.size() > selected_request){
                oit = originalrlist.listIterator();
                cit = rlist.listIterator();
                int n = 0;
                if(TWaiter!=null){
                    TWaiter.TWait();
                }
                while(cit.hasNext() && oit.hasNext()){
                    PRequestResponse ppr = cit.next();
                    PRequestResponse opr = oit.next();
                    stepno = n;
                    if(n++>=selected_request){
                        break;
                    }

                    if(ppr.isDisabled()){
                        continue;
                    }

                    if(MBResetToOriginal){
                        ppr = opr;//オリジナルにリセット
                    }

                    //Set Cookie Value from CookieStore.
                    ppr.request.setCookiesFromCookieMan(cookieMan);
                    byte[] byterequest = ppr.request.getByteMessage();
                    if(byterequest!=null){
                        String noresponse = "";
                        String host = ppr.request.getHost();
                        int port = ppr.request.getPort();
                        boolean isSSL = ppr.request.isSSL();
                        Encode _pageenc = ppr.request.getPageEnc();
                        BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
                        ParmVars.plog.debuglog(0, "PreMacro StepNo:" + stepno + " "+ host + " " + ppr.request.method + " "+ ppr.request.url);
                        //byte[] byteres = callbacks.makeHttpRequest(host,port, isSSL, byterequest);
                        ParmVars.plog.clearComments();
                        ParmVars.plog.setError(false);
                        IHttpRequestResponse IHReqRes = callbacks.makeHttpRequest(bserv, byterequest);
                        byte[] bytereq = IHReqRes.getRequest();
                        byte[] byteres = IHReqRes.getResponse();
                        if(bytereq==null){//Impossible..
                            bytereq = new String("").getBytes(Encode.ISO_8859_1.getIANACharset());
                        }
                        if(byteres==null){
                            byteres = new String("").getBytes(Encode.ISO_8859_1.getIANACharset());
                            noresponse = "\nNo Response(NULL)";
                        }


                        PRequestResponse pqrs = new PRequestResponse(host, port, isSSL, bytereq, byteres, _pageenc);
                        pqrs.setComments(ParmVars.plog.getComments() + noresponse);
                        pqrs.setError(ParmVars.plog.isError());
                        cit.set(pqrs);//更新
                    }
                    if(TWaiter!=null){
                        TWaiter.TWait();
                    }
                }
            }
        }catch(Exception e){
            ParmVars.plog.printException(e);
        }
        ParmVars.plog.debuglog(0, "END PreMacro");
        state = PMT_PREMACRO_END;

    }

    

    PRequest configureRequest(PRequest preq){
       
        if(isRunning()){//MacroBuilder list > 0 && state is Running.
            //ここでリクエストのCookieをCookie.jarで更新する。
            //List<ICookie> iclist = callbacks.getCookieJarContents();//Burp's cookie.jar
            String domain_req = preq.getHost().toLowerCase();
            String path_req = preq.getPath();
            boolean isSSL_req = preq.isSSL();
            List<HttpCookie> cklist = cookieMan.get(domain_req, path_req, isSSL_req);
            HashMap<CookieKey, ArrayList<CookiePathValue>> cookiemap = new HashMap<CookieKey, ArrayList<CookiePathValue>>();
            //for(ICookie cookie:iclist){
            for(HttpCookie cookie: cklist){
                String domain = cookie.getDomain();
                if(domain==null||domain.isEmpty()){
                    domain = domain_req;
                }
                domain = domain.toLowerCase();
                if(!domain.equals(domain_req)){// domain:.test.com != domain_req:www.test.com
                    if(domain_req.endsWith(domain)){//domain_req is belong to domain's subdomain.
                        domain = domain_req;
                    }
                }
                String name = cookie.getName();
                if(name==null)name = "";
                String path = cookie.getPath();
                if(path == null)path = "";
                String value = cookie.getValue();
                if(value==null) value = "";
                CookieKey cikey = new CookieKey(domain, name);
                ParmVars.plog.debuglog(0, "Cookiekey domain:" + domain + " name=" + name);
                CookiePathValue cpvalue = new CookiePathValue(path, value);
                ArrayList<CookiePathValue> cpvlist = cookiemap.get(cikey);
                if(cpvlist==null){
                    cpvlist = new ArrayList<CookiePathValue>();
                }

                cpvlist.add(cpvalue);

                cookiemap.put(cikey, cpvlist);
            }

            boolean ReplaceCookieflg = true;
            if(isCurrentRequest()){
                ReplaceCookieflg = MBreplaceCookie;
            }

            if(preq.setCookies(cookiemap, ReplaceCookieflg)){
                return preq;
            }
        }
       
        return null;
    }


    //４）後処理マクロの開始
    void  startPostMacro(){
        state = PMT_POSTMACRO_BEGIN;
        postmacro_RequestResponse = null;
        if(isMBFinalResponse()){
            //後処理マクロ　selected_request+1 ～最後まで実行。
            stepno = selected_request + 1;
            ParmVars.plog.debuglog(0, "BEGIN PostMacro");
            try{
                if(cit!=null&&oit!=null){
                    //List<ICookie> iclist = callbacks.getCookieJarContents();//Burp's cookie.jar
                    int n = stepno;
                    while(cit.hasNext() && oit.hasNext()){
                        stepno = n;
                        if(TWaiter!=null){
                            TWaiter.TWait();
                        }
                        n++;

                        PRequestResponse ppr = cit.next();
                        PRequestResponse opr = oit.next();
                        if(ppr.isDisabled()){
                            continue;
                        }
                        postmacro_RequestResponse = null;
                        if(MBResetToOriginal){
                            ppr = opr;
                        }

                        byte[] byterequest = ppr.request.getByteMessage();
                        if(byterequest!=null){
                            String host = ppr.request.getHost();
                            int port = ppr.request.getPort();
                            boolean isSSL = ppr.request.isSSL();
                            Encode _pageenc = ppr.request.getPageEnc();
                            BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
                            ParmVars.plog.debuglog(0, "PostMacro StepNo:" + stepno + " "+ host + " " + ppr.request.method + " "+ ppr.request.url);
                            //byte[] byteres = callbacks.makeHttpRequest(host,port, isSSL, byterequest);
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
                            pqrs.setComments(ParmVars.plog.getComments());
                            pqrs.setError(ParmVars.plog.isError());
                            cit.set(pqrs);//更新

                        }
                    }
                }
            } catch (Exception ex) {
              ParmVars.plog.printException(ex);
            }
        }
        cit = null;
        if(postmacro_RequestResponse!=null){
            state = PMT_POSTMACRO_END;
        }else{
            state = PMT_POSTMACRO_NULL;
        }
        ParmVars.plog.debuglog(0, "END PostMacro");
    }

    IHttpService getIHttpService() {
        if(postmacro_RequestResponse!=null){
            return postmacro_RequestResponse.getHttpService();
        }
        return null;
    }

    byte[] getPostMacroRequest(){
        if(postmacro_RequestResponse!=null){
            return postmacro_RequestResponse.getRequest();
        }
        return null;
    }

    byte[] getPostMacroResponse(){
        if(postmacro_RequestResponse!=null){
            return postmacro_RequestResponse.getResponse();
        }
       return null;
    }

    int getCurrentRequestPos(){
        return selected_request;
    }

   boolean isRunning(){
       if(rlist!=null&&rlist.size()>0)return state<PMT_POSTMACRO_END?true:false;
       return false;
   }

   boolean CurrentRequestIsTrackFromTarget(AppParmsIni pini){
       int FromStepNo = pini.getTrackFromStep();
       if(FromStepNo<0){
           return true;
       }else if(FromStepNo==stepno){
           return true;
       }
       return false;
   }
   
   boolean CurrentRequestIsSetToTarget(AppParmsIni pini){
       int ToStepNo = pini.getSetToStep();
       int ToStepBase = ParmVars.TOSTEPANY;
       
       if(ToStepNo==ToStepBase){
           return true;
       }else if(ToStepNo==stepno){
           return true;
       }
       //ParmVars.plog.debuglog(0, "!!!!!!!!!!!!!!!!! failed CurrentRequestIsSetToTarget: stepno=" + stepno + " ToStepNo=" + ToStepNo + " ToStepBase=" + ToStepBase) ;
       return false;
   }
   
   void setRecords(ArrayList <PRequestResponse> _rlist){
        //rlist = new ArrayList <PRequestResponse> (_rlist);//copy
        if(rlist==null){
            rlist = _rlist;//reference共有
            originalrlist = new ArrayList <PRequestResponse>(_rlist);//copy
        }else{
            originalrlist.addAll(new ArrayList <PRequestResponse>(_rlist));
        }
        ParmVars.plog.debuglog(0, "setRecords:" + rlist.size() + "/" + originalrlist.size());
        
   }
   
   
   void ParseResponse(){
       /*** REMOVE
	   if(rlist!=null){
	       cit = rlist.listIterator();
	       //csrflist = new ArrayList<ParmGenParser> ();
	       set_cookienames = new ArrayList<String>();
	       HashMap<String,String> uniquecookies = new HashMap<String, String>();
	       while(cit.hasNext()){
	           PRequestResponse prr = cit.next();

	           //ParmGenParser pgparser = new ParmGenParser(prr.response.getBody(), "[type=\"hidden\"],[type=\"HIDDEN\"]");

	           //csrflist.add(pgparser);
	           HashMap<String,ArrayList<String[]>> setcookieparams = prr.response.set_cookieparams;
	            for(Map.Entry<String, ArrayList<String[]>> e : setcookieparams.entrySet()) {
	                String k = e.getKey();
	                ArrayList<String[]> values = e.getValue();
	                String name = null;
	                for(String[] s: values){
	                    if(s[0].equals(k)){
	                        name = s[0];
	                        uniquecookies.put(name, s[1]);//name, value
	                    }
	                }

	            }
	       }
	       for(Map.Entry<String,String> e: uniquecookies.entrySet()){
	           String name = e.getKey();
	           set_cookienames.add(name);
	           ParmVars.plog.debuglog(0, "ParseResponse: Set-Cookie: " + name);
	       }
	   }
***********/

   }

   void macroStarted(){
       ParmVars.plog.debuglog(0, "<--Macro Started.-->");
       macroLock();
   }
   
   void macroEnded(boolean clrtidlist){
       nullState();
       macroUnLock();
       if(clrtidlist){
           tidlist = null;
       }
       ParmVars.plog.debuglog(0, "<--Macro Complete Ended.-->");
   }
   
   public synchronized void macroLock() {
       long tid = Thread.currentThread().getId();
       if(tidlist==null){
           tidlist = new ArrayDeque<>();
       }
       tidlist.add(tid);
        ParmVars.plog.debuglog(0, "macroLock Start. tid=" + tid);
        while (locked == true) {
            try {
                ParmVars.plog.debuglog(0, "macroLock wait In.  tid=" + tid);
                wait();  //availableがtrueの間、wait
                ParmVars.plog.debuglog(0, "macroLock wait Out. tid=" + tid);
            } catch (InterruptedException e) {
            }
        }
        //workAreaに値をセットする処理
        locked = true;
        
        ParmVars.plog.debuglog(0, "macroLock  Done. tid=" + tid);
        
    }
   
   public synchronized void macroUnLock(){
       //lockedにfalseを代入した後wait状態のスレッドを解除
       locked = false;
       notifyAll();
       Long tid = new Long(-1);
       try{
           if(tidlist!=null){
                tid = tidlist.remove();
           }
       }catch(Exception e){
           
       }
       ParmVars.plog.debuglog(0, "macroUnLock Done. tid=" + tid);
   }
   
   private void nullState(){
       state = PMT_POSTMACRO_NULL;
       stepno = -1;
   }
   
   void setToolBaseLine(PRequestResponse _baseline){
       
       toolbaseline = _baseline;
   }

   //
   // getter
   //
   int getState(){
        return state;
    }

    ArrayList <PRequestResponse> getRecords(){
        return rlist;
    }

    

    boolean isMBFinalResponse(){
        return MBFinalResponse;
    }

    int getStepNo(){
        return stepno;
    }
    
    PRequestResponse getToolBaseline(){
        return toolbaseline;
    }

    void sendToRepeater(int pos){
    	if(rlist!=null&&rlist.size()>0){
            PRequestResponse pqr = rlist.get(pos);
            if(MBResetToOriginal){
                pqr = originalrlist.get(pos);
            }
            if(pqr!=null){
                setToolBaseLine(pqr);
                String host = pqr.request.getHost();
                int port = pqr.request.getPort();
                boolean useHttps = pqr.request.isSSL();
                callbacks.sendToRepeater(
                    host,
                    port,
                    useHttps,
                    pqr.request.getByteMessage(),
                    "MacroBuilder:" + pos);
            }
    	}

    }
    void sendToScanner(int pos){
    	if(rlist!=null&&rlist.size()>0){
	    PRequestResponse pqr = rlist.get(pos);
            if(MBResetToOriginal){
                pqr = originalrlist.get(pos);
            }
            if(pqr!=null){
                setToolBaseLine(null);
                String host = pqr.request.getHost();
                int port = pqr.request.getPort();
                boolean useHttps = pqr.request.isSSL();
                scanque =callbacks.doActiveScan(
                    host,
                    port,
                    useHttps,
                    pqr.request.getByteMessage()
                );
            }
    	}

    }
    
    int getScanQuePercentage(){
        if(scanque!=null){
            byte b = scanque.getPercentageComplete();
            return b & 0xff;
        }
        return -1;
    }
    
    void sendToIntruder(int pos){
    	if(rlist!=null&&rlist.size()>0){
	    PRequestResponse pqr = rlist.get(pos);
            if(MBResetToOriginal){
                pqr = originalrlist.get(pos);
            }
            if(pqr!=null){
                setToolBaseLine(null);
                String host = pqr.request.getHost();
                int port = pqr.request.getPort();
                boolean useHttps = pqr.request.isSSL();
                callbacks.sendToIntruder(
                    host,
                    port,
                    useHttps,
                    pqr.request.getByteMessage()
                    );
            }
    	}

    }
    
    void JSONSave(JsonObjectBuilder builder){
        if(builder!=null){
            if(originalrlist!=null){
                builder.add("CurrentRequest" , getCurrentRequestPos());
                JsonArrayBuilder Request_List =Json.createArrayBuilder();
                JsonObjectBuilder Request_rec = Json.createObjectBuilder();
                for(PRequestResponse pqr: originalrlist){
                    byte[] qbin = pqr.request.getByteMessage();
                    byte[] rbin = pqr.response.getByteMessage();
                    //byte[] encodedBytes = Base64.encodeBase64(qbin);
                    String qbase64 =Base64.getEncoder().encodeToString(qbin);// same as new String(encode(src), StandardCharsets.ISO_8859_1)
                    /*
                    try {
                        qbase64 = new String(encodedBytes,"ISO-8859-1");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ParmGenMacroTrace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    */
                    //encodedBytes = Base64.encodeBase64(rbin);
                    String rbase64 = Base64.getEncoder().encodeToString(rbin);
                    /*
                    try {
                        rbase64 = new String(encodedBytes, "ISO-8859-1");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ParmGenMacroTrace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    */
                    Request_rec.add("PRequest", qbase64);
                    Request_rec.add("PResponse", rbase64);
                    String host = pqr.request.getHost();
                    int port = pqr.request.getPort();
                    boolean ssl = pqr.request.isSSL();
                    String comments = pqr.getComments();
                    boolean isdisabled = pqr.isDisabled();
                    boolean iserror = pqr.isError();
                    Request_rec.add("Host", host);
                    Request_rec.add("Port", port);
                    Request_rec.add("SSL", ssl);
                    Request_rec.add("Comments", comments==null?"":comments);
                    Request_rec.add("Disabled", isdisabled);
                    Request_rec.add("Error", iserror);
                    Request_List.add(Request_rec);
                    
                }
                builder.add("PRequestResponse", Request_List);
            }
        }
    }
    
    void GSONSave(GSONSaveObject gsonsaveobj){
        if(gsonsaveobj!=null){
            if(originalrlist!=null){
                gsonsaveobj.CurrentRequest = getCurrentRequestPos();
                

                for(PRequestResponse pqr: originalrlist){
                    PRequestResponses preqresobj = new PRequestResponses();
                    byte[] qbin = pqr.request.getByteMessage();
                    byte[] rbin = pqr.response.getByteMessage();
                    //byte[] encodedBytes = Base64.encodeBase64(qbin);
                    String qbase64 =Base64.getEncoder().encodeToString(qbin);// same as new String(encode(src), StandardCharsets.ISO_8859_1)
                    /*
                    try {
                        qbase64 = new String(encodedBytes,"ISO-8859-1");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ParmGenMacroTrace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    */
                    //encodedBytes = Base64.encodeBase64(rbin);
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
                    preqresobj.Comments = comments==null?"":comments;
                    preqresobj.Disabled = isdisabled;
                    preqresobj.Error = iserror;
                    
                    gsonsaveobj.PRequestResponse.add(preqresobj);
                }
                
            }
        }
    }
    
    public void initFetchResponseVal(){
        if(fetchResVal==null){
            fetchResVal = new FetchResponseVal();
        }
    }
    
    public FetchResponseVal getFetchResponseVal(){
        return fetchResVal;
    }
    
    public void nullfetchResValAndCookieMan(){
        fetchResVal = null;
        cookieMan = null;
    }
    
    public void initCookieManager(){
        if(cookieMan==null){
            cookieMan = new ParmGenCookieManager();
        }
    }
    
    public ArrayList<PRequestResponse> getOriginalrlist(){
        return originalrlist;
    }
    
    void parseSetCookie(PRequestResponse pqrs){
        //カレントリクエストのset-cookie値をcookie.jarに保管
            
            List<String> setcookieheaders = pqrs.response.getSetCookieHeaders();
            for(String headerval: setcookieheaders) {
                String cheader = "Set-Cookie: " +  headerval;
                String domain = pqrs.request.getHost();
                String path = "/";//default root path

                
                //BurpICookie bicookie = new BurpICookie(domain, path, name, value, null);// update cookie
                //callbacks.updateCookieJar(bicookie);
                cookieMan.parse(domain, path, cheader);
            }
    }
}
