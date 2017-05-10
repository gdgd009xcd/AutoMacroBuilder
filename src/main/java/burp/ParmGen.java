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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.stream.JsonParser;

import flex.messaging.util.URLDecoder;






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
            comments += _v;
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
	static String enc;
        static String formdataenc;//iso8859-1 encoding is fully  mapped binaries for form-data binaries.
	// Proxy Authentication
	// Basic username:password(base64 encoded)
	//String ProxyAuth = "Basic Y2hpa2FyYV8xLmRhaWtlOjdyOXR5QDRxMQ==";
	static String ProxyAuth;
        static ParmGenSession session;

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
		enc = "UTF-8";// default encoding.
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
	static Pattern pattern = Pattern.compile("(\"[^\"]*(?:\"\"[^\"]*)*\"|[^,\"]*)[ \t]*?,");
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
// ByteArray
//
class ByteArrayUtil {

//	private Logger log= BaseLogger.getLogger(BaseLogger.TYPE_SYSTEM);	// ログ
	private ByteArrayOutputStream stream= null;						// 保持しているbyte配列ストリーム

	/**
	 * 空のByteArrayUtilオブジェクトを生成する
	 */
	public ByteArrayUtil(){
		initByteArrayUtil(null);
	}

	/**
	 * 指定データを内部に保持するByteArrayUtilオブジェクトを生成する
	 *
	 * @param newByteArray 保持するbyte配列
	 */
	public ByteArrayUtil(byte[] newByteArray){
		initByteArrayUtil(newByteArray);
	}

	/**
	 * 指定データを内部に保持するByteArrayUtilオブジェクトを生成する
	 * 指定データが`null`の場合は空のByteArrayUtilオブジェクトを生成する
	 *
	 * @param newByteArray 保持するbyte配列
	 */
	void initByteArrayUtil(byte[] newByteArray){
		stream= new ByteArrayOutputStream();
		concat(newByteArray);
	}

	/**
	 * 内部保持しているbyte配列の長さを返します
	 *
	 * @return 内部保持しているbyte配列の長さ
	 */
	public int length(){
		return stream.size();
	}

	/**
	 * 指定されたインデックス位置にあるbyte値を返します
	 *
	 * @param index 取得したいインデックス
	 * @return byte値
	 * @throws OutOfMemoryError 内部保持している配列の長さ以上を指定した場合
	 */
	public byte byteAt(int index){
		byte[] b= getBytes();
		return b[index];
	}

	/**
	 * 保持データに指定したbyte配列を追加します<br>
	 * `ByteArrayUtil#insert()`でも同様の動作が可能だが
	 * ストリームの性質上こっちの実装が適しているため単独実装した。
	 *
	 * @param addBytes 新規に追加したいbyte配列
	 * @return 追加成功:true / 失敗:false
	 */
	public boolean concat(byte[] addBytes){

		// 評価以前の問題
		if ((stream== null)||(addBytes== null)){
			return false;
		}

		// 保持データに追加
		try {
			stream.write(addBytes);
		}
		catch (IOException e) {
		// 入出力チェックをしているので基本的には流れてこない
                    ParmVars.plog.printException(e);
                    return false;
		}
		return true;
	}

	/**
	 * `fromIndex`で指定するbyte配列`org`の位置に
	 * 配列`dest`を挿入する。
	 * <pre>
	 * Ex.
	 * 	内部保持データ= {0, 1, 2, 3};
	 * 	byte[] dest= {4, 5};
	 *
	 * 	とした時…
	 *
	 * 	insert(dest, 0)実行後の配列イメージ	: {4, 5, 0, 1, 2, 3}
	 * 	insert(dest, 1)実行後の配列イメージ	: {0, 4, 5, 1, 2, 3}
	 * 	insert(dest, 4)実行後の配列イメージ	: {0, 1, 2, 3, 4, 5}
	 * 	insert(dest, 5)実行後の配列イメージ	: OutOfMemoryErrorをスロー
	 * </pre>
	 * @param dest 挿入するbyte配列
	 * @param fromIndex 挿入するorgのインデックス
	 * @return 二つのbyte配列を合成したbyte配列
	 * @throws IllegalArgumentException - byte配列が`null`の場合と`fromIndex`に負の数を指定した場合
	 * @throws OutOfMemoryError - `org`の長さを超える`fromIndex`を指定した時(org.length < fromIndex)
	 */
	public void insert(byte[] dest, int fromIndex){

		byte[] org= getBytes();

		// 評価以前の問題
		if ((org== null)||(dest== null)||(fromIndex< 0)){	// 引数がおかしい
			throw new IllegalArgumentException();
		}
		if (org.length< fromIndex){							// 配列長を超えた値指定(OutOfBounds)
			throw new OutOfMemoryError("`fromIndex` is larger than length `org` of the specified array.");
		}

		ByteArrayOutputStream out= new ByteArrayOutputStream();		// 戻り値用ストリーム

		// ストリーム上で配列を結合
		if (fromIndex!= 0){
			out.write(org, 0, fromIndex);
		}
		try {
			out.write(dest);
		} catch (IOException e) {	// 理論的には出てこないはずだが…
                    ParmVars.plog.printException(e);
		}
		out.write(org, fromIndex, org.length - fromIndex);

		// 一度保持データを破棄して
		initByteArrayUtil(null);

		// 書き直す
		try {
			out.writeTo(stream);
		}
		catch (IOException e) {
                        ParmVars.plog.printException(e);
		}
	}

