/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tms783
 */
public class ParmGenUtil {
    public static int parseMaxInt(String i){
        
        int _i;
        try{
            _i = Integer.parseInt(i);
        }catch(Exception e){
            _i = 0x7fffffff;//integer 最大値
        }

        return _i;
    }
    
    public static int parseMinInt(String i){
        int _i;
        try{
            _i = Integer.parseInt(i);
        }catch(Exception e){
            _i = 0;//integer 最小値
        }

        return _i;
    }
    
    public static String escapeRegexChars(String _d){
        _d = _d.replaceAll("([\\+\\{\\}\\[\\]\\(\\)\\*\\.\\<\\>\\?\\^\\$])", "\\\\$1");
        return _d;
    }
    
    public static String getPathsRegex(String path){
        int question = path.indexOf("?");
        int hash = path.indexOf("#");
        
        if(question>=0){
            String url = path.substring(0, question);
            String args = path.substring(question+1);
            return "(" + escapeRegexChars(url) + "\\?" + getArgRegex(args) + ")";
        }else if(hash>=0){
            String url = path.substring(0, hash);
            String args = path.substring(hash+1);
            return "(" + escapeRegexChars(url) + "#"+ getArgRegex(args)+")";
        }
        return "(" + escapeRegexChars(path) + ")";
    }
    
    
    public static ArrayList<String>  getGroupRegexes(String r){
        // (?:^|[^\\])(\([^?].*?\))
        ArrayList<String> glist = new ArrayList<String>();
        
        String greg = "(?:^|[^\\\\])(\\([^?].*?\\))";//後方参照グループ
        Pattern pattern = Pattern.compile(greg);
        Matcher matcher = pattern.matcher(r);
        int gtotal = 0;
        while(matcher.find()){
            int n = matcher.groupCount();
            for(int j=1; j<=n; j++){
                String matchval = matcher.group(j);
                glist.add(matchval);
            }
        }
        return glist;
    }
    
   public static String getArgRegex(String v){
       int pos = 0;
       int nextpos;
       String arglist="";
       
       while((nextpos=v.indexOf("&", pos))>=0){
           String param = v.substring(pos, nextpos);
           int equalpos = param.indexOf("=");
           if(equalpos>=0){
              String name = param.substring(0, equalpos);
              String value = param.substring(equalpos+1);
              String vregex = ParmGenRegex.getParsedRegexRaw(value, "*");
              String namevalue = escapeRegexChars(name) + "=" + vregex;
              arglist += namevalue + "&";
           }else{
               arglist += escapeRegexChars(param) + "&";
           }
           pos = nextpos+1;
       }
       if(pos>0){
           arglist += escapeRegexChars(v.substring(pos));
       }else{
           arglist = escapeRegexChars(v);
       }
       return arglist;
   }
   
    //target文字列中の検索正規表現regexのマッチカウント
    public static int getRegexMatchpos(String regex, String target){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);
        int mcnt = 0;
        while(matcher.find()){
            mcnt++;
        }
        return mcnt;
    }
 
}
