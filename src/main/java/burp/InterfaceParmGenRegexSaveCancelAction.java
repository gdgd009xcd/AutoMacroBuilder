/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

/**
 *
 * @author daike
 */
public interface InterfaceParmGenRegexSaveCancelAction {
    public void ParmGenRegexSaveAction(String message);
    public void ParmGenRegexCancelAction();
    public String getParmGenRegexSaveBtnText();
    public String getParmGenRegexCancelBtnText();
    
}
