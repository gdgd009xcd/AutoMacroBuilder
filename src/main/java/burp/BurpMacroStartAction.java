/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

/**
 * マクロ実行前に登録するアクション
 * @author daike
 */
public class BurpMacroStartAction implements ISessionHandlingAction {
    ParmGenMacroTrace tr;
    
    BurpMacroStartAction(ParmGenMacroTrace _tr){
        tr = _tr;
    }
    
    @Override
    public String getActionName() {
        return "ParmGenStartAction";
    }

    @Override
    public void performAction(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        tr.startBeforePreMacro();//前処理マクロを実行。
        tr.startCurrentRequest(currentrequest);
    }
    
}
