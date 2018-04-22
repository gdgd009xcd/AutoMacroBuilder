/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.net.URL;

/**
 * デバッグ用アクション
 * @author daike
 */
public class BurpMacroLogAction implements ISessionHandlingAction {

    
    BurpMacroLogAction(){

    }
    
    @Override
    public String getActionName() {
        return "ParmGenLogAction";
    }

    @Override
    public void performAction(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        
    }
    
}
