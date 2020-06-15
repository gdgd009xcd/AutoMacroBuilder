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


/**
 * マクロ実行前に登録するアクション
 * @author daike
 */
public class BurpMacroStartDoAction implements InterfaceDoAction {
    
    private BurpMacroStartDoActionProvider provider = null;
    
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    
    public BurpMacroStartDoAction(BurpMacroStartDoActionProvider provider){
        this.provider = provider;
    }
    
   
    public boolean  performAction(ParmGenMacroTrace pmt, IHttpRequestResponse currentrequest, IHttpRequestResponse[] executedmacros) {
        pmt.startBeforePreMacro();//前処理マクロを実行。
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
     * Initialize action. It runs synchronusly.
     * 
     * @param tm
     * @param otp 
     */
    @Override
    public List<InterfaceAction> startAction(ThreadManager tm, OneThreadProcessor otp) {
        List<InterfaceAction> actionlist = new CopyOnWriteArrayList<>();
        // copy all parameters locally
        final IHttpRequestResponse currentrequest = this.provider.getCurrentRequest();
        final IHttpRequestResponse[] executedmacros = provider.getExecutedMacros();
        final ParmGenMacroTrace pmt = ParmGenMacroTraceProvider.getNewParmGenMacroTraceInstance(otp.getid());
        
        //otp.setOptData(pmt);// pass ParmGenMacroTrace to OneThreadProcessor's optdata 
        
        actionlist.add((t, o) -> {
            return performAction(pmt, currentrequest, executedmacros);
        });
        
        return actionlist;
    }

 

    @Override
    public InterfaceEndAction endAction(ThreadManager tm, OneThreadProcessor otp) {
        
        // nothing to do 
        InterfaceEndAction endaction = () -> {};
        
        return endaction;
    }

 
    
}
