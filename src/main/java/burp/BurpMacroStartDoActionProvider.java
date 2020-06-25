/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import org.zaproxy.zap.extension.automacrobuilder.InterfaceDoAction;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceDoActionProvider;

/**
 *
 * @author daike
 */
public class BurpMacroStartDoActionProvider implements InterfaceDoActionProvider {

    private BurpMacroStartDoAction doactioninstance = new BurpMacroStartDoAction();
    
    BurpMacroStartDoActionProvider(){
    }
    
    /**
     * Set parameters used in DoAction
     * 
     * @param currentrequest
     * @param executedmacros 
     */
    public void setParamters(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros){
        doactioninstance.setParamters(currentrequest, executedmacros);
    }
    
    // 
    @Override
    public int getSequnceNo() {
        return 0;
    }

    @Override
    public int getActionNo() {
        return 0;
    }

    /**
     * get DoactionInstance as singleton
     *
     * @return 
     */
    @Override
    public InterfaceDoAction getDoActionInstance() {
        return this.doactioninstance;
    }
    
}
