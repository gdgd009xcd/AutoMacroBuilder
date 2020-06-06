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

import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.zaproxy.zap.extension.automacrobuilder.generated.ParmGenTop;

// main class
public class ParmGen {

    public static List<AppParmsIni> parmcsv = null;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("burp/Bundle");

    private static org.apache.logging.log4j.Logger logger4j =
            org.apache.logging.log4j.LogManager.getLogger();

    public static ParmGenTop twin = null;
    public static boolean ProxyInScope = false;
    public static boolean IntruderInScope = true;
    public static boolean RepeaterInScope = true;
    public static boolean ScannerInScope = true;
    ParmGenMacroTrace pmt;

    public void disposeTop() {
        if (twin != null) {
            twin.dispose();
        }
        twin = null;
    }

    /**
     * load JSON file
     *
     * @param filename
     * @return
     */
    private ArrayList<AppParmsIni> loadGSON(String filename) {
        //
        List<Exception> exlist = new ArrayList<>(); // Exception list
        logger4j.info("loadGSON called.");

        ArrayList<AppParmsIni> rlist = null;
        String pfile = filename;

        try {

            String rdata;
            String jsondata = new String("");
            FileReader fr = new FileReader(pfile);
            try {

                BufferedReader br = new BufferedReader(fr);
                while ((rdata = br.readLine()) != null) {
                    jsondata += rdata;
                } // end of while((rdata = br.readLine()) != null)
                fr.close();
                fr = null;
            } catch (Exception e) {
                logger4j.error("File Open/RW error", e);
                exlist.add(e);
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                        fr = null;
                    } catch (Exception e) {
                        fr = null;
                        logger4j.error("File Close error", e);
                        exlist.add(e);
                    }
                }
            }

            if (exlist.size() > 0) return null;

            GsonParser parser = new GsonParser();

            ParmGenGSON gjson = new ParmGenGSON();
            JsonElement element = com.google.gson.JsonParser.parseString(jsondata);

