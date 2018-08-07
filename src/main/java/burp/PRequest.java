package burp;

class PRequest extends ParseHTTPHeaders {
	//PRequest(){
	//	super();
	//}
	
	//PRequest(byte[] _bin, Encode _pageenc){
	//	super(_bin, _pageenc);
	//}
        PRequest(String h, int p, boolean ssl, byte[] _binmessage, Encode _pageenc){
		super(h, p, ssl, _binmessage, _pageenc);
	}
}
