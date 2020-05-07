/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

/**
 *
 * @author daike
 */
public class ParmGenBeen implements DeepClone{
    //primitive or final mermbers
    String v = null;
    int i = 0;
    boolean b = false;
    
    //...etc.
    ParmGenBeen(){
        
    }
    
    
    @Override
    public ParmGenBeen clone() {
        try {
            ParmGenBeen nobj =  (ParmGenBeen)super.clone();//this class of which primitive or final member is olso copied
            return nobj;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); 
        }
    }
}
