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
    
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    
    public BurpMacroStartAction(ParmGenMacroTrace _tr){
        tr = _tr;
    }
    
    @Override
    public String getActionName() {
        return "ParmGenStartAction";
    }

    @Override
    public void performAction(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        Thread oldsender = tr.getSenderThread();
        if (oldsender!=null) {
            LOGGER4J.debug("oldsender id:" + oldsender.getId() +  " stat:" + getThreadStatus(oldsender.getState()));
        }
        tr.startBeforePreMacro();//前処理マクロを実行。
        startCurrentRequest(currentrequest);
    }
    
    private String getThreadStatus(Thread.State st){
        String stval = "";
        switch(st){
            case NEW:
                stval = "NEW";
                break;
            case RUNNABLE:
                stval = "RUNNABLE";
                break;
            case BLOCKED:
                stval = "BLOCKED";
                break;
            case WAITING:
                stval = "WAITING";
                break;
            case TIMED_WAITING:
                stval = "TIMED_WAITING";
                break;
            case TERMINATED:
                stval = "TERMINATED";
                break;
            default:
                stval = "UNKNOWN";
                break;
        }
        return stval;
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
        
        LOGGER4J.debug("StartAction Current StepNo:" + tr.getStepNo() + " host:"+ host + "Threadid:" + Thread.currentThread().getId() );
        tr.burpSetCurrentOriginalRequest(currentRequest.getRequest());
        
        byte[] retval = pgen.Run(host, port, isSSL, currentRequest.getRequest());
        if ( retval != null){
                currentRequest.setRequest(retval);
        }
    }
    
}
