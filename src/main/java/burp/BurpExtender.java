/*
 * Note - you need to rename this file to BurpExtender.java before compiling it
 */

package burp;

import burp.BurpMacroStartAction;
import burp.IBurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuFactory;
import burp.IContextMenuInvocation;
import burp.IHttpListener;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IInterceptedProxyMessage;
import burp.IProxyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.zaproxy.zap.extension.automacrobuilder.MacroBuilder;
import org.zaproxy.zap.extension.automacrobuilder.PRequestResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGen;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenUtil;
import org.zaproxy.zap.extension.automacrobuilder.ParmVars;
import org.zaproxy.zap.extension.automacrobuilder.Encode;
import org.zaproxy.zap.extension.automacrobuilder.InterfaceLangOKNG;
import org.zaproxy.zap.extension.automacrobuilder.LangSelectDialog;
import org.zaproxy.zap.extension.automacrobuilder.PRequest;
import org.zaproxy.zap.extension.automacrobuilder.PResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenJSONSave;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenTop;



public class BurpExtender implements IBurpExtender,IHttpListener, IProxyListener
{
    public static IBurpExtenderCallbacks mCallbacks;
    BurpHelpers helpers;
    MacroBuilder mbr = null;
    ParmGenMacroTrace pmt = null;
    IHttpRequestResponse[] selected_messageInfo = null;
    JMenuItem repeatermodeitem = null;