            if (parser.elementLoopParser(element, gjson)) {
                rlist = gjson.Getrlist();
                pmt.ui.clear();
                pmt.ui.addNewRequests(gjson.GetMacroRequests());
                int creq = gjson.getCurrentRequest();
                pmt.setCurrentRequest(creq);
                ParmVars.parmfile = filename;
                ParmVars.Version = gjson.getVersion();
                ParmVars.enc = gjson.getEncode();
                ParmVars.setExcludeMimeTypes(gjson.getExcludeMimeTypes());

                pmt.ui.Redraw();
                ParmVars.Saved();
            } else { // JSON parse failed by something wrong syntax/value..
                rlist = null;
            }
        } catch (Exception e) { // JSON file load failed.
            logger4j.error("Parse error", e);
            exlist.add(e);
            rlist = null;
        }

        logger4j.info("---------AppPermGen JSON load END ----------");
        return rlist;
    }

    PRequest ParseRequest(
            PRequest prequest,
            PRequest org_request,
            ParmGenBinUtil boundaryarray,
            ParmGenBinUtil _contarray,
            AppParmsIni pini,
            AppValue av,
            ParmGenHashMap errorhash) {

        //	String[] headers=request.getHeaderNames();
        //	boolean noauth = false;
        //	for(String header : headers){
        //		if ( header.indexOf("Authorization")==-1){
        //			noauth = true;
        //		}
        //		//printlog(header+" : " + request.getHeader(header), true);
        //	}

        // if(av.toStepNo>0&&av.toStepNo!=pmt.getStepNo())return null;
        if (av.getToStepNo() != ParmVars.TOSTEPANY) {
            if (av.getToStepNo() != pmt.getStepNo()) return null;
        }
        // ArrayList<String []> headers = prequest.getHeaders();

        String method = prequest.getMethod();
        String url = prequest.getURL();
        String path = new String(url);
        String orig_url = null;
        String orig_path = null;
        String orig_query = null;
        ParmGenBinUtil org_contarray = null;
        String org_content_iso8859 = null;

        if (org_request != null) {
            orig_url = org_request.getURL();
            int o_qpos = -1;
            if ((o_qpos = orig_url.indexOf('?')) != -1) {
                orig_path = url.substring(0, o_qpos);
                orig_query = orig_url.substring(o_qpos + 1);
            }
            org_contarray = org_request.getBinBody();
            org_content_iso8859 = org_request.getISO8859BodyString();
        }
        ParmVars.plog.debuglog(1, "method[" + method + "] request[" + url + "]");
        int qpos = -1;
        String[] nvcont = null;
        switch (av.getTypeInt()) {
                // switch(av.valparttype & AppValue.C_VTYPE){
            case AppValue.V_PATH: // path
                // path = url
                nvcont = av.replaceContents(pmt, pmt.getStepNo(), pini, path, orig_url, errorhash);
                if (nvcont != null) {
                    String n_path = nvcont[0];
                    String o_path = nvcont[1];
                    if (n_path != null && !path.equals(n_path)) {
                        url = n_path;
                        ParmVars.plog.debuglog(1, " Original path[" + path + "]");
                        ParmVars.plog.debuglog(1, " Modified path[" + n_path + "]");
                        // request.setURL(new HttpUrl(url));
                        prequest.setURL(url);
                        if (org_request != null
                                && o_path != null
                                && pmt.getToolBaseline() != null) {
                            org_request.setURL(o_path);
                        }
                        return prequest;
                    }
                }
                break;
            case AppValue.V_QUERY: // query
                if ((qpos = url.indexOf('?')) != -1) {
                    path = url.substring(0, qpos);
                    String query = url.substring(qpos + 1);
                    nvcont =
                            av.replaceContents(
                                    pmt, pmt.getStepNo(), pini, query, orig_query, errorhash);

                    if (nvcont != null) {
                        String n_query = nvcont[0];
                        String o_query = nvcont[1];
                        if (n_query != null && !query.equals(n_query)) {
                            url = path + '?' + n_query;
                            ParmVars.plog.debuglog(1, " Original query[" + query + "]");
                            ParmVars.plog.debuglog(1, " Modified path[" + n_query + "]");
                            // request.setURL(new HttpUrl(url));
                            prequest.setURL(url);
                            if (org_request != null
                                    && orig_path != null
                                    && o_query != null
                                    && pmt.getToolBaseline() != null) {
                                String o_url = orig_path + "?" + o_query;
                                org_request.setURL(o_url);
                            }
                            return prequest;
                        }
                    }
                }
                break;
            case AppValue.V_HEADER: // header
                // String[] headers=request.getHeaderNames();
                // for(String header : headers){
                // int i = 0;

                HashMap<String, ParmGenHeader> headers = prequest.getheadersHash();

                for (Map.Entry<String, ParmGenHeader> ent : headers.entrySet()) {
                    String hKeyUpperV = ent.getKey();
                    ParmGenHeader pgheader = ent.getValue();
                    ListIterator<ParmGenBeen> hit = pgheader.getValuesIter();
                    ParmGenHeader org_pgheader = null;
                    ListIterator<ParmGenBeen> oit = null;
                    if (org_request != null) {
                        org_pgheader = org_request.getParmGenHeader(hKeyUpperV);
                        if (org_pgheader != null) {
                            oit = org_pgheader.getValuesIter();
                        }
                    }
                    while (hit.hasNext()) {
                        ParmGenBeen been = hit.next();
                        String[] nv = prequest.getHeaderNV(been.i);
                        if (nv != null) {
                            String hval = nv[0] + ": " + nv[1]; // Cookie: value
                            String orig_hval = null;
                            ParmGenBeen o_been = null;
                            String[] onv = null;
                            if (oit != null && oit.hasNext()) {
                                o_been = oit.next();
                                onv = org_request.getHeaderNV(o_been.i);
                                orig_hval = onv[0] + ": " + onv[1]; // Cookie: value
                            }
                            nvcont =
                                    av.replaceContents(
                                            pmt, pmt.getStepNo(), pini, hval, orig_hval, errorhash);
                            if (nvcont != null) {
                                String n_hval = nvcont[0];
                                String o_hval = nvcont[1];
                                if (n_hval != null && !hval.equals(n_hval)) {
                                    ParmVars.plog.debuglog(1, " Original header[" + hval + "]");
                                    ParmVars.plog.debuglog(1, " Modified header[" + n_hval + "]");
                                    String htitle = nv[0] + ": ";
                                    n_hval = n_hval.substring(htitle.length());
                                    prequest.setHeader(been.i, nv[0], n_hval);
                                    if (org_request != null
                                            && o_been != null
                                            && onv != null
                                            && o_hval != null
                                            && pmt.getToolBaseline() != null) {
                                        o_hval = o_hval.substring(htitle.length());
                                        org_request.setHeader(o_been.i, onv[0], o_hval);
                                    }
                                    return prequest;
                                }
                            }
                        }
                    }
                }

                break;
            default: // body
                if (_contarray != null) {
                    if (boundaryarray == null) { // www-url-encoded
                        ParmVars.plog.debuglog(1, "application/x-www-form-urlencoded");
                        String content = null;
                        try {
                            content =
                                    new String(
                                            _contarray.getBytes(),
                                            ParmVars.enc.getIANACharsetName());
                        } catch (UnsupportedEncodingException e) {
                            content = null;
                        }
                        nvcont =
                                av.replaceContents(
                                        pmt,
                                        pmt.getStepNo(),
                                        pini,
                                        content,
                                        org_content_iso8859,
                                        errorhash);
                        if (nvcont != null) {
                            String n_content = nvcont[0];
                            String neworg_content_iso8859 = nvcont[1];

                            if (n_content != null && !content.equals(n_content)) {
                                ParmVars.plog.debuglog(1, " Original body[" + content + "]");
                                ParmVars.plog.debuglog(1, " Modified body[" + n_content + "]");
                                try {
                                    _contarray.initParmGenBinUtil(
                                            n_content.getBytes(ParmVars.enc.getIANACharsetName()));
                                } catch (UnsupportedEncodingException ex) {
                                    Logger.getLogger(ParmGen.class.getName())
                                            .log(Level.SEVERE, null, ex);
                                    _contarray.initParmGenBinUtil(n_content.getBytes());
                                }
                                if (org_request != null
                                        && org_content_iso8859 != null
                                        && neworg_content_iso8859 != null
                                        && pmt.getToolBaseline() != null) {
                                    try { // bodyの入れ替え
                                        org_request.setBody(
                                                neworg_content_iso8859.getBytes(
                                                        Encode.ISO_8859_1.getIANACharsetName()));
                                        byte[] bmessage = org_request.getByteMessage();
                                        String host = org_request.getHost();
                                        int port = org_request.getPort();
                                        boolean ssl = org_request.isSSL();
                                        org_request.construct(
                                                host, port, ssl, bmessage, ParmVars.enc);
                                    } catch (UnsupportedEncodingException ex) {
                                        Logger.getLogger(ParmGen.class.getName())
                                                .log(Level.SEVERE, null, ex);
                                    }
                                }
                                return prequest;
                            }
                        }
                    } else { // multipart/form-data
                        ParmVars.plog.debuglog(1, "multipart/form-data");
                        ParmGenBinUtil n_array = new ParmGenBinUtil();
                        int cpos = 0;
                        int npos = -1;
                        byte[] partdata = null;
                        boolean partupdt = false;
                        byte[] headerseparator = {0x0d, 0x0a, 0x0d, 0x0a}; // <CR><LF><CR><LF>
                        byte[] partheader = null;
                        String partenc = "";
                        String neworg_content_iso8859 = null;
                        boolean org_content_isupdated = false;
                        while ((npos = _contarray.indexOf(boundaryarray.getBytes(), cpos)) != -1) {
                            if (cpos != 0) { // cpos->npos == partdata
                                partdata = _contarray.subBytes(cpos, npos);
                                // マルチパート内のヘッダーまで(CRLFCRLF)読み込み、Content-typeを判定
                                int hend = _contarray.indexOf(headerseparator, cpos);
                                partheader = _contarray.subBytes(cpos, hend);
                                String partcontenttype = null;
                                try {
                                    partcontenttype = new String(partheader, ParmVars.formdataenc);
                                } catch (UnsupportedEncodingException ex) {
                                    partcontenttype = "";
                                }
                                int ctypestart = 0;
                                partenc = ParmVars.enc.getIANACharsetName();
                                if ((ctypestart = partcontenttype.indexOf("Content-Type:")) != -1) {
                                    String cstr =
                                            partcontenttype.substring(
                                                    ctypestart + "Content-Type:".length());
                                    String[] cstrvalues = cstr.split("[\r\n;]+");
                                    if (cstrvalues.length > 0) {
                                        String partcontenttypevalue = cstrvalues[0];
                                        if (!partcontenttypevalue.isEmpty()) {
                                            partenc = ParmVars.formdataenc;
                                            partcontenttypevalue = partcontenttypevalue.trim();
                                            ParmVars.plog.printlog(
                                                    "form-data Contentype:["
                                                            + partcontenttypevalue
                                                            + "]",
                                                    true);
                                        }
                                    }
                                }
                                String partdatastr = null;
                                try {
                                    partdatastr = new String(partdata, partenc);
                                } catch (UnsupportedEncodingException e) {
                                    partdatastr = null;
                                }
                                nvcont =
                                        av.replaceContents(
                                                pmt,
                                                pmt.getStepNo(),
                                                pini,
                                                partdatastr,
                                                org_content_iso8859,
                                                errorhash);
                                if (nvcont != null) {
                                    String n_partdatastr = nvcont[0];
                                    neworg_content_iso8859 = nvcont[1];

                                    if (n_partdatastr != null
                                            && partdatastr != null
                                            && !partdatastr.equals(n_partdatastr)) {
                                        ParmVars.plog.debuglog(
                                                1, " Original body[" + partdatastr + "]");
                                        ParmVars.plog.debuglog(
                                                1, " Modified body[" + n_partdatastr + "]");
                                        try {
                                            n_array.concat(n_partdatastr.getBytes(partenc));
                                        } catch (UnsupportedEncodingException e) {
                                            ParmVars.plog.printException(e);
                                            n_array.concat(n_partdatastr.getBytes());
                                        }
                                        if (org_request != null
                                                && org_content_iso8859 != null
                                                && neworg_content_iso8859 != null) {
                                            org_content_iso8859 = neworg_content_iso8859;
                                            org_content_isupdated = true;
                                        }
                                        partupdt = true;
                                    } else {
                                        n_array.concat(partdata);
                                    }
                                } else {
                                    n_array.concat(partdata);
                                }
                                int nextcpos = npos + boundaryarray.length() + 2;
                                n_array.concat(_contarray.subBytes(npos, nextcpos));
                                String lasthyphon =
                                        new String(_contarray.subBytes(nextcpos - 2, nextcpos));
                                if (lasthyphon.equals("--")) {
                                    n_array.concat("\r\n".getBytes()); // last hyphon "--" + CRLF
                                }
                                cpos = nextcpos;
                            } else {
                                cpos = npos + boundaryarray.length() + 2;
                                n_array.concat(_contarray.subBytes(0, cpos));
                            }
                        }

                        if (partupdt) {
                            // _contarray = n_array;
                            _contarray.initParmGenBinUtil(n_array.getBytes());
                            if (org_content_isupdated) {
                                if (org_request != null
                                        && org_content_iso8859 != null
                                        && pmt.getToolBaseline() != null) {
                                    try { // bodyの入れ替え
                                        org_request.setBody(
                                                org_content_iso8859.getBytes(
                                                        Encode.ISO_8859_1.getIANACharsetName()));
                                        byte[] bmessage = org_request.getByteMessage();
                                        String host = org_request.getHost();
                                        int port = org_request.getPort();
                                        boolean ssl = org_request.isSSL();
                                        org_request.construct(
                                                host, port, ssl, bmessage, ParmVars.enc);
                                    } catch (UnsupportedEncodingException ex) {
                                        Logger.getLogger(ParmGen.class.getName())
                                                .log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            return prequest;
                        }
                    }
                }

                break;
        }

        return null;
    }

    boolean FetchRequest(PRequest prequest, AppParmsIni pini, AppValue av, int r, int c) {
        if (av.getFromStepNo() < 0 || av.getFromStepNo() == pmt.getStepNo()) {
            String url = prequest.getURL();
            int row, col;
            row = r;
            col = c;
            switch (av.getResTypeInt()) {
                case AppValue.V_REQTRACKBODY:
                    return pmt.getFetchResponseVal()
                            .reqbodymatch(
                                    av,
                                    pmt.getStepNo(),
                                    av.getFromStepNo(),
                                    url,
                                    prequest,
                                    row,
                                    col,
                                    true,
                                    av.getResRegexPos(),
                                    av.getToken());
                default:
                    break;
            }
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    boolean ParseResponse(
            String url, PResponse presponse, AppParmsIni pini, AppValue av, int r, int c) {

        int row, col;
        row = r;
        col = c;
        boolean rflag = false;
        boolean autotrack = false;
        String rowcolstr = Integer.toString(row) + "," + Integer.toString(col);
        // String path = new String(url);
        if (av.getFromStepNo() < 0 || av.getFromStepNo() == pmt.getStepNo()) {
            int qpos = -1;
            switch (av.getResTypeInt()) {
                    // switch(av.resPartType & AppValue.C_VTYPE){
                case AppValue.V_PATH: // path
                    // ParmVars.plog.debuglog(0, "ParseResponse: V_PATH " + rowcolstr);
                    break;
                case AppValue.V_QUERY: // query
                    // ParmVars.plog.debuglog(0, "ParseResponse: V_QUERY " + rowcolstr);
                    break;
                case AppValue.V_HEADER: // header
                    // ParmVars.plog.debuglog(0, "ParseResponse: V_HEADER " + rowcolstr);
                    // String[] headers=request.getHeaderNames();
                    // for(String header : headers){
                    rflag =
                            pmt.getFetchResponseVal()
                                    .headermatch(
                                            pmt.getStepNo(),
                                            av.getFromStepNo(),
                                            url,
                                            presponse,
                                            row,
                                            col,
                                            true,
                                            av.getToken(),
                                            av);
                    break;
                case AppValue.V_REQTRACKBODY: // request追跡なのでNOP.
                    break;
                case AppValue.V_AUTOTRACKBODY: // responseのbodyを追跡
                    autotrack = true;
                default:
                    try {
                        // body
                        // ParmVars.plog.debuglog(0, "ParseResponse: V_BODY " + rowcolstr);
                        rflag =
                                pmt.getFetchResponseVal()
                                        .bodymatch(
                                                pmt.getStepNo(),
                                                av.getFromStepNo(),
                                                url,
                                                presponse,
                                                row,
                                                col,
                                                true,
                                                autotrack,
                                                av,
                                                av.getResRegexPos(),
                                                av.getToken(),
                                                av.isUrlEncode());
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ParmGen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        }

        return rflag;
    }

    // 何もしないコンストラクタ
    public ParmGen(ParmGenMacroTrace _pmt) {
        pmt = _pmt;
    }

    //
    public ParmGen(ParmGenMacroTrace _pmt, List<AppParmsIni> _parmcsv) {
        pmt = _pmt;
        if (_parmcsv != null) nullset();
        initMain(_parmcsv);
    }

    private void initMain(List<AppParmsIni> _newparmcsv) {
        // main start.
        // csv load
        // parmcsvはstatic
        if (parmcsv == null || _newparmcsv != null) {

            parmcsv = _newparmcsv;

            pmt.nullfetchResValAndCookieMan();
        }
    }

    private void nullset() {
        parmcsv = null;
    }

    public static void clearAll() {
        logger4j.debug("clearAll.");
        parmcsv = null;
        twin = null;
    }

    public boolean checkAndLoadFile(String fname) { // 20200206 this is executed when json load
        // at MacruBuilderUI 1304 , ParmGenTop 614
        // I must implement JSON file check and then ok load function...
        boolean noerror = false;
        List<AppParmsIni> newparmcsv = loadGSON(fname);
        if (newparmcsv != null) {
            nullset();

            initMain(newparmcsv);
            noerror = true;
        }

        return noerror;
    }

    public byte[] Run(String _h, int port, boolean isSSL, byte[] requestbytes) {

        ParmGenBinUtil boundaryarray = null;
        ParmGenBinUtil contarray = null;

        if (parmcsv == null || parmcsv.size() <= 0) {
            // NOP
            if (pmt.isRunning()) {
                PRequest prequest = new PRequest(_h, port, isSSL, requestbytes, ParmVars.enc);
                PRequest cookierequest = pmt.configureRequest(prequest);
                if (cookierequest != null) {
                    return cookierequest.getByteMessage();
                }
            }
        } else {
            // error hash
            ParmGenHashMap errorhash = new ParmGenHashMap();

            // Request request = connection.getRequest();
            PRequest prequest = new PRequest(_h, port, isSSL, requestbytes, ParmVars.enc);

            // check if we have parameters
            // Construct a new HttpUrl object, since they are immutable
            // This is a bit of a cheat!
            // String url = request.getURL().toString();
            String url = prequest.getURL();

            String content_type = prequest.getHeader("Content-Type");

            PRequestResponse org_PRequestResponse = pmt.getCurrentOriginalRequest(); // copy
            PRequest org_Request = null;
            if (pmt.isCurrentRequest() && pmt.isOverWriteCurrentRequestTrackigParam()) {
                PRequestResponse repeaterPRR = pmt.getToolBaseline(); // reference
                if (repeaterPRR != null) {
                    org_Request = repeaterPRR.request;
                } else { // intruder or scanner..
                    org_Request = org_PRequestResponse.request;
                }
            }

            boolean hasboundary = false;
            PRequest tempreq = null;
            PRequest modreq = null;
            if (url != null) {

                AppParmsIni pini = null;
                ListIterator<AppParmsIni> it = parmcsv.listIterator();
                while (it.hasNext()) {
                    pini = it.next();
                    Matcher urlmatcher = pini.getPatternUrl().matcher(url);
                    if (urlmatcher.find() && pmt.CurrentRequestIsSetToTarget(pini)) {
                        // Content-Type: multipart/form-data;
                        // boundary=---------------------------30333176734664
                        if (content_type != null
                                && !content_type.equals("")
                                && hasboundary == false) { // found
                            Pattern ctypepattern =
                                    ParmGenUtil.Pattern_compile(
                                            "multipart/form-data;.*?boundary=(.+)$");
                            Matcher ctypematcher = ctypepattern.matcher(content_type);
                            if (ctypematcher.find()) {
                                String Boundary = ctypematcher.group(1);
                                ParmVars.plog.debuglog(1, "boundary=" + Boundary);
                                Boundary = "--" + Boundary; //
                                boundaryarray = new ParmGenBinUtil(Boundary.getBytes());
                            }
                            hasboundary = true;
                        }
                        ParmVars.plog.debuglog(
                                0, "***URL正規表現[" + pini.getUrl() + "]マッチパターン[" + url + "]");
                        if (contarray == null) {

                            ParmGenBinUtil warray = new ParmGenBinUtil(requestbytes);
                            try {
                                // ParmVars.plog.debuglog(1,"request length : " +
                                // Integer.toString(warray.length()) + "/" +
                                // Integer.toString(prequest.getParsedHeaderLength()));
                                if (warray.length() > prequest.getParsedHeaderLength()) {
                                    byte[] wbyte =
                                            warray.subBytes(prequest.getParsedHeaderLength());
                                    contarray = new ParmGenBinUtil(wbyte);
                                }
                            } catch (Exception e) {
                                // contarray is null . No Body...
                            }
                        }

                        List<AppValue> parmlist = pini.getAppValueReadWriteOriginal();
                        Iterator<AppValue> pt = parmlist.iterator();
                        if (parmlist == null || parmlist.isEmpty()) {
                            //
                        }
                        ParmVars.plog.debuglog(1, "start");
                        while (pt.hasNext()) {
                            ParmVars.plog.debuglog(1, "loopin");
                            AppValue av = pt.next();
                            if (av.isEnabled()) {
                                if ((tempreq =
                                                ParseRequest(
                                                        prequest,
                                                        org_Request,
                                                        boundaryarray,
                                                        contarray,
                                                        pini,
                                                        av,
                                                        errorhash))
                                        != null) {
                                    modreq = tempreq;
                                    prequest = tempreq;
                                }
                            }
                        }
                        // ここでerrorhashを評価し、setErrorする。
                        Iterator<Map.Entry<ParmGenTokenKey, ParmGenTokenValue>> ic =
                                errorhash.iterator();
                        boolean iserror = false;
                        if (ic != null) {
                            while (ic.hasNext()) {
                                Map.Entry<ParmGenTokenKey, ParmGenTokenValue> entry = ic.next();
                                ParmGenTokenValue errorhash_value = entry.getValue();
                                if (!errorhash_value.getBoolean()) {
                                    iserror = true;
                                    break;
                                }
                            }
                        }
                        ParmVars.plog.setError(iserror);
                        ParmVars.plog.debuglog(1, "end");
                    }
                }
            }
            byte[] retval = null;

            PRequest cookierequest = pmt.configureRequest(prequest);
            if (cookierequest != null) {
                prequest = cookierequest;
                retval = prequest.getByteMessage();
            }

            if (modreq != null) {
                // You have to use connection.setRequest() to make any changes take effect!
                if (contarray != null) {
                    try {
                        prequest.setBody(contarray.getBytes());
                    } catch (Exception e) {
                        ParmVars.plog.printException(e);
                    }
                }
                if (ParmVars.ProxyAuth.length() > 0) {
                    prequest.setHeader(
                            "Proxy-Authorization", ParmVars.ProxyAuth); // username:passwd => base64
                }
                retval = prequest.getByteMessage();
            } else if (ParmVars.ProxyAuth.length() > 0) {
                prequest.setHeader(
                        "Proxy-Authorization", ParmVars.ProxyAuth); // username:passwd => base64
                retval = prequest.getByteMessage();
            }

            AppParmsIni pini = null;
            Iterator<AppParmsIni> it = parmcsv.iterator();
            int row = 0;
            while (it.hasNext()) {
                pini = it.next();
                if (pmt.CurrentRequestIsTrackFromTarget(pini)
                        && pini.getTypeVal() == AppParmsIni.T_TRACK) {
                    List<AppValue> parmlist = pini.getAppValueReadWriteOriginal();
                    Iterator<AppValue> pt = parmlist.iterator();
                    boolean fetched;
                    boolean apvIsUpdated = false;
                    int col = 0;
                    while (pt.hasNext()) {
                        AppValue av = pt.next();
                        if (av.isEnabled() && av.getResTypeInt() >= AppValue.V_REQTRACKBODY) {
                            fetched = FetchRequest(prequest, pini, av, row, col);
                            if (fetched) {
                                //pt.set(av); no need set
                                apvIsUpdated = true;
                            }
                        }
                        col++;
                    }
                    if (apvIsUpdated) {
                        //it.set(pini); no need set
                    }
                }
                row++;
            }

            return retval;
        }

        return null;
    }

    public int ResponseRun(String url, byte[] response_bytes, Encode _pageenc) {

        int updtcnt = 0;

        PResponse presponse = new PResponse(response_bytes, _pageenc);

        String res_content_type = presponse.getContent_Type();
        String res_content_subtype = presponse.getContent_Subtype();

        String res_contentMimeType = res_content_type + "/" + res_content_subtype;
        // if content_type/subtype matches excludeMimeType regex then skip below codes..
        if (!ParmVars.isMimeTypeExcluded(res_contentMimeType)) {
            // ### skip start
            if (url != null && parmcsv != null) {

                AppParmsIni pini = null;
                Iterator<AppParmsIni> it = parmcsv.iterator();
                int row = 0;
                while (it.hasNext()) {
                    pini = it.next();

                    if (pmt.CurrentRequestIsTrackFromTarget(pini)
                            && pini.getTypeVal() == AppParmsIni.T_TRACK) {
                        boolean apvIsUpdated = false;
                        List<AppValue> parmlist = pini.getAppValueReadWriteOriginal();
                        Iterator<AppValue> pt = parmlist.iterator();
                        int col = 0;
                        while (pt.hasNext()) {
                            AppValue av = pt.next();
                            if (av.isEnabled()) {
                                if (ParseResponse(url, presponse, pini, av, row, col)) {
                                    //pt.set(av); no need set
                                    updtcnt++;
                                    apvIsUpdated = true;
                                }
                            }
                            col++;
                        }
                        if (apvIsUpdated) {
                            //it.set(pini); no need set
                        }
                    }
                    row++;
                }
            }
            // ### skip end.
        } else {
            ParmVars.plog.debuglog(
                    0,
                    "ResponseRun skipped url[" + url + "] MimeType[" + res_contentMimeType + "]");
        }

        return updtcnt;
    }
}
