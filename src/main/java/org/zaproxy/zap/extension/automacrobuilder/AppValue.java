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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author daike */
//
// class AppValue
//
public class AppValue {
    private static org.apache.logging.log4j.Logger logger4j =
            org.apache.logging.log4j.LogManager.getLogger();

    // valparttype,         value, token, tamattack,tamadvance,tamposition,urlencode
    // 置換位置,置換しない,  value, Name,  Attack,   Advance,   Position,   URLencode
    public String valpart; // 置換位置
    private int valparttype; //  1-query, 2-body  3-header  4-path.... 16(10000) bit == no count
    // 32(100000) == no modify
    private String value = null; // value リクエストパラメータの正規表現文字列
    private Pattern valueregex; // リクエストパラメータの正規表現

    public int csvpos;
    // private int col;
    private int trackkey = -1;
    private String resURL = "";
    private Pattern Pattern_resURL = null;
    private String resRegex = "";
    private Pattern Pattern_resRegex = null;
    private int resPartType;
    public int resRegexPos = -1; // 追跡token　ページ内出現位置 0start
    public String token; // 追跡token　Name
    public String resFetchedValue = null; // レスポンスからフェッチしたtokenの値

    public TokenTypeNames tokentype = TokenTypeNames.INPUT;

    public enum TokenTypeNames {
        DEFAULT,
        INPUT,
        LOCATION,
        HREF,
        XCSRF,
        TEXT,
        TEXTAREA,
        JSON,
        ACTION,
    };

    public String tamattack;
    public int tamadvance;

    public boolean urlencode; // URLencodeする・しない
    public ResEncodeTypes resencodetype = ResEncodeTypes.RAW; // 追跡元のエンコードタイプ json/raw/urlencode

    public enum ResEncodeTypes {
        RAW,
        JSON,
        URLENCODE,
    }

    public int fromStepNo = -1; // TRACK追跡元 <0 :　無条件で追跡　>=0: 指定StepNoのリクエスト追跡
    public int toStepNo =
            ParmVars.TOSTEPANY; // TRACK:更新先 <0 currentStepNo == responseStepNo - toStepNo ==0: 無条件
    // 　>0:指定したStepNoのリクエスト更新

    public static final int V_QUERY = 1;
    public static final int V_BODY = 2;
    public static final int V_HEADER = 3;
    public static final int V_PATH = 4;
    public static final int V_AUTOTRACKBODY = 5; //  response body tracking
    public static final int V_REQTRACKBODY = 6; // password(request body) tracking
    public static final int V_REQTRACKQUERY = 7; // password(request query) tracking
    public static final int V_REQTRACKPATH = 8; // password (request path) tracking
    public static final int C_NOCOUNT = 16;
    public static final int C_VTYPE = 15;
    public static String[] ctypestr = {
        // V_QUERY ==1
        "",
        "query",
        "body",
        "header",
        "path",
        "responsebody",
        "requestbody",
        "requestquery",
        "requestpath",
        null,
        null,
        null,
        null,
        null,
        null,
        null // 0-15
    };

    public static final int I_APPEND = 0;
    public static final int I_INSERT = 1;
    public static final int I_REPLACE = 2;
    public static final int I_REGEX = 3;

    private static String[] payloadpositionnames = {
        // 診断パターン挿入位置
        // append 値末尾に追加
        // insert 値先頭に挿入
        // replace 値をパターンに置き換え
        // regex   埋め込み箇所正規表現指定
        "append", "insert", "replace", "regex", null
    };

    private boolean enabled = true; // 有効

    private void initctype() {
        trackkey = -1;
        resFetchedValue = null;
        enabled = true;
        tokentype = TokenTypeNames.INPUT;
    }

    public AppValue() {
        setVal(null);
        initctype();
        resRegexPos = -1;
    }

    public AppValue(String _Type, boolean _disabled, String _value) {
        initctype();
        setValPart(_Type);
        setEnabled(!_disabled); // NOT
        // value = _value;
        setVal(_value);
        resRegexPos = -1;
    }

