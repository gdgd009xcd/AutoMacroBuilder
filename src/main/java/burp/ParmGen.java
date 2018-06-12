package burp;
//
// AppScan用入力パラメータジェネレータ
//
//　仕様：設定ファイル(AppParmGen.csv)に指定したＵＲＬの指定したパラメータに指定した形式で
//　　　　実行毎に異なる値（乱数または昇順の数値）を設定する。
//
//
//
//



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.stream.JsonParser;


import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;






//
//Logger
//

class PLog {
	//ログレベル
	// 	0 - INFO
	// 1 - DEBUG
	// 2 - DETAIL
	// 3 - ALL
	public static final int C_INFO = 0;
	public static final int C_DEBUG = 1;
	public static final int C_DETAIL = 2;
	public static final int C_ALL = 3;

	int LogLevel = C_INFO;
	String logname = null;
	String comments = "";
	boolean LogfileOn;
	boolean iserror=false;
	PrintWriter Stdout = null;
	PrintWriter Stderr = null;

	PLog(String projectdir){

		// 設定ファイル
		//    ファイルフォーマット
		//    "パス（正規表現）", 桁数(number)|位置(csv), 値の種類rand（乱数）/number（昇順数値）/csv（ファイル）/track(レスポンス), 初期値(昇順数値)[:最大値]/csvファイルパス（ＣＳＶ）,"path/query/body/loc[-]","値[正規表現(\w+)で指定する]"....
		//
		//    例：".*/project/index.php.*", 4, number, 1,"body", "myname(\w+)","query", "myvalue(\w+)", "path", "id/\/(\w+)\/"
		//

		logname = projectdir + "\\AppScanPermGen.log";
		LogfileOn = false;// default disable file output
		File logfile = new File(logname);
		if ( ! logfile.exists()){
			debuglog(1, "started: projectdir=" + projectdir);
		}
		logfile = null;
		comments = "";//no null
		iserror = false;//==true then error

	}

	public void SetBurpPrintStreams(PrintWriter stdout, PrintWriter stderr){
		Stdout = stdout;
		Stderr = stderr;
	}

	private void StdoutPrintln(String v){
		if(Stdout!=null){
			Stdout.println(v);
		}
		System.out.println(v);
	}
	private void StderrPrintln(String v){
		if(Stderr!=null){
			Stderr.println(v);
		}
		System.err.println(v);
	}

	public String getLogname() { return logname; }

	private String getDateTimeStr(){
		Date date1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		return sdf1.format(date1);
	}


	public void printLF(){
		try {
			String v = "";
			String line = "";
			boolean append = true;
			if (LogLevel >= 0){

				line = v + "\n";
				StdoutPrintln(line);
				if(LogfileOn){
					FileWriter filewriter = new FileWriter(logname, append);
					filewriter.write(v + "\r\n");
					filewriter.close();
				}
			}
		}catch (Exception e){
			printException(e);
		}
	}

	public void printlog(String v, boolean append){
		try {
			v = getDateTimeStr() + " " + v;
			if (LogLevel >= 0){


				StdoutPrintln(v);
				if(LogfileOn){
					FileWriter filewriter = new FileWriter(logname, append);
					filewriter.write(v + "\r\n");
					filewriter.close();
				}
			}
		}catch (Exception e){
			printException(e);
		}
	}

	public void InitPrint(String v){
		printlog(v, false);
	}

	public void AppendPrint(String v){
		printlog(v, true);
	}

	boolean isLogfileOn(){
		return LogfileOn;
	}

	public void LogfileOn(boolean _on){
		LogfileOn = _on;
	}

	public void printException(Exception e){
		 StringWriter sw = null;
         PrintWriter  pw = null;

         sw = new StringWriter();
         pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         String trace = sw.toString();
         printlog(e.toString(), true);
         printlog(trace, true);

         try {
             if ( sw != null ) {
                 sw.flush();
                 sw.close();
             }
             if ( pw != null ) {
                 pw.flush();
                 pw.close();
             }
         } catch (IOException ignore){}
	}

       public void printError(String v){
           if(v==null){
               v = "";
           }
           printlog("ERROR: " + v, true);
       }

	public void debuglog(int l, String v){
		if ( l <= LogLevel ){
			printlog(v, true);
		}
	}

        void clearComments(){
            comments = "";//no null
        }

        void addComments(String _v){
            comments += _v + "\n";
        }

        void setComments(String _v){
            comments = _v;
        }

        String getComments(){
            return comments;
        }

        void setError(boolean _b){
            iserror = _b;
        }

        boolean isError(){
            return iserror;
        }
}





//
//class variable
//
// パラメータ初期化、ログ生成
//

class ParmVars {
	// グローバルパラメータ
	static String projectdir;
	static String parmfile;
	static PLog plog;
	static Encode enc;
	static String formdataenc;//iso8859-1 encoding is fully  mapped binaries for form-data binaries.
	// Proxy Authentication
	// Basic username:password(base64 encoded)
	//String ProxyAuth = "Basic Y2hpa2FyYV8xLmRhaWtlOjdyOXR5QDRxMQ==";
	static String ProxyAuth;
	static ParmGenSession session;
        static int displaylength = 10000;// JTextArea/JTextPane等swingの表示バイト数

	//
	// static変数初期化
	//
	static {
		formdataenc = "ISO-8859-1";
		File desktop = new File(System.getProperty("user.home"), "Desktop");
		if (! desktop.exists()){
			projectdir = System.getenv("HOMEDRIVE") + "\\" + System.getenv("HOMEPATH") + "\\\u30c7\u30b9\u30af\u30c8\u30c3\u30d7";
			desktop = new File(projectdir);
			if (! desktop.exists()){
				projectdir = System.getenv("HOMEDRIVE") + "\\" + System.getenv("HOMEPATH") + "\\Desktop";
			}
		}else{
			projectdir = desktop.getAbsolutePath();
		}
		desktop = null;
		File newdir = new File(projectdir + "\\ParmGenParms");

		if (! newdir.exists()){
			if(newdir.mkdirs()){
				projectdir =newdir.getAbsolutePath();
			}
		}else{
			projectdir =newdir.getAbsolutePath();
		}
		newdir = null;

		parmfile = projectdir + "\\AppParmGen.json";
		plog = new PLog(projectdir);
		enc = Encode.UTF_8;// default encoding.
		ProxyAuth = "";
		session = new ParmGenSession();
	}
	//
	//
	// HTTP Request parser
	//
	//
}





//
//
// CSV パーサ
//　""囲み、","対応、""""(ダブルクォートは連続"")
//
class CSVFields{
	public String field;
}

class CSVParser {
	static Pattern pattern = ParmGenUtil.Pattern_compile("(\"[^\"]*(?:\"\"[^\"]*)*\"|[^,\"]*)[ \t]*?,");
	static Matcher matcher = null;
	static String term = "A-----fd43214234897234----------~Terminator_---------89432091842390fdsaf---Z";

	static void Parse(String rdata){
		rdata = rdata.replaceAll("(?:\\x0D\\x0A|[\\x0D\\x0A])?$", ",")  + term;
		matcher = pattern.matcher(rdata);
	}

	static boolean getField(CSVFields csvf){
		if(matcher.find()) {
			csvf.field = matcher.group(1);
			csvf.field = csvf.field.trim();
			csvf.field = csvf.field.replaceAll("^\"(.*)\"$", "$1");
			csvf.field = csvf.field.replaceAll("\"\"", "\"");
			if ( csvf.field.equals(term)){
				return false;
			}
			return true;
		}

		return false;
	}
}


//
// class AppValue
//

class AppValue {
//tamperTable:
//valparttype,         value, token, tamattack,tamadvance,tamposition,urlencode
//置換位置,置換しない,  value, Name,  Attack,   Advance,   Position,   URLencode
	public String valpart;//置換位置
	private int valparttype;// 0-path, 1-query, 2-body  3-header   16(10000) bit == no count 32(100000) == no modify
	public String value = null;//value リクエストパラメータの正規表現文字列
	Pattern valueregex;//リクエストパラメータの正規表現

	public int csvpos;
	public int col;
        private int trackkey = -1;
	private String resURL = "";
        private Pattern Pattern_resURL = null;
	private String resRegex = "";
        private Pattern Pattern_resRegex = null;
	private int resPartType;
	public int resRegexPos = -1;//追跡token　ページ内出現位置 0start
	public String token;//追跡token　Name
        public String resFetchedValue=null;//レスポンスからフェッチしたtokenの値

