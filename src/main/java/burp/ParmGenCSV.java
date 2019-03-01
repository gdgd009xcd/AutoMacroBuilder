/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

import java.util.List;


/**
 *
 * @author daike
 */
public class ParmGenCSV {
    ParmGenMacroTrace pmt = null;
    List<AppParmsIni> records;
    Iterator<AppParmsIni> it;
    ParmGenWriteFile pfile;
    public static final String JSONVERSION = "1.0";
    public static ArrayList<PRequestResponse> selected_messages;
    public static ArrayList<PRequestResponse> proxy_messages;

    ParmGenCSV(ParmGenMacroTrace _pmt, ArrayList<PRequestResponse> _selected_messages){
       reloadParmGen(_pmt, null);
       selected_messages = new ArrayList<PRequestResponse>();
       proxy_messages = _selected_messages;
       selected_messages.add(proxy_messages.get(0));
       pfile = null;

    }
    
    ParmGenCSV( List<AppParmsIni> _newparmcsv,ParmGenMacroTrace _pmt){
        reloadParmGen(_pmt, _newparmcsv);
        pfile = null;
    }

    public void reloadParmGen(ParmGenMacroTrace _pmt, List<AppParmsIni>_newparmcsv){
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
    
    public void add(String URL, String initval, String valtype, String incval, ArrayList<AppValue> apps){
        int rowcnt = records.size();
        if(rowcnt>0){
                rowcnt = records.get(records.size()-1).getRow() + 1;
        }
        records.add(new AppParmsIni(URL, initval, valtype, incval, apps, rowcnt));
    }

    public void add(AppParmsIni pini){
        int rowcnt = records.size();
        if(rowcnt>0){
                rowcnt = records.get(records.size()-1).getRow() + 1;
        }
        pini.setRowAndCntFile(rowcnt);
        records.add(pini);
    }

    public void mod(int i, String URL, String initval, String valtype, String incval, ArrayList<AppValue> apps){
        int rowcnt = records.get(i).getRow();
        records.set(i, new AppParmsIni(URL, initval, valtype, incval, apps, rowcnt));
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

    public void save(){//NOP...
        //ファイル初期化
        try{
            pfile = new ParmGenWriteFile(ParmVars.parmfile);
        }catch(Exception ex){
            ParmVars.plog.printException(ex);
            return;
        }

        pfile.truncate();
        String scopelist = new String();
        if(ParmGen.ProxyInScope){
            scopelist = "1:";
        }else{
            scopelist = "0:";
        }
        if(ParmGen.IntruderInScope){
            scopelist += "1:";
        }else{
            scopelist += "0:";
        }
        if(ParmGen.RepeaterInScope){
            scopelist += "1:";
        }else{
            scopelist += "0:";
        }
        if(ParmGen.ScannerInScope){
            scopelist += "1";
        }else{
            scopelist += "0";
        }


        pfile.print("LANG," + ParmVars.enc.getIANACharset() + "," + scopelist);
        //pfile.print("");

        Iterator<AppParmsIni> it = records.iterator();
        while(it.hasNext()){
            AppParmsIni prec = it.next();
            //String URL, String initval, String valtype, String incval, ArrayList<ParmGenParam> parms
            Iterator<AppValue> pt = prec.parmlist.iterator();
            String paramStr = "";
            while(pt.hasNext()){
                AppValue param = pt.next();
                if (!paramStr.isEmpty()){
                    paramStr += ",";
                }
                paramStr += QUOTE(param.getValPart() + (param.isEnabled()?"":"-") + (param.isNoCount()?"":"+") +
                        (param.csvpos == -1?"":(":" +Integer.toString(param.csvpos)))
                        , true) ;
                if (prec.typeval != AppParmsIni.T_TRACK){
                        paramStr += QUOTE(escapeDelimiters(param.getVal(), null), false);
                }else{
                    paramStr += QUOTE(escapeDelimiters(param.getVal(), null), true) +
                        QUOTE(param.getresURL(), true) +
                        QUOTE(escapeDelimiters(param.getresRegex(), null), true) +
                        QUOTE(param.getResValPart(), true) +
                        QUOTE(Integer.toString(param.resRegexPos), true)+
                            QUOTE(param.token, true) +
                            QUOTE(param.urlencode==true?"true":"false", false);
                }


            }
            pfile.print(QUOTE(prec.getUrl(), true) +
                    QUOTE(Integer.toString(prec.len), true) +
                    QUOTE(prec.getTypeVal(), true) +
                    (prec.typeval==AppParmsIni.T_CSV?QUOTE(escapeDelimiters(prec.frl.getFileName(), "UTF-8"), true):QUOTE(Integer.toString(prec.inival), true)) +
                    paramStr
                    );
            //pfile.print("");
        }

        pfile.close();
        pfile = null;

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
