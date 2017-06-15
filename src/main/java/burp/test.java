/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.StringReader;
import java.util.Deque;
import javax.json.Json;
import javax.json.stream.JsonParser;

/**
 *
 * @author daike
 */
public class test {

    public static void main(String[] args) {
        //ParmGenCSV(String _filename, String _lang)
        ParmVars.parmfile = "xxx.csv";
        String seq = new String("AAAABBABABCxD123ABCD4");
        String key = new String("AAAB");

        byte[] seqbin = seq.getBytes();
        byte[] keybin = key.getBytes();

        int startpos = 0;
        int endpos = seqbin.length - keybin.length + 1;

        System.out.println("String.indexof=" + seq.indexOf(key, startpos));

        int idx = -1;
        for (int i = startpos; i < endpos; i++) {
            for (int j = 0; j < keybin.length; j++) {
                System.out.println("  i,j=" + i + "," + j);

                if (seqbin[i + j] == keybin[j]) {
                    if (j == keybin.length - 1) {
                        idx = i;
                        System.out.println(" result idx,i,j=" + idx + "," + i + "," + j);
                        break;
                    }

                } else {
                    break;
                }
            }

            if (idx != -1) {
                break;
            }
        }

        System.out.println(" result idx=" + idx);
        ParmGenStack<String> stack = new ParmGenStack<String>();
        stack.push("1");
        stack.push("2");
        stack.push("3");
        stack.push("4");

        String p = stack.pop();
        System.out.println("current:" + stack.getCurrent() + " p:" + p);
        String jsondata = "{\"name\":\"\\/\\u0027\"}";
        JsonParser parser = Json.createParser(new StringReader(jsondata));
        String keyname = "";
        String value = "";
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
        ParmVars.plog.debuglog(0, "key:[" + keyname + " value[" + value + "]");
    }

}