	public TokenTypeNames tokentype = TokenTypeNames.INPUT;


	public enum TokenTypeNames  {
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
	public int payloadposition;//I_APPEND, I_INSERT, I_REPLACE
	public boolean urlencode;// URLencodeする・しない
	public ResEncodeTypes resencodetype = ResEncodeTypes.RAW;//追跡元のエンコードタイプ json/raw/urlencode
	public enum ResEncodeTypes {
		RAW,
		JSON,
		URLENCODE,
	}

	public int fromStepNo = -1;//追跡元 <0 :　無条件で追跡　>=0: 指定StepNoのリクエスト追跡
	public int toStepNo = 0;//更新先 <0 currentStepNo == responseStepNo - toStepNo ==0: 無条件　>0:指定したStepNoのリクエスト更新

	public static final int V_QUERY = 1;
	public static final int V_BODY = 2;
	public static final int V_HEADER = 3;
	public static final int V_PATH = 4;
	public static final int V_AUTOTRACKBODY = 5;//  response body tracking
	public static final int V_REQTRACKBODY = 6;// password(request body) tracking
	public static final int V_REQTRACKQUERY = 7;// password(request query) tracking
	public static final int V_REQTRACKPATH = 8;//password (request path) tracking
	public static final int C_NOCOUNT = 16;
	public static final int C_VTYPE = 15;
	public static String[] ctypestr = null;

	public static final int I_APPEND = 0;
	public static final int I_INSERT = 1;
	public static final int I_REPLACE = 2;
	public static final int I_REGEX = 3;

	private static String[] payloadpositionnames = {
			//診断パターン挿入位置
			// append 値末尾に追加
			// insert 値先頭に挿入
			// replace 値をパターンに置き換え
			// regex   埋め込み箇所正規表現指定
			"append", "insert", "replace", "regex", null
	};

        private boolean enabled = true;//有効

        private void initctype(){
            trackkey = -1;
            resFetchedValue = null;
            enabled = true;
            if(ctypestr==null){
                ctypestr = new String[] {
                    //V_QUERY ==1
                  "", "query", "body", "header", "path", "responsebody", "requestbody", "requestquery", "requestpath",null,null,null,null,null,null,null //0-15
                };
                payloadposition = I_APPEND;
            }
            tokentype = TokenTypeNames.INPUT;
        }

        AppValue(){
            value = null;
            initctype();
            resRegexPos = -1;
        }

        AppValue(String _Type, boolean _disabled, String _value){
            initctype();
            setValPart(_Type);
            setEnabled(!_disabled);//NOT
            value = _value;
            resRegexPos = -1;
        }

        AppValue(String _Type, boolean _disabled, int _csvpos, String _value, boolean increment){
            initctype();
            setValPart(_Type);
            setEnabled(!_disabled);//NOT
            csvpos = _csvpos;
            value = _value;
            resRegexPos = -1;
            if(increment){
                clearNoCount();
            }else{
                setNoCount();
            }
        }

        AppValue(String _Type, boolean _disabled, String _value, boolean increment){
            initctype();
            setValPart(_Type);
            setEnabled(!_disabled);//NOT
            value = _value;
            resRegexPos = -1;
            if(increment){
                clearNoCount();
            }else{
                setNoCount();
            }
        }

