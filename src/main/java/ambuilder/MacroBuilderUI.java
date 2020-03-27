/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import ambuilder.MacroBuilderUIRequestListRender;
import ambuilder.PRequest;
import ambuilder.PRequestResponse;
import ambuilder.ParmFileFilter;
import ambuilder.AppParmsIni;
import ambuilder.AppValue;
import ambuilder.ParmGen;
import ambuilder.ParmGenArrayList;
import ambuilder.ParmGenGSONDecoder;
import ambuilder.ParmGenJSONSave;
import ambuilder.ParmGenMacroTrace;
import ambuilder.ParmGenParseURL;
import ambuilder.ParmGenParser;
import ambuilder.ParmGenRegex;
import ambuilder.ParmGenRequestToken;
import ambuilder.ParmGenRequestTokenKey;
import ambuilder.ParmGenResToken;
import ambuilder.ParmGenTextDoc;
import ambuilder.ParmGenToken;
import ambuilder.ParmGenTokenJDialog;
import ambuilder.ParmGenTop;
import ambuilder.ParmGenTrackingToken;
import ambuilder.ParmGenUtil;
import ambuilder.ParmVars;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

/**
 *
 * @author daike
 */
public class MacroBuilderUI  extends javax.swing.JPanel implements  InterfaceParmGenRegexSaveCancelAction {

    private static org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getLogger(MacroBuilderUI.class);
    
    private static final ResourceBundle bundle = ResourceBundle.getBundle("burp/Bundle");

    ArrayList<PRequestResponse> rlist = null;
    ParmGenMacroTrace pmt = null;

    DefaultListModel<String> RequestListModel = null;
    int OriginalEditTarget = -1;
    boolean EditTargetIsSSL = false;
    int EditTargetPort = 0;
    Encode EditPageEnc = Encode.ISO_8859_1;
    static final int REQUEST_DISPMAXSIZ = 1000000;//1MB
    static final int RESPONSE_DISPMAXSIZ = 1000000;//1MB
    
    private int selected_request_idx = -1;
    private boolean isLoadedMacroRequestContents = false;
    private boolean isLoadedMacroResponseContents = false;
    private boolean isLoadedMacroCommentContents = false;

    /**
     * Creates new form MacroBuilderUI
     */
    public MacroBuilderUI(ParmGenMacroTrace _pmt) {
        pmt = _pmt;
        initComponents();
        RequestList.setCellRenderer(new MacroBuilderUIRequestListRender(this));
        RequestListModel = new DefaultListModel();
        RequestListModel.clear();
        RequestList.setModel(RequestListModel);

        pmt.setUI(this);


        pmt.setMBreplaceCookie(true);
        pmt.setMBCookieFromJar(MBCookieFromJar.isSelected());
        pmt.setMBsettokencache(MBsettokenfromcache.isSelected());
        pmt.setMBFinalResponse(FinalResponse.isSelected());
        pmt.setMBResetToOriginal(MBResetToOriginal.isSelected());
        pmt.setMBmonitorofprocessing(MBmonitorofprocessing.isSelected());
        
        pmt.setMBreplaceTrackingParam(isReplaceMode());
        
        // waittimer setting.
        jCheckBox2ActionPerformed(null);
        

    }

    boolean isReplaceMode(){
        boolean mode = true;
        String selected = (String)TrackMode.getSelectedItem();
        if(selected!=null){
            if(selected.equals("replace")){
                return true;
            }else{
                return false;
            }
        }
        return true;
        
    }
    
    ParmGenMacroTrace getParmGenMacroTrace() {
        return pmt;
    }

    void clear() {
        selected_request_idx = -1;
        isLoadedMacroRequestContents = false;
        isLoadedMacroResponseContents = false;
        isLoadedMacroCommentContents = false;
        //JListをクリアするには、modelのremove & jListへModelセットが必須。
        RequestListModel.removeAllElements();
        RequestList.setModel(RequestListModel);
        MacroRequest.setText("");
        MacroResponse.setText("");
        MacroComments.setText("");
        rlist = null;
        if (pmt != null) {
            pmt.clear();
        }
    }

    void addNewRequests(ArrayList<PRequestResponse> _rlist) {
        DefaultListModel lmodel = new DefaultListModel();
        AppParmsIni pini;
        if (_rlist != null) {
            if(rlist==null){
                rlist = _rlist;
            }else{
                rlist.addAll(_rlist);
            }
            if (pmt != null) {
                pmt.setRecords(_rlist);
                pmt.ParseResponse();
            }
            Iterator<PRequestResponse> it = rlist.iterator();
            int ii = 0;
            while (it.hasNext()) {

                //model.addRow(new Object[] {false, pini.url, pini.getIniValDsp(), pini.getLenDsp(), pini.getTypeValDsp(),pini.getAppValuesDsp(),pini.getCurrentValue()});
                PRequestResponse pqr = it.next();
                String url = pqr.request.url;
                lmodel.addElement((Object) (String.format("%03d",ii++) + '|' + url));
            }
            RequestList.setModel(lmodel);
        }

    }

    void updateCurrentReqRes() {
        int cpos = pmt.getCurrentRequestPos();
        if (rlist != null) {
            PRequestResponse pqr = rlist.get(cpos);
            if(pmt.isMBmonitorofprocessing()){
                String reqstr = pqr.request.getMessage();
                int len = ParmVars.displaylength > reqstr.length()?reqstr.length():ParmVars.displaylength;
                Document  reqdoc = ParmGenUtil.createDoc(reqstr.substring(0,len));
                if(reqdoc!=null){
                    MacroRequest.setDocument(reqdoc);
                }

                String resstr = pqr.response.getMessage();
                len = ParmVars.displaylength > resstr.length() ? resstr.length():ParmVars.displaylength;
                Document resdoc = ParmGenUtil.createDoc(resstr.substring(0,len));
                if(resdoc!=null){
                    MacroResponse.setDocument(resdoc);
                }
            }

            MacroComments.setText(pqr.getComments());
        }
    }

