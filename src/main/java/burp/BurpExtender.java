/*
 * Note - you need to rename this file to BurpExtender.java before compiling it
 */

package burp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;



public class BurpExtender implements IBurpExtender,IHttpListener, IProxyListener
{
    public IBurpExtenderCallbacks mCallbacks;
    BurpHelpers helpers;
    MacroBuilder mbr = null;
    ParmGenMacroTrace pmt = null;


    public void processHttpMessage(
        	int toolflag,
            boolean messageIsRequest,
            IHttpRequestResponse messageInfo)
        {

                ParmGen pgen = new ParmGen(pmt);
                String url = null;
                String toolname = "";
                switch(toolflag){
                    case IBurpExtenderCallbacks.TOOL_INTRUDER:
                        toolname ="INTRUDER";
                        break;
                    case IBurpExtenderCallbacks.TOOL_REPEATER:
                        toolname = "REPEATER";
                         break;
                    case IBurpExtenderCallbacks.TOOL_COMPARER:
                        toolname = "COMPARER";
                        break;
                    case IBurpExtenderCallbacks.TOOL_DECODER:
                        toolname = "DECODER";
                         break;
                    case IBurpExtenderCallbacks.TOOL_EXTENDER:
                        toolname ="EXTENDER";
                        break;
                    case IBurpExtenderCallbacks.TOOL_PROXY:
                        toolname ="PROXY";
                         break;
                    case IBurpExtenderCallbacks.TOOL_SEQUENCER:
                        toolname ="SEQUENCER";
                        break;
                    case IBurpExtenderCallbacks.TOOL_SPIDER:
                        toolname ="SPIDER";
                         break;
                    case IBurpExtenderCallbacks.TOOL_SUITE:
                        toolname ="SUITE";
                         break;
                    case IBurpExtenderCallbacks.TOOL_TARGET:
                        toolname ="TARGET";
                         break;
                    default:
                        toolname ="UNKNOWN TOOL.";
                        break;
                }
                ParmVars.plog.debuglog(0, "toolname:" + toolname);
                if (/***
                        (toolName == "intruder" && pgen.IntruderInScope )||
                        (toolName == "repeater" && pgen.RepeaterInScope )||
                        (toolName == "scanner" && pgen.ScannerInScope )***/
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
                            ParmVars.plog.debuglog(0, "=====RequestRun start======");
                            if(pmt!=null){
                                ParmVars.plog.debuglog(0,"state:" + Integer.toString(pmt.getState()));

                            }
                            

                            byte[] retval = pgen.Run(messageInfo.getRequest());
 


                            if ( retval != null){
                                    messageInfo.setRequest(retval);

                            }

 
                        }catch(Exception e){
                            ParmVars.plog.debuglog(0, "RequestRun Exception");
                            ParmVars.plog.printException(e);
                        }

                        // Update last request time and append cookies to request
                        //messageInfo = deleteCookies(messageInfo);
                        //
                    }else{
                        try {
                            try{
                                String request_string = new String(messageInfo.getRequest(), ParmVars.enc.getIANACharset());
                                PRequest prequest = new PRequest(request_string);
                                url = prequest.getURL();
                            }catch(Exception e){
                                return;
                            }
                            PRequestResponse prs = new PRequestResponse(new String(messageInfo.getRequest(), ParmVars.enc.getIANACharset()), new String(messageInfo.getResponse(), ParmVars.enc.getIANACharset()));
                            ParmVars.plog.debuglog(0, "=====ResponseRun start====== status:" + prs.response.status);
                            int updtcnt = pgen.ResponseRun(url, messageInfo.getResponse(), ParmVars.enc.getIANACharset());
                            ParmVars.plog.debuglog(0, "=====ResponseRun end======");
                            if(pmt!=null){
                                switch(pmt.getState()){
                                    case ParmGenMacroTrace.PMT_CURRENT_BEGIN://カレントリクエストが終了。
                                        pmt.endAfterCurrentRequest(prs);
                                        break;
                                    default:
                                        break;
                                }
                                switch(pmt.getState()){
                                    case ParmGenMacroTrace.PMT_CURRENT_END:
                                        pmt.startPostMacro();
                                        break;
                                    default:
                                        break;
                                }
                                switch(pmt.getState()){
                                    case ParmGenMacroTrace.PMT_POSTMACRO_END:
                                        //カレントリクエストをpostマクロレスポンスの内容で更新

                                        if(pmt.isMBFinalResponse()){
                                            messageInfo.setResponse(pmt.getPostMacroResponse());
                                        }
                                        mbr.updateCurrentReqRes();
                                        pmt.nullState();
                                        break;
                                    case ParmGenMacroTrace.PMT_POSTMACRO_NULL:
                                        ParmVars.plog.debuglog(0, "====postmacro response NULL========");
                                    default:
                                        break;
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
    		return;
        }
 


    /*public byte[]*/@Override
    public void  processProxyMessage(
            /*****
            int messageReference,
            boolean messageIsRequest,
            String remoteHost,
            int remotePort,
            boolean serviceIsHttps,
            String httpMethod,
            String url,
            String resourceType,
            String statusCode,
            String responseContentType,
            byte[] message,
            int[] interceptAction
            *****/
            boolean messageIsRequest,
             IInterceptedProxyMessage message
            )
    {

        try
        {

            IHttpRequestResponse httpReqRes = message.getMessageInfo();

            ParmGen pgen = new ParmGen(pmt);
            if (pgen.ProxyInScope)
            {
                // update number sequeces...
                if (messageIsRequest){

                            try{
                                    byte[] retval = pgen.Run(httpReqRes.getRequest());
                                    if ( retval != null){
                                        httpReqRes.setRequest(retval);
                                    }
                            }catch(Exception e){
                                    ParmVars.plog.printlog(e.toString(), true);
                            }
                            // Update last request time and append cookies to request
                }else{//Fetch Responses...
                    try{
                        String request_string = new String(httpReqRes.getRequest(), ParmVars.enc.getIANACharset());
                        PRequest prequest = new PRequest(request_string);
                        try {
                            int updtcnt = pgen.ResponseRun(prequest.getURL(), httpReqRes.getResponse(), ParmVars.enc.getIANACharset());
                        } catch (Exception ex) {
                            Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }catch(Exception e){
                        ParmVars.plog.printException(e);
                    }
                    
                }
            }
        }
        catch (Exception e)
        {
            ParmVars.plog.printException(e);
        }
        return;
    }

    private void ProcessHTMLComments(String message, String host, String url)
    {
        try
        {
            // Create matcher
            Pattern pattern = Pattern.compile("<!--\n{0,}.+?\n{0,}-->");
            Matcher matcher = pattern.matcher(message);
            boolean printed = false;

            // Find all matches and print the url only one time
            while (matcher.find())
            {
                if (!printed)
                {
                    String header = "HTML COMMENT IN:" + host + url + "\r\n==========================";
                    ParmVars.plog.debuglog(1,header);
                    SaveToFile(host, header, true);
                    printed = true;
                }
                // Get the matching string
                String comment = matcher.group();
                ParmVars.plog.debuglog(1,comment);
                SaveToFile(host, comment, false);
            }
        }
        catch (Exception e)
        {
            ParmVars.plog.printException(e);
        }
    }

    private void SaveToFile(String fileName, String st2write, boolean printTime)
    {
        File aFile = new File(fileName + ".txt");
        Date now = new Date();
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(aFile, aFile.exists()));
            if (printTime)
            {
                out.write("\r\n\r\n" + now.toString() + "\r\n");
            }
            out.write(st2write + "\r\n");
            out.close();
        }
        catch (IOException e)
        {
            ParmVars.plog.printException(e);
        }
    }

    private ArrayList <PRequestResponse> convertMessageInfoToArray(IHttpRequestResponse[] messageInfo){
        ArrayList <PRequestResponse> messages = new ArrayList<PRequestResponse>() ;
        try {
            for(int i = 0; i< messageInfo.length; i++){
                byte[] binreq = null;
                String res = "";
                IHttpService iserv = null;
                if (messageInfo[i].getRequest() != null){
                    binreq = messageInfo[i].getRequest();
                    iserv = messageInfo[i].getHttpService();
                }
                if(messageInfo[i].getResponse()!=null){
                    res = new String(messageInfo[i].getResponse(), ParmVars.enc.getIANACharset());
                }
                if(iserv != null){
                    boolean ssl = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
                    messages.add(new PRequestResponse(iserv.getHost(), iserv.getPort(), ssl, binreq, res));
                }else{
                    messages.add(new PRequestResponse("", res));
                }
            }
        }catch(Exception e){
            ParmVars.plog.printException(e);
            return null;
        }
        return messages;
    }

   private PRequestResponse convertMessageInfoToPRR(IHttpRequestResponse messageInfo){
       PRequestResponse prr = null;
        try {

                String req = "";
                String res = "";
                IHttpService iserv = null;
                byte[] binreq = null;
                if (messageInfo.getRequest() != null){
                    binreq = messageInfo.getRequest();
                    iserv = messageInfo.getHttpService();
                }
                if(messageInfo.getResponse()!=null){
                    res = new String(messageInfo.getResponse(), ParmVars.enc.getIANACharset());
                }
                if(iserv !=null){
                    boolean ssl = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
                    prr = new PRequestResponse(iserv.getHost(), iserv.getPort(), ssl, binreq, res);
                }else{
                    prr = new PRequestResponse("", res);
                }

        }catch(Exception e){
            ParmVars.plog.printException(e);
            return null;
        }
        return prr;
    }


    class NewMenu implements IContextMenuFactory
    {

        IHttpRequestResponse[] messageInfo = null;

        @Override
        public List<JMenuItem> createMenuItems(IContextMenuInvocation icmi) {

            ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();

            JMenuItem item = new JMenuItem("■Custom■");
            JMenuItem itemmacro = new JMenuItem("■SendTo MacroBuilder■");
            messageInfo = icmi.getSelectedMessages();

            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuItemClicked(messageInfo);
                }
            });
            itemmacro.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuAddRequestsClicked(messageInfo);
                }
            });
            
            items.add(itemmacro);
            items.add(item);
            

            return items;
        }

        public void menuItemClicked( IHttpRequestResponse[] messageInfo)
        {
            try
            {
                //選択したリクエストレスポンス
                //プロキシヒストリのリクエストレスポンス
                //IHttpRequestResponse[] allmessages = mCallbacks.getProxyHistory();
                ParmGen pgen = new ParmGen(pmt, null);//csv読み込み。LANG（ParmVars.enc）を設定。
                if(pgen.twin==null){
                    pgen.twin = new ParmGenTop(pmt, new ParmGenCSV(pmt,
                        convertMessageInfoToArray(messageInfo))
                        );
                }
                pgen.twin.setVisible(true);
            }
            catch (Exception e)
            {
                ParmVars.plog.printException(e);
            }
        }

        public void menuAddRequestsClicked( IHttpRequestResponse[] messageInfo)
        {
            try
            {


                if(mbr!=null){
                //選択したリクエストレスポンス
                    mbr.addNewRequests(
                        convertMessageInfoToArray(messageInfo));
                }


            }

            catch (Exception e)
            {
                ParmVars.plog.printException(e);
            }
        }
    }


    //カスタム機能の登録
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        helpers = new BurpHelpers(callbacks.getHelpers());
        //burp 標準出力、標準エラー
        // obtain our output and error streams
        PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true);
        PrintWriter stderr = new PrintWriter(callbacks.getStderr(), true);
        //ParmVars.plog.SetBurpPrintStreams(stdout, stderr);
        pmt = new ParmGenMacroTrace(callbacks);
        //セッション管理
        callbacks.registerSessionHandlingAction(new BurpMacroStartAction(pmt));
        //callbacks.registerSessionHandlingAction(new BurpMacroLogAction());
    	//コンテキストメニューの追加：　マウス右クリックポップアップメニュー->[my menu item]
        callbacks.registerContextMenuFactory(new NewMenu());
        // register ourselves as an HTTP listener
        callbacks.registerHttpListener(this);
        // register proxy lister
        callbacks.registerProxyListener(this);
        //MacroBuilderタブ
        mbr = new MacroBuilder(pmt);
        callbacks.addSuiteTab(mbr);
        mCallbacks = callbacks;
    }

}