    public AppValue(
            String _Type, boolean _disabled, int _csvpos, String _value, boolean increment) {
        initctype();
        setValPart(_Type);
        setEnabled(!_disabled); // NOT
        csvpos = _csvpos;
        // value = _value;
        setVal(_value);
        resRegexPos = -1;
        if (increment) {
            clearNoCount();
        } else {
            setNoCount();
        }
    }

    public AppValue(String _Type, boolean _disabled, String _value, boolean increment) {
        initctype();
        setValPart(_Type);
        setEnabled(!_disabled); // NOT
        // value = _value;
        setVal(_value);
        resRegexPos = -1;
        if (increment) {
            clearNoCount();
        } else {
            setNoCount();
        }
    }

    public AppValue(
            String _Type,
            boolean _disabled,
            String _value,
            String _resURL,
            String _resRegex,
            String _resPartType,
            String _resRegexPos,
            String _token,
            boolean _urlenc,
            int _fromStepNo,
            int _toStepNo,
            String _tokentypename) {
        initctype();
        setValPart(_Type);
        setEnabled(!_disabled); // NOT
        // value = _value;
        setVal(_value);
        setresURL(_resURL);
        setresRegex(_resRegex);
        setresPartType(_resPartType);
        setresRegexPos(_resRegexPos);
        token = _token;
        urlencode = _urlenc;
        fromStepNo = _fromStepNo;
        toStepNo = _toStepNo;
        tokentype = parseTokenTypeName(_tokentypename);
    }

    /*public void setCol(int c){
        col = c;
    }*/

    /*public int getCol(){
        return col;
    }*/

    public void setTrackKey(int k) {
        trackkey = k;
    }

