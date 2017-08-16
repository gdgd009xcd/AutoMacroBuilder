package burp;



import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import flex.messaging.util.URLEncoder;


//<?xml version="1.0" encoding="utf-8"?>
//<AuthUpload>
//	<codeResult>0</codeResult>
//	<password>eUnknfj73OFBrMenCfFh</password>
//</AuthUpload>









//
//class variable
//
// FetchResponse初期化
//

class LocVal {
	//
	int noclear = 0;
	int rmax = 1;
	int cmax = COLMAX;
        static final int COLMAX = 50;
	boolean sticky = false;
	String data_response = null;
	//String [][] locarray = new String[rmax][cmax];
        String [][] locarray;
	//Pattern [][] regexes = new Pattern[rmax][cmax];
        Pattern [][] regexes;
	//Pattern [][] urlregexes = new Pattern[rmax][cmax];
        Pattern [][] urlregexes;
        //stepno
        int [][] responseStepNos;
        //distance = currentStepNo responseSteNos
        int [][] distance;
	PLog _logger = null;
	Encode _enc = null;
	int rpos = 0;
	int cpos = 0;

	//
	LocVal (int _rmax){
		_logger = ParmVars.plog;

		//pattern = "<AuthUpload>(?:.|\r|\n|\t)*?<password>([a-zA-Z0-9]+)</password>";
		allocLocVal(_rmax);

		_enc = ParmVars.enc;
		if(_enc == null  ){
			_enc = Encode.UTF_8;
		}
		initLocVal();


	}

        private String strrowcol(int r, int c){
            return Integer.toString(r) + "," + Integer.toString(c);
        }

	void setSticky(){
		sticky = true;
		//printlog("***fetchresponse sticky***");
	}

        void allocLocVal(int _rmax){
            if(_rmax>0){
                rmax = _rmax;
                locarray = new String[_rmax][cmax];
                regexes = new Pattern[_rmax][cmax];
                urlregexes = new Pattern[_rmax][cmax];
                responseStepNos = new int[_rmax][cmax];
                distance = new int[_rmax][cmax];
            }else{
                rmax = 0;
                locarray = null;
                regexes = null;
                urlregexes = null;
                responseStepNos = null;
                distance = null;
            }
        }

	void initLocVal(){
		for(int i = 0; i< rmax; i++){
			for(int j = 0 ; j<cmax ; j++){
				locarray[i][j] = null;
				regexes[i][j] = null;
				urlregexes[i][j] = null;
                                responseStepNos[i][j] = -1;
                                distance[i][j] = -1;
			}
		}
	}

	void clearCachedLocVal(){
		for(int i = 0; i< rmax; i++){
			for(int j = 0 ; j<cmax ; j++){
				locarray[i][j] = null;
                                responseStepNos[i][j] = -1;
                                distance[i][j] = -1;
			}
		}
	}
        void setStepNo(int snum, int r, int c){
            if(isValid(r,c)){
                responseStepNos[r][c] = snum;
            }
        }

	void setRegex(String pattern, int r, int c){
            if(isValid(r,c) && pattern != null && !pattern.isEmpty()){
                try{
                    regexes[r][c] = Pattern.compile(pattern);
                }catch(Exception e){
                    ParmVars.plog.debuglog(0, "ERROR: setRegex " + e.toString());
                }
            }
	}
	void setURLRegex(String pattern, int r, int c){
            if(isValid(r,c)&& pattern != null && !pattern.isEmpty()){
                printlog("setURLRegex:r,c,url=" + strrowcol(r,c) + "," + pattern);
                try{
                    urlregexes[r][c] = Pattern.compile(pattern);
                }catch(Exception e){
                    printlog("ERROR: setURLRegex " + e.toString());
                }
            }
	}

	void clearResponse(){
		data_response = null;
	}

    String getLocVal(int currentStepNo, int toStepNo, int r, int c) {
        String rval = null;
        if (isValid(r, c)) {

            String v = locarray[r][c];
            int responseStepNo = responseStepNos[r][c];
            int StepNodistance = distance[r][c];

            //toStepNo <0 :currentStepNo == responseStepNo - toStepNo
            if (toStepNo < 0) {
                if (currentStepNo == responseStepNo - toStepNo) {
                    rval = v;
                    //return v;
                }
            } else if (toStepNo > 0) {
                if (currentStepNo == toStepNo) {
                    rval = v;
                    //return v;
                }
            } else if (toStepNo == 0) {
                rval = v;
                //return v;
            }
            if(rval!=null){
                int newdistance = currentStepNo - responseStepNo;
                if(StepNodistance>=0){
                    if(StepNodistance<newdistance){
                        rval = null;
                    }
                }
                if(rval!=null){
                    distance[r][c] = newdistance;
                }
            }
        }
        return rval;
    }

