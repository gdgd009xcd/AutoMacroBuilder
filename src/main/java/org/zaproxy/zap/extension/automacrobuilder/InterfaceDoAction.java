/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.List;

/**
 *
 * @author daike
 */
public interface InterfaceDoAction {
    // start action (synchronized)
    // This function is called by where: synchronized OneThreadProcessor getProcess(InterfaceDoActionProvider provider)
    //                               new Instance: public OneThreadProcessor(ThreadManager tm, Thread th, InterfaceDoAction doaction)
    //                               RECYCLED    : public void setNewThread(Thread th)
    //
    // before action: do  initiatize or copy fields... and return acttion list.
    // action list:  actions can be performed by specifying the list number at the appropriate entry point.
    //
    List<InterfaceAction> startAction(ThreadManager tm, OneThreadProcessor otp);
    //
    
    //
    //List<InterfaceAction> getActionList();
    //
    // end action (synchronized)
    // This function is called by where: synchronized void endProcess(OneThreadProcessor p, InterfaceDoAction action)
    //
    // after action: do some result save/update etc...
    //
    InterfaceEndAction endAction(ThreadManager tm, OneThreadProcessor otp);
    //
 
}