	/**
	 * 内部に保持するデータをbyte配列形式で返す
	 *
	 * @return byte配列
	 */
	public byte[] getBytes(){
		if (stream== null){
			return null;
		}
		return stream.toByteArray();
	}

	/**
	 * この配列の部分配列を返します<br>
	 * 部分配列は、指定された `beginIndex` から `endIndex` - 1 にある byte要素 までです<br>
	 * したがって、部分配列の長さは `endIndex`-`beginIndex` になります。
	 *
	 * @param beginIndex 開始インデックス(この要素を含む)
	 * @param endIndex 終了インデックス(この要素を含まない)
	 * @return 指定された部分配列
	 * @throws OutOfMemoryError
	 * 				`beginIndex`が負の数の場合。`endIndex`がこの配列要素数以上の場合。
	 * 				`beginIndex>= endIndex` が成立する場合。
	 */
	public byte[] subBytes(int beginIndex, int endIndex){

		// 評価以前の問題
		if ((beginIndex< 0)||(endIndex> length())||(beginIndex>= endIndex)){
			throw new OutOfMemoryError();
		}

		int count= endIndex - beginIndex;		// 戻り値配列の要素数
		byte[] org= getBytes();
		byte[] result= new byte[count];

		for (int i= 0; i< count; i++){
			result[i]= org[beginIndex + i];
		}

		return result;
	}

	/**
	 * この配列の部分配列を返します<br>
	 * 部分配列は、指定された `beginIndex` から終端までです<br>
	 * したがって、部分配列の長さは `endIndex - (内部配列の長さ)` になります。
	 *
	 * @param beginIndex 開始インデックス(この要素を含む)
	 * @return 指定された部分配列
	 * @throws OutOfMemoryError
	 * 				`beginIndex`が負の数。またはがこの配列要素数以上の場合。
	 */
	public byte[] subBytes(int beginIndex){
		return subBytes(beginIndex, length());
	}

	/**
	 * この配列が`fromIndex`以降において、
	 * `dest`が最初に出現する位置を返します。<br>
	 * <br>
	 * 見つからない場合は -1 を返します
	 *
	 * @param dest 検索したいbyte配列
	 * @param fromIndex 検索開始位置
	 * @return 見つかったインデックス
	 */
	public int indexOf(byte[] dest, int fromIndex){

		byte[] org= getBytes();

		// 評価以前の問題
		if ((org== null)||(dest== null)||(fromIndex< 0)){
			throw new IllegalArgumentException("`dest` or `fromIndex` is null.");
		}
		if (org.length< fromIndex){							// 配列長を超えた値指定(OutOfBounds)
			throw new OutOfMemoryError("`fromIndex` is larger than length `org` of the specified array.");
		}
		if ((org.length== 0)||(dest.length== 0)				// 長さが無いので比較しようが無い
				||(org.length< dest.length))				// 探したい配列の方が長いので見つかるワケがない
		{
			return -1;
		}

		// 評価する限界値
		int limitIndex= org.length - dest.length + 1;

		for (int i= fromIndex; i< limitIndex; i++){
			for (int j= 0; j< dest.length; j++){
				if (org[i + j]== dest[j]){
					// 最後まで一致したら戻り値に設定する
					if (j== dest.length - 1){
						return i;
					}
				}
				else{
					break;
				}
			}
		}
		// 最後まで見つからなかった
		return -1;
	}

