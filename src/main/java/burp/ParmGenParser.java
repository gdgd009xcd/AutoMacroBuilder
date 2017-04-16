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
    HashMap<ParmGenTokenKey, ParmGenTokenValue> map;
    
    void init(){
        doc = null;
        elems = null;
        map = null;
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
    
    public ParmGenToken getParmGenToken(Element vtag, HashMap<String, Integer> namepos){
        String[] nv = null;
        ParmGenToken tk = null;
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
                //重複nameの検査
                int npos = 0;
                if(namepos.containsKey(n)){
                    npos = namepos.get(n);
                    npos++;
                    namepos.put(n, npos);
                }else{
                    namepos.put(n, npos);
                }
                tk = new ParmGenToken(AppValue.T_HIDDEN, "", n, v, npos);
            }
        }else if(vtag.tagName().toLowerCase().indexOf("a")!=-1){//<A 
            String h = vtag.attr("href");
            //href属性から、GETパラメータを抽出。
            //?name=value&....
            if(h!=null){
                String []nvpairs = h.split("[?&]");
                String url = nvpairs[0];
                for(String tnv:nvpairs){
                    String[] nvp = tnv.split("=");
                    String name = nvp[0];
                    String value = new String("");
                    if(nvp.length>1){
                        value = nvp[1];
                    }
                    if(name!=null&&name.length()>0&&value!=null){
                        //重複nameの検査
                        int npos = 0;
                        if(namepos.containsKey(name)){
                            npos = namepos.get(name);
                            npos++;
                            namepos.put(name, npos);
                        }else{
                            namepos.put(name, npos);
                        }
                        tk = new ParmGenToken(AppValue.T_HREF,url, name, value, npos);
                        break;
                    }
                }
            }
        }
        return tk;
    }
    //
    // 引き継ぎパラメータ一覧
    //
    public ArrayList<ParmGenToken>  getNameValues(){
            
            HashMap<String,Integer> namepos = new HashMap<String,Integer>();
            ArrayList<ParmGenToken> lst = new ArrayList<ParmGenToken>();
           
            try {
			
                    for(Element vtag : elems){

                            ParmGenToken tk = getParmGenToken(vtag, namepos);
                            if(tk!=null){
                                lst.add(tk);
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
    public ParmGenToken fetchNameValue(String name, int fcnt, int _tokentype){
            if(name ==null)return null;//name nullは不可。
            ParmGenTokenKey tkey = null;
            HashMap<String,Integer> namepos = new HashMap<String,Integer>();
            
            if(map==null){
                map = new HashMap<ParmGenTokenKey, ParmGenTokenValue>();
                for(Element vtag : elems){
                    ParmGenToken tkn = getParmGenToken(vtag, namepos);
                    if(tkn!=null){
                        map.put(tkn.getTokenKey(), tkn.getTokenValue());
                    }
                }
            }
            tkey = new ParmGenTokenKey(_tokentype, name, fcnt);
            ParmGenTokenValue tval = map.get(tkey);
            if(tval!=null){
                return new ParmGenToken(tkey, tval);
            }else if(fcnt>0){
                while(fcnt-->0){
                    tkey = new ParmGenTokenKey(_tokentype, name, fcnt);
                    tval = map.get(tkey);
                    if(tval!=null){
                        return new ParmGenToken(tkey, tval);
                    }
                }
            }
            return null;
    }
    
}