        AppValue(String _Type, boolean _disabled, String _value,
                String _resURL, String _resRegex, String _resPartType, String _resRegexPos, String _token, boolean _urlenc, int _fromStepNo, int _toStepNo, String _tokentypename){
            initctype();
            setValPart(_Type);
            setEnabled(!_disabled);//NOT
            value = _value;
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

        AppValue(String _Type, boolean _disabled,  String _value,String _name,
                String _tamattack, int _tamadvance, int _payloadposition,  boolean _urlenc){
            initctype();
            setValPart(_Type);
            setEnabled(!_disabled);//NOT
            token = _name;
            value = _value;
            tamattack = _tamattack;
            tamadvance = _tamadvance;
            payloadposition = _payloadposition;
            urlencode = _urlenc;
        }

        public void setTrackKey(int k){
            trackkey = k;
        }
        
        public int getTrackKey(){
            return trackkey;
        }
        
        public boolean isEnabled(){
            return enabled;
        }

        public void setEnabled(boolean b){
            enabled = b;
        }

        String getPayloadPositionName(int it){
            if(payloadpositionnames.length > it && it >=0){
                return payloadpositionnames[it];
            }
            return "";
        }

        public  void setResEncodeType(String t){
        	resencodetype = parseResEncodeType(t);
        }

        public ResEncodeTypes parseResEncodeType(String t){
            ResEncodeTypes[] encarray = ResEncodeTypes.values();
            if(t!=null&&!t.isEmpty()){
                String tupper = t.toUpperCase();
                for(ResEncodeTypes enc: encarray){
                    if(enc.name().toUpperCase().equals(tupper)){
                        return enc;
                    }
                }
            }
            return ResEncodeTypes.RAW;
        }

        public static String[] makePayloadPositionNames(){
            return new String[] {payloadpositionnames[I_APPEND], payloadpositionnames[I_INSERT], payloadpositionnames[I_REPLACE], payloadpositionnames[I_REGEX]};
        }

        // ParmGenNew 数値、追跡テーブル用　ターゲットリクエストパラメータタイプリスト
        public static String[] makeTargetRequestParamTypes(){
            return new String[] {ctypestr[V_PATH], ctypestr[V_QUERY], ctypestr[V_BODY], ctypestr[V_HEADER]};
        }

        //
        //
        String QUOTE(String t){
            if ( t==null || t.isEmpty()){
                return "";
            }
            return "\"" + t + "\"";
        }

        String QUOTE_PREFCOMMA(String t){
            String q = QUOTE(t);
            if(q!=null&&!q.isEmpty()){
                return "," + q;
            }
            return "";
        }

        String URLdecode(String _encoded){
                String exerr = null;
                String _raw = "";
                if(_encoded==null)_encoded = "";
		try{
			_raw = URLDecoder.decode(_encoded, ParmVars.enc.getIANACharset());
		}catch(Exception e){
			exerr = e.toString();
                        _raw = "";
		}

		return _raw;
        }

        public void setresURL(String _url){
            if(_url==null)_url = "";
            resURL = _url.trim();
            try{
                Pattern_resURL = ParmGenUtil.Pattern_compile(resURL);
            }catch(Exception e){
                Pattern_resURL = null;
                ParmVars.plog.debuglog(0, "ERROR: setresURL " + e.toString());
            }
        }
        
        public String getresURL(){
            return resURL;
        }
        
        public Pattern getPattern_resURL(){
            return Pattern_resURL;
        }
        
        public Pattern getPattern_resRegex(){
            return Pattern_resRegex;
        }
        
        public String getresRegex(){
            return resRegex;
        }

        public void setresRegexURLencoded(String _regex){
            if(_regex==null)_regex = "";
            setresRegex(URLdecode(_regex));
        }

        public void setresRegex(String _regex){
            if(_regex==null)_regex="";
            resRegex = _regex;
            try{
                Pattern_resRegex = ParmGenUtil.Pattern_compile(resRegex);
            }catch(Exception e){
                ParmVars.plog.debuglog(0, "ERROR: setresRegex " + e.toString());
                Pattern_resRegex = null;
            }
        }

        public void setresPartType(String respart){
            if(respart==null)respart = "";
            resPartType = parseValPartType(respart);
        }

        public void setresRegexPos(String _resregexpos){
            resRegexPos = Integer.parseInt(_resregexpos);
        }

        public int getTypeInt(){
            return valparttype & C_VTYPE;
        }

        public void setTypeInt(int t){
            valparttype = t;
        }

        public int getResTypeInt(){
            return resPartType & C_VTYPE;
        }

        public String getAppValueDsp(int _typeval){
            String avrec = QUOTE(getValPart() + (isEnabled()?"":"+") +(isNoCount()?"":"+")+ (_typeval==AppParmsIni.T_CSV?":"+Integer.toString(csvpos):"")) +","+ QUOTE(value)
                    + QUOTE_PREFCOMMA(resURL)
                    + QUOTE_PREFCOMMA(resRegex)
                    + QUOTE_PREFCOMMA(getResValPart())
                    + (resRegexPos!=-1?QUOTE_PREFCOMMA(Integer.toString(resRegexPos)):"") +
                    QUOTE_PREFCOMMA(token) + (_typeval==AppParmsIni.T_TRACK?QUOTE_PREFCOMMA(urlencode==true?"true":"false"):"")
                    + (_typeval==AppParmsIni.T_TRACK?QUOTE_PREFCOMMA(Integer.toString(fromStepNo)):"")
                    + (_typeval==AppParmsIni.T_TRACK?QUOTE_PREFCOMMA(Integer.toString(toStepNo)):"")
                    + QUOTE_PREFCOMMA(tokentype.name());

            return avrec;
        }

        String getValPart(){
            return getValPart(valparttype);
        }

        String getValPart(int _valparttype){
            int i = _valparttype & C_VTYPE;
            if(i<C_VTYPE){
                if(ctypestr[i]!=null)
                    return ctypestr[i];
            }
            return "";
        }


        public void setTokenTypeName(String tknames){
            tokentype = parseTokenTypeName(tknames);
        }

        public  static TokenTypeNames parseTokenTypeName(String tkname){
            if(tkname!=null&&!tkname.isEmpty()){
                String uppername = tkname.toUpperCase();
                TokenTypeNames[] tktypearray = TokenTypeNames.values();
                for(TokenTypeNames tktype: tktypearray){
                    if(tktype.name().toUpperCase().equals(uppername)){
                        return tktype;
                    }
                }

            }
            return TokenTypeNames.DEFAULT;
        }

        String getResValPart(){
            return getValPart(resPartType);
        }

        int parseValPartType(String _valtype){
            int _valparttype = 0;
            String []ivals = _valtype.split(":");
            String valtypewithflags = ivals[0];
            String _ctypestr = valtypewithflags.replaceAll("[^0-9a-zA-Z]", "");//英数字以外を除去
            for(int i = 1; ctypestr[i]!=null; i++){
                if(_ctypestr.equalsIgnoreCase(ctypestr[i])){
                    _valparttype = i;
                    break;
                }
            }
            return _valparttype;
        }

	void setValPart(String _valtype){
                valparttype = parseValPartType(_valtype);
		//
                if (_valtype.indexOf("+")!=-1){//increment
			clearNoCount();
		}else{
                    setNoCount();
                }
		valpart = _valtype;
		String []ivals = _valtype.split(":");
		csvpos = -1;
		if(ivals.length > 1 ){
			csvpos = Integer.parseInt(ivals[1].trim());
		}
	}



	void setNoCount(){
		valparttype = valparttype | C_NOCOUNT;
	}

	void clearNoCount(){
		valparttype = valparttype & ~C_NOCOUNT;
	}

        public boolean isNoCount(){
            return ((valparttype & C_NOCOUNT) == C_NOCOUNT?true:false);
        }

	String  setURLencodedVal(String _value){
		String exerr = null;
                valueregex = null;
		try{
			value = URLDecoder.decode(_value, ParmVars.enc.getIANACharset());
			valueregex = ParmGenUtil.Pattern_compile(value);
		}catch(Exception e){
			exerr = e.toString();
                        valueregex = null;
		}
		if ( valpart.length()<=0){
			exerr = "valpart is empty";
		}
                if(exerr!=null){
                    ParmVars.plog.debuglog(0, "ERROR: setURLencodedVal [" + value+  "] ERR:"  + exerr);
                }
		return null;
	}


	String replaceContents(ParmGenMacroTrace pmt, int currentStepNo, AppParmsIni pini, String contents, ParmGenHashMap errorhash){
		if (contents == null)
			return null;
		if (valueregex == null)
			return null;
                ParmGenTokenKey tk = null;
		if(toStepNo>0){
			if(currentStepNo!=toStepNo){
				return null;//
			}
                        //tokentype 固定。tokentypeは追跡元のタイプなので、追跡先toStepNoの埋め込み先タイプとは無関係で無視する。
                        tk = new ParmGenTokenKey(AppValue.TokenTypeNames.DEFAULT, token, toStepNo);
		}

                String errKeyName = "TypeVal:" + Integer.toString(pini.typeval) + " TargetPart:"+ getValPart() + " TargetRegex:" + value + " ResRegex:" + resRegex + " TokenName:" + token;
                ParmGenTokenKey errorhash_key = new ParmGenTokenKey(AppValue.TokenTypeNames.DEFAULT, errKeyName, 0);
		Matcher m = valueregex.matcher(contents);

		String newcontents = "";
		String tailcontents = "";
		String strcnt = null;
		int cpt = 0;
		while (m.find()) {
			int spt = -1;
			int ept = -1;
			int gcnt = m.groupCount();
			String matchval = null;
			for(int n = 0; n < gcnt ; n++){
				spt = m.start(n+1);
				ept = m.end(n+1);
				matchval = m.group(n+1);
			}
			if (spt != -1 && ept != -1) {
				strcnt = pini.getStrCnt(this,tk,currentStepNo, toStepNo, valparttype,  csvpos);
				ParmVars.plog.printLF();
				boolean isnull = false;
                                ParmGenTokenValue errorhash_value = null;
                                
                                switch(pini.getType()){
                                case AppParmsIni.T_TRACK:
                                    if(pmt.isOverWriteCurrentRequestTrackigParam()){
                                        int matchlen = matchval.length();
                                        int strcntlen = strcnt.length();
                                        int tail = matchlen - strcntlen;
                                        if(tail > 0){
                                            strcnt += matchval.substring(strcntlen);
                                        }else if(tail<0){
                                            strcnt = null;
                                        }
                                    }
                                default:
                                    break;
                                    
                                }
                                if (strcnt != null) {
                                        ParmVars.plog.debuglog(0,
                                                        java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("burp/Bundle").getString("ParmGen.parameter_regex_msg1.text"), new Object[] {value, matchval, token, strcnt}));
                                        //
                                        ParmVars.plog.addComments(
                                                        java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("burp/Bundle").getString("ParmGen.parameter_regex_msg2.text"), new Object[] {value, matchval, token, strcnt}));
                                        errorhash_value = new ParmGenTokenValue("", strcnt, true);
                                        errorhash.put(errorhash_key,errorhash_value);
                                } else {
                                        ParmVars.plog.debuglog(0,
                                                        java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("burp/Bundle").getString("ParmGen.parameter_regex_err1.text"), new Object[] {value, token, matchval}));
                                        ParmVars.plog
                                                        .addComments(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("burp/Bundle").getString("ParmGen.parameter_regex_err2.text"), new Object[] {value, token, matchval}));
                                        isnull = true;
                                        errorhash_value = new ParmGenTokenValue("", strcnt, false);
                                        ParmGenTokenValue storederror = errorhash.get(errorhash_key);
                                        if(storederror==null||storederror.getBoolean()==false){
                                            errorhash.put(errorhash_key,errorhash_value);
                                        }
                                        
                                }
				if (isnull) {// 値取得失敗時は、オリジナルに戻す。
					strcnt = matchval;
					//ParmVars.plog.setError(isnull);
				}
				newcontents += contents.substring(cpt, spt) + strcnt;
				cpt = ept;
				tailcontents = contents.substring(ept);
			}
		}
		newcontents = newcontents + tailcontents;
		if ( newcontents.length() == 0 ){
			newcontents = contents;
		}
		return newcontents;
	}
}

//
// class FileReadLine
//
class FileReadLine {
	String csvfile;
	String seekfile;
	RandomAccessFile raf = null;
	long seekp;
        int current_line;
        boolean saveseekp;
        ArrayList<String> columns;

	FileReadLine (String _filepath, boolean _saveseekp){
		csvfile = _filepath;
		seekfile =  csvfile + "_L_";
		raf = null;
		seekp = 0;
                saveseekp = _saveseekp;
                current_line = 0;
                columns = null;
	}

        String getFileName(){
            return csvfile;
        }

        public void rewind(){
            if(saveseekp){
                try {
                        FileWriter filewriter = new FileWriter(seekfile, false);
                        String s1=String.valueOf(0);
                        filewriter.write(s1);
                        filewriter.close();
                }catch (Exception e){
                        //
                        ParmVars.plog.printException(e);
                }
            }
            seekp = 0;
            current_line = 0;
        }