    public void Redraw() {
        //ListModel cmodel = RequestList.getModel();
        //RequestList.setModel(cmodel);
        logger4j.debug("RequestList.repaint called.");
        RequestList.repaint();
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        SendTo = new javax.swing.JMenu();
        Repeater = new javax.swing.JMenuItem();
        Scanner = new javax.swing.JMenuItem();
        Intruder = new javax.swing.JMenuItem();
        targetRequest = new javax.swing.JMenuItem();
        disableRequest = new javax.swing.JMenuItem();
        enableRequest = new javax.swing.JMenuItem();
        RequestEdit = new javax.swing.JPopupMenu();
        showRequest = new javax.swing.JMenuItem();
        edit = new javax.swing.JMenuItem();
        ResponseShow = new javax.swing.JPopupMenu();
        show = new javax.swing.JMenuItem();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        RequestList = new javax.swing.JList();
        paramlog = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        MacroRequest = new javax.swing.JEditorPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        MacroResponse = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        MacroComments = new javax.swing.JTextArea();
        ParamTracking = new javax.swing.JButton();
        custom = new javax.swing.JButton();
        ClearMacro = new javax.swing.JButton();
        Load = new javax.swing.JButton();
        Save = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        MBCookieFromJar = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        MBsettokenfromcache = new javax.swing.JCheckBox();
        TrackMode = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jCheckBox2 = new javax.swing.JCheckBox();
        waitsec = new javax.swing.JTextField();
        MBResetToOriginal = new javax.swing.JCheckBox();
        MBfromStepNo = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        FinalResponse = new javax.swing.JCheckBox();
        MBtoStepNo = new javax.swing.JCheckBox();
        MBmonitorofprocessing = new javax.swing.JCheckBox();

        SendTo.setText(bundle.getString("MacroBuilderUI.SENDTO.text")); // NOI18N

        Repeater.setText(bundle.getString("MacroBuilderUI.REPEATER.text")); // NOI18N
        Repeater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RepeaterActionPerformed(evt);
            }
        });
        SendTo.add(Repeater);

        Scanner.setText(bundle.getString("MacroBuilderUI.SCANNER.text")); // NOI18N
        Scanner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScannerActionPerformed(evt);
            }
        });
        SendTo.add(Scanner);

        Intruder.setText(bundle.getString("MacroBuilderUI.INTRUDER.text")); // NOI18N
        Intruder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IntruderActionPerformed(evt);
            }
        });
        SendTo.add(Intruder);

        jPopupMenu1.add(SendTo);

        targetRequest.setText(bundle.getString("MacroBuilderUI.TARGETREQUEST.text")); // NOI18N
        targetRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetRequestActionPerformed(evt);
            }
        });
        jPopupMenu1.add(targetRequest);

        disableRequest.setText(bundle.getString("MacroBuilderUI.DISABLEREQUEST.text")); // NOI18N
        disableRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableRequestActionPerformed(evt);
            }
        });
        jPopupMenu1.add(disableRequest);

        enableRequest.setText(bundle.getString("MacroBuilderUI.ENABLEREQUEST.text")); // NOI18N
        enableRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableRequestActionPerformed(evt);
            }
        });
        jPopupMenu1.add(enableRequest);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("burp/Bundle"); // NOI18N
        showRequest.setText(bundle.getString("MacroBuilderUI.ShowRequest.text")); // NOI18N
        showRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRequestActionPerformed(evt);
            }
        });
        RequestEdit.add(showRequest);

        edit.setText(bundle.getString("MacroBuilderUI.REQUESTEDIT.text")); // NOI18N
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editActionPerformed(evt);
            }
        });
        RequestEdit.add(edit);

        show.setText(bundle.getString("MacroBuilderUI.RESPONSESHOW.text")); // NOI18N
        show.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showActionPerformed(evt);
            }
        });
        ResponseShow.add(show);

        setPreferredSize(new java.awt.Dimension(873, 850));

        jPanel4.setPreferredSize(new java.awt.Dimension(871, 1400));

        jScrollPane1.setAutoscrolls(true);

        RequestList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        RequestList.setAutoscrolls(false);
        RequestList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                RequestListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                RequestListMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RequestListMouseClicked(evt);
            }
        });
        RequestList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                RequestListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(RequestList);

        paramlog.setPreferredSize(new java.awt.Dimension(847, 300));
        paramlog.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                paramlogStateChanged(evt);
            }
        });

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        MacroRequest.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MacroRequestMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MacroRequestMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MacroRequestMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(MacroRequest);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 835, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        paramlog.addTab(bundle.getString("MacroBuilderUI.リクエスト.text"), jPanel1); // NOI18N

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        MacroResponse.setColumns(20);
        MacroResponse.setLineWrap(true);
        MacroResponse.setRows(5);
        MacroResponse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MacroResponseMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MacroResponseMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MacroResponseMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(MacroResponse);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 835, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
        );

        paramlog.addTab(bundle.getString("MacroBuilderUI.レスポンス.text"), jPanel2); // NOI18N

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        MacroComments.setColumns(20);
        MacroComments.setLineWrap(true);
        MacroComments.setRows(5);
        jScrollPane5.setViewportView(MacroComments);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 835, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
        );

        paramlog.addTab(bundle.getString("MacroBuilderUI.追跡.text"), jPanel3); // NOI18N

        ParamTracking.setText(bundle.getString("MacroBuilderUI.追跡.text")); // NOI18N
        ParamTracking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParamTrackingActionPerformed(evt);
            }
        });

        custom.setText(bundle.getString("MacroBuilderUI.CUSTOM.text")); // NOI18N
        custom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customActionPerformed(evt);
            }
        });

        ClearMacro.setText(bundle.getString("MacroBuilderUI.クリア.text")); // NOI18N
        ClearMacro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearMacroActionPerformed(evt);
            }
        });

        Load.setText(bundle.getString("MacroBuilderUI.LOAD.text")); // NOI18N
        Load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadActionPerformed(evt);
            }
        });

        Save.setText(bundle.getString("MacroBuilderUI.SAVE.text")); // NOI18N
        Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
            }
        });

        jButton3.setText(bundle.getString("MacroBuilderUI.NOP.text")); // NOI18N
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle.getString("MacroBuilderUI.マクロリクエスト一覧.text")); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getBundle("burp/Bundle").getString("MacroBuilderUI.TakeOverCache.text"))); // NOI18N

        MBCookieFromJar.setSelected(true);
        MBCookieFromJar.setText(bundle.getString("MacroBuilderUI.TakeOverCacheCheckBox.text")); // NOI18N
        MBCookieFromJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBCookieFromJarActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("MacroBuilderUI.TakeOverInfoLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(MBCookieFromJar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel4)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(MBCookieFromJar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MacroBuilderUI.TrackingParamBorder.text"))); // NOI18N

        MBsettokenfromcache.setSelected(true);
        MBsettokenfromcache.setText(bundle.getString("MacroBuilderUI.開始時TOKENをキャッシュから引き継ぐ.text")); // NOI18N
        MBsettokenfromcache.setEnabled(false);
        MBsettokenfromcache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBsettokenfromcacheActionPerformed(evt);
            }
        });

        TrackMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "replace", "baseline" }));
        TrackMode.setToolTipText("<HTML>\n[baseline] mode:<BR>\nthe token parameter value is changed only the baseline part , so which you can tamper by burp tools.<BR>\n<BR>\nyou can add test pattern in parameter value, e.g. '||'<BR>\nex.<BR>\ntoken=8B12C123'||' ===> token=A912D8VC'||'<BR><BR>\nNote:  In baseline mode,if you encounter problem which fails tracking tokens, you should select \"■update baseline■\" menu in BurpTool's popup menu.<BR>\n<BR>\n[replace] mode:<BR>\nthe token parameter value is completely replaced with tracking value, so which you cannot tamper by burp tools.<BR>\nex.<BR>\ntoken=8B12C123'||' ===> token=A912D8VC<BR>");
        TrackMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TrackModeActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("<HTML>\n<DL>\n<BR>\n<LI>baseline(experimental): you can test(tamper) tracking tokens with scanner/intruder which has baseline request.\n<LI>replace(default): Tracking tokens is completely replaced with extracted value from previous page's response.\n<BR><BR>* For Details , refer ?button in the \"baseline/replace mode\" section. \n<DL>\n</HTML>");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/question.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MBsettokenfromcache, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(TrackMode, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(MBsettokenfromcache)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(131, 131, 131))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(TrackMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        jCheckBox2.setText("WaitTimer(sec)");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        waitsec.setText("0");

        MBResetToOriginal.setSelected(true);
        MBResetToOriginal.setText(bundle.getString("MacroBuilderUI.オリジナルにリセット.text")); // NOI18N
        MBResetToOriginal.setEnabled(false);
        MBResetToOriginal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBResetToOriginalActionPerformed(evt);
            }
        });

        MBfromStepNo.setSelected(true);
        MBfromStepNo.setText(bundle.getString("MacroBuilderUI.追跡FROM設定.text")); // NOI18N
        MBfromStepNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBfromStepNoActionPerformed(evt);
            }
        });

        jLabel1.setText("Other Options(Usually, you do not need chage options below.)");

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Pass back to the invoking tool"));

        FinalResponse.setSelected(true);
        FinalResponse.setText(bundle.getString("MacroBuilderUI.FINAL RESPONSE.text")); // NOI18N
        FinalResponse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FinalResponseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(FinalResponse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(FinalResponse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        MBtoStepNo.setSelected(true);
        MBtoStepNo.setText(bundle.getString("MacroBuilderUI.MBtoStepNo.text")); // NOI18N

        MBmonitorofprocessing.setText(bundle.getString("MacroBuilderUI.MBmonitorofprocessing.text")); // NOI18N
        MBmonitorofprocessing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBmonitorofprocessingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addGap(12, 12, 12)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(custom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(ClearMacro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(ParamTracking, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(Load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(Save, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(26, 26, 26))
                    .addComponent(jSeparator1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                        .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(18, 18, 18)
                                        .addComponent(waitsec, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(45, 45, 45))
                                    .addComponent(MBResetToOriginal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(MBfromStepNo, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(MBtoStepNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(MBmonitorofprocessing, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 826, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(paramlog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(ParamTracking)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(custom)
                        .addGap(18, 18, 18)
                        .addComponent(ClearMacro)
                        .addGap(10, 10, 10)
                        .addComponent(Load)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Save)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paramlog, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox2)
                    .addComponent(waitsec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MBmonitorofprocessing))
                .addGap(18, 18, 18)
                .addComponent(MBResetToOriginal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MBfromStepNo)
                    .addComponent(MBtoStepNo))
                .addContainerGap(230, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 870, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1346, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void customActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customActionPerformed
        // TODO add your handling code here:
        List<String> poslist = RequestList.getSelectedValuesList();
        if(rlist!=null){
            ArrayList <PRequestResponse> messages = new ArrayList<PRequestResponse>() ;
            for(String s: poslist){
                String[] values = s.split("[|]", 0);
                if(values.length>0){
                    int i = Integer.parseInt(values[0]);
                    PRequestResponse pqr = rlist.get(i);
                    pqr.setMacroPos(i);
                    messages.add(pqr);
                }
            }
            ParmGen pgen = new ParmGen(pmt);
            if(pgen.twin==null){
                    pgen.twin = new ParmGenTop(pmt, new ParmGenJSONSave(pmt,
                        messages)
                        );
            }
            
            pgen.twin.VisibleWhenJSONSaved(this);
            
        }
    }//GEN-LAST:event_customActionPerformed

    private void MacroRequestLoadContents(){
                
        if(selected_request_idx!=-1&&!isLoadedMacroRequestContents){
            PRequestResponse pqr = rlist.get(selected_request_idx);

            ParmGenTextDoc reqdoc = new ParmGenTextDoc(MacroRequest);

            String reqmess = "";
            if(pqr.request.getBodyContentLength() < REQUEST_DISPMAXSIZ){
                reqmess = pqr.request.getMessage();
            }else{//Content-Length < REQUEST_DISPMAXSIZ then no display body contents..
                reqmess = pqr.request.getHeaderOnly();
                reqmess = reqmess + "\r\n" + ".........omitted displaying body content......";
            }

            reqdoc.setText(reqmess);
            isLoadedMacroRequestContents = true;
        }
    }
    
    private void MacroResponseLoadContents(){
                
        if(selected_request_idx!=-1&&!isLoadedMacroResponseContents){
            PRequestResponse pqr = rlist.get(selected_request_idx);
            String res_contentMimeType = pqr.response.getContentMimeType();// Content-Type's Mimetype: ex. "text/html"

            // Content-Type/subtype matched excludeMimeType or Content-Length < RESPONSE_DISPMAXSIZ then no display body contents..
            String resmess = "";

            if((!ParmVars.isMimeTypeExcluded(res_contentMimeType)) && (pqr.response.getBodyContentLength() <  RESPONSE_DISPMAXSIZ)){
                resmess = pqr.response.getMessage();
            }else{
                resmess = pqr.response.getHeaderOnly();
                resmess = resmess + "\r\n" + ".........omitted displaying body content......";
            }
            ParmGenTextDoc resdoc = new ParmGenTextDoc(MacroResponse);
            resdoc.setText(resmess);
            isLoadedMacroResponseContents = true;
        }
    }
    
    private void MacroCommentLoadContents(){

        if(selected_request_idx!=-1&&!isLoadedMacroCommentContents){
            PRequestResponse pqr = rlist.get(selected_request_idx);
            MacroComments.setText(pqr.getComments());
            isLoadedMacroCommentContents = true;
        }
    }
    
    private void paramlogTabbedPaneSelectedContentsLoad(){
        int selIndex = paramlog.getSelectedIndex();//tabbedpanes selectedidx 0start..
        switch(selIndex){
            case 0:
                MacroRequestLoadContents();
                break;
            case 1:
                MacroResponseLoadContents();
                break;
            case 2:
                MacroCommentLoadContents();
            default:
                MacroRequestLoadContents();
                break;
        }
    }
    
    private void RequestListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_RequestListValueChanged
        // TODO add your handling code here:
        
        // below magical coding needs ,,,
        if (evt.getValueIsAdjusting()) {
            // The user is still manipulating the selection.
            return;
        }
        
        logger4j.debug("RequestListValueChanged Start...");
        int pos = RequestList.getSelectedIndex();
        if (pos != -1) {

            if (rlist != null && rlist.size() > pos) {
                //
                if(selected_request_idx!=pos){
                    selected_request_idx = pos;
                    isLoadedMacroCommentContents = false;
                    isLoadedMacroRequestContents = false;
                    isLoadedMacroResponseContents = false;

                    paramlogTabbedPaneSelectedContentsLoad();
                }
                

            }
        }
        logger4j.debug("RequestListValueChanged done");
    }//GEN-LAST:event_RequestListValueChanged

    private void MBCookieFromJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBCookieFromJarActionPerformed
        // TODO add your handling code here:
        pmt.setMBCookieFromJar(MBCookieFromJar.isSelected());
        boolean bflg = MBCookieFromJar.isSelected();
        MBsettokenfromcache.setSelected(bflg);
        pmt.setMBsettokencache(MBsettokenfromcache.isSelected());
    }//GEN-LAST:event_MBCookieFromJarActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
        if(jCheckBox2.isSelected()){
            pmt.setWaitTimer(waitsec.getText());
        }else{
            pmt.setWaitTimer("0");
        }
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void FinalResponseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FinalResponseActionPerformed
        // TODO add your handling code here:
        pmt.setMBFinalResponse(FinalResponse.isSelected());
    }//GEN-LAST:event_FinalResponseActionPerformed

    private void MBResetToOriginalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBResetToOriginalActionPerformed
        // TODO add your handling code here:
        pmt.setMBResetToOriginal(MBResetToOriginal.isSelected());
    }//GEN-LAST:event_MBResetToOriginalActionPerformed

    private void RequestListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RequestListMousePressed
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_RequestListMousePressed

    private void disableRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableRequestActionPerformed
        // TODO add your handling code here:
        int pos = RequestList.getSelectedIndex();
        if (pos != -1) {
            pmt.DisableRequest(pos);
        }
        Redraw();
    }//GEN-LAST:event_disableRequestActionPerformed

    private void enableRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableRequestActionPerformed
        // TODO add your handling code here:
        int pos = RequestList.getSelectedIndex();
        if (pos != -1) {
            pmt.EnableRequest(pos);
        }
        Redraw();
    }//GEN-LAST:event_enableRequestActionPerformed

    private void RequestListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RequestListMouseClicked
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_RequestListMouseClicked

    private void RequestListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RequestListMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_RequestListMouseReleased

    private void targetRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetRequestActionPerformed
        // TODO add your handling code here:
        int pos = RequestList.getSelectedIndex();
        if (pos != -1) {
            pmt.setCurrentRequest(pos);
        }
        Redraw();
    }//GEN-LAST:event_targetRequestActionPerformed

    private void ParamTrackingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ParamTrackingActionPerformed
        // TODO add your handling code here:
        //fileChooser起動
    	File cfile = new File(ParmVars.parmfile);
        String dirname = cfile.getParent();
        JFileChooser jfc = new JFileChooser(dirname) {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    String m = String.format(
                            "<html>%s already exists.<br>Do you want to replace it?",
                            f.getAbsolutePath());
                    int rv = JOptionPane.showConfirmDialog(
                            this, m, "Save As", JOptionPane.YES_NO_OPTION);
                    if (rv != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                super.approveSelection();
            }
        };
        ParmFileFilter pFilter = new ParmFileFilter();
        jfc.setFileFilter(pFilter);
        ArrayList<PRequestResponse> orglist = pmt.getOriginalrlist();
        if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION && orglist!=null) {

            //code to handle choosed file here.
            File file = jfc.getSelectedFile();
            String name = file.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
            if(!pFilter.accept(file)){//拡張子無しの場合は付与
                name += ".json";
            }
            ParmVars.parmfile = name;
            //エンコードの設定
            //ParmVars.encエンコードの決定
            //先頭ページのレスポンスのcharsetを取得
            PRequestResponse toppage = orglist.get(0);
            String tcharset = toppage.response.getCharset();
            //ParmVars.enc = Encode.getEnum(tcharset);

            String tknames[] = {//予約語 reserved token names
                "PHPSESSID",
                "JSESSIONID",
                "SESID",
                "TOKEN",
                "_CSRF_TOKEN",
                "authenticity_token",
                "NONCE",
                "access_id",
                "fid",
                "ethna_csrf",
                "uniqid",
                "oauth"
            };

            //token追跡自動設定。。
            //ArrayList<ParmGenToken> tracktokenlist = new ArrayList<ParmGenToken>();
            ArrayList<ParmGenResToken> urltokens = new ArrayList<ParmGenResToken>();
            Pattern patternw32 = ParmGenUtil.Pattern_compile("\\w{32}");

            List<AppParmsIni> newparms = new ArrayList<AppParmsIni>();//生成するパラメータ
            PRequestResponse respqrs = null;
            //int row = 0;
            int pos = 0;

            for (PRequestResponse pqrs : orglist) {
                HashMap<ParmGenTrackingToken, String> addedtokens = new HashMap<ParmGenTrackingToken, String>();
                for(ListIterator<ParmGenResToken> it = urltokens.listIterator(urltokens.size());it.hasPrevious();){//urltokens: extracted tokenlist from Response. 
                    //for loop order: fromStepno in descending order(hasPrevious)
                
                    //リクエストにtracktokenlistのトークンが含まれる場合のみ
                    ParmGenResToken restoken = it.previous();
                    int fromStepNo = restoken.fromStepNo;
                    ArrayList<ParmGenTrackingToken> requesttokenlist = new ArrayList<ParmGenTrackingToken>();
                    
                    for(int phase = 0 ; phase<2; phase++){//phase 0: request's token name & value matched,then add to request token list
                        // phase 1: request's token name matched. then add to request token list.
                        for (ParmGenToken tkn : restoken.tracktokenlist) {
                            String token = tkn.getTokenKey().getName();
                            String value = tkn.getTokenValue().getValue();
                            ParmGenGSONDecoder reqjdecoder = new ParmGenGSONDecoder(pqrs.request.getBody());

                            ArrayList<ParmGenToken> reqjtklist = reqjdecoder.parseJSON2Token();

                            ParmGenRequestToken _QToken = null;
                            ParmGenToken _RToken = null;
                            for(ParmGenToken reqtkn : reqjtklist){
                                if((reqtkn.getTokenKey().getName().equals(token)&& reqtkn.getTokenValue().getValue().equals(value))||(phase==1 && reqtkn.getTokenKey().getName().equals(token))){// same name && value
                                    //We found json tracking parameter in request.  
                                    _RToken = tkn;
                                    _QToken = new ParmGenRequestToken(reqtkn);
                                    
                                    ParmGenTrackingToken tracktoken = new ParmGenTrackingToken(_QToken, _RToken, null);
                                    if(!addedtokens.containsKey(tracktoken)){
                                        requesttokenlist.add(tracktoken);
                                        addedtokens.put(tracktoken, "");
                                    }
                                }
                            }



                            ParmGenRequestToken query_token = pqrs.request.getRequestQueryToken(token);
                            ParmGenRequestToken body_token = pqrs.request.getRequestBodyToken(token);
                            ParmVars.plog.debuglog(0, "phase:" + phase +" token[" + token + "] value[" + value + "]");
                            //phase==0: token name & value matched
                            //phase==1: token name matched only. we don't care value.
                            if (pqrs.request.hasQueryParam(token, value) || pqrs.request.hasBodyParam(token, value)
                                    || (phase==1 && (pqrs.request.hasQueryParamName(token) || pqrs.request.hasBodyParamName(token)))) {

                                //add a token to  Query / Body Request parameter. 
                                switch(tkn.getTokenKey().GetTokenType()){
                                case ACTION:
                                case HREF:

                                    ParmGenParseURL _psrcurl = new ParmGenParseURL(tkn.getTokenValue().getURL());
                                    ParmGenParseURL _pdesturl = new ParmGenParseURL(pqrs.request.getURL());
                                    String srcurl = _psrcurl.getPath();
                                    String desturl = _pdesturl.getPath();
                                    ParmVars.plog.debuglog(0, "srcurl|desturl:[" + srcurl + "]|[" + desturl + "]");
                                    if(desturl.indexOf(srcurl)!=-1){// ACTION SRC/HREF attribute's path == destination request path
                                        _RToken = tkn;
                                        if(query_token !=null){
                                            //We found same name/value ACTION/HREF's query paramter in request's query parameter.
                                            _QToken = query_token;
                                            ParmGenTrackingToken tracktoken = new ParmGenTrackingToken(_QToken, _RToken, null);
                                            if(!addedtokens.containsKey(tracktoken)){
                                                requesttokenlist.add(tracktoken);
                                                addedtokens.put(tracktoken, "");
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    _RToken = tkn;
                                    if(query_token !=null){
                                        //We found same name/value INPUT TAG(<INPUT type=...>)'s paramter in request's query parameter.
                                        _QToken = query_token;
                                        ParmGenTrackingToken tracktoken = new ParmGenTrackingToken(_QToken, _RToken, null);
                                        if(!addedtokens.containsKey(tracktoken)){
                                            requesttokenlist.add(tracktoken);
                                            addedtokens.put(tracktoken, "");
                                        }
                                    }
                                    if(body_token!=null){
                                        //We found same name/value INPUT TAG(<INPUT type=...>)'s paramter in request's body parameter.
                                        _QToken = body_token;
                                        ParmGenTrackingToken tracktoken = new ParmGenTrackingToken(_QToken, _RToken, null);
                                        if(!addedtokens.containsKey(tracktoken)){
                                            requesttokenlist.add(tracktoken);
                                            addedtokens.put(tracktoken, "");
                                        }
                                    }
                                    break;
                                }
                            }
                            
                            //bearer/cookie header parameter
                            ArrayList<HeaderPattern> hlist = pqrs.request.hasHeaderMatchedValue(value);
                            if(hlist!=null&&hlist.size()>0){
                                for(HeaderPattern hpattern: hlist){
                                    _QToken = hpattern.getQToken();
                                    _RToken = tkn;
                                    ParmGenTrackingToken tracktoken = new ParmGenTrackingToken(_QToken, _RToken, hpattern.getTokenValueRegex());
                                    if(!addedtokens.containsKey(tracktoken)){
                                        requesttokenlist.add(tracktoken);
                                        addedtokens.put(tracktoken, "");
                                    }
                                }
                            }
                            
                        }
                    }

                    if (requesttokenlist.size()>0) {//tracking parameters are generated from requesttokenlist.
                        //パラメータ生成
                        AppParmsIni aparms = new AppParmsIni();//add new record
                        //request URL
                        //String TargetURLRegex = ".*" + pqrs.request.getPath() + ".*";
                        String TargetURLRegex = ".*";//SetTo any 
                        //boolean isformdata = pqrs.request.isFormData();
                        aparms.setUrl(TargetURLRegex);
                        aparms.len = 4;//default
                        aparms.typeval = aparms.T_TRACK;
                        aparms.inival = 0;
                        aparms.maxval = 0;
                        aparms.csvname = "";
                        aparms.pause = false;
                        aparms.parmlist = new ArrayList<AppValue>();
                        if(MBfromStepNo.isSelected()){
                            aparms.setTrackFromStep(fromStepNo);
                        }else{
                            aparms.setTrackFromStep(-1);
                        }
                        
                        if(MBtoStepNo.isSelected()){
                            aparms.setSetToStep(pos);
                        }else{
                            aparms.setSetToStep(ParmVars.TOSTEPANY);
                        }
                        
                        for (ParmGenTrackingToken PGTtkn : requesttokenlist) {
                            AppValue apv = new AppValue();
                            
                            ParmGenRequestToken _QToken = PGTtkn.getRequestToken();
                            ParmGenToken _RToken = PGTtkn.getResponseToken();
                            ParmGenRequestTokenKey.RequestParamType rptype = _QToken.getKey().getRequestParamType();
                            String token = _RToken.getTokenKey().getName();
                            //body/query/header
                            String valtype = "query";
                            
                            switch(rptype){
                                case Query:
                                    break;
                                case Header:
                                    valtype = "header";
                                    break;
                                default:
                                    valtype = "body";
                                    break;
                            }

                            apv.setValPart(valtype);
                            apv.clearNoCount();
                            apv.csvpos = -1;
                            // (?:[&=?]+|^)token=(value)

                            String value = _RToken.getTokenValue().getValue();
                            apv.resFetchedValue = value;
                            int len = value.length();// For Future use. len is currently No Used. len: token value length. May be,we should be specified len into regex's token value length 
                            String paramname = token;
                            if(_QToken!=null){// May be Request Token name(_RToken's Name) != Response Token name(_QToken's name)
                                int rlen = _QToken.getValue().length();
                                if(len<rlen) len = rlen;
                                paramname = _QToken.getKey().getName();
                            }
                            
                            apv.urlencode = true;//www-form-urlencoded default
                            
                            String regex = "(?:[&=?]|^)" + ParmGenUtil.escapeRegexChars(paramname) + "=([^&=\\r\\n ;#]+)";//default regex. It may be necessary to set the embedding token value length.
                            switch(rptype){
                                case Form_data:
                                    regex = "(?:[A-Z].* name=\"" + ParmGenUtil.escapeRegexChars(paramname) + "\".*(?:\\r|\\n|\\r\\n))(?:[A-Z].*(?:\\r|\\n|\\r\\n)){0,}(?:\\r|\\n|\\r\\n)(?:.*?)(.+)";
                                    apv.urlencode = false;
                                    break;
                                case Json:
                                    regex = "\"" + ParmGenUtil.escapeRegexChars(paramname) + "\"(?:[\\t \\r\\n]*):(?:[\\t\\[\\r\\n ]*)\"(.+?)\"(?:[\\t \\]\\r\\n]*)(?:,|})";
                                    List<String> jsonmatchlist = ParmGenUtil.getRegexMatchGroups(regex, pqrs.request.getBody());
                                    boolean jsonmatched = false;
                                    String jsonvalue = _QToken.getValue();
                                    
                                    if(jsonmatchlist!=null&&jsonmatchlist.size()>0){
                                        jsonmatched = true;
                                    }
                                    if(!jsonmatched){// "key": value
                                        regex ="\"" + ParmGenUtil.escapeRegexChars(paramname) + "\"(?:[\\t \\r\\n]*):(?:[\\t\\[\\r\\n ]*)([^,:{}\\\"]+?)(?:[\\t \\]\\r\\n]*)(?:,|})";
                                        jsonmatchlist = ParmGenUtil.getRegexMatchGroups(regex, pqrs.request.getBody());
                                        
                                        if(jsonmatchlist!=null&&jsonmatchlist.size()>0){
                                            jsonmatched = true;
                                        }
                                    }
                                    apv.urlencode = false;
                                    break;
                                case X_www_form_urlencoded:
                                    regex = "(?:[&=?]|^)" + ParmGenUtil.escapeRegexChars(paramname) + "=([^&=]+)";
                                    break;
                                case Header:
                                    regex = PGTtkn.getRegex();
                                    apv.urlencode = false;
                                    break;
                            }
                            
                            
                            
                            String encodedregex = regex;
                            try {
                                encodedregex = URLEncoder.encode(regex, ParmVars.enc.getIANACharset());
                            } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(MacroBuilderUI.class.getName()).log(Level.SEVERE, null, ex);
                               
                            }
                            apv.setURLencodedVal(encodedregex);
                            //apv.setresURL(".*" + restoken.request.getPath() + ".*");
                            apv.setresURL(".*");//TrackFrom any URL
                            apv.setresRegexURLencoded("");
                            int resvalpart = AppValue.V_AUTOTRACKBODY;
                            switch (_RToken.getTokenKey().GetTokenType()) {
                            case LOCATION:
                                resvalpart = AppValue.V_HEADER;
                                break;
                            case XCSRF:
                                break;
                            default:
                                break;

                            }
                            apv.setresPartType(apv.getValPart(resvalpart));
                            apv.resRegexPos = _RToken.getTokenKey().getFcnt();
                            apv.token = token;
                            

                            apv.fromStepNo = -1;

                            apv.toStepNo = ParmVars.TOSTEPANY;
                            apv.tokentype = _RToken.getTokenKey().GetTokenType();
                            apv.setEnabled(_RToken.isEnabled());
                            aparms.addAppValue(apv);
                        }
                        //aparms.setRow(row);
                        //row++;
                        //aparms.crtGenFormat(true);
                        newparms.add(aparms);
                    }

                }
                

                //respqrs = pqrs;
                //レスポンストークン解析
                String body = pqrs.response.getBody();
                
                String res_contentMimeType = pqrs.response.getContentMimeType();// Content-Type's Mimetype: ex. "text/html"
                
                // Content-Type/subtype matched excludeMimeType then skip below codes..
                if(!ParmVars.isMimeTypeExcluded(res_contentMimeType)){
                    //### skip start
                    //レスポンスから追跡パラメータ抽出
                    ParmGenParser pgparser = new ParmGenParser(body);
                    ArrayList<ParmGenToken> bodytklist = pgparser.getNameValues();
                    ParmGenArrayList tklist = new ParmGenArrayList();// tklist: tracking token list
                    ParmGenResToken trackurltoken = new ParmGenResToken();
                    //trackurltoken.request = pqrs.request;
                    trackurltoken.tracktokenlist = new ArrayList<ParmGenToken>();
                    InterfaceCollection<ParmGenToken> ic = pqrs.response.getLocationTokens(tklist);
                    //JSON parse
                    ParmGenGSONDecoder jdecoder = new ParmGenGSONDecoder(body);
                    ArrayList<ParmGenToken> jtklist = jdecoder.parseJSON2Token();

                    //add extracted tokens to tklist
                    tklist.addAll(bodytklist);
                    tklist.addAll(jtklist);

                    for (ParmGenToken token : tklist) {
                        //PHPSESSID, token, SesID, jsessionid
                        String tokenname = token.getTokenKey().getName();
                        boolean namematched = false;
                        for (String tkn : tknames) {//予約語に一致 
                            if (tokenname.equalsIgnoreCase(tkn)) {//完全一致 tokenname  that matched reserved token name
                                namematched = true;
                                break;
                            }
                        }
                        if (!namematched) {//nameはtknamesに一致しない
                            for (String tkn : tknames) {
                                if (tokenname.toUpperCase().indexOf(tkn.toUpperCase()) != -1) {//予約語に部分一致 tokenname that partially matched reserved token name
                                    namematched = true;
                                    break;
                                }
                            }
                        }
                        // value値がToken値だとみられる
                        if (!namematched) {//nameはtknamesに一致しない
                            String tokenvalue = token.getTokenValue().getValue();

                            if (ParmGenUtil.isTokenValue(tokenvalue)) {// token value that looks like tracking token
                                namematched = true;
                            }
                        }
                        token.setEnabled(namematched);//namematched==true: token that looks like tracking token
                        trackurltoken.tracktokenlist.add(token);
                        trackurltoken.fromStepNo = pos;

                    }

                    if(!trackurltoken.tracktokenlist.isEmpty()){
                        urltokens.add(trackurltoken);
                    }
                    //### skip end
                }else{
                    ParmVars.plog.debuglog(0, "automacro:Response analysis skipped stepno:" + pos + " MIMEtype:" + res_contentMimeType);
                }
                
                
                pos++;
            }
            
            ParmVars.plog.debuglog(0, "newparms.size=" + newparms.size());
            new ParmGenTokenJDialog(null, false, newparms, pmt).setVisible(true);
        }
    }//GEN-LAST:event_ParamTrackingActionPerformed

    private void ClearMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearMacroActionPerformed
        // TODO add your handling code here:
        clear();
    }//GEN-LAST:event_ClearMacroActionPerformed

    private void LoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadActionPerformed
        // TODO add your handling code here:
        File cfile = new File(ParmVars.parmfile);
        String dirname = cfile.getParent();
        JFileChooser jfc = new JFileChooser(dirname);
        jfc.setSelectedFile(cfile);
        ParmFileFilter pFilter=new ParmFileFilter();
        jfc.setFileFilter(pFilter);
        if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //code to handle choosed file here.
            File file = jfc.getSelectedFile();
            String name = file.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");

            ParmGen pgen = new ParmGen(pmt);//20200208 なにもしないコンストラクター＞スタティックに置き換える。
            if(pgen.checkAndLoadFile(name)){//20200208 再読み込み -> 明示的なファイルのロード、チェック、チェックOKのみパラメータ更新する。
                //load succeeded..
            }
            
            
        }
        
    }//GEN-LAST:event_LoadActionPerformed

    private void MBsettokenfromcacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBsettokenfromcacheActionPerformed
        // TODO add your handling code here:
        pmt.setMBsettokencache(MBsettokenfromcache.isSelected());
    }//GEN-LAST:event_MBsettokenfromcacheActionPerformed

    private void RepeaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RepeaterActionPerformed
        // TODO add your handling code here:
    	int pos = RequestList.getSelectedIndex();
        if (pos != -1) {
            pmt.setCurrentRequest(pos);
            pmt.sendToRepeater(pos);

        }
        Redraw();
    }//GEN-LAST:event_RepeaterActionPerformed

    private void ScannerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScannerActionPerformed
        // TODO add your handling code here:
    	int pos = RequestList.getSelectedIndex();
        if (pos != -1) {
            pmt.setCurrentRequest(pos);
            pmt.sendToScanner(pos);

        }
        Redraw();
    }//GEN-LAST:event_ScannerActionPerformed

    private void IntruderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IntruderActionPerformed
        // TODO add your handling code here:
    	int pos = RequestList.getSelectedIndex();
        if (pos != -1) {
            pmt.setCurrentRequest(pos);
            pmt.sendToIntruder(pos);

        }
        Redraw();
    }//GEN-LAST:event_IntruderActionPerformed

    private void SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveActionPerformed
        // TODO add your handling code here:
        
        
        File cfile = new File(ParmVars.parmfile);
        String dirname = cfile.getParent();
        JFileChooser jfc = new JFileChooser(dirname);
        jfc.setSelectedFile(cfile);
        ParmFileFilter pFilter=new ParmFileFilter();
        jfc.setFileFilter(pFilter);
        if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            //code to handle choosed file here.
            File file = jfc.getSelectedFile();
            String name = file.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
            if(!pFilter.accept(file)){//拡張子無しの場合は付与
                name += ".json";
            }
            boolean filenamechanged = false;
            if(ParmVars.parmfile==null||!ParmVars.parmfile.equals(name)){
                filenamechanged = true;
            }
            ParmVars.parmfile = name;
             //csv.save();
             ParmGenJSONSave csv = new ParmGenJSONSave(null, pmt);
             csv.GSONsave();
             /*if(filenamechanged){//if filename changed then reload json
                ParmGen pgen = new ParmGen(pmt, null);
                pgen.reset();//再読み込み
             }*/
             
            
        }
        
    }//GEN-LAST:event_SaveActionPerformed

    private void MacroRequestMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MacroRequestMousePressed
        // TODO add your handling code here:
        logger4j.debug("MacroRequestMousePressed...start");
        if (evt.isPopupTrigger()) {
            logger4j.debug("MacroRequestMousePressed PopupTriggered.");
            RequestEdit.show(evt.getComponent(), evt.getX(), evt.getY());
        }
        logger4j.debug("MacroRequestMousePressed...end");
        
    }//GEN-LAST:event_MacroRequestMousePressed

    private void MacroResponseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MacroResponseMousePressed
        // TODO add your handling code here:
        logger4j.debug( "MacroResponseMousePressed...start");
        if (evt.isPopupTrigger()) {
            logger4j.debug("MacroResponseMousePressed PopupTriggered.");
            ResponseShow.show(evt.getComponent(), evt.getX(), evt.getY());
        }
        logger4j.debug("MacroResponseMousePressed...end");
    }//GEN-LAST:event_MacroResponseMousePressed

    private void editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editActionPerformed
        // TODO add your handling code here:
        String reg = "";
        //String orig = MacroRequest.getText();
        
    
        int pos = RequestList.getSelectedIndex();
        if(pos<0)return;
        if(pmt!=null){
            PRequestResponse pqr = pmt.getOriginalRequest(pos);
            if(pqr!=null){
                OriginalEditTarget = pos;
                String reqdata = pqr.request.getMessage();
                EditTargetIsSSL = pqr.request.isSSL();
                EditTargetPort = pqr.request.getPort();
                EditPageEnc = pqr.request.getPageEnc();
                new ParmGenRegex(this, reg,reqdata).setVisible(true);
            }
        }
      
        
    }//GEN-LAST:event_editActionPerformed

    private void showActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showActionPerformed
        // TODO add your handling code here:
        String reg = "";
        String orig = MacroResponse.getText();
        OriginalEditTarget = -1;
        new ParmGenRegex(this,reg,orig).setVisible(true);
    }//GEN-LAST:event_showActionPerformed

    private void MacroRequestMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MacroRequestMouseClicked
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {// popup menu trigger occured.
            logger4j.debug("MacroRequestMouseClicked PopupTriggered.");
            RequestEdit.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_MacroRequestMouseClicked

    private void MacroRequestMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MacroRequestMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {// popup menu trigger occured. 
            logger4j.debug("MacroRequestMouseReleased PopupTriggered.");
            RequestEdit.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_MacroRequestMouseReleased

    private void MacroResponseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MacroResponseMouseClicked
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {// popup menu trigger occured. 
            logger4j.debug("MacroResponseMouseClicked PoupupTriggered.");
            ResponseShow.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_MacroResponseMouseClicked

    private void MacroResponseMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MacroResponseMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {// popup menu trigger occured. 
            logger4j.debug("MacroResponseMouseReleased PopupTriggered.");
            ResponseShow.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_MacroResponseMouseReleased

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void MBmonitorofprocessingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBmonitorofprocessingActionPerformed
        // TODO add your handling code here:
        pmt.setMBmonitorofprocessing(MBmonitorofprocessing.isSelected());
    }//GEN-LAST:event_MBmonitorofprocessingActionPerformed

    private void MBfromStepNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBfromStepNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MBfromStepNoActionPerformed

    private void showRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showRequestActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        String reg = "";
        //String orig = MacroRequest.getText();
        Document docreq = MacroRequest.getDocument();
        int rlen = docreq.getLength();
        try {
            
            OriginalEditTarget = -1;
            String reqdata = docreq.getText(0, rlen);
            
            new ParmGenRegex(this, reg,reqdata).setVisible(true);
        } catch (BadLocationException ex) {
            Logger.getLogger(MacroBuilderUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_showRequestActionPerformed

    private void TrackModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TrackModeActionPerformed
        // TODO add your handling code here:
        pmt.setMBreplaceTrackingParam(isReplaceMode());
    }//GEN-LAST:event_TrackModeActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            // TODO add your handling code here:
            java.awt.Desktop.getDesktop().browse(new URI(java.util.ResourceBundle.getBundle("burp/Bundle").getString("MacroBuilderUI.baselinemode.text")));
        } catch (IOException ex) {
            Logger.getLogger(MacroBuilderUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(MacroBuilderUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
     
    }//GEN-LAST:event_jButton1ActionPerformed

    private void paramlogStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_paramlogStateChanged
        // TODO add your handling code here:
        // jTabbedPane tab select problem fixed. by this eventhandler is defined... what a strange behavior. 
        //int selIndex = paramlog.getSelectedIndex();
	//String t = paramlog.getTitleAt(selIndex);
	//logger4j.info("paramlogStateChanged: title[" + t + "]");
        paramlogTabbedPaneSelectedContentsLoad();
    }//GEN-LAST:event_paramlogStateChanged

    @Override
    public void ParmGenRegexSaveAction(String message) {
        if(pmt!=null&&OriginalEditTarget!=-1){
            PRequest request;
            try {
                PRequestResponse pqr = pmt.getOriginalRequest(OriginalEditTarget);
                PRequest origrequest = pqr.request;
                request = new PRequest(origrequest.getHost(), origrequest.getPort(), origrequest.isSSL(), message.getBytes(EditPageEnc.getIANACharset()),EditPageEnc);
                request.setSSL(EditTargetIsSSL);
                request.setPort(EditTargetPort);
                pmt.updateOriginalRequest(OriginalEditTarget, request);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MacroBuilderUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            OriginalEditTarget = -1;
        }
    }

    @Override
    public void ParmGenRegexCancelAction() {
        OriginalEditTarget = -1;
    }

    @Override
    public String getParmGenRegexSaveBtnText() {
        if(OriginalEditTarget==-1){
            return "Close";
        }
        return "Save";
    }

    @Override
    public String getParmGenRegexCancelBtnText() {
        if(OriginalEditTarget==-1){
            return "Close";
        }
        return "Cancel";
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ClearMacro;
    private javax.swing.JCheckBox FinalResponse;
    private javax.swing.JMenuItem Intruder;
    private javax.swing.JButton Load;
    private javax.swing.JCheckBox MBCookieFromJar;
    private javax.swing.JCheckBox MBResetToOriginal;
    private javax.swing.JCheckBox MBfromStepNo;
    private javax.swing.JCheckBox MBmonitorofprocessing;
    private javax.swing.JCheckBox MBsettokenfromcache;
    private javax.swing.JCheckBox MBtoStepNo;
    private javax.swing.JTextArea MacroComments;
    private javax.swing.JEditorPane MacroRequest;
    private javax.swing.JTextArea MacroResponse;
    private javax.swing.JButton ParamTracking;
    private javax.swing.JMenuItem Repeater;
    private javax.swing.JPopupMenu RequestEdit;
    private javax.swing.JList RequestList;
    private javax.swing.JPopupMenu ResponseShow;
    private javax.swing.JButton Save;
    private javax.swing.JMenuItem Scanner;
    private javax.swing.JMenu SendTo;
    private javax.swing.JComboBox<String> TrackMode;
    private javax.swing.JButton custom;
    private javax.swing.JMenuItem disableRequest;
    private javax.swing.JMenuItem edit;
    private javax.swing.JMenuItem enableRequest;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane paramlog;
    private javax.swing.JMenuItem show;
    private javax.swing.JMenuItem showRequest;
    private javax.swing.JMenuItem targetRequest;
    private javax.swing.JTextField waitsec;
    // End of variables declaration//GEN-END:variables


}