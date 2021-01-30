/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.HashSet;
import java.util.Set;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceDoAction;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceDoActionProvider;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;

/**
 *
 * @author daike
 */
public class BurpExtenderDoActionProvider implements InterfaceDoActionProvider {
    
    private BurpExtenderDoAction doaction = null;

    BurpExtenderDoActionProvider(ParmGenMacroTraceProvider pmtProvider) {
        doaction = new BurpExtenderDoAction(pmtProvider);
    }

    public void setParameters(int toolflag, boolean messageIsRequest, IHttpRequestResponse messageInfo){
        doaction.setParameters(toolflag, messageIsRequest, messageInfo);
    }
    
    @Override
    public int getSequnceNo() {
        return 1;
    }

    @Override
    public int getActionNo() {
        return 0;
    }

    /**
     * get DoAction instance as singleton
     *
     * @return 
     */
    @Override
    public InterfaceDoAction getDoActionInstance() {
        return this.doaction;
    }
    
}