	/**
 	* <code>RandomAccessFile.read</code>で読み込んだバイト配列を
	 * _encエンコードした<code>String</code>で返します。
 	* EOFに達するとnullを返します。
 	* @param f
 	* @return
 	* @throws IOException
 	*/
	String readLineRandomAccessFileCharset(RandomAccessFile f)
	  throws IOException {
	  ParmGenBinUtil barray = new ParmGenBinUtil();
	  byte[] onebyte = new byte[1];
	  int c = -1;
	  boolean eol = false;
	  while (!eol) {
	    switch (c = f.read()) {
	    case -1: //EOFに達した場合
	    case '\n':
	      eol = true;
	      break;
	    case '\r':
	      eol = true;
	      long cur = f.getFilePointer();
	      if ((f.read()) != '\n') {
	        f.seek(cur);
	      }
	      break;
	    default:
	      onebyte[0] = (byte)c;
	      barray.concat(onebyte);
	      break;
	    }
	  }


	  if ((c == -1) && (barray.length() == 0)) {
	    return null;
	  }

	  return new String(barray.getBytes(), ParmVars.enc.getIANACharset());
	}

        ArrayList<String> readColumns(){
            if(columns == null){
                columns = new ArrayList<String>();
            }
            columns.clear();
            String dummy = readLine(0, 99999, null);
            if( columns.size()>0){
                return columns;
            }
            return null;
        }

        int  skipLine(int l){
            if (l >=0){
                rewind();
                columns = null;
                while(l-->0){
                    String dummy =readLine(0, 1, null);
                    if(dummy==null){
                        break;
                    }
                }
            }else{
                return -1;
            }
            return current_line;
        }

	String readLine(int _valparttype, int _pos, AppParmsIni _parent){
                if(saveseekp){
                    seekp = 0;
                    current_line = 0;
                }
		String line = null;
		try {
                        if (saveseekp){//seekp保存
                            try{
                                    //seekfile読み込み
                                    FileReader fr = new FileReader(seekfile);
                                    BufferedReader br = new BufferedReader(fr);
                                    String rdata;
                                    String alldata = "";
                                    while((rdata = br.readLine()) != null) {
                                            rdata = rdata.replace("\r","");
                                            rdata = rdata.replace("\n","");
                                            alldata += rdata;
                                    }
                                    String[] slvalue = alldata.split(":");
                                    seekp =Long.valueOf(slvalue[0]);
                                    if(slvalue.length>1){
                                        current_line = Integer.parseInt(slvalue[1]);
                                    }
                                    fr.close();
                            }catch(Exception e){
                                    //NOP
                            }
                        }

			//csvファイルseek
			raf = new RandomAccessFile( csvfile, "r" );

			if (raf.length() <= seekp ){
				ParmVars.plog.debuglog(1, "seekp reached EOF\n");
				raf.close();
				raf = null;
				return null;
			}

			raf.seek( seekp );

			//csvファイル１レコード読み込み
			//line = raf.readLine();
			line = readLineRandomAccessFileCharset(raf);
			String _col = line;

    		CSVParser.Parse(line);

    		CSVFields csvf = new CSVFields();
    		while(CSVParser.getField(csvf)){
				_col = csvf.field;
                                if(columns!=null){
                                    String _c = _col;
                                    _c = _c.replace("\r","");
                                    _c = _c.replace("\n","");
                                    columns.add(_c);
                                }
				if(_pos--<=0)break;
			}
			line = _col;
			line = line.replace("\r","");
			line = line.replace("\n","");
			if ( ((_valparttype & AppValue.C_NOCOUNT ) ==  AppValue.C_NOCOUNT) || (_parent != null && _parent.ispaused())){
				//debuglog(1, " no seek forward:" + Long.toString(seekp));
			}else{
				ParmVars.plog.debuglog(1, " seek forward:" + Long.toString(seekp));
				//現在のseek値取得
				seekp = raf.getFilePointer();
                                current_line++;
				//seekfileにseek値保存
                                if(saveseekp){
                                    try {
                                            FileWriter filewriter = new FileWriter(seekfile, false);
                                            String s1=String.valueOf(seekp) + ":" + String.valueOf(current_line);
                                            filewriter.write(s1);
                                            filewriter.close();
                                    }catch (Exception e){
                                            //
                                            ParmVars.plog.printlog("FileReadLine::readLine failed  ERR:" + e.toString(), true);

                                    }
                                }
			}
		} catch( IOException e ) {
			ParmVars.plog.printlog("FileReadLine::readLine failed csvfile:" + csvfile + " ERR:" + e.toString(), true);
		}finally {
			if ( raf != null){
				try{
					raf.close();
				}catch(Exception e){
					//
				}
				raf = null;
			}
		}
		return line;
	}
}

//
//
//
class AppParmsIni {
        private static final ResourceBundle bundle = ResourceBundle.getBundle("burp/Bundle");
	private String url;
	private Pattern urlregex;
	public ArrayList<AppValue> parmlist = null;
        Iterator<AppValue> it;
	public int len = 4;
	private String type;
	public int typeval;//number:0, rand:1, csv:2, track:3
	public int inival = 0;
	int maxval = 2147483646;
	FileReadLine frl = null;
        String csvname = null;
	String exerr = "";
	String cntfile = "";
	String cstrcnt = null;
	int rndval = 1;
	public int row;
        Boolean pause =false;
        private int TrackFromStep =-1;// StepNo== -1 any >0 TrackingFrom 
        private int SetToStep = 0;// == 0 any > 0 SetTo 

        public static final int T_NUMBER = 0;//数値昇順
        public static final int T_RANDOM = 1;//乱数
        public static final int T_CSV = 2;//CSV入力
        public static final int T_TRACK = 3;//レスポンス追跡
        public static final int T_TAMPER = 4;//TamperProxy
        public static final String T_NUMBER_NAME = "number";
        public static final String T_RANDOM_NAME = "random";
        public static final String T_CSV_NAME = "csv";
        public static final String T_TRACK_NAME = "track";
        public static final String T_TAMPER_NAME = "tamper";
        public static final int T_NUMBER_AVCNT = 2;
        public static final int T_RANDOM_AVCNT = 2;
        public static final int T_CSV_AVCNT = 2;
        public static final int T_TRACK_AVCNT = 8;//csvファイルの旧フォーマットinival==0時は読み込み時のみ6
        public static final int T_TRACK_OLD_AVCNT = 6;
        public static final int T_TAMPER_AVCNT = 8;

        public enum NumberCounterTypes {
            NumberCount,
            DateCount,
        }
        
        public void setTrackFromStep(int _step){
            TrackFromStep = _step;
        }
        
        public int getTrackFromStep(){
            return TrackFromStep;
        }
        
        public void setSetToStep(int _step){
            SetToStep = _step;
        }
        
        public int getSetToStep(){
            return SetToStep;
        }
        
        public boolean ispaused() {
            return pause;
        }

        public void setPause(boolean b){
            pause = b;
            String _c = getCurrentValue();
            switch(typeval){
                case T_NUMBER:
                case T_CSV:
                    int _i = Integer.parseInt(_c);
                    if(pause){
                        if ( _i > 0){
                            _i--;//>0 ならデクリメントして元に戻す
                            updateCurrentValue(_i);
                        }
                    }else{
                        _i++;//インクリメント
                        updateCurrentValue(_i);
                    }
                    break;
                case T_TRACK:
                    break;
                case T_RANDOM:
                    break;
                }
        }

	int getRow(){
		return row;
	}

        public void clearAppValues(){
            if(parmlist!=null){
                for(AppValue ap: parmlist){
                    ParmGenTrackJarFactory.remove(ap.getTrackKey());
                }
            }
            parmlist = new ArrayList<AppValue>();
        }
        
        public void addAppValue(AppValue app){
            app.col = parmlist.size();
            parmlist.add(app);
        }

        public String getIniValDsp(){
            switch(typeval){
                case T_NUMBER:
                    return Integer.toString(inival);
                case T_CSV:
                    return frl.getFileName();
                case T_TRACK:
                    return "";
                case T_RANDOM:
                    return "";
            }
            return "";
        }

        public String getTypeValDsp(){
            switch(typeval){
                case T_NUMBER:
                    return bundle.getString("ParmGen.数値昇順.text");
                case T_CSV:
                    return bundle.getString("ParmGen.CSVファイル昇順.text");
                case T_RANDOM:
                    return bundle.getString("ParmGen.乱数.text");
                case T_TRACK:
                    return bundle.getString("ParmGen.追跡.text");
                case T_TAMPER:
                    return bundle.getString("ParmGen.TAMPERPROXY.text");
            }
            return "";
        }