    private String getToolname(int toolflag){
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
    
    public void processHttpMessage(
        	int toolflag,
            boolean messageIsRequest,
            IHttpRequestResponse messageInfo)
        {
               ParmGen pgen = new ParmGen(pmt);
                String url = null;
                String toolname = getToolname(toolflag);
                
                ParmVars.plog.debuglog(0, "toolname:" + toolname + " " + (messageIsRequest?"Request":"Response"));
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
                            ParmVars.plog.debuglog(0, "=====RequestRun start======");
                            if(pmt!=null){
                                ParmVars.plog.debuglog(0,"state:" + pmt.state_debugprint());

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
                            }

                        }catch(Exception e){
                            pmt.macroEnded(true);//something wrong happened. 
                            ParmVars.plog.debuglog(0, "RequestRun Exception");
                            ParmVars.plog.printException(e);
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
                            ParmVars.plog.debuglog(0, "=====ResponseRun start====== status:" + prs.response.getStatus());
                            int updtcnt = pgen.ResponseRun(url, messageInfo.getResponse(), ParmVars.enc);
                            ParmVars.plog.debuglog(0, "=====ResponseRun end======");
                            if(pmt!=null){
                                pmt.parseSetCookie(prs);//save Set-Cookie values into CookieStore.
                                switch(pmt.getState()){
                                    case ParmGenMacroTrace.PMT_CURRENT_BEGIN://カレントリクエストが終了。
                                        pmt.endAfterCurrentRequest(prs);
                                        pmt.startPostMacro();
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
                                        pmt.macroEnded(false);
                                        percent = pmt.getScanQuePercentage();
                                        break;
                                    case ParmGenMacroTrace.PMT_POSTMACRO_NULL:
                                        ParmVars.plog.debuglog(0, "====postmacro response NULL========");
                                        pmt.macroEnded(false);
                                        percent = pmt.getScanQuePercentage();
                                    default:
                                        break;
                                }
                                if(percent != -1){
                                    ParmVars.plog.debuglog(0, "ActiveScan [" + percent + "]%");
                                }
                            }
                        } catch (Exception ex) {
                            pmt.macroEnded(true);//something wrong happened. 
                            Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
    		return;
                
        }
 


    /*public byte[]*/@Override
    public void  processProxyMessage(
            boolean messageIsRequest,
             IInterceptedProxyMessage message
            )
    {

        try
        {

            IHttpRequestResponse messageInfo = message.getMessageInfo();
            IHttpService iserv = messageInfo.getHttpService();
            String host = iserv.getHost();
            int port = iserv.getPort();
            boolean isSSL = (iserv.getProtocol().toLowerCase().equals("https")?true:false);
            //PRequestResponse prs = new PRequestResponse(new String(messageInfo.getRequest(), ParmVars.enc.getIANACharset()), new String(messageInfo.getResponse(), ParmVars.enc.getIANACharset()));

            ParmGen pgen = new ParmGen(pmt);
            if (pgen.ProxyInScope)
            {
                // update number sequeces...
                if (messageIsRequest){
                    ParmVars.plog.debuglog(0, "ProcessProxyMessage: messageIsRequest start");
                }else{//Fetch Responses...
                    ParmVars.plog.debuglog(0, "ProcessProxyMessage: messageIsResponse start");
                    try{
                        PRequestResponse prs = new PRequestResponse(host, port, isSSL, messageInfo.getRequest(), messageInfo.getResponse(), ParmVars.enc);
                        String url = prs.request.getURL();
                        try {
                            int updtcnt = pgen.ResponseRun(url, messageInfo.getResponse(), ParmVars.enc);
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
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }catch(Exception e){
                        ParmVars.plog.printException(e);
                    }
                    
                    
                }
                ParmVars.plog.debuglog(0, "ProcessProxyMessage: End");
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

    private Encode analyzeCharset(IHttpRequestResponse[] messageInfo){
        String tcharset = "";
        
        HashMap<Encode, String> langs = new HashMap<Encode,String>();
        for(int i = 0; i< messageInfo.length; i++){
            if(messageInfo[i].getResponse()!=null){
                if(tcharset.isEmpty()){
                    PResponse pres = new PResponse(messageInfo[i].getResponse(), Encode.ISO_8859_1);
                    tcharset = pres.getCharset();
                    ParmVars.plog.debuglog(0,tcharset);
                    if(tcharset!=null&&!tcharset.isEmpty()){
                        if(Encode.isExistEnc(tcharset)){
                            langs.put(Encode.getEnum(tcharset), tcharset);
                        }
                    }
                }
            }
        }
        
        for(Map.Entry<Encode, String> e : langs.entrySet()) {
            Encode lang = e.getKey();
            switch(lang){
            case KOI8_R:
            case Big5:
            case Big5_HKSCS:
            case EUC_JP:
            case EUC_KR:
            case GB18030:
            case GB2312:
            case GBK:
            case IBM_Thai:
            case IBM00858:
            case IBM01140:
            case IBM01141:
            case IBM01142:
            case IBM01143:
            case IBM01144:
            case IBM01145:
            case IBM01146:
            case IBM01147:
            case IBM01148:
            case IBM01149:
            case IBM037:
            case IBM1026:
            case IBM1047:
            case IBM273:
            case IBM277:
            case IBM278:
            case IBM280:
            case IBM284:
            case IBM285:
            case IBM297:
            case IBM420:
            case IBM424:
            case IBM437:
            case IBM500:
            case IBM775:
            case IBM850:
            case IBM852:
            case IBM855:
            case IBM857:
            case IBM860:
            case IBM861:
            case IBM862:
            case IBM863:
            case IBM864:
            case IBM865:
            case IBM866:
            case IBM868:
            case IBM869:
            case IBM870:
            case IBM871:
            case IBM918:
            case ISO_2022_CN:
            case ISO_2022_JP:
            case ISO_2022_KR:
            case Shift_JIS:
            case TIS_620:
            case windows_1255:
            case windows_1256:
            case windows_1258:
            case windows_31j:
            case x_Big5_Solaris:
            case x_euc_jp_linux:
            case x_EUC_TW:
            case x_eucJP_Open:
            case x_IBM1006:
            case x_IBM1025:
            case x_IBM1046:
            case x_IBM1097:
            case x_IBM1098:
            case x_IBM1112:
            case x_IBM1122:
            case x_IBM1123:
            case x_IBM1124:
            case x_IBM1381:
            case x_IBM1383:
            case x_IBM33722:
            case x_IBM737:
            case x_IBM856:
            case x_IBM874:
            case x_IBM875:
            case x_IBM921:
            case x_IBM922:
            case x_IBM930:
            case x_IBM933:
            case x_IBM935:
            case x_IBM937:
            case x_IBM939:
            case x_IBM942:
            case x_IBM942C:
            case x_IBM943:
            case x_IBM943C:
            case x_IBM948:
            case x_IBM949:
            case x_IBM949C:
            case x_IBM950:
            case x_IBM964:
            case x_IBM970:
            case x_ISCII91:
            case x_ISO2022_CN_CNS:
            case x_ISO2022_CN_GB:
            case x_iso_8859_11:
            case x_JISAutoDetect:
            case x_Johab:
            case x_MacArabic:
            case x_MacCentralEurope:
            case x_MacCroatian:
            case x_MacCyrillic:
            case x_MacDingbat:
            case x_MacGreek:
            case x_MacHebrew:
            case x_MacIceland:
            case x_MacRoman:
            case x_MacRomania:
            case x_MacSymbol:
            case x_MacThai:
            case x_MacTurkish:
            case x_MacUkraine:
            case x_MS950_HKSCS:
            case x_mswin_936:
            case x_PCK:
            case x_windows_874:
            case x_windows_949:
            case x_windows_950:
                return lang;
            default:
                
                break;
                    
            }
            
        }
        
        
        return Encode.UTF_8;
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
            ParmVars.plog.printException(e);
            return null;
        }
        return prr;
    }




    class NewMenu implements IContextMenuFactory, InterfaceLangOKNG
    {

        IHttpRequestResponse[] messageInfo = null;
        IHttpRequestResponse[] repeaterbaseline = null;
        int toolflg = -1;

        @Override
        public List<JMenuItem> createMenuItems(IContextMenuInvocation icmi) {

            ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();
            
            toolflg = icmi.getToolFlag();
            messageInfo = icmi.getSelectedMessages();
            
            JMenuItem item = new JMenuItem("■Custom■");
            JMenuItem itemmacro = new JMenuItem("■SendTo MacroBuilder■");
            
            if(pmt.isBaseLineMode()){
                boolean hasMenu = false;
                String menutitle = "■Update Baseline■";
                String tooltip = "Update Baseline: You can tamper tracking tokens which is such like CSRF tokens.";
                switch(toolflg){
                    case IBurpExtenderCallbacks.TOOL_REPEATER:
                        
                        repeaterbaseline = messageInfo;
                        hasMenu = true;
                        break;
                    case IBurpExtenderCallbacks.TOOL_SCANNER:
                    case IBurpExtenderCallbacks.TOOL_INTRUDER:
                        menutitle = "■Clear Baseline■";
                        tooltip = "Clear Baseline: You should select this menu when  you used repeater  in baseline mode.";
                        if(pmt.getToolBaseline()!=null){
                            hasMenu = true;
                        }
                    default:
                        repeaterbaseline = null;
                        break;
                }
                if(hasMenu){
                    repeatermodeitem = new JMenuItem(menutitle);
                    repeatermodeitem.setToolTipText(tooltip);

                    repeatermodeitem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {

                            String toolname = getToolname(toolflg);
                            ParmVars.plog.debuglog(0, "updatebaselineAction:" + toolname + ":" + (repeaterbaseline==null?"NULL":"NONULL"));
                            UpdateToolBaseline(repeaterbaseline);
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
                ParmVars.plog.printException(e);
            }
        }

        
        public void menuAddRequestsClicked( IHttpRequestResponse[] messageInfo)
        {
            if(pmt!=null){
                if(pmt.getRlistCount()<=0){
                    Encode lang = analyzeCharset(messageInfo);
                    new LangSelectDialog(null, this, lang, false).setVisible(true);
                }else{
                    LangOK();
                }
            }
        }
        
        public void UpdateToolBaseline( IHttpRequestResponse[] messageInfo){
            if(pmt!=null){
                if(messageInfo!=null&& messageInfo.length>0){
                    IHttpRequestResponse minfo = messageInfo[0];

                    pmt.setToolBaseLine(convertMessageInfoToPRR(minfo));
                }else{
                    pmt.setToolBaseLine(null);
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
        pmt = new ParmGenMacroTrace();
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
