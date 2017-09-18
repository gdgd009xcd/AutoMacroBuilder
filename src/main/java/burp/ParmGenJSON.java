/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.stream.JsonParser;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import flex.messaging.util.URLDecoder;

/**
 *
 * @author chikara_1.daike
 */
public class ParmGenJSON {
    ArrayList<AppParmsIni> rlist;
    ArrayList<PRequestResponse> ReqResList;
    AppParmsIni aparms;
    AppValue apv;
    String exerr = null;
    int row = 0;
    int currentrequest;
    //PRequestResponse params
    String PRequest64;
    String PResponse64;
    String Host;
    int Port;
    boolean SSL;
    String Comments;
    boolean Disabled;
    boolean Error;




    ParmGenJSON(){

    	rlist = new ArrayList<AppParmsIni>();
        aparms = null;
        apv = null;
        ReqResList = new ArrayList<PRequestResponse>();
        currentrequest = 0;
        row = 0;
        exerr = null;
        initReqRes();

    }

    private void initReqRes(){
        PRequest64 = null;
        PResponse64 = null;
        Host = null;
        Port = 0;
        SSL = false;
        Comments = "";
        Disabled = false;
        Error = false;
    }

    ArrayList<AppParmsIni> Getrlist(){
        return rlist;
    }

    ArrayList<PRequestResponse> GetMacroRequests(){
        return ReqResList;
    }
    int getCurrentRequest(){
        return currentrequest;
    }

    private String GetString(JsonParser.Event ev, Object value, String defval){
        String v = "";
        switch(ev){
            case VALUE_STRING:
                try{
                    v =  (String)value;
                }catch(Exception e){
                    v ="";
                }
                break;
            default:
                v = defval;
                break;

        }
        return v;
    }

    private int GetNumber(JsonParser.Event ev, Object value, int defval){
        int i = 0;
        switch(ev){
            case VALUE_NUMBER:
                try{
                    String vstring = (String)value;
                    i =  (int)Integer.parseInt(vstring);
                }catch(Exception e){
                    i = 0;
                }
                break;
            default:
                i = defval;
                break;

        }
        return i;
    }

    private boolean Getboolean(JsonParser.Event ev, Object value, boolean defval){
        boolean b = false;
        switch(ev){
            case VALUE_FALSE:
                b = false;
                break;
            case VALUE_TRUE:
                b = true;
                break;
            default:
                b = defval;
                break;
        }

        return b;
    }