        public void setType(String _type){
            type = _type;
            if ( type.indexOf(T_RANDOM_NAME)!=-1){//random
                    for(int x = 0; x < len; x++){
                            rndval = rndval * 10;
                    }
                    typeval = T_RANDOM;
            }else if(type.indexOf(T_NUMBER_NAME)!=-1){
                    typeval = T_NUMBER;
            }else if(type.indexOf(T_TRACK_NAME)!=-1){
                    typeval = T_TRACK;
            }else if(type.indexOf(T_TAMPER_NAME)!=-1){
                    typeval = T_TAMPER;
            }else{
                    typeval = T_CSV;
            }
        }

        public int getType(){
            return typeval;
        }

        public int getReadAVCnt(int _plen){
            switch(typeval){
                case T_NUMBER:
                    return T_NUMBER_AVCNT;
                case T_CSV:
                    return T_CSV_AVCNT;
                case T_RANDOM:
                    return T_RANDOM_AVCNT;
                case T_TAMPER:
                    return T_TAMPER_AVCNT;
                case T_TRACK:
                    if(_plen>0)return _plen;//parameter count
                    else{
                        return T_TRACK_OLD_AVCNT;//旧フォーマット
                    }
            }
            return 0;
        }

        public String getLenDsp(){
            return Integer.toString(len);
        }

        public int getAppValuesLineCnt(){
            if(parmlist !=null) {
                int l =  parmlist.size();
                if (l<=0) l=1;
                return l;
            }
            return 1;
        }

        public String getAppValuesDsp(){
            it = parmlist.iterator();
            String appvalues = "";
            while(it.hasNext()){
                AppValue ap = it.next();
                if (appvalues.length()>0){
                    appvalues +="\n";
                }
                appvalues += ap.getAppValueDsp(typeval);
            }
            return appvalues;
        }

	String  setUrl(String _url){
		exerr = null;
		try{
			url = _url;
			urlregex = ParmGenUtil.Pattern_compile(url);

		}catch(Exception e){
                    urlregex = null;
                    exerr = e.toString();
		}
		return exerr;
	}
        
        public String getUrl(){
            return url;
        }
        
        public Pattern getPatternUrl(){
            return urlregex;
        }

        AppParmsIni(String _URL, String _initval, String _type, String _len, ArrayList<AppValue> _apps, int _row){
            setUrl(_URL);
            inival = Integer.parseInt(_initval);
            type = _type;
            len = Integer.parseInt(_len);
            setRowAndCntFile(_row);
            parmlist = _apps;
            crtGenFormat(false);
            rewindAppValues();


        }

        AppParmsIni(){
            rewindAppValues();
        }

        public String getTypeVal(){
            switch(typeval){
                case T_NUMBER:
                    return T_NUMBER_NAME;
                case T_RANDOM:
                    return T_RANDOM_NAME;
                case T_CSV:
                    return T_CSV_NAME;
                case T_TRACK:
                    return T_TRACK_NAME;
                case T_TAMPER:
                    return T_TAMPER_NAME;
                default:
                    break;
            }
            return "";
        }

        void setCntFileName(){
            if(cntfile==null||cntfile.length()==0){
            	File cfile = new File(ParmVars.parmfile);
                String dirname = cfile.getParent();
                String filename = cfile.getName();
                
                int lastpos = filename.lastIndexOf(".");
                int slen = filename.length();
                String name = filename;
                if(lastpos>0&& slen > lastpos){
                    String prefix = filename.substring(0, lastpos);
                    String suffix = filename.substring(lastpos+1);
                    name = prefix;
                }
                cntfile = dirname + "\\" + name + "_" +Integer.toString(row) + ".txt";
            }
        }

        void setRowAndCntFile(int _r){
            row = _r;
            setCntFileName();
        }

	void crtGenFormat(Boolean lastEntryNoCount){

		if ( parmlist != null){
			int plast = parmlist.size() - 1;
			if ( plast >= 0 ){
				AppValue av = (AppValue)parmlist.get(plast);
                                if(!lastEntryNoCount)av.clearNoCount();
				parmlist.set(plast, av);
			}
		}
	}

	String getFillZeroInt(int v){
		String nval = Integer.toString(v);
		int zero = len - nval.length();
		while(zero>0){
			nval = "0" + nval;
			zero--;
		}
		return nval;
	}

	String getGenValue(AppValue apv, ParmGenTokenKey tk, int currentStepNo, int toStepNo, int _valparttype,  int csvpos){
		int n;
		switch(typeval){
		case T_NUMBER://number
			n = countUp(_valparttype, this);
			if ( n > -1 ){
				return getFillZeroInt(n);
			}else{
				return null;
			}
		case T_RANDOM://random
			Random rand = new Random();
			n = rand.nextInt(rndval);
			return  getFillZeroInt(n);
		case T_TRACK://loc
			//if ( global.Location != void ){

			return FetchResponse.loc.getLocVal(apv.getTrackKey(), tk, currentStepNo, toStepNo);
			//}
		default://csv
			if ( frl != null){
				ParmVars.plog.debuglog(1, "frl.csvfile:" + frl.csvfile);
				if ( csvpos == -1 ){
					csvpos = len;
				}
				return frl.readLine(_valparttype, csvpos, this);//CSVレコード
			}else{
				ParmVars.plog.debuglog(1, "getGenValue frl is NULL");
			}
			break;
		}
		return null;
	}

	String getStrCnt(AppValue apv, ParmGenTokenKey tk, int currentStepNo, int toStepNo,int _valparttype,  int csvpos){
		//if ( cstrcnt == null|| typeval == 3){
		cstrcnt = getGenValue(apv, tk, currentStepNo,toStepNo,_valparttype, csvpos);
		//}
		return cstrcnt;
	}

	int countUp(int _valparttype, AppParmsIni _parent){
		//counter file open
		int cnt = inival;
                try {


			FileReader fr = new FileReader(cntfile);
			BufferedReader br = new BufferedReader(fr);
			String rdata;
			String alldata = "";
			while((rdata = br.readLine()) != null) {
				rdata = rdata.replace("\r","");
				rdata = rdata.replace("\n","");
				alldata += rdata;
			}
			cnt =Integer.valueOf(alldata).intValue();

			fr.close();


		} catch(Exception e) {
			ParmVars.plog.printlog("read file:" + cntfile + " " + e.toString(), true);
                        cnt = inival;
		}

		int ncnt = cnt + 1;

		if ( ((_valparttype & AppValue.C_NOCOUNT ) ==  AppValue.C_NOCOUNT) || _parent.ispaused()){
			ncnt = cnt;//no countup
		}else if(ncnt > maxval){
			ParmVars.plog.debuglog(0, "CountUp maxval reached. reset to inival" + Integer.toString(ncnt) + "->" + Integer.toString(inival));
			ncnt = inival;
		}else{
			ParmVars.plog.debuglog(1, "CountUp ncnt:" + Integer.toString(ncnt));
		}


		if ( (_valparttype & AppValue.C_NOCOUNT ) !=  AppValue.C_NOCOUNT){
                    try {
                            FileWriter filewriter = new FileWriter(cntfile, false);
                            String s1=String.valueOf(ncnt);
                            filewriter.write(s1);
                            filewriter.close();
                    }catch (Exception e){
                            ParmVars.plog.printlog("write file:" + cntfile + " " + e.toString(), true);
                            throw new RuntimeException(e.toString());
                    }
                }
		return cnt;
	}

        int updateCounter(int i){
            if (i >=0){
                try {
                    ParmVars.plog.debuglog(1, "cntfile:" + cntfile);
                            FileWriter filewriter = new FileWriter(cntfile, false);
                            String s1=String.valueOf(i);
                            filewriter.write(s1);
                            filewriter.close();
                    }catch (Exception e){
                            ParmVars.plog.printException(e);
                            return -1;
                    }
            }
            return i;
        }

        int updateCSV(int i){
            return frl.skipLine(i);
        }

        public String getCurrentValue(){
            String rval = null;
            switch(typeval){
                case T_NUMBER:
                    int i = countUp(AppValue.C_NOCOUNT, this);
                    rval = Integer.toString(i);
                    break;
                case T_RANDOM:
                    break;
                case T_CSV:
                    String rec = frl.readLine(AppValue.C_NOCOUNT, 0, this);
                    rval = String.valueOf(frl.current_line);
                    break;
                case T_TRACK:
                    break;
                default:
                    break;
            }
            return rval;
        }