        int getStepNo(int r, int c){
            if(isValid(r,c)){
                return responseStepNos[r][c];
            }
            return -1;
        }

	void setLocVal(int currentStepNo, int fromStepNo, int r, int c, String val, boolean overwrite){
            if(isValid(r,c)){
		if ( locarray[r][c] == null ){
			locarray[r][c] = val;
		}else if(sticky == false||overwrite == true){
			locarray[r][c] = val;
		}else{
			//printlog("setLocVal sticky r,c,noclear, locarray[r][c]:" + r + "," + c + "," + noclear + "," + locarray[r][c]);
		}
                if(fromStepNo<0||currentStepNo==fromStepNo){
                    setStepNo(currentStepNo, r, c);
                }
            }
		//printlog("setLocVal r,c,noclear, val:" + r + "," + c + "," + noclear + "," + val);
	}

	void copyLocVal(int fr, int fc, int tr, int tc){
            if(isValid(fr,fc) && isValid(tr,tc)){
		String v = locarray[fr][fc];
                int stepno = responseStepNos[fr][fc];
		setLocVal(stepno, -1, tr, tc, v, true);
            }
	}

        boolean isValid(int r, int c){
            if(rmax > 0 && r >= 0 && r < rmax && c >= 0 && c < COLMAX){
		return true;

            }
            return false;
	}

	boolean isExist(int r, int c){
            if(isValid(r,c)){
		if (locarray[r][c] == null ){
			return false;
		}
            }else{
                return false;
            }
            return true;
	}

	int noClear(){
		return noclear;
	}
	void resetnoClear(){
		noclear = 0;
	}

	void printlog(String v){
		_logger.printlog(v, true);
	}


