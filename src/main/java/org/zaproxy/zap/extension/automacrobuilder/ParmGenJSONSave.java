/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import java.util.List;
import org.zaproxy.zap.extension.automacrobuilder.GSONSaveObject.AppParmsIni_List;
import org.zaproxy.zap.extension.automacrobuilder.GSONSaveObject.AppValue_List;


/**
 *
 * @author daike
 */
public class ParmGenJSONSave {
    private static org.apache.logging.log4j.Logger logger4j = org.apache.logging.log4j.LogManager.getLogger();
    ParmGenMacroTrace pmt = null;
    private List<AppParmsIni> records;
    Iterator<AppParmsIni> it;
    ParmGenWriteFile pfile;
    public static final String JSONVERSION = "1.1";//OUTPUT JSON VERSION
    public static ArrayList<PRequestResponse> selected_messages;
    public static ArrayList<PRequestResponse> proxy_messages;

    public ParmGenJSONSave(ParmGenMacroTrace _pmt, ArrayList<PRequestResponse> _selected_messages){
       saveParmGenSetUp(_pmt, null);
       selected_messages = new ArrayList<PRequestResponse>();
       proxy_messages = _selected_messages;
       selected_messages.add(proxy_messages.get(0));
       pfile = null;

    }
    
    public ParmGenJSONSave( List<AppParmsIni> _newparmcsv,ParmGenMacroTrace _pmt){
        saveParmGenSetUp(_pmt, _newparmcsv);
        pfile = null;
    }

    private void saveParmGenSetUp(ParmGenMacroTrace _pmt, List<AppParmsIni>_newparmcsv){
       pmt = _pmt;
       ParmGen pgen = new ParmGen(_pmt, _newparmcsv);
       records = ParmGen.parmcsv;
       logger4j.debug("records is " + (records==null?"null": "No null"));
       if (records==null){
           records = new ArrayList<AppParmsIni>();
       }
       rewindAppParmsIni();
    }

    public void setParms(ArrayList<AppParmsIni> _records){
        records = _records;//reference
    }



    public List<AppParmsIni> getrecords(){
        return records;
    }
    
    

    public void add(AppParmsIni pini){
        
        records.add(pini);
    }


    public void mod(int i, AppParmsIni pini){
        records.set(i, pini);
    }

    public void del(int i){
        records.remove(i);
    }

    private String escapeDelimiters(String _d, String code) {
        //String _dd = _d.replaceAll("\\\\", "\\\\");
        String _dd = _d;
        //String _ddd = _dd.replaceAll("\"", "\"\"");
        String encoded = _d;
        try{
            if(code==null){
                code = ParmVars.enc.getIANACharsetName();
            }
            if(_dd!=null){
                encoded = URLEncoder.encode(_dd, code);
            }
        }catch(UnsupportedEncodingException e){
            ParmVars.plog.printException(e);
            encoded = _dd;
        }
        return encoded;
    }

