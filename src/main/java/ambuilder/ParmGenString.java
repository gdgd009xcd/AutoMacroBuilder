/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

/**
 *
 * @author daike
 */
public class ParmGenString {
    private String value;
    int s;
    int e;
    boolean matched;
    
    ParmGenString(boolean _m, int _s, int _e, String _v){
        s = _s; e = _e; value = _v; matched = _m;
    }
    
    public String getValue(){
        return value;
    }
    
    public int getStartPos(){
        return s;
    }
    
    public int getEndPos(){
        return e;
    }
    
    public boolean isMatched(){
        return matched;
    }
    
}
