package burp;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.stream.JsonParser;

public class ParmGenJSONDecoder {

    JsonParser parser = null;
    HashMap<ParmGenTokenKey, ParmGenTokenValue> map = null;
    
    ParmGenJSONDecoder(String jsondata){
        crtparse(jsondata);
    }
    
    private JsonParser crtparse(String jsondata) {
        try{
            parser = Json.createParser(new StringReader(jsondata));
        }catch(Exception e){
            parser = null;
        }
        return parser;
    }

    public String decodeStringValue(String jval) {
        String jsondata = "{\"name\":\"" + jval + "\"}";
        if (crtparse(jsondata) != null) {
            return simpleParseStringValue();

        }

        return null;
    }

    private String simpleParseStringValue() {
        String keyname = "";
        String value = "";
        if (parser != null) {
            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();
                switch (event) {
                    case START_ARRAY:

                        //ParmVars.plog.debuglog(0, "START_ARRAY NAME:" +keyname + " level:" + arraylevel);
                        break;
                    case END_ARRAY:

                        //ParmVars.plog.debuglog(0, "END_ARRAY NAME:" +ep + " level:" + arraylevel);
                        break;
                    case KEY_NAME:
                        keyname = parser.getString();
                        break;
                    case START_OBJECT:
                    case END_OBJECT:
                        break;
                    case VALUE_TRUE:

                    case VALUE_FALSE:

                        break;
                    case VALUE_STRING:
                    case VALUE_NUMBER:
                        value = parser.getString();
                    case VALUE_NULL:

                        break;
                }
            }
        }
        return value;

    }

    public ArrayList<ParmGenToken> parseJSON2Token() {
        ArrayList<ParmGenToken> tknlist = new ArrayList<ParmGenToken>();
        map = new HashMap<ParmGenTokenKey, ParmGenTokenValue>();
        String URL = "";
        if (parser != null) {
            String keyname = "";
            String value = "";
            int fcnt = 0;
            ParmGenToken tkn = null;
            HashMap<String, Integer> samenamehash = new HashMap<String, Integer>();
            while (parser.hasNext()) {
                try {
                    JsonParser.Event event = parser.next();
                    switch (event) {
                        case START_ARRAY:

                            //ParmVars.plog.debuglog(0, "START_ARRAY NAME:" +keyname + " level:" + arraylevel);
                            break;
                        case END_ARRAY:

                            //ParmVars.plog.debuglog(0, "END_ARRAY NAME:" +ep + " level:" + arraylevel);
                            break;
                        case KEY_NAME:
                            keyname = parser.getString();
                            break;
                        case START_OBJECT:
                        case END_OBJECT:
                            break;
                        case VALUE_TRUE:
                        case VALUE_FALSE:
                            break;
                        case VALUE_STRING:
                        case VALUE_NUMBER:
                            value = parser.getString();
                            fcnt = 0;
                            if (samenamehash.containsKey(keyname)) {
                                fcnt = samenamehash.get(keyname) + 1;
                            }
                            samenamehash.put(keyname, fcnt);
                            tkn = new ParmGenToken(AppValue.TokenTypeNames.JSON, URL, keyname, value, false, fcnt);
                            tknlist.add(tkn);
                            map.put(tkn.getTokenKey(), tkn.getTokenValue());
                            break;
                        case VALUE_NULL:
                            value = null;
                            break;
                    }
                } catch (Exception e) {
                    parser = null;
                    tknlist.clear();
                    break;
                }
            }
        }
        return tknlist;

    }
    
    public ParmGenToken fetchNameValue(String name, int fcnt, AppValue.TokenTypeNames _tokentype){
        if(parser!=null){
            if(map==null){
                parseJSON2Token();
            }
            if(map!=null){
                ParmGenTokenKey tkey = new ParmGenTokenKey(_tokentype, name, fcnt);
                if(map.containsKey(tkey)){
                    ParmGenTokenValue tval = map.get(tkey);
                    ParmGenToken tkn = new ParmGenToken(tkey, tval);
                    return tkn;
                }
            }
        }
        return null;
    }
}