    private String QUOTE(String val, boolean comma){
        return "\"" + (val==null?"":val) + "\"" + ( comma ? "," : "" );
    }

    
    public void GSONsave(){
        //ファイル初期化
    	ParmVars.plog.debuglog(0, "gsonsave called.");
        try{
            FileInfo finfo = new FileInfo(ParmVars.parmfile);
            pfile = new ParmGenWriteFile(finfo.getFullFileName());
        }catch(Exception ex){
            ParmVars.plog.printException(ex);
            return;
        }

        
        
        


        pfile.truncate();
        
        GSONSaveObject gsobject = new GSONSaveObject();
        
        gsobject.VERSION = JSONVERSION;
        gsobject.LANG = ParmVars.enc.getIANACharsetName();
        gsobject.ProxyInScope = ParmGen.ProxyInScope;
        gsobject.IntruderInScope = ParmGen.IntruderInScope;
        gsobject.RepeaterInScope = ParmGen.RepeaterInScope;
        gsobject.ScannerInScope = ParmGen.ScannerInScope;
        
        

        //excludeMimeTypelist
        //
        // { "ExcludeMimeTypes" : ["image/.*", "application/json"],
        //
        
                
        ParmVars.ExcludeMimeTypes.forEach((mtype) -> {
            gsobject.ExcludeMimeTypes.add(mtype);
        });


        Iterator<AppParmsIni> it = records.iterator();
        while(it.hasNext()){
            AppParmsIni prec = it.next();
            //String URL, String initval, String valtype, String incval, ArrayList<ParmGenParam> parms
            AppParmsIni_List AppParmsIni_ListObj = new AppParmsIni_List();
            AppParmsIni_ListObj.URL = prec.getUrl();
            AppParmsIni_ListObj.len = prec.len;
            AppParmsIni_ListObj.typeval = prec.typeval;
            AppParmsIni_ListObj.inival = prec.inival;
            AppParmsIni_ListObj.maxval = prec.maxval;
            AppParmsIni_ListObj.csvname = (prec.typeval==AppParmsIni.T_CSV?escapeDelimiters(prec.frl.getFileName(), "UTF-8"):"");
            AppParmsIni_ListObj.pause = prec.pause;
            AppParmsIni_ListObj.TrackFromStep = prec.getTrackFromStep();
            AppParmsIni_ListObj.SetToStep = prec.getSetToStep();
            AppParmsIni_ListObj.relativecntfilename = prec.getRelativeCntFileName();

            Iterator<AppValue> pt = prec.parmlist.iterator();
            
            while(pt.hasNext()){
                AppValue param = pt.next();
                AppValue_List AppValue_ListObj = new AppValue_List();
                AppValue_ListObj.valpart = param.getValPart();
                AppValue_ListObj.isEnabled = param.isEnabled();
                AppValue_ListObj.isNoCount = param.isNoCount();
                AppValue_ListObj.csvpos = param.csvpos;
                AppValue_ListObj.value = escapeDelimiters(param.getVal(), null);
                AppValue_ListObj.resURL = param.getresURL()==null?"":param.getresURL();
                AppValue_ListObj.resRegex = (escapeDelimiters(param.getresRegex(), null)==null?"":escapeDelimiters(param.getresRegex(), null));
                AppValue_ListObj.resValpart = param.getResValPart();
                AppValue_ListObj.resRegexPos = param.resRegexPos;
                AppValue_ListObj.token = param.token==null?"":param.token;
                AppValue_ListObj.urlencode = param.urlencode;
                AppValue_ListObj.fromStepNo = param.fromStepNo;
                AppValue_ListObj.toStepNo = param.toStepNo;
                AppValue_ListObj.TokenType = param.tokentype.name();
                
                AppParmsIni_ListObj.AppValue_List.add(AppValue_ListObj);

            }

            gsobject.AppParmsIni_List.add(AppParmsIni_ListObj);

        }

        
        
        //save Macros
        if(pmt!=null){
            pmt.GSONSave(gsobject);
        }
        
        PrintWriter pw = pfile.getPrintWriter();
        
        GsonBuilder gbuilder = new GsonBuilder();
        gbuilder.setPrettyPrinting();
        String prettygson = gbuilder.create().toJson(gsobject);
        pw.print(prettygson);
        

        //String jsonData = stWriter.toString();

        //pfile.print(jsonData);

        pfile.close();
        pfile = null;
        ParmVars.Saved();    
    }

    public AppParmsIni getAppParmsIni(int i){
        if ( records.size() > i){
            return records.get(i);
        }
        return null;
    }

    public void rewindAppParmsIni(){
        it = records.iterator();
    }

    public AppParmsIni getNextAppParmsIni(){
        if(it.hasNext()){
            return it.next();
        }
        return null;
    }

    public int sizeAppParmsIni(){
        return records.size();
    }

}
