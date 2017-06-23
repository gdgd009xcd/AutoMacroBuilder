package burp;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.stream.JsonParser;

public class ParmGenJSONDecoder {

    JsonParser parser = null;

    JsonParser crtparse(String jsondata) {
        parser = Json.createParser(new StringReader(jsondata));
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

    public ArrayList<ParmGenToken> parseJSON2Token(String URL, String jsondata) {
        ArrayList<ParmGenToken> tknlist = new ArrayList<ParmGenToken>();
        
        if (crtparse(jsondata) != null) {
            String keyname = "";
            String value = "";
            int fcnt = 0;
            ParmGenToken tkn = null;
            HashMap<String, Integer> samenamehash = new HashMap<String, Integer>();
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
                        fcnt = 0;
                        if(samenamehash.containsKey(keyname)){
                            fcnt = samenamehash.get(keyname) + 1;
                        }
                        samenamehash.put(keyname, fcnt);
                        tkn = new ParmGenToken(AppValue.TokenTypeNames.JSON, URL, keyname, value, false, fcnt);
                        tknlist.add(tkn);
                        break;
                    case VALUE_NULL:
                        value = null;
                        break;
                }
            }
        }
        return tknlist;

    }
}
