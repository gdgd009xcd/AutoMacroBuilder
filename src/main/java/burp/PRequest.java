package burp;

import java.nio.charset.StandardCharsets;

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
        
        PRequest newRequestWithRemoveSpecialChars(String regex){//remove section chars
            byte[] binmessage = getByteMessage();
            String isomessage = new String(binmessage, StandardCharsets.ISO_8859_1);
            String defaultregex = "[§]";
            if(regex!=null&&!regex.isEmpty()){
                defaultregex = regex;
            }
            String rawmessage = isomessage.replaceAll(defaultregex, "");
            String host = getHost();
            int port = getPort();
            boolean isSSL = isSSL();
            Encode penc = getPageEnc();
            return new PRequest(host, port, isSSL, rawmessage.getBytes(StandardCharsets.ISO_8859_1), penc);
        }
        
        
}
