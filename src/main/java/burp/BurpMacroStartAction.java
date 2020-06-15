/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceAction;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceDoAction;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceEndAction;
import org.zaproxy.zap.extension.automacrobuilder.OneThreadProcessor;
import org.zaproxy.zap.extension.automacrobuilder.ParmGen;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;
import static org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace.PMT_CURRENT_BEGIN;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;
import org.zaproxy.zap.extension.automacrobuilder.ParmVars;
import org.zaproxy.zap.extension.automacrobuilder.ThreadManager;
import org.zaproxy.zap.extension.automacrobuilder.ThreadManagerProvider;


/**
 * マクロ実行前に登録するアクション
 * @author daike
 */
public class BurpMacroStartAction implements ISessionHandlingAction {
    

    private BurpMacroStartDoActionProvider provider = null;
    
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    
    public BurpMacroStartAction(){
    }

    private BurpMacroStartDoActionProvider getProvider(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros){
        if ( this.provider == null) {
            this.provider = new BurpMacroStartDoActionProvider();
        }
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
