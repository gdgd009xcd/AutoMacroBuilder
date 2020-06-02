/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import org.zaproxy.zap.extension.automacrobuilder.ParmGen;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;
import static org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace.PMT_CURRENT_BEGIN;
import org.zaproxy.zap.extension.automacrobuilder.ParmVars;


/**
 * マクロ実行前に登録するアクション
 * @author daike
 */
public class BurpMacroStartAction implements ISessionHandlingAction {
    ParmGenMacroTrace tr;
    
    public BurpMacroStartAction(ParmGenMacroTrace _tr){
        tr = _tr;
    }
    
    @Override
    public String getActionName() {
        return "ParmGenStartAction";
    }

    @Override
    public void performAction(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        tr.startBeforePreMacro();//前処理マクロを実行。
        startCurrentRequest(currentrequest);
    }
    
    public void startCurrentRequest(IHttpRequestResponse currentRequest){
        ParmVars.plog.clearComments();
        ParmVars.plog.setError(false);
        //state = PMT_CURRENT_BEGIN;
        tr.setState(PMT_CURRENT_BEGIN);
        ParmGen pgen = new ParmGen(tr);
        IHttpService iserv = currentRequest.getHttpService();
        String host = iserv.getHost();
        int port = iserv.getPort();
        boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
        ParmVars.plog.debuglog(0, "Current StepNo:" + tr.getStepNo() + " "+ host );
        tr.burpSetCurrentOriginalRequest(currentRequest.getRequest());
        byte[] retval = pgen.Run(host, port, isSSL, currentRequest.getRequest());
        if ( retval != null){
                currentRequest.setRequest(retval);
        }
    }
    
}
