package burp;

import java.util.ArrayList;

class PResponse extends ParseHTTPHeaders {
	//PResponse(){
	//	super();
	//}
	ArrayList<ParmGenToken> tklist;//Locationヘッダーのパラメータ一覧

	PResponse(String httpmessage){
		super(httpmessage);
	}

	//Location headerのパラメータ取得
	public ArrayList<ParmGenToken> getLocationTokens(){
		String locheader = getHeader("Location");
		ArrayList<ParmGenToken> tklist = null;
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
                    	if(tklist==null){
                    		tklist = new ArrayList<ParmGenToken>();
                    	}
                        ParmGenToken tk = new ParmGenToken(AppValue.T_LOCATION,url, name, value, 0);
                        tklist.add(tk);
                    }
                }
            }
            if(tklist!=null){
            	return tklist;
            }
		}
		return null;
	}
}