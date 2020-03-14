/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import burp.GSONSaveObject.AppParmsIni_List;
import burp.GSONSaveObject.AppValue_List;
import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* 20200314 deleted
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

*/
import java.util.List;


/**
 *
 * @author daike
 */
public class ParmGenJSONSave {
    ParmGenMacroTrace pmt = null;
    private List<AppParmsIni> records;
    Iterator<AppParmsIni> it;
    ParmGenWriteFile pfile;
    public static final String JSONVERSION = "1.1";//OUTPUT JSON VERSION
    public static ArrayList<PRequestResponse> selected_messages;
    public static ArrayList<PRequestResponse> proxy_messages;

    ParmGenJSONSave(ParmGenMacroTrace _pmt, ArrayList<PRequestResponse> _selected_messages){
       saveParmGenSetUp(_pmt, null);
       selected_messages = new ArrayList<PRequestResponse>();
       proxy_messages = _selected_messages;
       selected_messages.add(proxy_messages.get(0));
       pfile = null;

    }
    
    ParmGenJSONSave( List<AppParmsIni> _newparmcsv,ParmGenMacroTrace _pmt){
        saveParmGenSetUp(_pmt, _newparmcsv);
        pfile = null;
    }

    private void saveParmGenSetUp(ParmGenMacroTrace _pmt, List<AppParmsIni>_newparmcsv){
       pmt = _pmt;
       ParmGen pgen = new ParmGen(_pmt, _newparmcsv);
       records = pgen.parmcsv;
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
    
    /*public void add(String URL, String initval, String valtype, String incval, ArrayList<AppValue> apps){
        int rowcnt = records.size();
        if(rowcnt>0){
                rowcnt = records.get(records.size()-1).getRow() + 1;
        }
        records.add(new AppParmsIni( URL, initval, valtype, incval, apps, rowcnt));
    }*/

    public void add(AppParmsIni pini){
        /*
        int rowcnt = records.size();
        if(rowcnt>0){
                rowcnt = records.get(records.size()-1).getRow() + 1;
        }
        pini.setRow(rowcnt);//add new record.
        */
        records.add(pini);
    }

    /*
    public void mod(int i, String URL, String initval, String valtype, String incval, ArrayList<AppValue> apps){
        int rowcnt = records.get(i).getRow();
        records.set(i, new AppParmsIni(URL, initval, valtype, incval, apps, rowcnt));
    }
*/

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
                code = ParmVars.enc.getIANACharset();
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

    /* 20200314 deleted.
    public void jsonsave(){
        //ファイル初期化
    	ParmVars.plog.debuglog(0, "jsonsave called.");
        try{
            pfile = new ParmGenWriteFile(ParmVars.parmfile);
        }catch(Exception ex){
            ParmVars.plog.printException(ex);
            return;
        }

        
        
        //JSON pretty print
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);


        pfile.truncate();
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("VERSION", JSONVERSION);
        
        builder.add("LANG", ParmVars.enc.getIANACharset());

        if(ParmGen.ProxyInScope){
            builder.add("ProxyInScope", true);
        }else{
            builder.add("ProxyInScope", false);
        }
        if(ParmGen.IntruderInScope){
             builder.add("IntruderInScope", true);
        }else{
             builder.add("IntruderInScope", false);
        }
        if(ParmGen.RepeaterInScope){
            builder.add("RepeaterInScope", true);
        }else{
            builder.add("RepeaterInScope", false);
        }
        if(ParmGen.ScannerInScope){
            builder.add("ScannerInScope", true);
        }else{
            builder.add("ScannerInScope", false);
        }

        //excludeMimeTypelist
        //
        // { "ExcludeMimeTypes" : ["image/.*", "application/json"],
        //
        
        JsonArrayBuilder ExcludeMimeTypes_List = Json.createArrayBuilder();
        
        ParmVars.ExcludeMimeTypes.forEach((mtype) -> {
            ExcludeMimeTypes_List.add(mtype);
        });
        
        
        builder.add("ExcludeMimeTypes", ExcludeMimeTypes_List);
        
        JsonArrayBuilder AppParmsIni_List =Json.createArrayBuilder();



        Iterator<AppParmsIni> it = records.iterator();
        while(it.hasNext()){
            AppParmsIni prec = it.next();
            //String URL, String initval, String valtype, String incval, ArrayList<ParmGenParam> parms
            JsonObjectBuilder AppParmsIni_prec = Json.createObjectBuilder();
            AppParmsIni_prec.add("URL", prec.getUrl());
            AppParmsIni_prec.add("len", prec.len);
            AppParmsIni_prec.add("typeval", prec.typeval);
            AppParmsIni_prec.add("inival", prec.inival);
            AppParmsIni_prec.add("maxval", prec.maxval);
            AppParmsIni_prec.add("csvname", prec.typeval==AppParmsIni.T_CSV?escapeDelimiters(prec.frl.getFileName(), "UTF-8"):"");
            AppParmsIni_prec.add("pause", prec.pause);
            AppParmsIni_prec.add("TrackFromStep", prec.getTrackFromStep());
            AppParmsIni_prec.add("SetToStep", prec.getSetToStep());
            AppParmsIni_prec.add("relativecntfilename", prec.getRelativeCntFileName());

            JsonArrayBuilder AppValue_List =Json.createArrayBuilder();

            Iterator<AppValue> pt = prec.parmlist.iterator();
            String paramStr = "";
            while(pt.hasNext()){
                AppValue param = pt.next();
                JsonObjectBuilder AppValue_rec = Json.createObjectBuilder();

                AppValue_rec.add("valpart", param.getValPart());
                AppValue_rec.add("isEnabled", param.isEnabled());
                AppValue_rec.add("isNoCount", param.isNoCount());
                AppValue_rec.add("csvpos", param.csvpos);
                AppValue_rec.add("value", escapeDelimiters(param.getVal(), null));
                AppValue_rec.add("resURL", param.getresURL()==null?"":param.getresURL());
                AppValue_rec.add("resRegex", (escapeDelimiters(param.getresRegex(), null)==null?"":escapeDelimiters(param.getresRegex(), null)));
                AppValue_rec.add("resValpart", param.getResValPart());
                AppValue_rec.add("resRegexPos", param.resRegexPos);
                AppValue_rec.add("token", param.token==null?"":param.token);
                AppValue_rec.add("urlencode", param.urlencode);
                AppValue_rec.add("fromStepNo", param.fromStepNo);
                AppValue_rec.add("toStepNo", param.toStepNo);
                AppValue_rec.add("TokenType", param.tokentype.name());
                AppValue_List.add(AppValue_rec);
            }

            AppParmsIni_prec.add("AppValue_List", AppValue_List);

            AppParmsIni_List.add(AppParmsIni_prec);


        }

        builder.add("AppParmsIni_List", AppParmsIni_List);
        
        //save Macros
        if(pmt!=null){
            pmt.JSONSave(builder);
        }
        
        JsonObject model = builder.build();

        //StringWriter stWriter = new StringWriter();
        //JsonWriter jsonWriter = Json.createWriter(stWriter);
        JsonWriter jsonWriter = Json.createWriterFactory(properties).createWriter(pfile.getPrintWriter());
        jsonWriter.writeObject(model);
        jsonWriter.close();

        //String jsonData = stWriter.toString();

        //pfile.print(jsonData);

        pfile.close();
        pfile = null;
        ParmVars.Saved();
    }
*/
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
        gsobject.LANG = ParmVars.enc.getIANACharset();
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
