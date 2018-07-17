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
public class BurpRepeaterAction implements ISessionHandlingAction{
     ParmGenMacroTrace tr;
    
    BurpRepeaterAction(ParmGenMacroTrace _tr){
        tr = _tr;
    }
    
    @Override
    public String getActionName() {
        return "ParmGenRepeaterAction";
    }

    @Override
    public void performAction(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        tr.setMBtoolIsRepeater(true);//repeaterが実行された。
    }
}
