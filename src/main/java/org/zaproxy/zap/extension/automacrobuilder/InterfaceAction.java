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
public interface InterfaceAction {
    // concurrent executed method.
    // return true: endaction execute.  false:  nothing to do endAction
    boolean action(ThreadManager tm, OneThreadProcessor otp);
}
