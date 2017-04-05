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
                    elems = doc.select("input[type=hidden],a[href]");//name属性を持つHIDDENタグ全部、A HREFタグ
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
            String h = vtag.attr("href");
            if(vtag.tagName().toLowerCase().indexOf("input")!=-1){//<input
                ParmVars.plog.AppendPrint("<" + vtag.tagName() + " name=\"" + n + "\" value=\"" + v + "\">");
            }else if(vtag.tagName().toLowerCase().indexOf("a")!=-1){//<A 
                ParmVars.plog.AppendPrint("<" + vtag.tagName() + " href=\"" + h + "\">");
            }else{
                ParmVars.plog.AppendPrint("<" + vtag.tagName()  + "\">");
            }
        }
    }
    
    //
    // 引き継ぎパラメータ一覧
    //
    public ArrayList<HashMap<ParmGenTokenKey,String>>  getNameValues(){
            HashMap<ParmGenTokenKey, String> map = null;
            ArrayList<HashMap<ParmGenTokenKey,String>> lst = new ArrayList<HashMap<ParmGenTokenKey,String>>();
           
            try {
			
                    for(Element vtag : elems){
                            
                            ParmGenTokenKey ptk = null;
                            if(vtag.tagName().toLowerCase().indexOf("input")!=-1){//<input
                                String n = vtag.attr("name");
                                String v = vtag.attr("value");
                                // <inputタグのnameパラメータ
                                if(n == null){
                                    n = null;
                                }else if(n.isEmpty()){
                                    n = null;
                                }
                                if(v == null){
                                    v = "";
                                }
                                if(n != null){
                                    ptk = new ParmGenTokenKey(AppValue.T_HIDDEN, n);
                                    map = new HashMap<ParmGenTokenKey, String>();
                                    map.put(ptk, v);
                                    lst.add(map);
                                }
                            }else if(vtag.tagName().toLowerCase().indexOf("a")!=-1){//<A 
                                String h = vtag.attr("href");
                                //href属性から、GETパラメータを抽出。
                                //?name=value&....
                                if(h!=null){
                                    String []nvpairs = h.split("[?&]");
                                    for(String nv:nvpairs){
                                        String[] nvp = nv.split("=");
                                        String name = nvp[0];
                                        String value = "";
                                        if(nvp.length>1){
                                            value = nvp[1];
                                        }
                                        if(name!=null&&name.length()>0&&value!=null){
                                            ptk = new ParmGenTokenKey(AppValue.T_HREF, name);
                                            map = new HashMap<ParmGenTokenKey, String>();
                                            map.put(ptk, value);
                                            lst.add(map);
                                        }
                                    }
                                }
                            }
                    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ParmVars.plog.printException(e);
		}
            
            return lst;
    }
    
    //
    // レスポンスパラメータ抽出
    //
    public HashMap<ParmGenTokenKey,String> fetchNameValue(String name, int fcnt, int _tokentype){
            HashMap<ParmGenTokenKey, String> map = null;
            if(name ==null)return map;//name nullは不可。
            HashMap<ParmGenTokenKey,String> lastmap = null;
            ParmGenTokenKey ptk = null;
            try {
                for(Element vtag : elems){
                    String n = vtag.attr("name");
                    String v = vtag.attr("value");
                    String h = vtag.attr("href");
                    switch(_tokentype){
                        case AppValue.T_HIDDEN:
                            if(name.toLowerCase().equals(n.toLowerCase())){//指定したname値発見
                                if(fcnt> 0){//fcnt　指定したnameをfcnt回スキップ
                                    fcnt--;
                                    if ( v != null && !v.isEmpty()){
                                        if(lastmap==null){
                                            lastmap = new HashMap<ParmGenTokenKey, String>();
                                        }
                                        lastmap.clear();
                                        ptk = new ParmGenTokenKey(AppValue.T_HIDDEN, n);
                                        lastmap.put(ptk,v);
                                    }
                                }else if ( v != null && !v.isEmpty()){//valueに値がある場合のみfetch
                                    //ParmVars.plog.AppendPrint("<" + vtag.tagName() + " name=\"" + n + "\" value=\"" + v + "\">");
                                    if(map==null){
                                        map = new HashMap<ParmGenTokenKey, String>();
                                    }
                                    map.clear();
                                    ptk = new ParmGenTokenKey(AppValue.T_HIDDEN, n);
                                    map.put(ptk, v);
                                    break;
                                }
                            }
                            break;
                    }
                    
                }
            } catch (Exception e) {
                    // TODO Auto-generated catch block
                    ParmVars.plog.printException(e);
            }
            if(map==null&&lastmap!=null){
                map = lastmap;
            }
            return map;
    }
    
}
