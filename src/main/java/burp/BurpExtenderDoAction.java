/*
 * Note - you need to rename this file to BurpExtender.java before compiling it
 */

package burp;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zaproxy.zap.extension.automacrobuilder.PRequestResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGen;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;
import org.zaproxy.zap.extension.automacrobuilder.ParmVars;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceAction;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceDoAction;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceEndAction;
import org.zaproxy.zap.extension.automacrobuilder.OneThreadProcessor;
import org.zaproxy.zap.extension.automacrobuilder.PRequest;

import static org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace.PMT_POSTMACRO_NULL;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;
import org.zaproxy.zap.extension.automacrobuilder.ThreadManager;


public class BurpExtenderDoAction implements InterfaceDoAction
{
    BurpExtenderDoActionProvider provider = null;
    //private int toolflag;
    //private boolean messageIsRequest;
    //IHttpRequestResponse messageInfo;

    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();

    BurpExtenderDoAction(BurpExtenderDoActionProvider provider){
        this.provider = provider;
    }
    
    
    
    public boolean processHttpMessage(OneThreadProcessor otp, ParmGenMacroTrace pmt,
        	int toolflag,
            boolean messageIsRequest,
            IHttpRequestResponse messageInfo)
        {
            

               ParmGen pgen = new ParmGen(pmt);
                String url = null;
                String toolname = BurpExtender.getToolname(toolflag);
                
                LOGGER4J.debug("toolname:" + toolname + " " + (messageIsRequest?"Request":"Response"));
                
                switch(toolflag){
                    case IBurpExtenderCallbacks.TOOL_INTRUDER:
                    case IBurpExtenderCallbacks.TOOL_SCANNER:
                        pmt.setToolBaseLine(null);
                        break;
                    default:
                        break;
                }
                if (
                        (toolflag == IBurpExtenderCallbacks.TOOL_INTRUDER && pgen.IntruderInScope)||
                        (toolflag == IBurpExtenderCallbacks.TOOL_REPEATER && pgen.RepeaterInScope)||
                        (toolflag == IBurpExtenderCallbacks.TOOL_SCANNER && pgen.ScannerInScope)||
                        ((pgen.ScannerInScope||pgen.RepeaterInScope||pgen.IntruderInScope)&&toolflag==IBurpExtenderCallbacks.TOOL_EXTENDER)
                    )
                {
                    if (messageIsRequest)
                    {
                                        // update number sequeces...

                        try{
                            LOGGER4J.debug("=====RequestRun start======");
                            if(pmt!=null){
                                LOGGER4J.debug("state:" + pmt.state_debugprint());
                            }
                            

                            if(pmt!=null&&!pmt.isCurrentRequest()){
                                IHttpService iserv = messageInfo.getHttpService();
                                String host = iserv.getHost();
                                int port = iserv.getPort();
                                boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
                                byte[] retval = pgen.Run(host, port, isSSL, messageInfo.getRequest());
                                if ( retval != null){
                                        messageInfo.setRequest(retval);

                                }
                            } else {
                                LOGGER4J.debug("current request object comment:" + messageInfo.getComment());
                            }

                        }catch(Exception e){
                            pmt.macroEnded();//something wrong happened. 
                            LOGGER4J.error("RequestRun Exception", e);
                        }

                       
                       
                    }else{
                        try {
                            IHttpService iserv = messageInfo.getHttpService();
                            String host = iserv.getHost();
                            int port = iserv.getPort();
                            boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
                            //PRequestResponse prs = new PRequestResponse(new String(messageInfo.getRequest(), ParmVars.enc.getIANACharset()), new String(messageInfo.getResponse(), ParmVars.enc.getIANACharset()));
                            PRequestResponse prs = new PRequestResponse(host, port, isSSL, messageInfo.getRequest(), messageInfo.getResponse(), ParmVars.enc);
                            url = prs.request.getURL();
                            LOGGER4J.debug("=====ResponseRun start====== status:" + prs.response.getStatus());
                            int updtcnt = pgen.ResponseRun(url, messageInfo.getResponse(), ParmVars.enc);
                            LOGGER4J.debug("=====ResponseRun end======");
                            if(pmt!=null){
                                pmt.parseSetCookie(prs);//save Set-Cookie values into CookieStore.
                                switch(pmt.getState()){
                                    case ParmGenMacroTrace.PMT_CURRENT_BEGIN://カレントリクエストが終了。
                                        pmt.endAfterCurrentRequest(prs);
                                        pmt.startPostMacro(otp);
                                        break;
                                    default:
                                        break;
                                }
                                
                                int percent = -1;
                                switch(pmt.getState()){
                                    case ParmGenMacroTrace.PMT_POSTMACRO_END:
                                        //update current burptool(repeater/scanner/intruder..)'s response by last postmacro response.
                                        if(pmt.isMBFinalResponse()){
                                            messageInfo.setResponse(pmt.getPostMacroResponse());
                                        }
                                        pmt.macroEnded();
                                        percent = pmt.getScanQuePercentage();
                                        break;
                                    case ParmGenMacroTrace.PMT_POSTMACRO_NULL:
                                        LOGGER4J.debug("====postmacro response NULL========");
                                        pmt.macroEnded();
                                        percent = pmt.getScanQuePercentage();
                                    default:
                                        break;
                                }
                                if(percent != -1){
                                    LOGGER4J.info("ActiveScan [" + percent + "]%");
                                }
                            }
                        } catch (Exception ex) {
                            pmt.macroEnded();//something wrong happened. 
                            LOGGER4J.error("", ex);
                            //Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }else{// NOP
                    if (messageIsRequest){
                        if(pmt!=null&&pmt.isCurrentRequest()){
                            // restore currentRequest
                            byte[] originalrequestbytes = pmt.burpGetCurrentOriginalRequest();
                            if( originalrequestbytes!=null) {
                                LOGGER4J.debug("REQUEST  RESET To OriginalRequest.");
                                messageInfo.setRequest(originalrequestbytes);
                                ParmGenMacroTraceProvider.removeEndInstance(pmt.getUUID());
                            }
                        }
                    } else {
                        pmt.macroEnded();
                    }
                    
                }
                if(pmt.getState() == PMT_POSTMACRO_NULL) {
                    otp.setOptData(pmt);
                    return true;
                }
                return false;
                
        }
 

    
    /**
     * get parameters from provider and create InterfaceAction. this function runs as synchronusly
     *
     * @param tm
     * @param otp
     * @return 
     */

    @Override
    public List<InterfaceAction> startAction(ThreadManager tm, OneThreadProcessor otp) {
        List<InterfaceAction> actionlist = new CopyOnWriteArrayList<>();
        final int toolflag = provider.getToolFlag();
        final boolean messageIsRequest = provider.getMessageIsRequest();
        final IHttpRequestResponse messageInfo = provider.getMessageInfo();
        IHttpService tiserv = messageInfo.getHttpService();
        String h = tiserv.getHost();
        int p = tiserv.getPort();
        boolean tisSSL = (tiserv.getProtocol().toLowerCase().equals("https")?true:false);
        PRequest preq = new PRequest(h, p, tisSSL, messageInfo.getRequest(), ParmVars.enc);
        UUID uuid = preq.getUUID5CustomHeader();
        ParmGenMacroTrace pmt = ParmGenMacroTraceProvider.getRunningInstance(uuid);
        if(pmt != null) {
            long tid = pmt.getThreadId();
            LOGGER4J.debug("startAction "+ (messageIsRequest?"REQUEST":"RESPONSE")+" tool:"  + BurpExtender.getToolname(toolflag)+ " threadid:" + Thread.currentThread().getId() + " X-THREAD:" + tid + " UUID:" + uuid.toString());
            actionlist.add((t, o) ->{
                return processHttpMessage(o, pmt, toolflag, messageIsRequest, messageInfo);
            });
        } else {
            LOGGER4J.error("startAction "+ (messageIsRequest?"REQUEST":"RESPONSE")+" tool:"  + BurpExtender.getToolname(toolflag)+"pmt is null");
        }
        return actionlist;
    }

 

    @Override
    public InterfaceEndAction endAction(ThreadManager tm, OneThreadProcessor otp) {
        ParmGenMacroTrace pmt = otp.getOptData();
        
        ParmGenMacroTrace pmtoriginal = ParmGenMacroTraceProvider.getRunningInstance(pmt.getUUID());
        if(pmtoriginal == pmt) {
            InterfaceEndAction action = () -> {
                ParmGenMacroTraceProvider.getOriginalBase().updateOriginalBase(pmt);
                ParmGenMacroTraceProvider.removeEndInstance(pmt.getUUID());
            };
            return action;
        } else {
            LOGGER4J.error("pmtoriginal id:"+ pmtoriginal.getThreadId() +" != pmt id:" + pmt.getThreadId());
        }
        InterfaceEndAction nothingaction = () -> {
            LOGGER4J.debug("Endaction Nothing to do");
        };
        
        return nothingaction;

    }





}