    public int getTrackKey() {
        return trackkey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public String getPayloadPositionName(int it) {
        if (payloadpositionnames.length > it && it >= 0) {
            return payloadpositionnames[it];
        }
        return "";
    }

    public void setResEncodeType(String t) {
        resencodetype = parseResEncodeType(t);
    }

    public ResEncodeTypes parseResEncodeType(String t) {
        ResEncodeTypes[] encarray = ResEncodeTypes.values();
        if (t != null && !t.isEmpty()) {
            String tupper = t.toUpperCase();
            for (ResEncodeTypes enc : encarray) {
                if (enc.name().toUpperCase().equals(tupper)) {
                    return enc;
                }
            }
        }
        return ResEncodeTypes.RAW;
    }

    public static String[] makePayloadPositionNames() {
        return new String[] {
            payloadpositionnames[I_APPEND],
            payloadpositionnames[I_INSERT],
            payloadpositionnames[I_REPLACE],
            payloadpositionnames[I_REGEX]
        };
    }

    // ParmGenNew 数値、追跡テーブル用　ターゲットリクエストパラメータタイプリスト
    public static String[] makeTargetRequestParamTypes() {
        return new String[] {
            ctypestr[V_PATH], ctypestr[V_QUERY], ctypestr[V_BODY], ctypestr[V_HEADER]
        };
    }

    //
    //
    String QUOTE(String t) {
        if (t == null || t.isEmpty()) {
            return "";
        }
        return "\"" + t + "\"";
    }

    String QUOTE_PREFCOMMA(String t) {
        String q = QUOTE(t);
        if (q != null && !q.isEmpty()) {
            return "," + q;
        }
        return "";
    }

    String URLdecode(String _encoded) {
        String exerr = null;
        String _raw = "";
        if (_encoded == null) _encoded = "";
        try {
            _raw = URLDecoder.decode(_encoded, ParmVars.enc.getIANACharsetName());
        } catch (UnsupportedEncodingException e) {
            exerr = e.toString();
            _raw = "";
        }

        return _raw;
    }

    public void setresURL(String _url) {
        if (_url == null) _url = "";
        resURL = _url.trim();
        try {
            Pattern_resURL = ParmGenUtil.Pattern_compile(resURL);
        } catch (Exception e) {
            Pattern_resURL = null;
            ParmVars.plog.debuglog(0, "ERROR: setresURL " + e.toString());
        }
    }

    public String getresURL() {
        return resURL;
    }

    public Pattern getPattern_resURL() {
        return Pattern_resURL;
    }

    public Pattern getPattern_resRegex() {
        return Pattern_resRegex;
    }

    public String getresRegex() {
        return resRegex;
    }

    public void setresRegexURLencoded(String _regex) {
        if (_regex == null) _regex = "";
        setresRegex(URLdecode(_regex));
    }

    public void setresRegex(String _regex) {
        if (_regex == null) _regex = "";
        resRegex = _regex;
        try {
            Pattern_resRegex = ParmGenUtil.Pattern_compile(resRegex);
        } catch (Exception e) {
            ParmVars.plog.debuglog(0, "ERROR: setresRegex " + e.toString());
            Pattern_resRegex = null;
        }
    }

    public void setresPartType(String respart) {
        if (respart == null) respart = "";
        resPartType = parseValPartType(respart);
    }

    public void setresRegexPos(String _resregexpos) {
        resRegexPos = Integer.parseInt(_resregexpos);
    }

    public int getTypeInt() {
        return valparttype & C_VTYPE;
    }

    public void setTypeInt(int t) {
        valparttype = t;
    }

    public int getResTypeInt() {
        return resPartType & C_VTYPE;
    }

    public String getAppValueDsp(int _typeval) {
        String avrec =
                QUOTE(
                                getValPart()
                                        + (isEnabled() ? "" : "+")
                                        + (isNoCount() ? "" : "+")
                                        + (_typeval == AppParmsIni.T_CSV
                                                ? ":" + Integer.toString(csvpos)
                                                : ""))
                        + ","
                        + QUOTE(value)
                        + QUOTE_PREFCOMMA(resURL)
                        + QUOTE_PREFCOMMA(resRegex)
                        + QUOTE_PREFCOMMA(getResValPart())
                        + (resRegexPos != -1 ? QUOTE_PREFCOMMA(Integer.toString(resRegexPos)) : "")
                        + QUOTE_PREFCOMMA(token)
                        + (_typeval == AppParmsIni.T_TRACK
                                ? QUOTE_PREFCOMMA(urlencode == true ? "true" : "false")
                                : "")
                        + (_typeval == AppParmsIni.T_TRACK
                                ? QUOTE_PREFCOMMA(Integer.toString(fromStepNo))
                                : "")
                        + (_typeval == AppParmsIni.T_TRACK
                                ? QUOTE_PREFCOMMA(Integer.toString(toStepNo))
                                : "")
                        + QUOTE_PREFCOMMA(tokentype.name());

        return avrec;
    }

    String getValPart() {
        return getValPart(valparttype);
    }

    public String getValPart(int _valparttype) {
        int i = _valparttype & C_VTYPE;
        if (i < C_VTYPE) {
            if (ctypestr[i] != null) return ctypestr[i];
        }
        return "";
    }

    public void setTokenTypeName(String tknames) {
        tokentype = parseTokenTypeName(tknames);
    }

    public static TokenTypeNames parseTokenTypeName(String tkname) {
        if (tkname != null && !tkname.isEmpty()) {
            String uppername = tkname.toUpperCase();
            TokenTypeNames[] tktypearray = TokenTypeNames.values();
            for (TokenTypeNames tktype : tktypearray) {
                if (tktype.name().toUpperCase().equals(uppername)) {
                    return tktype;
                }
            }
        }
        return TokenTypeNames.DEFAULT;
    }

    String getResValPart() {
        return getValPart(resPartType);
    }

    public static int parseValPartType(String _valtype) {
        int _valparttype = 0;
        String[] ivals = _valtype.split(":");
        String valtypewithflags = ivals[0];
        String _ctypestr = valtypewithflags.replaceAll("[^0-9a-zA-Z]", ""); // 英数字以外を除去
        for (int i = 1; ctypestr[i] != null; i++) {
            if (_ctypestr.equalsIgnoreCase(ctypestr[i])) {
                _valparttype = i;
                break;
            }
        }
        return _valparttype;
    }

    public boolean setValPart(String _valtype) {
        boolean noerror = false;
        valparttype = parseValPartType(_valtype);
        //
        if (_valtype.indexOf("+") != -1) { // increment
            clearNoCount();
        } else {
            setNoCount();
        }
        valpart = _valtype;
        String[] ivals = _valtype.split(":");
        csvpos = -1;
        if (ivals.length > 1) {
            csvpos = Integer.parseInt(ivals[1].trim());
        }
        if (getTypeInt() > 0) {
            noerror = true;
        }
        return noerror;
    }

    void setNoCount() {
        valparttype = valparttype | C_NOCOUNT;
    }

    public void clearNoCount() {
        valparttype = valparttype & ~C_NOCOUNT;
    }

    public boolean isNoCount() {
        return ((valparttype & C_NOCOUNT) == C_NOCOUNT ? true : false);
    }

    public boolean setURLencodedVal(String _value) {
        boolean noerror = false;
        valueregex = null;
        try {
            value = URLDecoder.decode(_value, ParmVars.enc.getIANACharsetName());
            valueregex = ParmGenUtil.Pattern_compile(value);
            noerror = true;
        } catch (UnsupportedEncodingException e) {
            logger4j.error("decode failed value:[" + _value + "]", e);
            valueregex = null;
        }

        return noerror;
    }

    void setVal(String _value) {
        valueregex = null;
        value = _value;
        if (value != null) {
            valueregex = ParmGenUtil.Pattern_compile(value);
        }
    }

    String getVal() {
        return value;
    }

    String[] replaceContents(
            ParmGenMacroTrace pmt,
            int currentStepNo,
            AppParmsIni pini,
            String contents,
            String org_contents_iso8859,
            ParmGenHashMap errorhash) {
        if (contents == null) return null;
        if (valueregex == null) return null;
        ParmGenTokenKey tk = null;
        if (toStepNo >= 0) {
            if (toStepNo != ParmVars.TOSTEPANY) {
                if (currentStepNo != toStepNo) {
                    return null; //
                } else {
                    // ParmVars.plog.debuglog(0, "replaceContents currentStepNo==toStepNo " +
                    // currentStepNo + "==" + toStepNo);
                }
                // tokentype 固定。tokentypeは追跡元のタイプなので、追跡先toStepNoの埋め込み先タイプとは無関係で無視する。
                // tk = new ParmGenTokenKey(AppValue.TokenTypeNames.DEFAULT, token, toStepNo);
                tk =
                        new ParmGenTokenKey(
                                AppValue.TokenTypeNames.DEFAULT,
                                token,
                                currentStepNo); // token: tracking param name, currentStepNo: target
                // request StepNo
            } else {
                // ParmVars.plog.debuglog(0, "replaceContents toStepNo==TOSTEPANY " + toStepNo + "
                // ==" + ParmVars.TOSTEPANY);
            }
        } else {
            // ParmVars.plog.debuglog(0, "replaceContents toStepNo<0 " + toStepNo + "<0 TOSTEPANY="
            // + ParmVars.TOSTEPANY);
        }

        String[] nv = new String[2];

        String errKeyName =
                "TypeVal:"
                        + Integer.toString(pini.typeval)
                        + " TargetPart:"
                        + getValPart()
                        + " TargetRegex:"
                        + value
                        + " ResRegex:"
                        + resRegex
                        + " TokenName:"
                        + token;
        ParmGenTokenKey errorhash_key =
                new ParmGenTokenKey(AppValue.TokenTypeNames.DEFAULT, errKeyName, 0);
        Matcher m = valueregex.matcher(contents);
        Matcher m_org = null;

        if (org_contents_iso8859 != null) {
            m_org = valueregex.matcher(org_contents_iso8859);
        }

        String newcontents = "";
        String tailcontents = "";
        String o_newcontents = "";
        String o_tailcontents = "";
        String strcnt = null;
        int cpt = 0;
        int o_cpt = 0;

        while (m.find()) {
            int spt = -1;
            int ept = -1;
            int o_spt = -1;
            int o_ept = -1;
            int gcnt = m.groupCount();
            String matchval = null;
            for (int n = 0; n < gcnt; n++) {
                spt = m.start(n + 1);
                ept = m.end(n + 1);
                matchval = m.group(n + 1);
            }
            String org_matchval = null;
            if (m_org != null) {
                if (m_org.find()) {
                    int org_gcnt = m_org.groupCount();
                    for (int n = 0; n < org_gcnt; n++) {
                        o_spt = m_org.start(n + 1);
                        o_ept = m_org.end(n + 1);
                        org_matchval = m_org.group(n + 1);
                    }
                }
            }

            if (spt != -1 && ept != -1) {
                strcnt =
                        pini.getStrCnt(pmt, this, tk, currentStepNo, toStepNo, valparttype, csvpos);
                ParmVars.plog.printLF();
                boolean isnull = false;
                ParmGenTokenValue errorhash_value = null;
                String org_newval = strcnt;
                if (org_matchval != null) {
                    ParmGenStringDiffer differ = new ParmGenStringDiffer(org_matchval, matchval);
                    ParmVars.plog.debuglog(
                            0, "org_matchval[" + org_matchval + "] matchval[" + matchval + "]");
                    strcnt = differ.replaceOrgMatchedValue(strcnt);
                }
                if (strcnt != null) {
                    ParmVars.plog.debuglog(
                            0,
                            java.text.MessageFormat.format(
                                    java.util.ResourceBundle.getBundle("burp/Bundle")
                                            .getString("ParmGen.parameter_regex_msg1.text"),
                                    new Object[] {value, matchval, token, strcnt}));
                    //
                    ParmVars.plog.addComments(
                            java.text.MessageFormat.format(
                                    java.util.ResourceBundle.getBundle("burp/Bundle")
                                            .getString("ParmGen.parameter_regex_msg2.text"),
                                    new Object[] {value, matchval, token, strcnt}));
                    errorhash_value = new ParmGenTokenValue("", strcnt, true);
                    errorhash.put(errorhash_key, errorhash_value);
                } else {
                    ParmVars.plog.debuglog(
                            0,
                            java.text.MessageFormat.format(
                                    java.util.ResourceBundle.getBundle("burp/Bundle")
                                            .getString("ParmGen.parameter_regex_err1.text"),
                                    new Object[] {value, token, matchval}));
                    ParmVars.plog.addComments(
                            java.text.MessageFormat.format(
                                    java.util.ResourceBundle.getBundle("burp/Bundle")
                                            .getString("ParmGen.parameter_regex_err2.text"),
                                    new Object[] {value, token, matchval}));
                    isnull = true;
                    errorhash_value = new ParmGenTokenValue("", strcnt, false);
                    ParmGenTokenValue storederror = errorhash.get(errorhash_key);
                    if (storederror == null || storederror.getBoolean() == false) {
                        errorhash.put(errorhash_key, errorhash_value);
                    }
                }
                if (isnull) { // 値取得失敗時は、オリジナルに戻す。
                    strcnt = matchval;
                    org_newval = org_matchval;
                    // ParmVars.plog.setError(isnull);
                }
                newcontents += contents.substring(cpt, spt) + strcnt;
                cpt = ept;
                tailcontents = contents.substring(ept);
                if (org_matchval != null) {
                    o_newcontents += org_contents_iso8859.substring(o_cpt, o_spt) + org_newval;
                    o_cpt = o_ept;
                    o_tailcontents = org_contents_iso8859.substring(o_ept);
                }
            }
        }
        newcontents = newcontents + tailcontents;
        if (newcontents.length() == 0) {
            newcontents = contents;
        }
        o_newcontents = o_newcontents + o_tailcontents;
        if (o_newcontents.length() == 0) {
            o_newcontents = org_contents_iso8859;
        }
        nv[0] = newcontents;
        nv[1] = o_newcontents;
        return nv;
    }
}