	/**
	 * この配列内において、配列`dest`が最初に出現する位置を返します。<br>
	 * <br>
	 * 見つからない場合は -1 を返します
	 *
	 * @param dest 検索したいbyte
	 * @return 見つかったインデックス
	 */
	public int indexOf(byte[] dest){
		return indexOf(dest, 0);
	}

	/**
	 * この配列内において、`dest`が最初に出現する位置を返します。<br>
	 * <br>
	 * 見つからない場合は -1 を返します
	 *
	 * @param dest 検索したいbyte
	 * @return 見つかったインデックス
	 */
	public int indexOf(byte dest){
		byte[] b= {dest};
		return indexOf(b, 0);
	}

	/**
	 * この内部配列が、指定された配列`regex`で始まるかどうかを判定する
	 *
	 * @param regex 接頭配列
	 * @return `regex`で始まる:true / 始まらない:false
	 * @throws IllegalArgumentException `regex`が`null`の時
	 */
	public boolean startsWith(byte[] regex){

		if (regex== null){
			throw new IllegalArgumentException("`regex` is null.");
		}

		if (indexOf(regex)!= 0){
			return false;
		}
		return true;
	}

	/**
	 * この内部配列と指定したbyte配列が等しいかどうかを判定する
	 *
	 * @param dest 比較したいbyte配列
	 * @return 等しい:true / 等しくない:false
	 * @throws IllegalArgumentException `dest`が`null`の時
	 */
	public boolean equals(byte[] dest){

		if (dest== null){
			throw new IllegalArgumentException("`dest` is null.");
		}

		if ((length()!= dest.length)||(indexOf(dest)!= 0)){
			return false;
		}
		return true;
	}

	/**
	 * この配列内において配列`target`に一致する部分配列を
	 * 配列`replacement`に置換したbyte配列を返します。
	 *
	 * @param target ターゲット配列(置換したいbyte配列)
	 * @param replacement 置換後の部分配列
	 * @return 全体を置換した配列
	 */
	public byte[] replace(byte[] target, byte[] replacement){

		// 評価以前の問題
		if ((target== null)||(replacement== null)){
			throw new IllegalArgumentException("`target` or `replacement` is null.");
		}

		int index= indexOf(target);
		byte[] org= getBytes();

		// 見つからないので分解生成は不要
		if (index== -1){
			return org;
		}

		ByteArrayOutputStream out= new ByteArrayOutputStream();
		int beforeIndex= 0;
		int skipCount= target.length;							// 要素発見時の配列スキップ数

		// `target`が見つかる間は書き込み続ける
		while(index!= -1){

			out.write(org, beforeIndex, index - beforeIndex);	// 直前まで書き込む
			out.write(replacement, 0, replacement.length);		// 置換配列を書き込む
			beforeIndex= index + skipCount;						// 見つけたターゲット分飛ばす

			if (length()< index + skipCount){					// 残りの探索幅より`target`が大きければ終了
				break;
			}
			index= indexOf(target, index + skipCount);
		}

		// 残りの要素を書き込む
		out.write(org, beforeIndex, org.length - beforeIndex);

		return out.toByteArray();
	}

	/**
	 * この配列内にある全ての`oldByte`を`newByte`に置換した
	 * byte配列を返します。
	 *
	 * @param oldByte 置換したいbyte
	 * @param newByte 置換後のbyte
	 * @return 置換後のbyte配列
	 */
	public byte[] replace(byte oldByte, byte newByte){

		byte[] org= getBytes();
		int count= org.length;

		for (int i= 0; i< count; i++){
			if (org[i]== oldByte){
				org[i]= newByte;
			}
		}
		return org;
	}

