package burp;

class PRequest extends ParseHTTPHeaders {
	//PRequest(){
	//	super();
	//}
	
	PRequest(String httpmessage){
		super(httpmessage);
	}
        PRequest(String h, int p, boolean ssl, byte[] _binmessage){
		super(h, p, ssl, _binmessage);
	}
}