    boolean Parse(ParmGenStack<String> astack, int alevel, JsonParser.Event ev, String name, Object value ){
        String current = astack.getCurrent();
        switch(alevel){
            case 0:
                if(name.toUpperCase().equals("LANG")){
                    ParmVars.enc = Encode.getEnum(GetString(ev, value, "UTF-8"));
                }else if(name.toUpperCase().equals("PROXYINSCOPE")){
                    ParmGen.ProxyInScope = Getboolean(ev, value, false);
                }else if(name.toUpperCase().equals("INTRUDERINSCOPE")){
                    ParmGen.IntruderInScope = Getboolean(ev,value, true);
                }else if(name.toUpperCase().equals("REPEATERINSCOPE")){
                    ParmGen.RepeaterInScope = Getboolean(ev, value, true);
                }else if(name.toUpperCase().equals("SCANNERINSCOPE")){
                    ParmGen.ScannerInScope = Getboolean(ev, value, true);
                }else if(name.toUpperCase().equals("CURRENTREQUEST")){
                    currentrequest = GetNumber(ev, value,0);
                }
                break;
            case 1:
                switch(ev){
                    case START_OBJECT:
                        if(current!=null&&current.toUpperCase().equals("APPPARMSINI_LIST")){
                            //ParmVars.plog.debuglog(0, "START_OBJECT level1 name:" + current);
                            aparms = new AppParmsIni();
                            aparms.parmlist = new ArrayList<AppValue>();
                        }else if(current!=null&&current.toUpperCase().equals("PREQUESTRESPONSE")){
                            initReqRes();
                        }
                        break;
                    case END_OBJECT:
                        if(exerr==null){
                            if(current!=null&&current.toUpperCase().equals("APPPARMSINI_LIST")){
                                if(aparms!=null&&rlist!=null){
                                    if(aparms.getType()==AppParmsIni.T_CSV){
                                        String decodedname = "";
                                        try{
                                                decodedname = URLDecoder.decode(aparms.csvname, "UTF-8");
                                        }catch(Exception e){
                                                ParmVars.plog.printException(e);
                                                exerr = e.getMessage();
                                        }
                                        aparms.frl = new FileReadLine(decodedname, true);
                                    }

                                    aparms.setRowAndCntFile(row);row++;
                                    aparms.crtGenFormat(true);
                                    rlist.add(aparms);
                                }
                                aparms = null;
                            }else if(current!=null&&current.toUpperCase().equals("PREQUESTRESPONSE")){
                                if(PRequest64!=null){
                                    byte[] binreq = Base64.decode(PRequest64);
                                    byte[] binres = Base64.decode(PResponse64);

                                    String res = "";
                                    try {
                                        res = new String(binres,ParmVars.enc.getIANACharset());
                                    } catch (UnsupportedEncodingException ex) {
                                        Logger.getLogger(ParmGenJSON.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    PRequestResponse pqr = new PRequestResponse(Host, Port, SSL, binreq, res);
                                    if(Disabled){
                                        pqr.Disable();
                                    }
                                    pqr.setComments(Comments);
                                    pqr.setError(Error);
                                    ReqResList.add(pqr);
                                    initReqRes();
                                }
                            }
                        }

                        break;
                    default:
                        if(aparms!=null){
                            if(name.toUpperCase().equals("URL")){
                                aparms.setUrl(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("LEN")){
                                aparms.len = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("TYPEVAL")){
                                aparms.typeval = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("INIVAL")){
                                aparms.inival = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("MAXVAL")){
                                aparms.maxval = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("CSVNAME")){
                                aparms.csvname = GetString(ev, value, "");
                            }else if(name.toUpperCase().equals("PAUSE")){
                                aparms.pause = Getboolean(ev,value, false);
                            }
                        }else if(current!=null&&current.toUpperCase().equals("PREQUESTRESPONSE")){
                            if(name.toUpperCase().equals("PREQUEST")){
                                PRequest64 = GetString(ev, value, "");
                            }else if(name.toUpperCase().equals("PRESPONSE")){
                                PResponse64 = GetString(ev,value, "");
                            }else if(name.toUpperCase().equals("HOST")){
                                Host = GetString(ev,value, "");
                            }else if(name.toUpperCase().equals("PORT")){
                                Port = GetNumber(ev,value, 0);
                            }else if(name.toUpperCase().equals("SSL")){
                                SSL = Getboolean(ev,value, false);
                            }else if(name.toUpperCase().equals("COMMENTS")){
                                Comments = GetString(ev,value, "");
                            }else if(name.toUpperCase().equals("DISABLED")){
                                Disabled = Getboolean(ev,value, false);
                            }else if(name.toUpperCase().equals("ERROR")){
                                Error = Getboolean(ev,value, false);
                            }
                        }
                        break;
                }

                break;
            case 2:
                switch(ev){
                    case START_OBJECT:
                        if(current!=null&&current.toUpperCase().equals("APPVALUE_LIST")){
                            //ParmVars.plog.debuglog(0, "START_OBJECT level2 name:" + current);
                            apv = new AppValue();
                        }
                        break;
                    case END_OBJECT:
                        if(exerr==null){
                            if(apv!=null&&aparms!=null){
                                apv.col = aparms.parmlist.size();
                                aparms.parmlist.add(apv);
                            }
                        }
                        apv = null;
                        break;
                    default:
                        if(apv!=null){
                            if(name.toUpperCase().equals("VALPART")){
                                apv.setValPart(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("ISMODIFY")){
                                if(Getboolean(ev, value, true)==false){
                                    apv.setEnabled(false);
                                }
                            }else if(name.toUpperCase().equals("ISENABLED")){
                                if(Getboolean(ev, value, true)==false){
                                    apv.setEnabled(false);
                                }
                            }else if(name.toUpperCase().equals("ISNOCOUNT")){
                                if(Getboolean(ev, value, true)==true){
                                    apv.setNoCount();
                                }else{
                                    apv.clearNoCount();
                                }
                            }else if(name.toUpperCase().equals("CSVPOS")){
                                apv.csvpos = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("VALUE")){
                                exerr = apv.setURLencodedVal(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("RESURL")){
                                apv.setresURL(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("RESREGEX")){
                                apv.setresRegexURLencoded(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("RESVALPART")){
                                apv.setresPartType(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("RESREGEXPOS")){
                                apv.resRegexPos = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("TOKEN")){
                                apv.token = GetString(ev, value, "");
                            }else if(name.toUpperCase().equals("URLENCODE")){
                                apv.urlencode = Getboolean(ev, value, false);
                            }else if(name.toUpperCase().equals("FROMSTEPNO")){
                                apv.fromStepNo = GetNumber(ev, value, -1);
                            }else if(name.toUpperCase().equals("TOSTEPNO")){
                                apv.toStepNo = GetNumber(ev, value, 0);
                            }else if(name.toUpperCase().equals("TOKENTYPE")){
                                apv.setTokenTypeName(GetString(ev, value, ""));
                            }else if(name.toUpperCase().equals("RESENCODETYPE")){
                            	apv.setResEncodeType(GetString(ev, value, ""));
                            }
                        }
                        break;
                }
                break;
            default:
                break;
        }

        if(exerr==null){
            return true;
        }else{
            ParmVars.plog.printError("ParmGenJSON::Parse " + exerr);
        }
        return false;

    }
}
