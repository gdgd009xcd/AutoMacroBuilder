/*
 * Note - you need to rename this file to BurpExtender.java before compiling it
 */

package burp;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zaproxy.zap.extension.automacrobuilder.Encode;
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

    private ThreadLocal<List<InterfaceAction>> ACTION_LIST = new ThreadLocal<>();
    private ThreadLocal<InterfaceEndAction> ENDACTION = new ThreadLocal<>();
    ParmGenMacroTraceProvider pmtProvider = null;
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();

    BurpExtenderDoAction(ParmGenMacroTraceProvider pmtProvider){
        this.pmtProvider = pmtProvider;
    }

    /**
     * set parameters into InterfaceAction and InterfaceEndAction
     * 
     * @param toolflag
     * @param messageIsRequest
     * @param messageInfo 
     */
    public void setParameters(int toolflag, boolean messageIsRequest, IHttpRequestResponse messageInfo){
        List<InterfaceAction> actionlist = new CopyOnWriteArrayList<>();
        IHttpService tiserv = messageInfo.getHttpService();
        String h = tiserv.getHost();
        int p = tiserv.getPort();
        boolean tisSSL = (tiserv.getProtocol().toLowerCase().equals("https")?true:false);
        PRequest preq = new PRequest(h, p, tisSSL, messageInfo.getRequest(), Encode.ISO_8859_1);
        UUID uuid = preq.getUUID5CustomHeader();
        ParmGenMacroTrace pmtRunning = pmtProvider.getRunningInstance(uuid);
        LOGGER4J.debug("getRunnningInstance this:" + pmtRunning + " uuid:" + uuid);

        if(pmtRunning != null) {
            ParmGenMacroTrace pmtBase = pmtProvider.getBaseInstance(pmtRunning.getTabIndex());
            long tid = pmtRunning.getThreadId();

            actionlist.add((t, o) ->{
                return processHttpMessage(o, pmtRunning, toolflag, messageIsRequest, messageInfo);
            });
            ACTION_LIST.set(actionlist);

            InterfaceEndAction endaction = () -> {
                pmtBase.updateOriginalBase(pmtRunning);
                pmtProvider.removeEndInstance(pmtRunning.getUUID());
            };
            ENDACTION.set(endaction);

            LOGGER4J.debug("setParameters "+ (messageIsRequest?"REQUEST":"RESPONSE")+" tool:"  + BurpExtender.getToolname(toolflag)+ " threadid:" + Thread.currentThread().getId() + " X-THREAD:" + tid + " UUID:" + uuid.toString());
        } else {
            LOGGER4J.debug("setParameters "+ (messageIsRequest?"REQUEST":"RESPONSE")+" tool:"  + BurpExtender.getToolname(toolflag) + " pmt is null");
        }
    }
    
    public boolean processHttpMessage(OneThreadProcessor otp, ParmGenMacroTrace pmt,
        	int toolflag,
            boolean messageIsRequest,
            IHttpRequestResponse messageInfo)
        {


            Encode lastResponseEncode = Encode.ISO_8859_1;
            if (pmt != null) {
                lastResponseEncode = pmt.getLastResponseEncode();
            }

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

                            PRequestResponse prs = new PRequestResponse(host, port, isSSL, messageInfo.getRequest(), messageInfo.getResponse(), lastResponseEncode, lastResponseEncode);
                            url = prs.request.getURL();
                            LOGGER4J.debug("=====ResponseRun start====== status:" + prs.response.getStatus());
                            int updtcnt = pgen.ResponseRun(url, prs);
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
                                        if(pmt.isCBFinalResponse()){
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
                                pmtProvider.removeEndInstance(pmt.getUUID());
                            }
                        }
                    } else {
                        pmt.macroEnded();
                    }
                    
                }
                if(pmt.getState() == PMT_POSTMACRO_NULL) {
                    // otp.setOptData(pmt);
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
        try{
            List<InterfaceAction> actionlist = ACTION_LIST.get();
            return actionlist;
        } catch (Exception e) {
            LOGGER4J.error("", e);
            return null;
        } finally {
            ACTION_LIST.remove();
        }
    }

    @Override
    public InterfaceEndAction endAction(ThreadManager tm, OneThreadProcessor otp) {
        try {
            InterfaceEndAction endaction = ENDACTION.get();
            return endaction;
        } catch (Exception e) {
            LOGGER4J.error("", e);
            return null;
        } finally {
            ENDACTION.remove();
        }
    }
}
