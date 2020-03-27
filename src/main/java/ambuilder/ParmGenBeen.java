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
public class ParmGenBeen {
    String v = null;
    int i = 0;
    boolean b = false;
    //...etc.
    ParmGenBeen(){
        
    }
    
    ParmGenBeen(ParmGenBeen sbeen){
        v = sbeen.v;
        i = sbeen.i;
        b = sbeen.b;
    }
}
