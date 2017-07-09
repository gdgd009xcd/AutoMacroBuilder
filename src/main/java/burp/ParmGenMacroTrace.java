/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author daike
 */
public class ParmGenMacroTrace {

    MacroBuilderUI ui = null;
    IBurpExtenderCallbacks callbacks;

    ArrayList <PRequestResponse> rlist = null;//マクロ実行後の全リクエストレスポンス
    ArrayList <PRequestResponse> originalrlist = null; //オリジナルリクエストレスポンス
    //ArrayList <ParmGenParser> csrflist = null;//引き継ぎhidden値リスト
    ArrayList<String> set_cookienames = null;//レスポンスのSet-Cookie値の名前リスト
    int selected_request = 0;//現在選択しているカレントのリクエスト
    int stepno = -1;//実行中のリクエスト番号
    boolean MBExec = false;//==true実行中
    boolean MBCookieUpdate = false;//==true Cookie更新
    boolean MBCookieFromJar = false;//==true 開始時Cookie.jarから引き継ぐ
    boolean MBFinalResponse = false;//==true 結果は最後に実行されたマクロのレスポンス
    boolean MBResetToOriginal =false;//==true オリジナルリクエストを実行。
    boolean MBdeletesetcookies = false;//==true リクエストからSet-Cookie値全削除
    boolean MBcleartokencache = false;//開始時tokenキャッシュクリア
    int waittimer = 1;//実行間隔(msec)

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

    ParmGenMacroTrace(IBurpExtenderCallbacks _callbacks){
        callbacks = _callbacks;
    }


    //
    // setter
    //
    void clear(){
    	rlist = null;
    	originalrlist = null;
    	set_cookienames = null;
    	selected_request = 0;
    	stepno = -1;
    	oit = null;
    	cit = null;
    	pit = null;
    	postmacro_RequestResponse = null;
    }

    void setUI(MacroBuilderUI _ui){
        ui = _ui;
    }

    void setMBExec(boolean b){
        MBExec = b;
    }
    void setMBCookieUpdate(boolean b){
        MBCookieUpdate= b;
    }
    void setMBCookieFromJar(boolean b){
        MBCookieFromJar = b;
    }
     void setMBdeletesetcookies(boolean b){
        MBdeletesetcookies = b;
    }
    void setMBFinalResponse(boolean b){
        MBFinalResponse = b;
    }
    void setMBResetToOriginal(boolean b){
        MBResetToOriginal = b;
    }

    void setMBcleartokencache(boolean b){
        MBcleartokencache = b;
    }

    void setWaitTimer(String msec){
        try{
            waittimer = Integer.parseInt(msec);//msec
            if(waittimer <= 0) waittimer = 0;
        }catch(Exception e){
            waittimer = 0;
        }
    }

