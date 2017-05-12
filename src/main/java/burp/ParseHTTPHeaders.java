package burp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
//HTTP request/response parser
//

class ParseHTTPHeaders {
	Pattern valueregex;
	//Pattern formdataregex;
	String formdataheader;
	String formdatafooter;
	Pattern formdataheaderregex;
	Pattern formdatafooterregex;
	String[] nv;
	boolean crlf;
	String method;
	String url;
	boolean isSSL;//==true then ssl
	String path;
	String path_pref_url;
	String protocol;
	String status;
	String reason;
	String host;
	int port;
	public ArrayList<String> pathparams;
	public ArrayList<String[]> cookieparams;

	public ArrayList<String[]> queryparams;
	private ArrayList<String[]> bodyparams;
	ArrayList<String []> headers;
	public HashMap<String,ArrayList<String[]>> set_cookieparams;//String Key, ArrayList name=value pair
	String content_type; // Content-Type: image/gif
	String content_subtype;
	String charset;
	String boundary;
	int parsedheaderlength;
	boolean isHeaderModified;//==true parsedheaderlengthは再計算。
	int content_length;
	boolean formdata;

	String body;
	byte[] bytebody;

	String message;// when update method(Ex. setXXX) is called, then this value must set to null;
	boolean isrequest;// == true - request, false - response

	private void init(){
		valueregex = Pattern.compile("(([^\r\n:]*):{0,1}[ \t]*([^\r\n]*))(\r\n)");
		//formdataregex = Pattern.compile("-{4,}[a-zA-Z0-9]+(?:\r\n)(?:[A-Z].* name=\"(.*?)\".*(?:\r\n))(?:[A-Z].*(?:\r\n)){0,}(?:\r\n)((?:.|\r|\n)*?)(?:\r\n)-{4,}[a-zA-Z0-9]+");
		formdataheader = "(?:[A-Z].* name=\"(.*?)\".*(?:\r\n))(?:[A-Z].*(?:\r\n)){0,}(?:\r\n)";
		formdatafooter = "(?:\r\n)";

		isSSL = false;
		crlf = false;
		isrequest = false;
		message = null;
		pathparams = new ArrayList<String>();
		cookieparams = new ArrayList<String[]>();
		set_cookieparams = new HashMap<String,ArrayList<String[]>>();
		charset = "";
		content_type = "";
		content_subtype = "";
		queryparams = new ArrayList<String[]>();
		bodyparams = null;
		boundary = null;
		formdata = false;
		content_length = 0;
		body = null;
		bytebody = null;
		path_pref_url = "";
		parsedheaderlength = 0;
		isHeaderModified = true;
	}

	//public ParseHTTPHeaders(){
	//	init();
	//}

	ParseHTTPHeaders(String httpmessage){
		init();
		ArrayList<String []> dummy = Parse(httpmessage);
	}
        ParseHTTPHeaders(String _h, int _p, boolean _isssl, byte[] _binmessage){
		init();
                String httpmessage;
                try {
                    httpmessage = new String(_binmessage, ParmVars.enc);
                    ArrayList<String []> dummy = Parse(httpmessage);
                    int hlen = getParsedHeaderLength();
                    ByteArrayUtil warray = new ByteArrayUtil(_binmessage);
                    if(_binmessage!=null&&hlen < warray.length()){
                        byte[] _body = warray.subBytes(hlen);
                        setBody(_body);
                    }
                    //parse後に明示的に設定。
                    host = _h;
                    port = _p;
                    isSSL = _isssl;
                } catch (UnsupportedEncodingException ex) {
                    ParmVars.plog.printException(ex);
                }


	}

        public String getHost() {
            return host;
        }

        public int getPort(){
            return port;
        }

        public boolean isSSL(){
            return isSSL;
        }

        public String getPathPrefURL(){
            return path_pref_url;
        }

        public boolean isFormData(){
            return formdata;
        }

        public int getBodyLength() {
            if (body!=null){
                try{
                    int blen =  body.getBytes(ParmVars.enc).length;
                    return blen;
                }catch(UnsupportedEncodingException e){
                    ParmVars.plog.printException(e);
                }
            }
            return 0;
        }

        public int getHeaderLength() {
            String h = getHeaderOnly();
            if (h!=null){
                try{
                    int blen =  h.getBytes(ParmVars.enc).length;
                    return blen;
                }catch(UnsupportedEncodingException e){
                    ParmVars.plog.printException(e);
                }
            }
            return 0;
        }

