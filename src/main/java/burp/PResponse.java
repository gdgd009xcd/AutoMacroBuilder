package burp;

import java.util.Map.Entry;

class PResponse extends ParseHTTPHeaders {
	private ParmGenHashMap map;
        private ParmGenParser htmlparser;
        private ParmGenJSONDecoder jsonparser;
	//PResponse(){
	//	super();
	//}

	PResponse(String httpmessage){
		super(httpmessage);
                map = null;
                htmlparser = null;
                jsonparser = null;
	}


	//Location headerのパラメータ取得
	public  <T> InterfaceCollection<T> getLocationTokens(InterfaceCollection<T> tklist){
		String locheader = getHeader("Location");

		if(locheader!=null){
                    String []nvpairs = locheader.split("[?&]");
                    String url = nvpairs[0];
                    for(String tnv:nvpairs){
                        String[] nvp = tnv.split("=");
                        String name = nvp[0];
                        String value = new String("");
                        if(nvp.length>1){
                            value = nvp[1];

                            if(name!=null&&name.length()>0&&value!=null){
                                tklist.addToken(AppValue.TokenTypeNames.LOCATION,url, name, value, false,0);
                            }
                        }
                    }
                    if(tklist!=null&&tklist.size()>0){
                        return tklist;
                    }
		}
		return null;
	}

        public ParmGenToken fetchNameValue(String name, AppValue.TokenTypeNames _tokentype, int fcnt){
            if(map==null){
                map = new ParmGenHashMap();
                InterfaceCollection<Entry<ParmGenTokenKey, ParmGenTokenValue>> ic = getLocationTokens(map);
                String subtype = getContent_Subtype();
                if(subtype!=null&&subtype.toLowerCase().equals("json")){
                	jsonparser = new ParmGenJSONDecoder(body);
                }else{
                	htmlparser = new ParmGenParser(body);
                }


            }
            ParmGenTokenKey tkey = new ParmGenTokenKey(_tokentype, name, fcnt);
            ParmGenTokenValue tval = map.get(tkey);
            if(tval!=null){
                return new ParmGenToken(tkey, tval);
            }
            if(htmlparser!=null){
                return htmlparser.fetchNameValue(name, fcnt, _tokentype);
            }
            if(jsonparser!=null){
                return jsonparser.fetchNameValue(name, fcnt, _tokentype);
            }
            return null;
        }


}