/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.ArrayList;
import java.util.ListIterator;



/**
 *
 * @author daike
 */
public class ParmGenHeader {
    private String name;
    private ArrayList<ParmGenBeen> values = null;
    private String key_uppername;
 
    
    ParmGenHeader(int _i, String _n, String _v){
        name = _n;
        key_uppername = _n;
        if(name!=null){
            key_uppername = name.toUpperCase();
        }
        values = new ArrayList<ParmGenBeen>();
        ParmGenBeen been = new ParmGenBeen();
        been.v = _v;
        been.i = _i;
        values.add(been);
        
    }
    
    public String getName(){
        return name;
    }
    
    
    
    public String getKeyUpper(){
        return key_uppername;
    }
    
    public void addValue(int _i, String _v){
        ParmGenBeen been = new ParmGenBeen();
        been.i = _i;
        been.v = _v;
        values.add(been);
    }
    
    public ListIterator<ParmGenBeen> getValuesIter(){
        return values.listIterator();
    }
    
    public int getValuesSize(){
        return values.size();
    }
    
}

   