        public int getParsedHeaderLength(){
            if(isHeaderModified){
                //再計算
                String headerdata = getHeaderOnly();
                parsedheaderlength = headerdata.length();
                isHeaderModified = false;
            }
            return parsedheaderlength;
        }

        ArrayList<String []> Parse(String httpmessage){//request or response
        	parsedheaderlength = 0;
        	Matcher m = valueregex.matcher(httpmessage);
        	String name = "";
        	String value = "";
        	String rec = "";
        	crlf = false;
        	headers = new ArrayList<String []
        			>();
        			boolean frec = true;
        			message = null;
        			boundary = null;
        			while(m.find()){
        				int gcnt = m.groupCount();
        				if ( gcnt > 1){
        					rec = m.group(1);
        				}
        				if ( gcnt > 2){
        					name = m.group(2);
        				}
        				if ( gcnt > 3){
        					value = m.group(3);
        				}
        				if ( name.length() <= 0 && value.length() <= 0 ){
        					crlf = true;
        					int epos = m.end(gcnt);
        					parsedheaderlength = epos;
        					body = httpmessage.substring(epos);
        					break;
        				}else{
        					if ( frec ){//start-line
        						nv = rec.split("[ \t]+" , 3);
        						//request nv[0] method nv[1] url nv[2] protocol
        						if ( nv.length > 2){
        							method = nv[0];

        							String lowerline = method.toLowerCase();

        							if ( lowerline.startsWith("http") ){//response
        								isrequest = false;
        								protocol = nv[0];
        								status = nv[1];
        								reason = nv[2];
        								set_cookieparams.clear();
        							}else{//request;
        								isrequest = true;
        								method = nv[0];
        								url = nv[1];
        								String[] parms = url.split("[?&]");
        								if (parms.length > 0){
        									path = parms[0];
        									String lowerpath = path.toLowerCase();
        									if(lowerpath.startsWith("http")){
        										path_pref_url = "http";
        										isSSL = false;
        										if(lowerpath.startsWith("https")){
        											path_pref_url = "https";
        											isSSL = true;
        										}
        										String[] actualpaths = path.split("[/]");
        										String resultpath = "/";
        										for(int k = 0; k < actualpaths.length; k++){
        											if(k>2){
        												resultpath += (resultpath.equals("/")?"":"/") + actualpaths[k];
        											}
        										}
        										path = resultpath;
        									}
        									if(!path.isEmpty()){
        										String[] pathlist = path.split("[/]");
        										for(int j=1; j<pathlist.length;j++){
        											pathparams.add(pathlist[j]);
        										}
        									}
        								}
        								if (parms.length > 1){
        									for(int i = 1; i < parms.length; i++){
        										String [] nv = parms[i].trim().split("=");
        										String [] nvpair = new String[2];
        										if (nv.length > 0){
        											nvpair[0] = new String(nv[0]);
        										}
        										if (nv.length > 1){
        											nvpair[1] = new String(nv[1]);
        										}else{
        											nvpair[1] = new String("");
        										}
        										queryparams.add(nvpair);

        									}
        								}
        								protocol = nv[2];
        							}
        						}
        						frec = false;
        					}else{//headers
        						nv = new String[2];
        						nv[0] = new String(name.trim());
        						nv[1] = new String(value.trim());
        						headers.add(nv);
        						port = 80;//default
        						if (nv[0].toLowerCase().startsWith("host")) {
        							String[] hasport = nv[1].split("[:]");
        							if(hasport.length>1){
        								port = Integer.parseInt(hasport[1]);
        							}
        							if(hasport.length>0){
        								host = hasport[0];
        							}

        						}
        						if (nv[0].toLowerCase().startsWith("content-type")) {
        							String[] types = nv[1].split("[ ;\t]");

        							for(int i = 0;i<types.length;i++){
        								if(types[i].toLowerCase().startsWith("charset")){
        									String[] csets = types[i].split("[ \t=]");
        									charset = "";
        									for(String v: csets){
        										charset = v;
        									}
        								}else{
	        								int slpos = types[i].indexOf("/");
	        								if ( slpos > 0){// type/subtype
	        									content_type = types[i].substring(0, slpos).toLowerCase();
	        									if(types[i].length() > slpos+1){
	        										content_subtype = types[i].substring(slpos+1, types[i].length()).toLowerCase();
	        									}
	        								}else{
	        									if ( types[i].toLowerCase().startsWith("boundary=")){
	        										String[] boundaries = types[i].split("[=]");
	        										if(boundaries.length>1){
	        											boundary = boundaries[1];
	        											formdataheaderregex = Pattern.compile(formdataheader);
	        											formdatafooterregex = Pattern.compile(formdatafooter + "--" + boundary);
	        											formdata = true;
	        										}
	        									}
	        								}
        								}
        							}
        						}else if(nv[0].toLowerCase().startsWith("content-length")){
        							content_length = Integer.parseInt(nv[1]);
        						}else if(nv[0].toLowerCase().startsWith("cookie")){
        							String[] cookies = nv[1].split("[\r\n;]");
        							cookieparams.clear();
        							for(int ck = 0; ck < cookies.length; ck++){
        								String[] cnv = cookies[ck].trim().split("[=]");
        								if(cnv.length>1){
        									String[] nvpair = new String[2];
        									nvpair[0] = new String(cnv[0]);
        									nvpair[1] = new String(cnv[1]);
        									cookieparams.add(nvpair);
        								}
        							}
        						}else if(nv[0].toLowerCase().startsWith("set-cookie")){//レスポンスのSet-Cookie値
        							String[] cookies = nv[1].split("[\r\n;]");
        							String setckey = null;
        							String setcval = null;
        							ArrayList<String[]> setclist = new ArrayList<String[]>();
        							for(int ck = 0; ck < cookies.length; ck++){
        								String[] cnv = cookies[ck].trim().split("[=]");
        								String[] nvpair = new String[2];
        								if(cnv.length>1){
        									nvpair[0] = new String(cnv[0]);
        									nvpair[1] = new String(cnv[1]);
        									if(ck==0){//cookie name=value
        											setckey = nvpair[0];
        											setcval = nvpair[1];
        									}
        									setclist.add(nvpair);
        									//ParmVars.plog.debuglog(0, "Set-Cookie: K[" + setckey + "] " + nvpair[0] + "=" + nvpair[1]);

        								}else{
        									if(cnv[0].toLowerCase().startsWith("httponly")){
        										nvpair[0] = new String("httponly");
        										nvpair[1] = new String(cnv[0]);
        									}else if(cnv[0].toLowerCase().startsWith("secure")){
        										nvpair[0] = new String("secure");
        										nvpair[1] = new String(cnv[0]);
        									}
        									setclist.add(nvpair);
        									//ParmVars.plog.debuglog(0, "Set-Cookie: K[" + setckey + "] " + nvpair[0] + "=" + nvpair[1]);
        								}
        							}
        							if(setckey!=null){
        								set_cookieparams.put(setckey, setclist);
        							}
        						}


        					}

        				}

        			}

        			isHeaderModified = false;
        			return headers;
        }