	//
	// header match
	//
	boolean headermatch(int currentStepNo, int fromStepNo, String url, PResponse presponse, int r, int c,
			boolean overwrite, String name, AppValue av) {
		AppValue.TokenTypeNames _tokentype = av.tokentype;
		String comments = "";
		if (urlmatch(url, r, c)) {
			if (_tokentype == AppValue.TokenTypeNames.LOCATION) {
				ParmGenToken tkn = presponse.fetchNameValue(name, _tokentype, 0);
				if (tkn != null) {
					ParmGenTokenValue tval = tkn.getTokenValue();
					if (tval != null) {// value値nullは追跡しない
						String matchval = tval.getValue();
						if (matchval != null && !matchval.isEmpty()) {
							comments = "*****FETCHRESPONSE header r,c/ header: value" + r + "," + c + " => " + matchval;
							printlog(comments);
							ParmVars.plog.addComments(comments);
							setLocVal(currentStepNo, fromStepNo, r, c, matchval, overwrite);
							return true;
						}
					} else {
						comments = "xxxxxIGNORED FETCHRESPONSE header r,c/ header: value" + r + "," + c + " => null";
						printlog(comments);
						ParmVars.plog.addComments(comments);
					}
				}
			} else if (regexes[r][c] != null) {
				//
				int size = presponse.getHeadersCnt();
				for (int i = 0; i < size; i++) {
					// String nvName = (nv[i]).getName();
					// String nvValue = (nv[i]).getValue();
					// String hval = nvName + ": " + nvValue;
					String hval = presponse.getHeaderLine(i);
					Matcher matcher = null;
					try {
						matcher = regexes[r][c].matcher(hval);
					} catch (Exception e) {
						printlog("matcher例外：" + e.toString());
					}
					if (matcher.find()) {
						int gcnt = matcher.groupCount();
						String matchval = null;
						for (int n = 0; n < gcnt; n++) {
							matchval = matcher.group(n + 1);
						}

						if (matchval != null) {
							if (!matchval.isEmpty()) {// value値nullは追跡しない
								comments = "*****FETCHRESPONSE header r,c/ header: value" + r + "," + c + "/" + hval
										+ " => " + matchval;
								printlog(comments);
								ParmVars.plog.addComments(comments);
								setLocVal(currentStepNo, fromStepNo, r, c, matchval, overwrite);
								return true;
							} else {
								comments = "xxxxxIGNORED FETCHRESPONSE header r,c/ header: value" + r + "," + c + "/"
										+ hval + " => null";
								printlog(comments);
								ParmVars.plog.addComments(comments);
							}
						}
					}
				}
			}
		}
		return false;
	}
	//
	// body match
	//
	boolean bodymatch(int currentStepNo, int fromStepNo, String url, PResponse presponse, int r, int c,
			boolean overwrite, boolean autotrack, AppValue av, int fcnt, String name, boolean _uencode)
			throws UnsupportedEncodingException {
		AppValue.TokenTypeNames _tokentype = av.tokentype;
		if (urlmatch(url, r, c)) {

			String body = presponse.getBody();

			if (autotrack) {
				// ParmGenParser parser = new ParmGenParser(body);
				// ParmGenToken tkn = parser.fetchNameValue(name, fcnt,
				// _tokentype);
				ParmGenToken tkn = presponse.fetchNameValue(name, _tokentype, fcnt);
				if (tkn != null) {
					ParmGenTokenValue tval = tkn.getTokenValue();
					if (tval != null) {
						String v = tval.getValue();
						if (v != null && !v.isEmpty()) {// value null値は追跡しない。
							String comments = "*****FETCHRESPONSE auto track body r,c,p:" + r + "," + c + "," + fcnt + ": "	+ name + "=" + v;
							printlog(comments);
							ParmVars.plog.addComments(comments);
							if (_uencode == true) {
								String venc = v;
								try {
									venc = URLEncoder.encode(v, ParmVars.enc.getIANACharset());
								} catch (UnsupportedEncodingException e) {
									// NOP
								}
								v = venc;
							}
							String ONETIMEPASSWD = v.replaceAll(",", "%2C");

							setLocVal(currentStepNo, fromStepNo, r, c, ONETIMEPASSWD, overwrite);
							return true;
						} else {
							String comments = "xxxxx FAILED FETCHRESPONSE auto track body r,c,p:" + r + "," + c + ","
									+ fcnt + ": " + name + "=" + "null";
							printlog(comments);
							ParmVars.plog.addComments(comments);
						}
					}
				}
			}

			Matcher matcher = null;

			if (regexes != null && av.resRegex != null && !av.resRegex.isEmpty()) {

				try {
					matcher = regexes[r][c].matcher(body);
				} catch (Exception e) {
					String comments =  "xxxxx EXCEPTION FETCHRESPONSE " + name + " 正規表現[" + av.resRegex  + "] 例外：" + e.toString();
					printlog(comments);
					ParmVars.plog.addComments(comments);
					matcher = null;
				}

				if (matcher != null && matcher.find()) {
					int gcnt = matcher.groupCount();
					String matchval = null;
					for (int n = 0; n < gcnt; n++) {
						matchval = matcher.group(n + 1);
					}

					if (matchval != null) {
						switch (av.resencodetype) {
						case JSON:
							ParmGenJSONDecoder jdec = new ParmGenJSONDecoder(null);
							matchval = jdec.decodeStringValue(matchval);
							break;
						default:
							break;
						}
						if (_uencode == true) {
							String venc = matchval;
							try {
								venc = URLEncoder.encode(matchval, ParmVars.enc.getIANACharset());
							} catch (UnsupportedEncodingException e) {
								// NOP
							}
							matchval = venc;
						}
						String ONETIMEPASSWD = matchval.replaceAll(",", "%2C");
						String comments = "";

						if (ONETIMEPASSWD != null && !ONETIMEPASSWD.isEmpty()) {// value値nullは追跡しない
							comments = "*****FETCHRESPONSE body r,c:" + r + "," + c + ": " + name + "=" + ONETIMEPASSWD;
							printlog(comments);
							ParmVars.plog.addComments(comments);
							setLocVal(currentStepNo, fromStepNo, r, c, ONETIMEPASSWD, overwrite);
							return true;
						} else {
							comments = "xxxxxx FAILED FETCHRESPONSE body r,c:" + r + "," + c + ": " + name + "=" +  "null";
							printlog(comments);
							ParmVars.plog.addComments(comments);
						}
					}
				}
			}
		}
		return false;
	}

