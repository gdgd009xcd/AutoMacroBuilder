/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automacrobuilder;

/**
 *
 * @author daike
 */
public class CookiePathValue {
    private String path;
    private String value;
    
    CookiePathValue(String _path, String _value){
        path = _path;
        value = _value;
    }
    
    String getPath(){
        return path;
    }
    
    String getValue(){
        return value;
    }
    
    
    
}

class PathComparator implements java.util.Comparator {
	public int compare(Object s, Object t) {
		String spath = ((CookiePathValue)s).getPath();
                String tpath = ((CookiePathValue)t).getPath();
                //
                // s > t  : >0
                // s ==t  : =0
                // s < t  : <0
                if(spath != null && tpath == null){
                    return 1;
                }
                if(spath == null && tpath != null){
                    return -1;
                }
                
                if(spath != null && tpath != null){
                    return spath.compareTo(tpath);
                }
		return 0; 
	}
}
