package burp;

import java.util.Map.Entry;

class PResponse extends ParseHTTPHeaders {
	private ParmGenHashMap map;
    
	//PResponse(){
	//	super();
	//}

	PResponse(String httpmessage){
		super(httpmessage);
                map = null;
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
        
        public ParmGenToken fetchNameValue(String name, AppValue.TokenTypeNames _tokentype){
            if(map==null){
                map = new ParmGenHashMap();
                InterfaceCollection<Entry<ParmGenTokenKey, ParmGenTokenValue>> ic = getLocationTokens(map);             
            }
            ParmGenTokenKey tkey = new ParmGenTokenKey(_tokentype, name, 0);
            ParmGenTokenValue tval = map.get(tkey);
            if(tval!=null){
                return new ParmGenToken(tkey, tval);
            }
            return null;
        }
}