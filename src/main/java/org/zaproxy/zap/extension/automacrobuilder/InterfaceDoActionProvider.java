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
public interface InterfaceDoActionProvider {
    // sequnce number 0start
    int getSequnceNo();
    // action number 0start: specified number Action run in List<InterfaceAction> getActionList()
    int getActionNo();
    // create new InterfaceDoAction  Instance.
    InterfaceDoAction getDoActionInstance();
}
