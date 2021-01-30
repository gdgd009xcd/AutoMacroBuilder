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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;

import org.zaproxy.zap.extension.automacrobuilder.PRequestResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGen;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenUtil;
import org.zaproxy.zap.extension.automacrobuilder.ParmVars;
import org.zaproxy.zap.extension.automacrobuilder.Encode;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceLangOKNG;
import org.zaproxy.zap.extension.automacrobuilder.generated.LangSelectDialog;
import org.zaproxy.zap.extension.automacrobuilder.PRequest;
import org.zaproxy.zap.extension.automacrobuilder.PResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenJSONSave;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceParams;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;
import org.zaproxy.zap.extension.automacrobuilder.ThreadManagerProvider;
import org.zaproxy.zap.extension.automacrobuilder.generated.ParmGenTop;



public class BurpExtender implements IBurpExtender,IHttpListener
{
    public static IBurpExtenderCallbacks mCallbacks;
    BurpExtenderDoActionProvider provider = null;
    BurpHelpers helpers;
    MacroBuilder mbr = null;
    IHttpRequestResponse[] selected_messageInfo = null;
    JMenuItem repeatermodeitem = null;
    ParmGenMacroTraceProvider pmtProvider = null;
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();

    private void ProcessHTMLComments(String message, String host, String url)
    {
        try
        {
            // Create matcher
            Pattern pattern = ParmGenUtil.Pattern_compile("<!--\n{0,}.+?\n{0,}-->");
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
    
    public static String getToolname(int toolflag){
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
                    case IBurpExtenderCallbacks.TOOL_SCANNER:
                        toolname ="SCANNER";
                        break;
                    default:
                        toolname ="UNKNOWN TOOL.";
                        break;
                }
                return toolname;
    }
    
    private Encode analyzeCharset(IHttpRequestResponse[] messageInfo){
        
        List<PResponse> resopt = null;
        
        if(messageInfo!=null&&messageInfo.length>0){
            resopt = Arrays.stream(messageInfo).map(minfo -> new PResponse(minfo.getResponse(), Encode.ISO_8859_1)).collect(Collectors.toList());
        }
        
        return Encode.analyzeCharset(resopt);
    }
    
    private ArrayList <PRequestResponse> convertMessageInfoToArray(IHttpRequestResponse[] messageInfo, int toolflg){
        ArrayList <PRequestResponse> messages = new ArrayList<PRequestResponse>() ;
        try {
            
            
            for(int i = 0; i< messageInfo.length; i++){
                byte[] binreq = new String("").getBytes(Encode.ISO_8859_1.getIANACharset());//length 0 String byte
                byte[] binres = new String("").getBytes(Encode.ISO_8859_1.getIANACharset());//length 0 String byte
                String res = "";
                IHttpService iserv = null;
                if (messageInfo[i].getRequest() != null){
                    binreq = messageInfo[i].getRequest();
                    iserv = messageInfo[i].getHttpService();
                }
                if(messageInfo[i].getResponse()!=null){
                    binres = messageInfo[i].getResponse();
                }
                if(iserv != null){
                    boolean ssl = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
                    switch(toolflg){
                        case IBurpExtenderCallbacks.TOOL_INTRUDER:
                            //remove special § chars
                            PRequest cleanreq = new PRequest(iserv.getHost(), iserv.getPort(), ssl, binreq, Encode.ISO_8859_1).newRequestWithRemoveSpecialChars(null);
                            binreq = cleanreq.getByteMessage();
                            break;
                        default:
                            break;
                    }
                    
                    messages.add(new PRequestResponse(iserv.getHost(), iserv.getPort(), ssl, binreq, binres, ParmVars.enc));
                }else{
                    messages.add(new PRequestResponse("", 0, false, binreq, binres, ParmVars.enc));
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

                byte[] binreq = new String("").getBytes(Encode.ISO_8859_1.getIANACharset());//length 0 String byte
                byte[] binres = new String("").getBytes(Encode.ISO_8859_1.getIANACharset());//length 0 String byte
                IHttpService iserv = null;
                if (messageInfo.getRequest() != null){
                    binreq = messageInfo.getRequest();
                    iserv = messageInfo.getHttpService();
                }
                if(messageInfo.getResponse()!=null){
                    binres = messageInfo.getResponse();
                }
                if(iserv !=null){
                    boolean ssl = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
                    prr = new PRequestResponse(iserv.getHost(), iserv.getPort(), ssl, binreq, binres, ParmVars.enc);
                }else{
                    prr = new PRequestResponse("", 0, false, binreq, binres, ParmVars.enc);
                }

        }catch(Exception e){
            LOGGER4J.error("", e);
            return null;
        }
        return prr;
    }

    private BurpExtenderDoActionProvider getProvider(int toolflag, boolean messageIsRequest, IHttpRequestResponse messageInfo){
        if ( this.provider == null ) {
            this.provider = new BurpExtenderDoActionProvider(this.pmtProvider);
        }
        this.provider.setParameters(toolflag, messageIsRequest, messageInfo);
        return this.provider;
    }
    
    @Override
    public void processHttpMessage(
        	int toolflag,
            boolean messageIsRequest,
            IHttpRequestResponse messageInfo ){
        ThreadManagerProvider.getThreadManager().beginProcess(getProvider(toolflag, messageIsRequest, messageInfo));
    }




    class NewMenu implements IContextMenuFactory, InterfaceLangOKNG
    {

        IHttpRequestResponse[] messageInfo = null;
        ParmGenMacroTrace pmtBase = null;
        PRequestResponse newToolBaseLine = null;

        int toolflg = -1;

        @Override
        public List<JMenuItem> createMenuItems(IContextMenuInvocation icmi) {

            ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();
            
            toolflg = icmi.getToolFlag();
            messageInfo = icmi.getSelectedMessages();
            
            JMenuItem item = new JMenuItem("■Custom■");
            JMenuItem itemmacro = new JMenuItem("■SendTo MacroBuilder■");

            newToolBaseLine = null;
            if (messageInfo != null && messageInfo.length > 0) {
                newToolBaseLine = convertMessageInfoToPRR(messageInfo[0]);
            }

            int tabIndex = -1;
            pmtBase = null;
            PRequestResponse currentToolBaseLine = null;
            if (newToolBaseLine != null) {
                ParmGenMacroTraceParams pmtParams = newToolBaseLine.request.getParamsCustomHeader();
                tabIndex = pmtParams.getTabIndex();
                pmtBase = BurpExtender.this.pmtProvider.getBaseInstance(tabIndex);
                if (pmtBase != null) {
                    currentToolBaseLine = pmtBase.getToolBaseline();
                }
            }
            if(BurpExtender.this.pmtProvider.isBaseLineMode()){
                boolean hasMenu = false;
                String menutitle = "■Update Baseline■";
                String tooltip = "Update Baseline: You can tamper tracking tokens which is such like CSRF tokens.";
                switch(toolflg){
                    case IBurpExtenderCallbacks.TOOL_REPEATER:
                        hasMenu = true;
                        break;
                    case IBurpExtenderCallbacks.TOOL_SCANNER:
                    case IBurpExtenderCallbacks.TOOL_INTRUDER:
                        menutitle = "■Clear Baseline■";
                        tooltip = "Clear Baseline: You should select this menu when  you used repeater  in baseline mode.";
                        if (currentToolBaseLine != null) {
                            hasMenu = true;
                        }
                    default:
                        newToolBaseLine = null;
                        break;
                }
                if(hasMenu && pmtBase != null){
                    repeatermodeitem = new JMenuItem(menutitle);
                    repeatermodeitem.setToolTipText(tooltip);

                    repeatermodeitem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            String toolname = getToolname(toolflg);
                            LOGGER4J.debug("updatebaselineAction:" + toolname + ":" + (newToolBaseLine==null?"NULL":"NONULL"));
                            pmtBase.setToolBaseLine(newToolBaseLine);
                        }
                    });
                }else{
                    repeatermodeitem = null;
                }
            
            }else{
                repeatermodeitem = null;
            }
            
            

            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuItemClicked(messageInfo, toolflg);
                }
            });
            itemmacro.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuAddRequestsClicked(messageInfo);
                }
            });
            
            
            items.add(itemmacro);
            items.add(item);
            if(repeatermodeitem!=null){
                items.add(repeatermodeitem);
            }
            
            

            return items;
        }

        public void menuItemClicked( IHttpRequestResponse[] messageInfo, int toolflg)
        {
            try
            {
                //選択したリクエストレスポンス
                //プロキシヒストリのリクエストレスポンス
                //IHttpRequestResponse[] allmessages = mCallbacks.getProxyHistory();
                int tabindex = BurpExtender.this.mbr.getMacroRequestListTabsSelectedIndex();
                ParmGenMacroTrace pmt = BurpExtender.this.pmtProvider.getBaseInstance(tabindex);
                
                ParmGen pgen = new ParmGen(pmt);
                if(pgen.twin==null){
                    pgen.twin = new ParmGenTop(pmt, new ParmGenJSONSave(pmt,
                        convertMessageInfoToArray(messageInfo, toolflg))
                        );
                }
                pgen.twin.VisibleWhenJSONSaved(mbr.getUiComponent());
            }
            catch (Exception e)
            {
                LOGGER4J.error("", e);
            }
        }

        
        public void menuAddRequestsClicked( IHttpRequestResponse[] messageInfo)
        {
            int tabindex = BurpExtender.this.mbr.getMacroRequestListTabsSelectedIndex();
            ParmGenMacroTrace pmt = BurpExtender.this.pmtProvider.getBaseInstance(tabindex);
            if(pmt!=null){
                if(pmt.getRlistCount()<=0){
                    Encode lang = analyzeCharset(messageInfo);
                    new LangSelectDialog(null, this, lang, false).setVisible(true);
                }else{
                    LangOK();
                }
            }
        }
        
        @Override
        public void LangOK() {
            if(messageInfo!=null){
                if(mbr!=null){
                //選択したリクエストレスポンス
                    mbr.addNewRequests(
                        convertMessageInfoToArray(messageInfo, toolflg));
                }
            }
        }

        @Override
        public void LangCANCEL() {
            /*** NOP ****/
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
        //LockInstance locker = new LockInstance();
        this.pmtProvider = new ParmGenMacroTraceProvider();
        //セッション管理
        callbacks.registerSessionHandlingAction(new BurpMacroStartAction(this.pmtProvider));
        //callbacks.registerSessionHandlingAction(new BurpMacroLogAction());
    	//コンテキストメニューの追加：　マウス右クリックポップアップメニュー->[my menu item]
        callbacks.registerContextMenuFactory(new NewMenu());
        // register ourselves as an HTTP listener
        callbacks.registerHttpListener(this);
        // register proxy lister
        //callbacks.registerProxyListener(this);
        //MacroBuilderタブ
        
        mbr = new MacroBuilder(pmtProvider);
        callbacks.addSuiteTab(mbr);
        mCallbacks = callbacks;
    }

}