        public String updateCurrentValue(int i){
            int r = -1;
            String rval = null;;
            switch(typeval){
                case T_NUMBER:
                    r = updateCounter(i);
                    if(r!=-1){
                        rval = Integer.toString(r);
                    }
                    break;
                case T_RANDOM:
                    break;
                case T_CSV:
                    r = frl.skipLine(i);
                    if(r!=-1){
                        rval = String.valueOf(r);
                    }
                    break;
                case T_TRACK:
                    break;
                default:
                    break;
            }
            return rval;
        }

        public final void rewindAppValues(){
            if (parmlist!=null){
                it = parmlist.iterator();
            }else{
                it = null;
            }
        }

        public Object[] getNextAppValuesRow(){
            AppValue app;
            if(it!=null && it.hasNext()){
                app = it.next();
                switch(typeval){
                case T_NUMBER:
                     return new Object[] {app.getValPart(), (app.isEnabled()?false:true), app.value, app.isNoCount()?false:true};
                case T_RANDOM:
                    break;
                case T_CSV:
                    return new Object[] {app.getValPart(), (app.isEnabled()?false:true), app.csvpos, app.value, app.isNoCount()?false:true};
                case T_TRACK:
                    return new Object[] {app.getValPart(), (app.isEnabled()?false:true), app.value,
                        app.getresURL(),
                        app.getresRegex(),
                        app.getResValPart(),
                        Integer.toString(app.resRegexPos),
                    app.token, app.urlencode, app.fromStepNo, app.toStepNo, app.tokentype.name()};
                case T_TAMPER:
                    return new Object[] {app.getValPart(), (app.isEnabled()?false:true), app.value,
app.getValPart(), (app.isEnabled()?false:true), app.value,
                        app.token,
                        app.tamattack,
                        app.tamadvance,
                        app.getPayloadPositionName(app.payloadposition),
                    app.urlencode};
                default:
                    break;
                }

            }
            return null;
        }
}

// main class
class ParmGen {

	public static List<AppParmsIni> parmcsv = null;
        public static ArrayList<AppParmsIni> parmjson = null;

        private static final ResourceBundle bundle = ResourceBundle.getBundle("burp/Bundle");


        public static ParmGenTop twin = null;
        public static boolean ProxyInScope = false;
        public static boolean IntruderInScope = true;
        public static boolean RepeaterInScope = true;
        public static boolean ScannerInScope = true;
        ParmGenMacroTrace pmt;


        void disposeTop(){
            if(twin!=null){
                twin.dispose();
            }
            twin = null;
        }

        //
        //
        //
        ArrayList<AppParmsIni> loadJSON(){
        	//
        	int arraylevel = 0;
        	ParmVars.plog.debuglog(0, "loadJSON called.");
        	ArrayList<AppParmsIni> rlist = null;
        	String pfile = ParmVars.parmfile;
        	ParmVars.plog.debuglog(1, "---------AppPermGen.json----------");

        	try{

        		String rdata;
        		String jsondata=new String("");
        		FileReader fr = new FileReader(pfile);
        		try{

        			BufferedReader br = new BufferedReader(fr);
        			while((rdata = br.readLine()) != null) {
        				jsondata += rdata;
        			}//end of while((rdata = br.readLine()) != null)
        			fr.close();
        			fr = null;
        		}catch(Exception e){
        			throw new RuntimeException(e.toString());
        		}finally{
        			if(fr!=null){
        				try{
        					fr.close();
        				}catch (Exception e){
        					throw new RuntimeException(e.toString());
        				}
        				fr = null;
        			}
        		}
                        ParmGenStack<String> astack = new ParmGenStack<String>();
        		JsonParser parser = Json.createParser(new StringReader(jsondata));
        		String keyname = null;
        		boolean noerrflg = false;
        		ParmGenJSON gjson = new ParmGenJSON();
        		while (parser.hasNext()) {
        			JsonParser.Event event = parser.next();
        			boolean bval = false;
        			Object obj = null;
        			if(keyname==null){
        				keyname ="";
        			}
        			switch(event) {
        			case START_ARRAY:
        				arraylevel++;
                                        astack.push(keyname);
                                        //ParmVars.plog.debuglog(0, "START_ARRAY NAME:" +keyname + " level:" + arraylevel);
        				break;
        			case END_ARRAY:
        				arraylevel--;
                                        String ep = astack.pop();
                                        //ParmVars.plog.debuglog(0, "END_ARRAY NAME:" +ep + " level:" + arraylevel);
        				break;
        			case KEY_NAME:
        				keyname = parser.getString();
        				break;
        			case START_OBJECT:
        			case END_OBJECT:
        				noerrflg = gjson.Parse(astack,arraylevel, event, keyname, null);
        				break;
        			case VALUE_TRUE:
        				bval = true;
        			case VALUE_FALSE:
        				noerrflg = gjson.Parse(astack,arraylevel, event, keyname, bval);
        				break;
        			case VALUE_STRING:
        			case VALUE_NUMBER:
        				obj = parser.getString();
        			case VALUE_NULL:
        				noerrflg = gjson.Parse(astack,arraylevel, event, keyname, obj);
        				break;
        			}
        		}
        		if(noerrflg){
        			rlist = gjson.Getrlist();
                                pmt.ui.clear();
                                pmt.ui.addNewRequests(gjson.GetMacroRequests());
                                int creq = gjson.getCurrentRequest();
                                pmt.setCurrentRequest(creq);
                                pmt.ui.Redraw();
        		}else{
        			ParmVars.plog.printError("JSON load failed.");
        		}
        	}catch(Exception e){//設定ファイル無し。
        		ParmVars.plog.printException(e);
        		rlist = null;

        	}

        	ParmVars.plog.debuglog(1, "---------AppPermGen JSON load END ----------");
        	return rlist;
        }



