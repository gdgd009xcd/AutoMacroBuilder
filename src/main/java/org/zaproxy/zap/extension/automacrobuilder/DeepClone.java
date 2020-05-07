/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

/**
 *
 * @author daike
 *
 */
public interface DeepClone extends Cloneable {
    //
    //
    // Correct example:
    //
    //    class crazyobject implements DeepClone{
    //
    //    ...
    //        @Override
    //        public crazyobject clone() {// return this Type object which is not java.lang.Object Type.
    //             try {
    //               crazyobject nobj =  (crazyobject)super.clone();//  new clone object is created and  primitive or final member object of this class is also copied 
    //               nobj.optlist = ListDeepCopy.listDeepCopy(this.optlist);// member of this class that require deep copy must be explicitly copied.
    //               return nobj;
    //             } catch (CloneNotSupportedException e) {
    //               throw new AssertionError(); 
    //             }
    //        }
    //
    public Object clone() ;
}