	boolean reqbodymatch(int currentStepNo, int fromStepNo, String url, PRequest prequest, int r, int c,
			boolean overwrite, int fcnt, String name) {
		String comments = "";
		if (urlmatch(url, r, c)) {
			ArrayList<String[]> namelist = prequest.getBodyParams();
			Iterator<String[]> it = namelist.iterator();
			while (it.hasNext()) {
				String[] nv = it.next();
				if (name.equals(nv[0])) {
					if (nv.length > 1 && nv[1] != null && !nv[1].isEmpty()) {// value値nullは追跡しない
						comments =
								"******FETCH REQUEST body r,c: name=value:" + r + "," + c + ": " + nv[0] + "=" + nv[1];
						printlog(comments);
						ParmVars.plog.addComments(comments);
						setLocVal(currentStepNo, fromStepNo, r, c, nv[1], overwrite);
						return true;
					} else {
						comments = "xxxxxFAILED FETCH REQUEST body r,c: name=value:" + r + "," + c + ": " + nv[0]
								+ "=null";
						printlog(comments);
						ParmVars.plog.addComments(comments);
					}
				}
			}
		}
		return false;
	}
	//
	// URL match
	//
	boolean urlmatch(String url, int r, int c){
                if (isValid(r,c)){
                    try {
                            if ( urlregexes[r][c] != null){
                                    Matcher	matcher = urlregexes[r][c].matcher(url);
                                    if ( matcher.find()){
                                            //printlog("*****FETCHRESPONSE URL match:" + url);
                                            ParmVars.plog.debuglog(0, " FETCH RESPONSE URL matched:[" + url + "]");
                                            return true;
                                    }
                                    //printlog("urlmatch find failed:r,c,url, rmax=" + strrowcol(r,c) + "," + url + "," + Integer.toString(rmax));

                            }
                    }catch (Exception e){
                            printlog("matcher例外：" + e.toString());
                    }
                }
		return false;
	}


}

//
//class variable
//
// FetchResponse初期化
//

