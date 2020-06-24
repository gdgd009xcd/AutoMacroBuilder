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
public class BurpExtenderDoActionProvider implements InterfaceDoActionProvider {
    
    private BurpExtenderDoAction doaction = new BurpExtenderDoAction(this);
    private int toolflag;
    private boolean messageIsRequest;
    IHttpRequestResponse messageInfo;
    
    public void setParameters(int toolflag, boolean messageIsRequest, IHttpRequestResponse messageInfo){
        this.toolflag = toolflag;
        this.messageIsRequest = messageIsRequest;
        this.messageInfo = messageInfo;
    }
    
    public int getToolFlag(){
        return this.toolflag;
    }
    
    public boolean getMessageIsRequest(){
        return this.messageIsRequest;
    }
    
    public IHttpRequestResponse getMessageInfo(){
        return this.messageInfo;
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
