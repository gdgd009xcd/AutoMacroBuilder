/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author tms783
 */
public class ParmGenParser {
    //get the factory
    Document doc;
    Elements elems;
    
    void init(){
        doc = null;
        elems = null;
    }
    
    //tokenらしき値を自動引継ぎ
    ParmGenParser(String htmltext){
            init();

            try {
                    doc = Jsoup.parse(htmltext);//パース実行
                    elems = doc.select("[name]");//name属性を持つタグ全部
                    //elemsprint(htmltext);
            } catch (Exception e) {
                    // TODO Auto-generated catch block
                    ParmVars.plog.printException(e);
            }

    }
    
    ParmGenParser(String htmltext, String selector){
            init();

            try {
                    doc = Jsoup.parse(htmltext);//パース実行
                    elems = doc.select(selector);//selecterで指定したタグ
                    //elemsprint(htmltext);
            } catch (Exception e) {
                    // TODO Auto-generated catch block
                    ParmVars.plog.printException(e);
            }

    }
    
    void elemsprint(String _t){
        for(Element vtag : elems){
            String n = vtag.attr("name");
            String v = vtag.attr("value");
            ParmVars.plog.AppendPrint("<" + vtag.tagName() + " name=\"" + n + "\" value=\"" + v + "\">");
        }
    }
    
    public ArrayList<HashMap<String,String>>  getNameValues(){
            HashMap<String, String> map = null;
            ArrayList<HashMap<String,String>> lst = new ArrayList<HashMap<String,String>>();
           
            try {
			
                    for(Element vtag : elems){
                            String n = vtag.attr("name");
                            String v = vtag.attr("value");


                            //if ( v != null && !v.isEmpty()){//value値の存在するパラメータを抽出
                                if(n == null){
                                    n = "";
                                }
                                if(v == null){
                                    v = "";
                                }
                                //ParmVars.plog.AppendPrint("<" + vtag.tagName() + " name=\"" + n + "\" value=\"" + v + "\">");
                                map = new HashMap<String, String>();
                                map.put(n, v);
                                
                                lst.add(map);
                            //}
                    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ParmVars.plog.printException(e);
		}
            
            return lst;
    }
    
    public HashMap<String,String> fetchNameValue(String name, int fcnt){
            HashMap<String, String> map = null;
            if(name ==null)return map;//name nullは不可。
            try {
                for(Element vtag : elems){
                    String n = vtag.attr("name");
                    String v = vtag.attr("value");
                    if(name.toLowerCase().equals(n.toLowerCase())){//指定したname値発見
                        if(fcnt> 0){//fcnt　指定したnameをfcnt回スキップ
                            fcnt--;
                        }else if ( v != null && !v.isEmpty()){//valueに値がある場合のみfetch
                            //ParmVars.plog.AppendPrint("<" + vtag.tagName() + " name=\"" + n + "\" value=\"" + v + "\">");
                            map = new HashMap<String, String>();
                            map.put(n, v);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                    // TODO Auto-generated catch block
                    ParmVars.plog.printException(e);
            }
            return map;
    }
    
}
