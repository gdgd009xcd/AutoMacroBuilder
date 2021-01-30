/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zaproxy.zap.extension.automacrobuilder.*;

import static org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace.PMT_CURRENT_BEGIN;


/**
 * マクロ実行前に登録するアクション
 * @author daike
 */
public class BurpMacroStartDoAction implements InterfaceDoAction {
    
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    
    // ACTION LIST is per thread instance.
    private ThreadLocal<List<InterfaceAction>> ACTION_LIST = new ThreadLocal<>();
    
    ParmGenMacroTraceProvider pmtProvider = null;
        
    
    public BurpMacroStartDoAction(ParmGenMacroTraceProvider pmtProvider){
        this.pmtProvider = pmtProvider;
    }
    
    /**
     * Set parameters used in DoAction
     * 
     * @param currentrequest
     * @param executedmacros 
     */
    public void setParamters(IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros){
        // create ParmGenMacroTrace new Instance per thread.
        IHttpService iserv = currentrequest.getHttpService();
        String host = iserv.getHost();
        int port = iserv.getPort();
        boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
        PRequest request = new PRequest(host, port, isSSL, currentrequest.getRequest(), ParmVars.enc);
        ParmGenMacroTraceParams pmtParams = request.getParamsCustomHeader();
        final ParmGenMacroTrace pmt = this.pmtProvider.getNewParmGenMacroTraceInstance(Thread.currentThread().getId(), pmtParams);
        
        List<InterfaceAction> actionlist = new CopyOnWriteArrayList<>();
        
        actionlist.add((t, o) -> {
            return performAction(o, pmt, currentrequest, executedmacros);
        });
        
        ACTION_LIST.set(actionlist);
    }
    
   
    public boolean  performAction(OneThreadProcessor otp, ParmGenMacroTrace pmt, IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        pmt.startBeforePreMacro(otp);//前処理マクロを実行。
        startCurrentRequest(pmt, currentrequest);
        return true;
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
    
    public void startCurrentRequest(ParmGenMacroTrace pmt, IHttpRequestResponse currentRequest){
        pmt.clearComments();
        pmt.setError(false);
        //state = PMT_CURRENT_BEGIN;
        pmt.setState(PMT_CURRENT_BEGIN);
        ParmGen pgen = new ParmGen(pmt);
        IHttpService iserv = currentRequest.getHttpService();
        String host = iserv.getHost();
        int port = iserv.getPort();
        boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
        
        LOGGER4J.debug("StartAction Current StepNo:" + pmt.getStepNo() + " host:"+ host + "Threadid:" + Thread.currentThread().getId() );
        pmt.burpSetCurrentOriginalRequest(currentRequest.getRequest());

        byte[] retval = pgen.Run(host, port, isSSL, currentRequest.getRequest());
        if ( retval != null){
            currentRequest.setRequest(retval);
        }
    }

    /**
     * newly create action and Initialize it. this function runs synchronusly.
     * 
     * @param tm
     * @param otp 
     */
    @Override
    public List<InterfaceAction> startAction(ThreadManager tm, OneThreadProcessor otp) {
       try {
            List<InterfaceAction> actionlist = ACTION_LIST.get();
            return actionlist;
       } finally {
           ACTION_LIST.remove();
       }
    }

 

    @Override
    public InterfaceEndAction endAction(ThreadManager tm, OneThreadProcessor otp) {
        
        // nothing to do 
        
        return null;
    }

 
    
}