	PRequest ParseRequest(PRequest prequest,  ParmGenBinUtil boundaryarray, ParmGenBinUtil _contarray, AppParmsIni pini, AppValue av, ParmGenHashMap errorhash)  {


	//	String[] headers=request.getHeaderNames();
	//	boolean noauth = false;
	//	for(String header : headers){
	//		if ( header.indexOf("Authorization")==-1){
	//			noauth = true;
	//		}
	//		//printlog(header+" : " + request.getHeader(header), true);
	//	}
                if(av.toStepNo>0&&av.toStepNo!=pmt.getStepNo())return null;
                
		ArrayList<String []> headers = prequest.getHeaders();

		String method = prequest.getMethod();
		String url = prequest.getURL();
		String path = new String(url);
		ParmVars.plog.debuglog(1, "method[" + method + "] request[" + url + "]");
		int qpos = -1;
                switch(av.getTypeInt()){
		//switch(av.valparttype & AppValue.C_VTYPE){
		case AppValue.V_PATH://path
			// path = url
			String n_path = av.replaceContents(pmt, pmt.getStepNo(), pini, path, errorhash);
			if (n_path != null && !path.equals(n_path) ){
				url = n_path;
				ParmVars.plog.debuglog(1, " Original path[" + path + "]");
				ParmVars.plog.debuglog(1, " Modified path[" + n_path + "]");
				//request.setURL(new HttpUrl(url));
				prequest.setURL(url);
				return prequest;
			}
			break;
		case AppValue.V_QUERY://query
			if ((qpos = url.indexOf('?'))!=-1){
				path = url.substring(0,qpos);
				String query = url.substring(qpos+1);
				String n_query = av.replaceContents(pmt,pmt.getStepNo(),pini, query, errorhash);
                                ParmVars.plog.debuglog(1, query);
                                ParmVars.plog.debuglog(1, n_query);
				if ( n_query!=null && !query.equals(n_query) ){
					url = path + '?' + n_query;
					ParmVars.plog.debuglog(1, " Original query[" + query + "]");
					ParmVars.plog.debuglog(1, " Modified path[" + n_query + "]");
					//request.setURL(new HttpUrl(url));
					prequest.setURL(url);
					return prequest;
				}
			}
			break;
		case AppValue.V_HEADER://header
			//String[] headers=request.getHeaderNames();
			//for(String header : headers){
			int i = 0;
			for(String[] nv : headers){
				String hval = nv[0] + ": " + nv[1];//Cookie: value
				String n_hval = av.replaceContents(pmt, pmt.getStepNo(),pini, hval, errorhash);
				if (n_hval !=null && !hval.equals(n_hval) ){
					ParmVars.plog.debuglog(1, " Original header[" + hval + "]");
					ParmVars.plog.debuglog(1, " Modified header[" + n_hval + "]");
					String htitle = nv[0] + ": ";
					n_hval = n_hval.substring(htitle.length());
					//request.setHeader(header, n_hval);
					prequest.setHeader(i, nv[0], n_hval);
					return prequest;
				}
				i++;
			}

			break;
		default://body
	        if (_contarray != null) {
	        	if ( boundaryarray == null ){//www-url-encoded
                            ParmVars.plog.debuglog(1, "application/x-www-form-urlencoded");
                            String content = null;
                            try{
                                    content = new String(_contarray.getBytes(), ParmVars.enc.getIANACharset());
                            }catch(UnsupportedEncodingException e){
                                    content = null;
                            }
                            String n_content = av.replaceContents(pmt, pmt.getStepNo(),pini, content, errorhash);
                            if ( n_content != null && !content.equals(n_content) ){
                                    ParmVars.plog.debuglog(1, " Original body[" + content + "]");
                                    ParmVars.plog.debuglog(1, " Modified body[" + n_content + "]");
                                    try {
                                        _contarray.initParmGenBinUtil(n_content.getBytes(ParmVars.enc.getIANACharset()));
                                    } catch (UnsupportedEncodingException ex) {
                                        Logger.getLogger(ParmGen.class.getName()).log(Level.SEVERE, null, ex);
                                        _contarray.initParmGenBinUtil(n_content.getBytes());
                                    }
                                    return prequest;
                            }
	        	}else{//multipart/form-data
                            ParmVars.plog.debuglog(1, "multipart/form-data");
                            ParmGenBinUtil n_array = new ParmGenBinUtil();
                            int cpos = 0;
                            int npos = -1;
                            byte[] partdata= null;
                            boolean partupdt = false;
                            byte[] headerseparator = {0x0d, 0x0a, 0x0d, 0x0a};//<CR><LF><CR><LF>
                            byte[] partheader = null;
                            String partenc = "";
                            while ( (npos=_contarray.indexOf(boundaryarray.getBytes(), cpos))!=-1){
                                if(cpos!=0){//cpos->npos == partdata
                                    partdata = _contarray.subBytes(cpos, npos);
                                    //マルチパート内のヘッダーまで(CRLFCRLF)読み込み、Content-typeを判定
                                    int hend = _contarray.indexOf(headerseparator, cpos);
                                    partheader = _contarray.subBytes(cpos, hend);
                                    String partcontenttype = null;
                                    try {
                                        partcontenttype = new String(partheader, ParmVars.formdataenc);
                                    } catch (UnsupportedEncodingException ex) {
                                        partcontenttype = "";
                                    }
                                    int ctypestart = 0;
                                    partenc = ParmVars.enc.getIANACharset();
                                    if((ctypestart=partcontenttype.indexOf("Content-Type:"))!=-1){
                                        String cstr = partcontenttype.substring(ctypestart + "Content-Type:".length());
                                        String[] cstrvalues = cstr.split("[\r\n;]+");
                                        if(cstrvalues.length>0){
                                            String partcontenttypevalue = cstrvalues[0];
                                            if(!partcontenttypevalue.isEmpty()){
                                                partenc = ParmVars.formdataenc;
                                                partcontenttypevalue = partcontenttypevalue.trim();
                                                ParmVars.plog.printlog( "form-data Contentype:[" + partcontenttypevalue + "]", true);
                                            }
                                        }
                                    }
                                    String partdatastr = null;
                                    try {
                                            partdatastr = new String(partdata, partenc);
                                    }catch(UnsupportedEncodingException e){
                                            partdatastr = null;
                                    }
                                    String n_partdatastr = av.replaceContents(pmt, pmt.getStepNo(), pini, partdatastr, errorhash);
                                    if(n_partdatastr!=null && partdatastr != null && !partdatastr.equals(n_partdatastr) ){
                                        ParmVars.plog.debuglog(1, " Original body[" + partdatastr + "]");
                                        ParmVars.plog.debuglog(1, " Modified body[" + n_partdatastr + "]");
                                        try{
                                            n_array.concat(n_partdatastr.getBytes(partenc));
                                        }catch(UnsupportedEncodingException e){
                                            ParmVars.plog.printException(e);
                                            n_array.concat(n_partdatastr.getBytes());
                                        }
                                        partupdt = true;
                                    }else{
                                        n_array.concat(partdata);
                                    }
                                    int nextcpos = npos + boundaryarray.length()+2;
                                    n_array.concat(_contarray.subBytes(npos, nextcpos));
                                    String lasthyphon = new String(_contarray.subBytes(nextcpos-2, nextcpos));
                                    if ( lasthyphon.equals("--")){
                                        n_array.concat("\r\n".getBytes());//last hyphon "--" + CRLF
                                    }
                                    cpos = nextcpos;
                                }else{
                                    cpos = npos + boundaryarray.length() + 2;
                                    n_array.concat(_contarray.subBytes(0, cpos));
                                }
                            }

                            if ( partupdt ){
                                //_contarray = n_array;
                                _contarray.initParmGenBinUtil(n_array.getBytes());
                                return prequest;
                            }
	        	}
                    }

                    break;
		}


		return null;
	}

boolean FetchRequest(PRequest prequest,   AppParmsIni pini, AppValue av){
    if(av.fromStepNo<0||av.fromStepNo==pmt.getStepNo()){
        String url = prequest.getURL();
        int row,col;
        row = pini.row;
        col = av.col;
        switch(av.getResTypeInt()){
            case AppValue.V_REQTRACKBODY:
                return FetchResponse.loc.reqbodymatch(av,pmt.getStepNo(), av.fromStepNo,url, prequest, row, col, true, av.resRegexPos, av.token);
            default:
                break;
        }
    }
    return false;
}

boolean ParseResponse(String url,  PResponse presponse, AppParmsIni pini, AppValue av)  {

                int row,col;
                row = pini.row;
                col = av.col;
                boolean rflag = false;
                boolean autotrack = false;
                String rowcolstr = Integer.toString(row) + "," + Integer.toString(col);
		//String path = new String(url);
                if(av.fromStepNo<0||av.fromStepNo==pmt.getStepNo()){
                    int qpos = -1;
                    switch(av.getResTypeInt()){
                    //switch(av.resPartType & AppValue.C_VTYPE){
                    case AppValue.V_PATH://path
                            //ParmVars.plog.debuglog(0, "ParseResponse: V_PATH " + rowcolstr);
                            break;
                    case AppValue.V_QUERY://query
                            //ParmVars.plog.debuglog(0, "ParseResponse: V_QUERY " + rowcolstr);
                            break;
                    case AppValue.V_HEADER://header
                            //ParmVars.plog.debuglog(0, "ParseResponse: V_HEADER " + rowcolstr);
                            //String[] headers=request.getHeaderNames();
                            //for(String header : headers){
                            rflag = FetchResponse.loc.headermatch(pmt.getStepNo(), av.fromStepNo,url, presponse, row, col, true,av.token, av);
                            break;
                    case AppValue.V_REQTRACKBODY://request追跡なのでNOP.
                        break;
                    case AppValue.V_AUTOTRACKBODY://responseのbodyを追跡
                        autotrack = true;
                    default:
                            try {
                                //body
                                //ParmVars.plog.debuglog(0, "ParseResponse: V_BODY " + rowcolstr);
                                rflag = FetchResponse.loc.bodymatch(pmt.getStepNo(),av.fromStepNo,url, presponse, row, col, true, autotrack, av,av.resRegexPos, av.token, av.urlencode);
                            } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(ParmGen.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                    }
                }


		return rflag;
	}

        //何もしないコンストラクタ
        ParmGen(ParmGenMacroTrace _pmt){
            pmt = _pmt;
        }
        
        //
	ParmGen(ParmGenMacroTrace _pmt, List<AppParmsIni>_parmcsv){
		pmt = _pmt;
		if(_parmcsv!=null)nullset();
		initMain(_parmcsv);
	}


	void initMain(List<AppParmsIni> _newparmcsv){
		//main start.
		// csv load
		// parmcsvはstatic
		if ( parmcsv == null || _newparmcsv != null){
			if(_newparmcsv==null){

				parmcsv = loadJSON();
			}else{
				parmcsv = _newparmcsv;
			}
			//ArrayList<AppParmsIni> parmjson = loadJSON();

			if(parmcsv==null)return;
                        //colmax計算
                        
			
			FetchResponse.loc = new LocVal();                        
			
		}
	}

	private void nullset(){
		 parmcsv = null;
         FetchResponse.loc = null;

	}
        public void reset(){
            nullset();
            initMain(null);
        }

	byte[] Run(byte[] requestbytes){
            String request_string = null;
            try{
                request_string = new String(requestbytes, ParmVars.enc.getIANACharset());
            }catch(Exception e){
                ParmVars.plog.printException(e);
            }
            ParmGenBinUtil boundaryarray = null;
            ParmGenBinUtil contarray = null;


            if( parmcsv == null || parmcsv.size()<=0){
                //NOP
                if(pmt.isRunning()){
                    PRequest prequest = new PRequest(request_string);
                    PRequest cookierequest = pmt.configureRequest(prequest);
                    if(cookierequest!=null) {
                        return cookierequest.getByteMessage();
                    }
                }
            }else{
                    // error hash
                    ParmGenHashMap errorhash = new ParmGenHashMap();
                    
                    //Request request = connection.getRequest();
                    PRequest prequest = new PRequest(request_string);

                    // check if we have parameters
                    // Construct a new HttpUrl object, since they are immutable
                    // This is a bit of a cheat!
                    //String url = request.getURL().toString();
                    String url = prequest.getURL();

                    String content_type =prequest.getHeader("Content-Type");


                    boolean hasboundary = false;
                    PRequest tempreq = null;
                    PRequest modreq = null;
                    if ( url != null ){

                            AppParmsIni pini = null;
                            ListIterator<AppParmsIni> it = parmcsv.listIterator();
                            while(it.hasNext()) {
                                pini = it.next();
                                Matcher urlmatcher = pini.getPatternUrl().matcher(url);
                                if ( urlmatcher.find() && pmt.CurrentRequestIsSetToTarget(pini)){
                                        //Content-Type: multipart/form-data; boundary=---------------------------30333176734664
                                        if (content_type != null && !content_type.equals("") && hasboundary ==false){//found
                                                Pattern ctypepattern = ParmGenUtil.Pattern_compile("multipart/form-data;.*?boundary=(.+)$");
                                                Matcher ctypematcher = ctypepattern.matcher(content_type);
                                                if ( ctypematcher.find()){
                                                        String Boundary = ctypematcher.group(1);
                                                        ParmVars.plog.debuglog(1, "boundary=" + Boundary);
                                                        Boundary = "--" + Boundary;//
                                                        boundaryarray = new ParmGenBinUtil(Boundary.getBytes());

                                                }
                                                hasboundary = true;
                                        }
                                        ParmVars.plog.debuglog(0, "***URL正規表現[" + pini.getUrl() + "]マッチパターン[" + url + "]");
                                        if( contarray == null ){
                                            /*****
                                                byte[] bytes = null;
                                                try{
                                                        bytes = prequest.getBody().getBytes(ParmVars.enc);
                                                }catch(UnsupportedEncodingException e){
                                                    ParmVars.plog.printException(e);
                                                        bytes = null;
                                                }
                                                if ( bytes != null ){
                                                        contarray = new ParmGenBinUtil(bytes);
                                                }
                                                bytes = null;
                                                * **/
                                            ParmGenBinUtil warray = new ParmGenBinUtil(requestbytes);
                                            try{
                                                //ParmVars.plog.debuglog(1,"request length : " + Integer.toString(warray.length()) + "/" + Integer.toString(prequest.getParsedHeaderLength()));
                                                if(warray.length()>prequest.getParsedHeaderLength()){
                                                    byte[] wbyte = warray.subBytes(prequest.getParsedHeaderLength());
                                                    contarray = new ParmGenBinUtil(wbyte);
                                                }
                                            }catch(Exception e ){
                                                //contarray is null . No Body...
                                        }

                                    }

                                    List<AppValue> parmlist = pini.parmlist;
                                    ListIterator<AppValue> pt = parmlist.listIterator();
                                    if (parmlist == null || parmlist.isEmpty()) {
                                        //
                                    }
                                    ParmVars.plog.debuglog(1, "start");
                                    while (pt.hasNext()) {
                                        ParmVars.plog.debuglog(1, "loopin");
                                        AppValue av = pt.next();
                                        if (av.isEnabled()) {
                                            if ((tempreq = ParseRequest(prequest, boundaryarray, contarray, pini, av, errorhash)) != null) {
                                                modreq = tempreq;
                                                prequest = tempreq;
                                            }
                                        }
                                    }
                                    //ここでerrorhashを評価し、setErrorする。
                                    Iterator<Map.Entry<ParmGenTokenKey, ParmGenTokenValue>> ic =errorhash.iterator();
                                    boolean iserror = false;
                                    if(ic!=null){
                                        while(ic.hasNext()){
                                            Map.Entry<ParmGenTokenKey, ParmGenTokenValue> entry = ic.next();
                                            ParmGenTokenValue errorhash_value = entry.getValue();
                                            if(!errorhash_value.getBoolean()){
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
                    if(cookierequest!=null){
                        prequest = cookierequest;
                        retval =  prequest.getByteMessage();
                    }

                    if ( modreq != null){
                            // You have to use connection.setRequest() to make any changes take effect!
                            if (contarray != null){
                                    try {
                                            prequest.setBody(contarray.getBytes());
                                    }catch(Exception e){
                                            ParmVars.plog.printException(e);
                                    }
                            }
                            if(ParmVars.ProxyAuth.length()>0){
                                    prequest.setHeader("Proxy-Authorization", ParmVars.ProxyAuth);// username:passwd => base64
                            }
                            retval =  prequest.getByteMessage();
                    }else if(ParmVars.ProxyAuth.length()>0){
                            prequest.setHeader("Proxy-Authorization", ParmVars.ProxyAuth);// username:passwd => base64
                            retval = prequest.getByteMessage();
                    }


                    AppParmsIni pini = null;
                    ListIterator<AppParmsIni> it = parmcsv.listIterator();
                    while(it.hasNext()){
                        pini = it.next();
                        if(pmt.CurrentRequestIsTrackFromTarget(pini) && pini.getType()==AppParmsIni.T_TRACK){
                            List<AppValue> parmlist = pini.parmlist;
                            ListIterator<AppValue> pt = parmlist.listIterator();
                            boolean fetched;
                            boolean apvIsUpdated = false;
                            while(pt.hasNext()){
                                    AppValue av = pt.next();
                                    if(av.isEnabled()&&av.getResTypeInt()>=AppValue.V_REQTRACKBODY){
                                        fetched = FetchRequest(prequest,  pini, av);
                                        if(fetched){
                                            pt.set(av);
                                            apvIsUpdated = true;
                                        }
                                    }
                            }
                            if(apvIsUpdated){
                                it.set(pini);
                            }
                        }
                    }


                    return retval;
            }

            return null;
	}

    int ResponseRun(String url,  byte[] response_bytes, String _enc){

        int updtcnt = 0;

	
                // main loop
                //Request request = connection.getRequest();

                String response_string = null;
                try{
                    response_string = new String(response_bytes, _enc);
                }catch(Exception e){
                    return -1;
                }
                PResponse presponse = new PResponse(response_string);
                // check if we have parameters
                // Construct a new HttpUrl object, since they are immutable
                // This is a bit of a cheat!
                //String url = request.getURL().toString();

                if ( url != null ){

                    AppParmsIni pini = null;
                    ListIterator<AppParmsIni> it = parmcsv.listIterator();
                    while(it.hasNext()) {
                        pini = it.next();

                        if(pmt.CurrentRequestIsTrackFromTarget(pini)&& pini.getType()==AppParmsIni.T_TRACK){
                            boolean apvIsUpdated = false;
                            List<AppValue> parmlist = pini.parmlist;
                            ListIterator<AppValue> pt = parmlist.listIterator();

                            while(pt.hasNext()){
                                AppValue av = pt.next();
                                if(av.isEnabled()){
                                    if (ParseResponse(url, presponse,  pini, av)){
                                        pt.set(av);
                                        updtcnt++;
                                        apvIsUpdated = true;
                                    }
                                }
                            }
                            if(apvIsUpdated){
                                it.set(pini);
                            }
                        }
                    }
                }


		return updtcnt;
	}



}