class FetchResponse {
	// グローバルパラメータ
	static LocVal loc = null;
	//
	// static変数初期化
	//
	static {


		//loc.setSticky();

		//１）指定したポジションr,cの正規表現グループを指定
		//発注取消用
		//global.Location.setRegex("新規保存完了しました.*?\\<rr:Target name=\"OrderInfo\">\\<rr:Key name=\"ID\" value=\"([0-9A-Za-z,]+?)\"/>\\</rr:Target>\\<rr:Contents>\\<o:OrderInfo xmlns:o=\"http://animate\\.es-presso.jp/OrderInfo\" xmlns:lnk=\"http://esp\\.es-presso.jp/Link\">(?:\\r|\\n|\\t|[ ])*?<o:ID>\\<\\!\\[CDATA\\[(?:[0-9A-Za-z,]+?)\\]\\]\\>\\</o\\:ID\\>(?:\\r|\\n|\\t|[ ])*?<o:Cart>(?:\\r|\\n|\\t|[ ])*?<lnk:Link target=\"Cart\">(?:\\r|\\n|\\t|[ ])*?<lnk:Key name=\"ID\" value=\"(?:[0-9A-Za-z,]+?)\"/>", 0, 0);
		//2-2-3.発注履歴カート番号承認実行
		//発注取消用カートＩＤ Line:0
		//https://musha.pub.toppan.co.jp:8463/animate/v1/Cart/Searchのレス。
		//global.Location.setRegex("<c:ID>\\<\\!\\[CDATA\\[([A-Z]{2}(?:[0-9]|%2C|,)+)\\]\\]\\>\\<\\/c\\:ID>(?:\\r|\\n|\\t|[ ])*?<c:Name>\\<\\!\\[CDATA\\[TSS承認テストＡＡＡ１\\]\\]\\>\\<\\/c\\:Name>", 0, 0);
		//オーダーＩＤhttps://musha.pub.toppan.co.jp:8463/animate/v1/Cart/Getのレス
		//global.Location.setRegex("<o:ID>\\<\\!\\[CDATA\\[([A-Z]{2}(?:[0-9]|%2C|,)+)\\]\\]\\>\\</o:ID>", 0, 3);
		//発注申請用 Line:2 https://musha.pub.toppan.co.jp:8463/animate/v1/OrderInfo/Create
		//global.Location.setRegex("新規保存完了しました.*?\\<rr:Target name=\"OrderInfo\">\\<rr:Key name=\"ID\" value=\"([0-9A-Za-z,]+?)\"/>\\</rr:Target>\\<rr:Contents>\\<o:OrderInfo xmlns:o=\"http://animate\\.es-presso.jp/OrderInfo\" xmlns:lnk=\"http://esp\\.es-presso.jp/Link\">(?:\\r|\\n|\\t|[ ])*?<o:ID>\\<\\!\\[CDATA\\[(?:[0-9A-Za-z,]+?)\\]\\]\\>\\</o\\:ID\\>(?:\\r|\\n|\\t|[ ])*?<o:Cart>(?:\\r|\\n|\\t|[ ])*?<lnk:Link target=\"Cart\">(?:\\r|\\n|\\t|[ ])*?<lnk:Key name=\"ID\" value=\"(?:[0-9A-Za-z,]+?)\"/>", 2, 3);
		//global.Location.setRegex("新規保存完了しました.*?\\<rr:Target name=\"OrderInfo\">\\<rr:Key name=\"ID\" value=\"(?:[0-9A-Za-z,]+?)\"/>\\</rr:Target>\\<rr:Contents>\\<o:OrderInfo xmlns:o=\"http://animate\\.es-presso.jp/OrderInfo\" xmlns:lnk=\"http://esp\\.es-presso.jp/Link\">(?:\\r|\\n|\\t|[ ])*?<o:ID>\\<\\!\\[CDATA\\[(?:[0-9A-Za-z,]+?)\\]\\]\\>\\</o\\:ID\\>(?:\\r|\\n|\\t|[ ])*?<o:Cart>(?:\\r|\\n|\\t|[ ])*?<lnk:Link target=\"Cart\">(?:\\r|\\n|\\t|[ ])*?<lnk:Key name=\"ID\" value=\"([0-9A-Za-z,]+?)\"/>", 2, 0);
		//ランク削除
		//global.Location.setRegex("<ic2:ID>\\<\\!\\[CDATA\\[(\\d+?)\\]\\]>\\</ic2:ID>(?:\\r|\\n|[ \\t])*?<ic2:Name>\\<\\!\\[CDATA\\[ＴＳＳＴＥＳＴランク１\\]\\]\\>\\</ic2:Name>", 4, 0);
		//商品カテゴリ削除
		//global.Location.setRegex("<ic1:ID>\\<\\!\\[CDATA\\[(\\d+?)\\]\\]>\\</ic1:ID>(?:\\r|\\n|[ \\t])*?<ic1:Name>\\<\\!\\[CDATA\\[ＴＳＳＴＥＳＴカテゴリ１\\]\\]\\>\\</ic1:Name>", 4, 0);
		//発注分類削除
		//global.Location.setRegex("<oc:ID>\\<\\!\\[CDATA\\[(\\d+?)\\]\\]>\\</oc:ID>(?:\\r|\\n|[ \\t])*?<oc:Name>\\<\\!\\[CDATA\\[ＴＳＳ発注分類１\\]\\]\\>\\<\\/oc:Name>", 5, 0);
		//店舗削除
		//global.Location.setRegex("<s:ID>\\<\\!\\[CDATA\\[(\\d+?)\\]\\]>\\</s:ID>(?:\\r|\\n|[ \\t])*?<s:Name>\\<\\!\\[CDATA\\[ＴＳＳ店舗１\\]\\]\\>\\<\\/s:Name>", 6, 0);

		//loc.setURLRegex(".*test.kurashi-research.jp:(\\d+)/top.php.*", 0,0);
		//loc.setRegex("'PU31', '00', 'exchange', '([a-z0-9]+)'",0,0);

		// for test
		//global.Location.setURLRegex(".*hello.php.*", 0,0);
		//global.Location.setRegex("(Hello,world)",0,0);
	}
}





//Request request = connection.getRequest();
//Response response = connection.getResponse();
//String url = request.getURL().toString();
//global.Location.clearResponse();

// for TEST
//if(global.Location.bodymatch(url, response, 0, 0, true)){//
//}


// ２）指定したポジションr,cのレスポンスマッチを指定
//カートＩＤ 削除
//if(global.Location.bodymatch(url, response, 0, 0, false)){
//	global.Location.copyLocVal(0, 0, 0, 1, false);
//	global.Location.copyLocVal(0, 0, 0, 2, false);
//	global.Location.copyLocVal(0, 0, 0, 4, false);
//	global.Location.copyLocVal(0, 0, 1, 0, false);
//	global.Location.copyLocVal(0, 0, 1, 1, false);
//}

//みんなのわんこ Set-Cookie取得
//if(global.Location.headermatch(url, response, 1, 9, false)){//LocationヘッダーのあるレスポンスのCookieを取得
//	if(global.Location.headermatch(url, response, 1, 0, false)){
//	}
//	if(global.Location.headermatch(url, response, 1, 1, false)){
//	}
//}

//if(global.Location.bodymatch(url, response, 0, 0, true)){//
//}



//global.Location.clearResponse();
//request = null;
//response = null;
//url = null;
//connection = null;