        public ArrayList<String[]> getBodyParams(){
            if ( isrequest==true&&bodyparams==null){
                bodyparams = new ArrayList<String[]>();
                if ( boundary == null){
                    //application/x-www-form-urlencoded
                    String[] parms = body.split("[&]");
                    if (parms.length >= 1){
                        for(int i = 0; i < parms.length; i++){
                            String [] nv = parms[i].trim().split("=");
                            String [] nvpair = new String[2];
                            if (nv.length > 0 && !nv[0].isEmpty()){
                                nvpair[0] = new String(nv[0]);
                            }else{
                                nvpair[0] = null;
                            }
                            if (nv.length > 1){
                                nvpair[1] = new String(nv[1]);
                            }else{
                                nvpair[1] = new String("");
                            }
                            if(nvpair[0]!=null) bodyparams.add(nvpair);
                        }
                    }
                }else{//multipart/form-data
                    boolean lasthyphen = true;
                    String parsebody = "\r\n" + body;
                    String formvalue = null;
                    int bpos = -1;
                    int epos = -1;
                    int nextbpos = -1;
                    Matcher fm = formdatafooterregex.matcher(parsebody);
                    while(fm.find()){
                        if(bpos==-1){
                            bpos = fm.end();
                            continue;
                        }else{
                            epos = fm.start();
                            nextbpos = fm.end();
                            ParmVars.plog.debuglog(1, "bpos=" + Integer.toString(bpos));
                            ParmVars.plog.debuglog(1, "epos=" + Integer.toString(epos));
                            ParmVars.plog.debuglog(1, "nextbpos=" + Integer.toString(nextbpos));
                        }
                        formvalue = parsebody.substring(bpos, epos);//セパレータ間のデータ（header含む）
                        String dv =  formvalue.replaceAll("\r", "<CR>");
                        dv = dv.replaceAll("\n", "<LF>");
                        ParmVars.plog.debuglog(1, dv);
                        Matcher fn = formdataheaderregex.matcher(formvalue);
                        if(fn.find()){
                            try{
                                String[] nvpair = new String[2];
                                int fgcnt = fn.groupCount();

                                nvpair[0] = new String(fgcnt>0?fn.group(1):"");

                                nvpair[1] = formvalue.substring(fn.end());
                                nvpair[1] = nvpair[1].replaceAll("\r\n", "(?:\\\\r\\\\n|\\\\n)");
                                nvpair[1] = nvpair[1].replaceAll("\r", "(?:\\\\r|\\\\n)");
                                nvpair[1] = nvpair[1].replaceAll("\n","(?:\\\\r|\\\\n)");
                                ParmVars.plog.debuglog(1, "name[" + nvpair[0] + "]");
                                if(fgcnt>0){
                                    bodyparams.add(nvpair);
                                }
                            }catch(Exception e){
                                ParmVars.plog.printException(e);
                            }
                        }
                        bpos = nextbpos;
                    }
                }//
            }
            return bodyparams;
        }
	//
	// setter
	//
	//
	void setURL(String _url){
		url = _url;
		message = null;
                isHeaderModified = true;
	}



