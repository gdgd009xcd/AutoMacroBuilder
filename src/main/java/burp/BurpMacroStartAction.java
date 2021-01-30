/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import static org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace.PMT_CURRENT_BEGIN;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;

import org.zaproxy.zap.extension.automacrobuilder.ThreadManagerProvider;


/**
 * マクロ実行前に登録するアクション
 * @author daike
 */
public class BurpMacroStartAction implements ISessionHandlingAction {

    private BurpMacroStartDoActionProvider provider = null;
    
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    
    public BurpMacroStartAction(ParmGenMacroTraceProvider pmtProvider){
        provider = new BurpMacroStartDoActionProvider(pmtProvider);
    }

    private BurpMacroStartDoActionProvider getProvider(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros){
        this.provider.setParamters(currentrequest, executedmacros);
        return this.provider;
    }
    
    @Override
    public String getActionName() {
        return "ParmGenStartAction";
    }

    @Override
    public void performAction(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        ThreadManagerProvider.getThreadManager().beginProcess(getProvider(currentrequest, executedmacros));
    }
}