    void startCurrentRequest(){
        ParmVars.plog.clearComments();
        ParmVars.plog.setError(false);
        state = PMT_CURRENT_BEGIN;
    }
     //３）カレントリクエスト終了(レスポンス受信後)後に実行
    void endAfterCurrentRequest(PRequestResponse pqrs){
        if(rlist!=null && selected_request < rlist.size() && selected_request >=0 ){
            pqrs.setComments(ParmVars.plog.getComments());
            pqrs.setError(ParmVars.plog.isError());
            rlist.set(selected_request, pqrs);
            //カレントリクエストのset-cookie値をcookie.jarに保管
            HashMap<String,ArrayList<String[]>> setcookieparams = pqrs.response.set_cookieparams;
            for(Map.Entry<String, ArrayList<String[]>> e : setcookieparams.entrySet()) {
                String k = e.getKey();
                ArrayList<String[]> values = e.getValue();
                String domain = null;
                String path = null;
                String name = null;
                String value = null;
                for(String[] s: values){
                    if(s[0].equals(k)){
                        name = s[0];
                        value = s[1];
                    }else if(s[0].toLowerCase().equals("path")){
                        path = s[1];
                    }else if(s[0].toLowerCase().equals("domain")){
                        domain = s[1];
                    }
                }
                if(domain==null){
                   domain = pqrs.request.getHost();
                }
                if(path==null){
                    path = "/";//root path
                }
                ParmVars.plog.debuglog(0, "Set-Cookie: " +  name + "=" + value + "; domain=" +  domain +  "; path=" + path);
                BurpICookie bicookie = new BurpICookie(domain, path, name, value, null);// delete cookie.
                callbacks.updateCookieJar(bicookie);

            }
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

    synchronized void TWait(){
        if(waittimer>0){
            ParmVars.plog.debuglog(0, "....sleep Start:" + waittimer + "(msec)");
            try{
                wait(waittimer);
            }catch(Exception e){
                ParmVars.plog.debuglog(0, "....sleep Exception..");
            }
            ParmVars.plog.debuglog(0, "....sleep End.");
        }
    }


    //１）前処理マクロ開始
    void  startBeforePreMacro(){
        if(!MBcleartokencache){
            if(FetchResponse.loc!=null){
                    FetchResponse.loc.clearCachedLocVal();
            }
        }
        state = PMT_PREMACRO_BEGIN;
        ParmVars.plog.debuglog(0, "BEGIN PreMacro");
        //前処理マクロの0～selected_request-1まで実行。
        //開始時Cookieを参照しない。...cookie.jarから全削除
        List<ICookie> iclist = callbacks.getCookieJarContents();//Burp's cookie.jar
        if(!MBCookieFromJar){
            for(ICookie cookie:iclist){
                BurpICookie bicookie = new BurpICookie(cookie, true);// delete cookie.
                callbacks.updateCookieJar(bicookie);
            }
            iclist = callbacks.getCookieJarContents();
        }
        oit = null;
        cit = null;

        stepno = 0;

        try{
        if(rlist!=null&&selected_request>=0&& rlist.size() > selected_request){
            oit = originalrlist.listIterator();
            cit = rlist.listIterator();
            int n = 0;
            TWait();
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


                if(MBCookieUpdate){
                    for(ICookie cookie:iclist){
                        String cname = cookie.getName();
                        String cvalue = cookie.getValue();
                        ppr.request.setCookie(cname, cvalue);//cookie.jarから更新
                    }
                }

                byte[] byterequest = ppr.request.getByteMessage();
                if(byterequest!=null){
                    String host = ppr.request.getHost();
                    int port = ppr.request.getPort();
                    boolean isSSL = ppr.request.isSSL();
                    BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
                    ParmVars.plog.debuglog(0, "Request PreMacro:" + stepno + " "+ host + " " + ppr.request.method + " "+ ppr.request.url);
                    //byte[] byteres = callbacks.makeHttpRequest(host,port, isSSL, byterequest);
                    ParmVars.plog.clearComments();
                    ParmVars.plog.setError(false);
                    IHttpRequestResponse IHReqRes = callbacks.makeHttpRequest(bserv, byterequest);
                    byte[] bytereq = IHReqRes.getRequest();
                    byte[] byteres = IHReqRes.getResponse();
                    if(byteres!=null&&byteres.length>0){
                        String res = new String(byteres, ParmVars.enc.getIANACharset());
                        PResponse ppres = new PResponse(res);
                        ParmVars.plog.debuglog(0, "Response PreMacro:" + ppres.status);
                    }
                    PRequestResponse pqrs = new PRequestResponse(new String(bytereq, ParmVars.enc.getIANACharset()),new String(byteres, ParmVars.enc.getIANACharset()));
                    pqrs.setComments(ParmVars.plog.getComments());
                    pqrs.setError(ParmVars.plog.isError());
                    cit.set(pqrs);//更新
                }
                TWait();
            }
        }
        }catch(Exception e){
            ParmVars.plog.printException(e);
        }
        ParmVars.plog.debuglog(0, "END PreMacro");
        state = PMT_PREMACRO_END;

    }

    boolean isconfigurable(){
        if(MBdeletesetcookies&&set_cookienames!=null&&set_cookienames.size()>0){
            return true;
        }
        return false;
    }

    PRequest configureRequest(PRequest preq){
        ArrayList<String> delcookies = null;
        if(MBdeletesetcookies){
                    delcookies = new ArrayList<String>(set_cookienames);//copy contents
                    List<ICookie> iclist = callbacks.getCookieJarContents();//Burp's cookie.jar
                    // 値がdeleted, 0 のゴミcookie削除
                     for(ICookie cookie:iclist){
                        String cname = cookie.getName();
                        String cvalue = cookie.getValue();

                        String dcookiename = null;
                        if(cvalue!=null){
                            if(cvalue.toLowerCase().indexOf("delete")!=-1){//xxxx=delete or deleted
                                dcookiename = cname;
                            }else if(cvalue.equals("0")){// xxxx=0
                                dcookiename = cname;
                            }
                        }
                        if(dcookiename!=null){
                            if(delcookies==null){
                                delcookies = new ArrayList<String>();
                            }
                            delcookies.add(dcookiename);
                        }else if(delcookies!=null&&delcookies.size()>0){
                            while(delcookies.remove(cname)){;//cookie.jarに値があるので削除しない
                                //ParmVars.plog.debuglog(0, "configreq remove from delcookies : Cookie "+ cname + "=" + cvalue);
                            }
                        }
                    }
        }

        if(delcookies!=null&&delcookies.size()>0){
            if(preq.removeCookies(delcookies)){
                return preq;
            }
        }
        return null;
    }




    //４）後処理マクロの開始
    void  startPostMacro(){
        state = PMT_POSTMACRO_BEGIN;
        postmacro_RequestResponse = null;
        //後処理マクロ　selected_request+1 ～最後まで実行。
        stepno = selected_request + 1;
        ParmVars.plog.debuglog(0, "BEGIN PostMacro");
        try{
            if(cit!=null&&oit!=null){
                List<ICookie> iclist = callbacks.getCookieJarContents();//Burp's cookie.jar
                int n = stepno;
                while(cit.hasNext() && oit.hasNext()){
                    stepno = n;
                    TWait();
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
                    if(MBCookieUpdate){
                            for(ICookie cookie:iclist){
                                String cname = cookie.getName();
                                String cvalue = cookie.getValue();
                                ParmVars.plog.debuglog(0, "Cookie: " + cname + "=" + cvalue);
                                ppr.request.setCookie(cname, cvalue);//cookie.jarから更新
                            }
                    }

                    byte[] byterequest = ppr.request.getByteMessage();
                    if(byterequest!=null){
                        String host = ppr.request.getHost();
                        int port = ppr.request.getPort();
                        boolean isSSL = ppr.request.isSSL();
                        BurpIHttpService bserv = new BurpIHttpService(host, port, isSSL);
                        ParmVars.plog.debuglog(0, "Request PostMacro:" + stepno + " "+ host + " " + ppr.request.method + " "+ ppr.request.url);
                        //byte[] byteres = callbacks.makeHttpRequest(host,port, isSSL, byterequest);
                        ParmVars.plog.clearComments();
                        ParmVars.plog.setError(false);
                        postmacro_RequestResponse = callbacks.makeHttpRequest(bserv, byterequest);
                        byte[] bytereq = postmacro_RequestResponse.getRequest();
                        byte[] byteres = postmacro_RequestResponse.getResponse();
                        if(postmacro_RequestResponse!=null){

                            if(byteres.length>0){
                                String res;
                                res = new String(byteres, ParmVars.enc.getIANACharset());
                                PResponse ppres = new PResponse(res);
                                ParmVars.plog.debuglog(0, "Response PostMacro: " + ppres.status);
                            }
                        }
                        PRequestResponse pqrs = new PRequestResponse(new String(bytereq, ParmVars.enc.getIANACharset()),new String(byteres, ParmVars.enc.getIANACharset()));
                        pqrs.setComments(ParmVars.plog.getComments());
                        pqrs.setError(ParmVars.plog.isError());
                        cit.set(pqrs);//更新

                    }
                }
            }
          } catch (Exception ex) {
            ParmVars.plog.printException(ex);
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

    int getCurrentRequest(){
        return selected_request;
    }

   boolean isRunning(){
       return state<PMT_POSTMACRO_END?true:false;
   }

   void setRecords(ArrayList <PRequestResponse> _rlist){
       //rlist = new ArrayList <PRequestResponse> (_rlist);//copy
       rlist = _rlist;//reference
       originalrlist = new ArrayList <PRequestResponse>(_rlist);//copy
   }

   void ParseResponse(){
	   if(rlist!=null){
	       cit = rlist.listIterator();
	       //csrflist = new ArrayList<ParmGenParser> ();
	       set_cookienames = new ArrayList<String>();
	       HashMap<String,String> uniquecookies = new HashMap<String, String>();
	       while(cit.hasNext()){
	           PRequestResponse prr = cit.next();
	           ParmVars.plog.debuglog(0, "body lenght=" + prr.response.getBodyLength());
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


   }

   void nullState(){
       state = PMT_POSTMACRO_NULL;
       stepno = -1;
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

    /**ParmGenParser getParmGenParser(int _p){
        return csrflist.get(_p);
    }**/

    boolean isMBFinalResponse(){
        return MBFinalResponse;
    }

    int getStepNo(){
        return stepno;
    }

    void sendToRepeater(int pos){
    	if(rlist!=null&&rlist.size()>0){
            PRequestResponse pqr = rlist.get(pos);
            if(MBResetToOriginal){
                pqr = originalrlist.get(pos);
            }
	        if(pqr!=null){
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
	        	String host = pqr.request.getHost();
	        	int port = pqr.request.getPort();
	        	boolean useHttps = pqr.request.isSSL();
	        	IScanQueueItem que =callbacks.doActiveScan(
	                    host,
	                    port,
	                    useHttps,
	                    pqr.request.getByteMessage()
	            );
	        }
    	}

    }
    void sendToIntruder(int pos){
    	if(rlist!=null&&rlist.size()>0){
	    PRequestResponse pqr = rlist.get(pos);
            if(MBResetToOriginal){
                pqr = originalrlist.get(pos);
            }
	        if(pqr!=null){
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
}