	void setHeader(int i, String name, String value){
		String[] nv = new String[2];
		nv[0] = new String(name);
		nv[1] = new String(value);
		headers.set(i, nv);
		message = null;
                isHeaderModified = true;
	}

	void setHeader(String name, String value){
		int i = findHeader(name);
		if (i >= 0){
			setHeader(i, name, value);
		}else{//追加
                    String[] nv = new String[2];
                    nv[0] = new String(name);
                    nv[1] = new String(value);
                    headers.add(nv);
                }
                isHeaderModified = true;
	}

        void removeHeader(String name){
            int i = findHeader(name);
		if (i >= 0){
			headers.remove(i);
		}
        }

	void setBody(byte[] _bval){
                bytebody = _bval;
                try {
                    body = new String(bytebody, ParmVars.enc);
                } catch (UnsupportedEncodingException ex) {
                    ParmVars.plog.printException(ex);
                }
		message = null;
	}



        void setCookie(String name, String value){
            int len = cookieparams.size();
            boolean isCookieUpdated = false;

            for(int i = 0; i<len; i++){
                String[] pair = cookieparams.get(i);
                if(pair[0].equals(name)){
                    //cookie更新
                    pair[1] = value;
                    cookieparams.set(i, pair);
                    isCookieUpdated = true;
                    break;
                }
            }
            if(!isCookieUpdated){
                //cookie追加
                String[] cv = new String[2];
                cv[0] = name;
                cv[1] = value;
                cookieparams.add(cv);
                isCookieUpdated = true;
            }
            //headersのcookieを更新
            Iterator<String[]> it = cookieparams.iterator();
            String cookiedata = "";
            while(it.hasNext()){
                if(!cookiedata.equals("")){
                    cookiedata += "; ";
                }
                String[] nv = it.next();
                cookiedata += nv[0] + "=" + nv[1];

            }
            setHeader("Cookie", cookiedata);
            ParmVars.plog.debuglog(0, "Cookie:" + cookiedata);
            isHeaderModified = true;
        }

        boolean removeCookies(ArrayList<String> names){
            Iterator<String[]> it = cookieparams.iterator();
            String cookiedata = "";
            boolean cookie_deleted = false;
            boolean isdeleted = false;
            if(names!=null&&names.size()>0){
                while(it.hasNext()){
                    if(!cookiedata.equals("")&&!isdeleted){
                        cookiedata += "; ";
                    }
                    String[] nv = it.next();

                    isdeleted = false;
                    for(int i = 0; i<names.size();i++){
                        if(nv[0].equals(names.get(i))){// Cookie name is case sensitive..
                            //ParmVars.plog.debuglog(0, "removeCookie: " + nv[0] );
                            it.remove();
                            cookie_deleted = true;
                            isdeleted = true;
                            break;
                        }
                    }
                    if(!isdeleted){
                            cookiedata += nv[0] + "=" + nv[1];
                    }

                }
                if(cookie_deleted){//削除された
                    if(cookiedata.isEmpty()){
                        removeHeader("Cookie");
                    }else{
                        setHeader("Cookie", cookiedata);
                    }
                    isHeaderModified = true;
                    message = null;
                    int l = getParsedHeaderLength();
                }
            }
            return cookie_deleted;
        }