	/**
	 * この配列内に含まれる部分配列`target`の個数を返す
	 *
	 * @param target 探し出す部分配列
	 * @return 見つかった部分配列の個数
	 */
	public int countOf(byte[] target){

		// 評価以前の問題
		if (target== null){
			throw new IllegalArgumentException("`target` is null.");
		}
		if (length()< target.length){
			return 0;
		}

		int count= 0;
		int skip= target.length;
		int index= indexOf(target);
		while(index!= -1){
			count++;

			if (length()< index + skip){
				break;
			}
			index= indexOf(target, index + skip);
		}

		return count;
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
	public int valparttype;// 0-path, 1-query, 2-body  3-header   16(10000) bit == no count 32(100000) == no modify
	public String value;//value
	Pattern valueregex;
	public int csvpos;
	public int col;
        public String resURL;
        public String resRegex;
        public int resPartType;
        public int resRegexPos = -1;
        public String token;//追跡token　Name

        public int tokentype;
        public static final int T_DEFAULT = 0;
        public static final int T_HIDDEN = 1;
        public static final int T_LOCATION = 2;
        public static final int T_HREF = 3;
        public static final int T_XCSRF_TOKEN = 4;
        public static final int T_TEXT = 5;

        private static String[] TokenTypeNames = {
            "",
            "hidden",
            "location",
            "href",
            "xcsrf",
            "text",
            null
        };

        public String tamattack;
        public int tamadvance;
        public int payloadposition;//I_APPEND, I_INSERT, I_REPLACE
        public boolean urlencode;// URLencodeする・しない
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
	public static final int C_NOMODIFY = 32;
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

        private void initctype(){
            if(ctypestr==null){
                ctypestr = new String[] {
                    //V_QUERY ==1
                  "", "query", "body", "header", "path", "responsebody", "requestbody", "requestquery", "requestpath",null,null,null,null,null,null,null //0-15
                };
                payloadposition = I_APPEND;
            }
            tokentype = T_HIDDEN;
        }

        AppValue(){
            initctype();
            resRegexPos = -1;
        }

        AppValue(String _Type, boolean _nomodify, String _value){
            initctype();
            setValPart(_Type);
            if(_nomodify){
                setNoModify();
            }
            value = _value;
            resRegexPos = -1;
        }

        AppValue(String _Type, boolean _nomodify, int _csvpos, String _value, boolean increment){
            initctype();
            setValPart(_Type);
            if(_nomodify){
                setNoModify();
            }
            csvpos = _csvpos;
            value = _value;
            resRegexPos = -1;
            if(increment){
                clearNoCount();
            }else{
                setNoCount();
            }
        }

        AppValue(String _Type, boolean _nomodify, String _value, boolean increment){
            initctype();
            setValPart(_Type);
            if(_nomodify){
                setNoModify();
            }
            value = _value;
            resRegexPos = -1;
            if(increment){
                clearNoCount();
            }else{
                setNoCount();
            }
        }

        AppValue(String _Type, boolean _nomodify, String _value,
                String _resURL, String _resRegex, String _resPartType, String _resRegexPos, String _token, boolean _urlenc, int _fromStepNo, int _toStepNo, String _tokentypename){
            initctype();
            setValPart(_Type);
            if(_nomodify){
                setNoModify();
            }
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

        AppValue(String _Type, boolean _nomodify,  String _value,String _name,
                String _tamattack, int _tamadvance, int _payloadposition,  boolean _urlenc){
            initctype();
            setValPart(_Type);
            if(_nomodify){
                setNoModify();
            }
            token = _name;
            value = _value;
            tamattack = _tamattack;
            tamadvance = _tamadvance;
            payloadposition = _payloadposition;
            urlencode = _urlenc;
        }

        String getPayloadPositionName(int it){
            if(payloadpositionnames.length > it && it >=0){
                return payloadpositionnames[it];
            }
            return "";
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
			_raw = URLDecoder.decode(_encoded, ParmVars.enc);
		}catch(Exception e){
			exerr = e.toString();
                        _raw = "";
		}

		return _raw;
        }

        public void setresURL(String _url){
            if(_url==null)_url = "";
            resURL = _url.trim();
        }

        public void setresRegexURLencoded(String _regex){
            if(_regex==null)_regex = "";
            resRegex = URLdecode(_regex);
        }

        public void setresRegex(String _regex){
            if(_regex==null)_regex="";
            resRegex = _regex;
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
            String avrec = QUOTE(getValPart() + (isModify()?"":"-") +(isNoCount()?"":"+")+ (_typeval==AppParmsIni.T_CSV?":"+Integer.toString(csvpos):"")) +","+ QUOTE(value)
                    + QUOTE_PREFCOMMA(resURL)
                    + QUOTE_PREFCOMMA(resRegex)
                    + QUOTE_PREFCOMMA(getResValPart())
                    + (resRegexPos!=-1?QUOTE_PREFCOMMA(Integer.toString(resRegexPos)):"") +
                    QUOTE_PREFCOMMA(token) + (_typeval==AppParmsIni.T_TRACK?QUOTE_PREFCOMMA(urlencode==true?"true":"false"):"")
                    + (_typeval==AppParmsIni.T_TRACK?QUOTE_PREFCOMMA(Integer.toString(fromStepNo)):"")
                    + (_typeval==AppParmsIni.T_TRACK?QUOTE_PREFCOMMA(Integer.toString(toStepNo)):"")
                    + QUOTE_PREFCOMMA(Integer.toString(tokentype));

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

        public String getTokentypeName(int _tktype){
            if(TokenTypeNames.length>_tktype&&_tktype>=0){
                return TokenTypeNames[_tktype];
            }
            return "";
        }

        public  int parseTokenTypeName(String tkname){
        	if(tkname!=null){
            for(int i=0; i<TokenTypeNames.length;i++){
                if(tkname.toLowerCase().equals(TokenTypeNames[i])){
                    return i;
                }
            }
        	}
            return 0;
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
            if (_valtype.indexOf("-")!=-1){//no modify
                    _valparttype |= C_NOMODIFY;
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

	boolean isModify(){
		return ((valparttype & C_NOMODIFY)==0);
	}

        public void setNoModify(){
            valparttype |= C_NOMODIFY;
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
			value = URLDecoder.decode(_value, ParmVars.enc);
			valueregex = Pattern.compile(value);
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
	/***
	void updateCount(AppParmsIni pini){
		clearNoCount();
		int r = pini.getRow();

			if ( FetchResponse.loc.isExist(r, col)){
				String gval = FetchResponse.loc.getLocVal(r, col);
				String cval = pini.getStrCnt(valparttype, col, csvpos);
				ParmVars.plog.debuglog(0, "**** Response Location updateCount update r/c=" + r + "/" + col );
				ParmVars.plog.debuglog(0, "**** Response Location updateCount Location:" + gval + "count:" + cval);
			}

	}
        * ***/

	String replaceContents(int currentStepNo, AppParmsIni pini, String contents){
                if(contents==null)return null;
                if(valueregex==null)return null;

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
			if ( spt != -1 && ept != -1 ){
				strcnt = pini.getStrCnt(currentStepNo, toStepNo,valparttype, col, csvpos);
                                ParmVars.plog.printLF();
                                boolean isnull=false;

				if (isModify()){
                                        if(strcnt!=null){
                                            ParmVars.plog.debuglog(0, "******パラメータ正規表現[" + value + "]マッチパターン[" + matchval + "]値[" + strcnt + "]\n");
                                            //
                                            ParmVars.plog.addComments("******パラメータ正規表現[" + value + "]マッチパターン[" + matchval + "]値[" + strcnt + "]\n");
                                        }else{
                                            ParmVars.plog.debuglog(0, "ERROR*パラメータ正規表現[" + value + "]マッチパターン[" + matchval + "]値が取得できません。\n");
                                            ParmVars.plog.addComments("ERROR*パラメータ正規表現[" + value + "]マッチパターン[" + matchval + "]値が取得できません。\n");
                                            isnull = true;
                                        }
				}else{
                                        if(strcnt!=null){
                                            ParmVars.plog.debuglog(0, "######無修正パラメータ正規表現[" + value + "]マッチパターン[" + matchval + "]値[" + strcnt + "]\n");
                                        }else{
                                            ParmVars.plog.debuglog(0, "ERROR#無修正パラメータ正規表現[" + value + "]マッチパターン[" + matchval + "]値が取得できません。\n");

                                            isnull = true;
                                        }
				}
                                if(isnull){//値取得失敗時は、オリジナルに戻す。
                                    strcnt = matchval;
                                    ParmVars.plog.setError(isnull);
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
	  ByteArrayUtil barray = new ByteArrayUtil();
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

	  return new String(barray.getBytes(), ParmVars.enc);
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

	public String url;
	Pattern urlregex;
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
	int row;
        Boolean pause =false;

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
            parmlist = null;
            parmlist = new ArrayList<AppValue>();
        }
        public void addAppValue(AppValue app){
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
                    return "数値昇順";
                case T_CSV:
                    return "CSVファイル昇順";
                case T_RANDOM:
                    return "乱数";
                case T_TRACK:
                    return "追跡";
                case T_TAMPER:
                    return "TamperProxy";
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
			urlregex = Pattern.compile(url);

		}catch(Exception e){
			exerr = e.toString();
		}
		return exerr;
	}

        AppParmsIni(String _URL, String _initval, String _type, String _len, ArrayList<AppValue> _apps, int _row){
            setUrl(_URL);
            inival = Integer.parseInt(_initval);
            type = _type;
            len = Integer.parseInt(_len);
            row = _row;
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
                cntfile = ParmVars.projectdir + "\\AppGenParmCnt" + Integer.toString(row) + ".txt";
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

	String getGenValue(int currentStepNo, int toStepNo, int _valparttype, int col, int csvpos){
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

				return FetchResponse.loc.getLocVal(currentStepNo, toStepNo, row, col);
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

	String getStrCnt(int currentStepNo, int toStepNo,int _valparttype, int col, int csvpos){
		//if ( cstrcnt == null|| typeval == 3){
				cstrcnt = getGenValue(currentStepNo,toStepNo,_valparttype, col, csvpos);
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
                     return new Object[] {app.getValPart(), (app.isModify()?false:true), app.value, app.isNoCount()?false:true};
                case T_RANDOM:
                    break;
                case T_CSV:
                    return new Object[] {app.getValPart(), (app.isModify()?false:true), app.csvpos, app.value, app.isNoCount()?false:true};
                case T_TRACK:
                    return new Object[] {app.getValPart(), (app.isModify()?false:true), app.value,
                        app.resURL,
                        app.resRegex,
                        app.getResValPart(),
                        Integer.toString(app.resRegexPos),
                    app.token, app.urlencode, app.fromStepNo, app.toStepNo, app.getTokentypeName(app.tokentype)};
                case T_TAMPER:
                    return new Object[] {app.getValPart(), (app.isModify()?false:true), app.value,
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

	public static ArrayList<AppParmsIni> parmcsv = null;
        public static ArrayList<AppParmsIni> parmjson = null;
        public static ArrayList<AppParmsIni> trackcsv = null;// response tracking
        public static boolean hasTrackRequest=false;//==true: リクエストを追跡
        public static ParmGenTop twin = null;
        public static boolean ProxyInScope = true;
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

                                JsonParser parser = Json.createParser(new StringReader(jsondata));
                                String keyname = null;
                                boolean errflg = false;
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
                                           break;
                                       case END_ARRAY:
                                           arraylevel--;
                                           break;
                                       case KEY_NAME:
                                          keyname = parser.getString();
                                          break;
                                       case START_OBJECT:
                                       case END_OBJECT:
                                           errflg = gjson.Parse(arraylevel, event, keyname, null);
                                           break;
                                       case VALUE_TRUE:
                                           bval = true;
                                       case VALUE_FALSE:
                                           errflg = gjson.Parse(arraylevel, event, keyname, bval);
                                           break;
                                       case VALUE_STRING:
                                       case VALUE_NUMBER:
                                           obj = parser.getString();
                                       case VALUE_NULL:
                                          errflg = gjson.Parse(arraylevel, event, keyname, obj);
                                          break;
                                    }
                                 }
				if(errflg){
                                   rlist = gjson.Getrlist();
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



	PRequest ParseRequest(PRequest prequest,  ByteArrayUtil boundaryarray, ByteArrayUtil _contarray, AppParmsIni pini, AppValue av)  {


	//	String[] headers=request.getHeaderNames();
	//	boolean noauth = false;
	//	for(String header : headers){
	//		if ( header.indexOf("Authorization")==-1){
	//			noauth = true;
	//		}
	//		//printlog(header+" : " + request.getHeader(header), true);
	//	}

		ArrayList<String []> headers = prequest.getHeaders();

		String method = prequest.getMethod();
		String url = prequest.getURL();
		String path = new String(url);
		ParmVars.plog.debuglog(1, "method[" + method + "] request[" + url + "]");
		int qpos = -1;
		switch(av.valparttype & AppValue.C_VTYPE){
		case AppValue.V_PATH://path
			// path = url
			String n_path = av.replaceContents(pmt.getStepNo(), pini, path);
			if (n_path != null && !path.equals(n_path) && av.isModify()){
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
				String n_query = av.replaceContents(pmt.getStepNo(),pini, query);
                                ParmVars.plog.debuglog(1, query);
                                ParmVars.plog.debuglog(1, n_query);
				if ( n_query!=null && !query.equals(n_query) && av.isModify()){
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
				String n_hval = av.replaceContents(pmt.getStepNo(),pini, hval);
				if (n_hval !=null && !hval.equals(n_hval) && av.isModify()){
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
	        			content = new String(_contarray.getBytes(), ParmVars.enc);
	        		}catch(UnsupportedEncodingException e){
	        			content = null;
	        		}
		        	String n_content = av.replaceContents(pmt.getStepNo(),pini, content);
		        	if ( content != null && !content.equals(n_content) && av.isModify()){
		        		ParmVars.plog.debuglog(1, " Original body[" + content + "]");
		        		ParmVars.plog.debuglog(1, " Modified body[" + n_content + "]");
						_contarray.initByteArrayUtil(n_content.getBytes());
		        		return prequest;
		        	}
	        	}else{//multipart/form-data
	        		ParmVars.plog.debuglog(1, "multipart/form-data");
	        		ByteArrayUtil n_array = new ByteArrayUtil();
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
                                                partenc = ParmVars.enc;
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
	        				String n_partdatastr = av.replaceContents(pmt.getStepNo(), pini, partdatastr);
	        				if(n_partdatastr!=null && partdatastr != null && !partdatastr.equals(n_partdatastr) && av.isModify()){
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
	        			_contarray.initByteArrayUtil(n_array.getBytes());
		        		return prequest;
	        		}
	        	}
			}

			break;
		}


		return null;
	}

boolean FetchRequest(PRequest prequest,   AppParmsIni pini, AppValue av){
    String url = prequest.getURL();
    int row,col;
    row = pini.row;
    col = av.col;
    switch(av.getResTypeInt()){
        case AppValue.V_REQTRACKBODY:
            return FetchResponse.loc.reqbodymatch(pmt.getStepNo(), av.fromStepNo,url, prequest, row, col, true, av.resRegexPos, av.token);
        default:
            break;
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
		int qpos = -1;
		switch(av.resPartType & AppValue.C_VTYPE){
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
			rflag = FetchResponse.loc.headermatch(pmt.getStepNo(), av.fromStepNo,url, presponse, row, col, true,av.token, av.tokentype);
			break;
                case AppValue.V_REQTRACKBODY://request追跡なのでNOP.
                    break;
                case AppValue.V_AUTOTRACKBODY://responseのbodyを追跡
                    autotrack = true;
		default:
                        try {
                            //body
                            //ParmVars.plog.debuglog(0, "ParseResponse: V_BODY " + rowcolstr);
                            rflag = FetchResponse.loc.bodymatch(pmt.getStepNo(),av.fromStepNo,url, presponse, row, col, true, autotrack, av.resRegexPos, av.token, av.urlencode, av.tokentype);
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(ParmGen.class.getName()).log(Level.SEVERE, null, ex);
                        }
			break;
		}


		return rflag;
	}

	ParmGen(ParmGenMacroTrace _pmt){
                pmt = _pmt;
		initMain();
	}

	void initMain(){
		//main start.
		// csv load
		// parmcsvはローカル
		if ( parmcsv == null ){
			parmcsv = loadJSON();
                        //ArrayList<AppParmsIni> parmjson = loadJSON();

                        if(parmcsv==null)return;
                        FetchResponse.loc = new LocVal(parmcsv.size());
                        Iterator<AppParmsIni> api = parmcsv.iterator();
                        while(api.hasNext()){
                            AppParmsIni pini = api.next();
                            int row = pini.row;
                            Iterator<AppValue> apv = pini.parmlist.iterator();
                            if(pini.getType()==AppParmsIni.T_TRACK){
                                if(trackcsv==null){
                                    trackcsv = new ArrayList<AppParmsIni>();
                                }
                                while(apv.hasNext()){
                                    AppValue apval = apv.next();
                                    if(apval.getResTypeInt()>=AppValue.V_REQTRACKBODY){
                                        hasTrackRequest =true;
                                    }

                                    int col = apval.col;
                                    //loc.setURLRegex(".*test.kurashi-research.jp:(\\d+)/top.php.*", 0,0);
                                    //ParmVars.plog.debuglog(0, "r,c,resURL:" + Integer.toString(row) + "," + Integer.toString(col) + ","+ apval.resURL);
                                    FetchResponse.loc.setURLRegex(apval.resURL, row, col);
                                    //loc.setRegex("'PU31', '00', 'exchange', '([a-z0-9]+)'",0,0);
                                    FetchResponse.loc.setRegex(apval.resRegex, row, col);
                                }
                                trackcsv.add(pini);

                            }
                        }
			//debuglog(0, "loadCSV executed.");
		}
	}

        public void reset(){
            parmcsv = null;trackcsv=null;
            FetchResponse.loc = null;
            hasTrackRequest = false;
            initMain();
        }

	byte[] Run(byte[] requestbytes){
                String request_string = null;
                try{
                    request_string = new String(requestbytes, ParmVars.enc);
                }catch(Exception e){
                    ParmVars.plog.printException(e);
                }
		ByteArrayUtil boundaryarray = null;
		ByteArrayUtil contarray = null;


		if( parmcsv == null){
                    //NOP
		}else{
			// main loop
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
				Iterator<AppParmsIni> it = parmcsv.iterator();
				while(it.hasNext()) {
					pini = it.next();
					Matcher urlmatcher = pini.urlregex.matcher(url);
					if ( urlmatcher.find() ){
                                                //Content-Type: multipart/form-data; boundary=---------------------------30333176734664
                                                if (content_type != null && !content_type.equals("") && hasboundary ==false){//found
                                                        Pattern ctypepattern = Pattern.compile("multipart/form-data;.*?boundary=(.+)$");
                                                        Matcher ctypematcher = ctypepattern.matcher(content_type);
                                                        if ( ctypematcher.find()){
                                                                String Boundary = ctypematcher.group(1);
                                                                ParmVars.plog.debuglog(1, "boundary=" + Boundary);
                                                                Boundary = "--" + Boundary;//
                                                                boundaryarray = new ByteArrayUtil(Boundary.getBytes());

                                                        }
                                                        hasboundary = true;
                                                }
						ParmVars.plog.debuglog(0, "***URL正規表現[" + pini.url + "]マッチパターン[" + url + "]");
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
								contarray = new ByteArrayUtil(bytes);
							}
							bytes = null;
                                                        * **/
                                                    ByteArrayUtil warray = new ByteArrayUtil(requestbytes);
                                                    try{
                                                        //ParmVars.plog.debuglog(1,"request length : " + Integer.toString(warray.length()) + "/" + Integer.toString(prequest.getParsedHeaderLength()));
                                                        if(warray.length()>prequest.getParsedHeaderLength()){
                                                            byte[] wbyte = warray.subBytes(prequest.getParsedHeaderLength());
                                                            contarray = new ByteArrayUtil(wbyte);
                                                        }
                                                    }catch(Exception e ){
                                                        //contarray is null . No Body...
                                                    }

						}

						ArrayList<AppValue> parmlist = pini.parmlist;
						Iterator<AppValue> pt = parmlist.iterator();
						if (parmlist == null || parmlist.isEmpty()){
							//
						}
                                                ParmVars.plog.debuglog(1,"start");
						while(pt.hasNext()){
                                                    ParmVars.plog.debuglog(1, "loopin");
							AppValue av = pt.next();
							if ((tempreq = ParseRequest(prequest, boundaryarray, contarray, pini, av))!=null){
								modreq = tempreq;
								prequest = tempreq;
							}
						}
                                                ParmVars.plog.debuglog(1,"end");
					}
				}
			}
                        byte[] retval = null;
                        //ここで、prequestでexpiredしたcookie値を削除する。

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
                        if(hasTrackRequest){

                            AppParmsIni pini = null;
                            Iterator<AppParmsIni> it = trackcsv.iterator();
                            while(it.hasNext()){
                                pini = it.next();
                                ArrayList<AppValue> parmlist = pini.parmlist;
                                Iterator<AppValue> pt = parmlist.iterator();
                                boolean fetched;
                                while(pt.hasNext()){
                                        AppValue av = pt.next();
                                        fetched = FetchRequest(prequest,  pini, av);
                                }
                            }

                        }
                        return retval;
		}

                return null;
	}

    int ResponseRun(String url,  byte[] response_bytes, String _enc){

        int updtcnt = 0;

		if( trackcsv == null){
			//ParmVars.plog.printlog("loadCSV failed. program has aborted\n", true);
		}else{
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
				Iterator<AppParmsIni> it = trackcsv.iterator();
				while(it.hasNext()) {
					pini = it.next();
                                        //if (pini.getType()==AppParmsIni.T_TRACK){

                                                    ArrayList<AppValue> parmlist = pini.parmlist;
                                                    Iterator<AppValue> pt = parmlist.iterator();

                                                    while(pt.hasNext()){
                                                            AppValue av = pt.next();

                                                            if (ParseResponse(url, presponse,  pini, av)){
                                                                updtcnt++;
                                                            }
                                                    }
                                        //}
				}
			}
		}

		return updtcnt;
	}


/****
	void ResponseCountUpdate(int r, int c){
		AppParmsIni pini = null;
		if ( parmcsv != null && parmcsv.size()>0){
			if(r > -1 && r <= parmcsv.size()){
				pini = parmcsv.get(r);
				ArrayList<AppValue> parmlist = pini.parmlist;
				AppValue av = null;
				if ( c > -1 && c <= parmlist.size()){
					av = parmlist.get(c);
					av.updateCount(pini);
				}
			}
		}
	}
        * ***/
}