	//
	// getter
	//
	//
	String getMethod(){
		return method;
	}

	String getURL(){
		return url;
	}

        String getPath(){
            return path;
        }

	String getBody(){
		return body;
	}

	boolean isRequest(){
		return isrequest;
	}

	String getStartline(){
		if ( isrequest ){
			return method + " " + url + " " + protocol;
		}
		return protocol + " " + status + " " + reason;
	}

        String getStatus(){
            return status;
        }

	String getMessage(){

		if ( message != null ){
			return message;
		}

                int blen = getBodyLength();
                setHeader("Content-Length", Integer.toString(blen));

		StringBuilder sb = new StringBuilder();

		sb.append(getStartline() + "\r\n");
		for(int i = 0; i< headers.size(); i++){
			sb.append(getHeaderLine(i) + "\r\n");
		}
		sb.append("\r\n");
		sb.append(getBody());

		message = new String(sb);

		return message;
	}

    byte[] getByteMessage(){


                if(bytebody!=null){//byte[] bytebodyから
                    setHeader("Content-Length", Integer.toString(bytebody.length));
                    StringBuilder sb = new StringBuilder();

                    sb.append(getStartline() + "\r\n");
                    for(int i = 0; i< headers.size(); i++){
                            sb.append(getHeaderLine(i) + "\r\n");
                    }
                    sb.append("\r\n");

                    String headerpart = new String(sb);
                    ByteArrayUtil rawmessage = new ByteArrayUtil();
                    try {
                        rawmessage = new ByteArrayUtil(headerpart.getBytes(ParmVars.enc));
                        if(bytebody!=null){
                            rawmessage.concat(bytebody);
                        }
                    } catch (UnsupportedEncodingException ex) {
                        ParmVars.plog.printException(ex);
                    }

                    return rawmessage.getBytes();
                }else{//String bodyから
                    String strmess = getMessage();
                    try{
                        byte[] binmess =  strmess.getBytes(ParmVars.enc);
                        return binmess;
                    }catch(UnsupportedEncodingException e){
                        ParmVars.plog.printException(e);
                    }
                }
                return null;
	}

        String getHeaderOnly(){
            StringBuilder sb = new StringBuilder();

            sb.append(getStartline() + "\r\n");
            for(int i = 0; i< headers.size(); i++){
                    sb.append(getHeaderLine(i) + "\r\n");
            }
            sb.append("\r\n");
            return new String(sb);
        }

        //
        // headerの数
        //
	int getHeadersCnt(){
		return headers.size();
	}

	String getHeaderLine(int i){
		String result = null;
		if ( i >= 0 && headers.size() > i){
			String[] nv = (String[])headers.get(i);
			result = new String(nv[0]+ ": " + nv[1]);
		}
		return result;
	}

	public String getHeader(String name){
		int i = findHeader(name);
		if ( i >= 0) {
			String[] nv = (String[])headers.get(i);
			return nv[1];
		}
		return null;
	}

	ArrayList<String []> getHeaders(){
		return headers;
	}

	// RFC 2616 - "Hypertext Transfer Protocol -- HTTP/1.1", Section 4.2, "Message Headers":
	//Each header field consists of a name followed by a colon (":") and the field value. Field names are case-insensitive.
	int findHeader(String name){
		Iterator ite = headers.iterator();
		String[] nv;
		int i = 0;
		while(ite.hasNext()){
			Object obj = ite.next();
			if (obj instanceof String[]) {
				nv = (String[])obj;
				if(name.toLowerCase().equals(nv[0].toLowerCase())){
					return i;
				}
			}
			i++;
		}
		return -1;
	}

        String getContent_Type(){
            return content_type;
        }

        String getContent_Subtype(){
            return content_subtype;
        }

        String getCharset(){
        	return charset;
        }
}




