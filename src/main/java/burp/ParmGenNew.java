/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tms783
 */
public class ParmGenNew extends javax.swing.JFrame implements InterfaceRegex, interfaceParmGenWin {
    //下記定数P_XXXは、ModelTabsの各タブの出現順序と一致しなければならない。
    //ModelTabsStateChangedでタブ切り替えた場合に、切り替えたタブの番号ModelTabs.getSelectedIndex()の返値と下記定数は
    //対応している。
    final static int P_NUMBERMODEL = 0;
    final static int P_CSVMODEL = 1;
    final static int P_TRACKMODEL = 2;
    final static int P_TAMPERMODEL = 3;
    final static int P_RANDOMMODEL = 4;//NOP
    
    
    //
    public final static int P_REQUESTTAB = 0;
    public final static int P_RESPONSETAB = 1;
   

    int current_model;

    int current_tablerowidx;
    int current_tablecolidx;
    
    int current_reqrespanel;
    
    
    
    AppParmsIni rec;
    AppParmsIni addrec;

    //起動元ウィンドウ
    private ParmGenTop parentwin;
    
    DefaultTableModel[] ParamTableModels={
        null, null, null, null,null
    };

    /**
     * Creates new form ParmGenNew
     */
    
    
    
    
    public ParmGenNew(ParmGenTop _parentwin, AppParmsIni _rec){
        
        current_tablerowidx = 0;
        
        parentwin = _parentwin;
        
        
        initComponents();
        ParamTableModels[P_NUMBERMODEL] = (DefaultTableModel)nParamTable.getModel();
        ParamTableModels[P_CSVMODEL] = (DefaultTableModel)csvParamTable.getModel();
        ParamTableModels[P_TRACKMODEL] = (DefaultTableModel)trackTable.getModel();
        ParamTableModels[P_TAMPERMODEL] = (DefaultTableModel)tamperTable.getModel();

        addJComboBoxToJTable();
        
        PRequestResponse mess = parentwin.csv.proxy_messages.get(0);
        String _url = mess.request.getURL();
        String _requestmess = mess.request.getMessage();
        
        selected_requestURL.setText(_url);
        RequestArea.setText(_requestmess);
        
        current_model = P_NUMBERMODEL;
        
        if(_rec!=null){
            rec = _rec;
            rec.setCntFileName();
            addrec = null;
            switch(rec.getType()){
                case AppParmsIni.T_NUMBER:
                    current_model = P_NUMBERMODEL;
                    break;
                case AppParmsIni.T_CSV:
                    current_model = P_CSVMODEL;
                    break;
                case AppParmsIni.T_TRACK:
                    current_model =  P_TRACKMODEL;
                    break;
                case AppParmsIni.T_RANDOM:
                    current_model = P_RANDOMMODEL;
                    break;
                case AppParmsIni.T_TAMPER:
                    current_model = P_TAMPERMODEL;
                    break;
            }
            CSVrewind.setSelected(false);
            NumberRewind.setSelected(false);
        }else{
            rec = new AppParmsIni();
            rec.setRowAndCntFile(parentwin.getRowSize());
            addrec = rec;
            CSVrewind.setSelected(true);
            NumberRewind.setSelected(true);
        }
        
        setAppParmsIni();
        
        
        ResponseArea.setToolTipText("<html>※追跡パラメータの登録方法<BR>追跡する値を選択し、追加ボタンを押す。</html>");
        
        ModelTabs.setSelectedIndex(current_model);
        
    }
    
    public void setPatternFileName(String _name){
        AttackPatternFile.setText(_name);
    }
    public int getCurrentModel(){
        return current_model;
    }
    
private void setAppParmsIni(){
        Object[] row;
        switch(current_model){
            case P_NUMBERMODEL:
                numberTargetURL.setText(rec.url);
                NumberLen.setText(Integer.toString(rec.len));
                NumberInit.setText(Integer.toString(rec.inival));
                rec.rewindAppValues();
                while((row=rec.getNextAppValuesRow())!=null){
                    ParamTableModels[P_NUMBERMODEL].addRow(row);
                }
                ResReqTabs.remove(ResPanel);
                break;
            case P_CSVMODEL:
                csvTargetURL.setText(rec.url);
                csvFilePath.setText(rec.frl.getFileName());
                rec.rewindAppValues();
                CSVSkipLine.setText(rec.getCurrentValue());
                while((row=rec.getNextAppValuesRow())!=null){
                    ParamTableModels[P_CSVMODEL].addRow(row);
                }
                break;
            case P_TRACKMODEL:
                trackTargetURL.setText(rec.url);
                rec.rewindAppValues();
                while((row=rec.getNextAppValuesRow())!=null){
                    ParamTableModels[P_TRACKMODEL].addRow(row);
                }
                break;
             case P_TAMPERMODEL:
                tamperTargetURL.setText(rec.url);
                rec.rewindAppValues();
                while((row=rec.getNextAppValuesRow())!=null){
                    ParamTableModels[P_TAMPERMODEL].addRow(row);
                }
                break;
            default:
                break;
        }
        
        current_reqrespanel = P_REQUESTTAB;
    }
    
    private void clearTable(DefaultTableModel model){
        int rcnt = model.getRowCount();
        for(int i = 0 ; i < rcnt; i++){
            model.removeRow(0);
        }
    }
    private void addJComboBoxToJTable(){
        //ComboBoxを設定
        AppValue ap = new AppValue();//static 初期化。
        JComboBox cb = new JComboBox(AppValue.makeTargetRequestParamTypes());
        JComboBox tb = new JComboBox(AppValue.makePayloadPositionNames());
        DefaultCellEditor dce = new DefaultCellEditor(cb);
        DefaultCellEditor tbe = new DefaultCellEditor(tb);
        nParamTable. getColumnModel().getColumn(0).setCellEditor(dce);
        trackTable.getColumnModel().getColumn(0).setCellEditor(dce);
        tamperTable.getColumnModel().getColumn(0).setCellEditor(dce);
        tamperTable.getColumnModel().getColumn(6).setCellEditor(tbe);
        
        //modelの初期化とクリア
        for(int i = 0; i < ParamTableModels.length; i++){
            DefaultTableModel model = ParamTableModels[i];
            if ( model!=null){
                clearTable(model);
            }
        }
        numberTargetURL.setText("");
        NumberInit.setText("");
        NumberLen.setText("");
        NumberRewind.setSelected(false);
        
        
        
    }

    public String getRegex(){
        return getTableRowRegex();
    }
    
    public String getOriginal(){
        if (current_model == P_TRACKMODEL){
            if( current_tablecolidx > 2){
                return getResponseArea();
            }
        }
        return getRequestArea();
    }
    
    public void setRegex(String regex){
        updateTableRowRegex(regex);
    }
    
    public void addParamToSelectedModel(String reqplace, String name, int ni, String value, boolean target_req_isformdata){
        addParam(current_model, reqplace, name, ni, value, target_req_isformdata);
    }
    /*
     *  指定されたメッセージで、カレントのボタンのmessageAreaを更新
     */
    public void updateMessageAreaInSelectedModel(int panelno){
        PRequestResponse rs = ParmGenCSV.selected_messages.get(0);
        if(panelno==-1){
            panelno = current_reqrespanel;
        }
        String TargetURLRegex = ".*" + rs.request.getPath() + ".*";
       
        
        switch(panelno){
            case P_REQUESTTAB:
                ParmVars.session.put(ParmGenSession.K_REQUESTURLREGEX, TargetURLRegex);
                selected_requestURL.setText(rs.request.getURL());
                ParmVars.session.put(ParmGenSession.K_HEADERLENGTH, Integer.toString(rs.request.getHeaderLength()));
                RequestArea.setText(rs.request.getMessage());
                RequestArea.setCaretPosition(0);
                break;
            case P_RESPONSETAB:
                ParmVars.session.put(ParmGenSession.K_RESPONSEURLREGEX, TargetURLRegex);
                ParmVars.session.put(ParmGenSession.K_HEADERLENGTH, Integer.toString(rs.response.getHeaderLength()));
                selected_responseURL.setText(rs.request.getURL());
                ResponseArea.setText(rs.response.getMessage());
                ResponseArea.setCaretPosition(0);
                break;
            default:
                break;
            
        }
    }
    
    private void addParam(int m, String reqplace, String name, int ni, String value, boolean target_req_isformdata){
        DefaultTableModel model = ParamTableModels[m];
        //name=valueにデフォルトの正規表現を生成してセット
        String nval =  (name!=null?("(?:[&=?]+|^)" + name + "="):"") + value;
        String _reqplace = reqplace;
        if ( reqplace.toLowerCase().equals("formdata")){
            nval = "(?:[A-Z].* name=\"" + ParmGenUtil.escapeRegexChars(name) + "\".*(?:\\r|\\n|\\r\\n))(?:[A-Z].*(?:\\r|\\n|\\r\\n)){0,}(?:\\r|\\n|\\r\\n)(?:.*?)" + value + "(?:.*?)(?:\\r|\\n|\\r\\n)";
            _reqplace = "body";
        }
        Object []row = null;
        boolean urlencode = false;
        AppValue ap = new AppValue();
        
        String payloadposition = ParmVars.session.get(ParmGenSession.K_PAYLOADPOSITION);
        if(payloadposition==null){
            payloadposition = ap.getPayloadPositionName(AppValue.I_APPEND);
        }
        
        switch(m){
            case P_NUMBERMODEL:
                row = new Object[]{_reqplace, false, nval, false};
                break;
            case P_CSVMODEL:
                row = new Object[]{_reqplace, false, Integer.parseInt(ParmVars.session.get(ni, ParmGenSession.K_COLUMN)), nval, false};
                break;
            case P_TAMPERMODEL://とりあえず、パラメータレコードを作成し、この時点では一部のデータをセットする。
                //置換位置　   置換する・しない　Value　Name　Attack(未使用)　Advance　Position　URLencode
                // _reqplace  false            nval   name  ""      "0"       "append"        false
                row = new Object[]{_reqplace, false, nval, name, "", "0" , payloadposition , false};
                break;
            case P_TRACKMODEL:
                if(ParmVars.session.get(0, ParmGenSession.K_TOKEN)==null){
                    urlencode = false;
                    if(target_req_isformdata!=true){
                        if(Boolean.parseBoolean(ParmVars.session.get(ParmGenSession.K_URLENCODE))==true){
                            //引き継ぎ先リクエストがformdataではなく、引き継ぎ元リクエストがレスポンスボディ。
                            urlencode = true;
                        }
                    }
                    row = new Object[]{_reqplace, false, nval,
                    ParmVars.session.get(ParmGenSession.K_RESPONSEURLREGEX), 
                    ParmVars.session.get(ParmGenSession.K_RESPONSEREGEX),
                    ParmVars.session.get(ParmGenSession.K_RESPONSEPART),
                    ParmVars.session.get(ParmGenSession.K_RESPONSEPOSITION),
                    ParmVars.session.get(ParmGenSession.K_TOKEN),
                    urlencode,-1,0,ParmVars.session.get(ParmGenSession.K_TOKENTYPE)
                    };
                }else{
                    String _token;
                    if((_token=ParmVars.session.get(ni, ParmGenSession.K_TOKEN))!=null){
                        urlencode = false;
                        if(target_req_isformdata!=true){
                            if(Boolean.parseBoolean(ParmVars.session.get(ni, ParmGenSession.K_URLENCODE))==true){
                                urlencode = true;
                            }
                        }
                        row = new Object[]{_reqplace, false, nval,
                        ParmVars.session.get(ParmGenSession.K_RESPONSEURLREGEX), 
                        ParmVars.session.get(ni, ParmGenSession.K_RESPONSEREGEX),
                        ParmVars.session.get(ni, ParmGenSession.K_RESPONSEPART),
                        ParmVars.session.get(ni, ParmGenSession.K_RESPONSEPOSITION),
                        ParmVars.session.get(ni, ParmGenSession.K_TOKEN),
                        urlencode,-1,0,ParmVars.session.get(ni, ParmGenSession.K_TOKENTYPE)
                        };
                    }
                }
                break;
           
        }
        
        if(row !=null){
            model.addRow(row);
        }
    }
    /*
     * current_modelのtargetURLを更新
     */
    public void updateTargetURL(String targetURL){
        String mname = "";
        switch(current_model){
            case P_NUMBERMODEL:
                mname = "NUMBERMODEL";
                numberTargetURL.setText(targetURL);
                break;
            case P_CSVMODEL:
                mname = "CSVMODEL";
                csvTargetURL.setText(targetURL);
                break;
            case P_TRACKMODEL:
                mname = "TRACKMODEL";
                trackTargetURL.setText(targetURL);
                break;
            case P_RANDOMMODEL:
                mname = "RANDOMMODEL";
                break;
            case P_TAMPERMODEL:
                mname = "TAMPERMODEL";
                tamperTargetURL.setText(targetURL);
                new ParmGenAttackListDialog(this, true, "").setVisible(true);
                break;
            default:
                mname = "UNKNOWNMODEL";
                break;
        }
        //ParmVars.plog.debuglog(0, mname);
    }
    
    /*
     * current_modelのtargetURLを返す
     */
    public String getTargetURL(){
        switch(current_model){
            case P_NUMBERMODEL:
                return numberTargetURL.getText();
            case P_CSVMODEL:
                return csvTargetURL.getText();
            case P_TRACKMODEL:
                return trackTargetURL.getText();
            case P_RANDOMMODEL:
                break;
            case P_TAMPERMODEL:
                return tamperTargetURL.getText();
            default:
                break;
        }
        return "";
    }
    
    private JTable getCurrentTable(){
        JTable current_table = null;
        
        switch(current_model){
            case P_NUMBERMODEL:
                current_table = nParamTable;
            case P_CSVMODEL:
                current_table = csvParamTable;
            case P_TRACKMODEL:
                current_table = trackTable;
            case P_RANDOMMODEL:
                break;
            case P_TAMPERMODEL:
                current_table = tamperTable;
                break;
            default:
                break;
        }
        return current_table;
    }
    
    public void updateTableRowRegex(String regex){
        int pos = current_tablecolidx;
        if(current_model==P_CSVMODEL){
            pos = 3;
        }
        ParamTableModels[current_model].setValueAt(regex, current_tablerowidx, pos);
        
    }
    
    public String getTableRowRegex(){
        int pos = current_tablecolidx;
        if(current_model==P_CSVMODEL){
            pos = 3;
        }
        return (String)ParamTableModels[current_model].getValueAt(current_tablerowidx, pos);
    }
    
    public String getRequestArea(){
        return RequestArea.getText();
    }
    
    public String getResponseArea(){
        return ResponseArea.getText();
    }
    
    ParmGenCSV getCSV(){
        return parentwin.csv;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ModelTabs = new javax.swing.JTabbedPane();
        SeqNumber = new javax.swing.JPanel();
        NumberRegexTest = new javax.swing.JButton();
        nParamDel = new javax.swing.JButton();
        nParamUP = new javax.swing.JButton();
        nParamDOWN = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        nParamTable = new javax.swing.JTable();
        nParamAdd = new javax.swing.JButton();
        numberTargetURL = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        NumberInit = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        NumberLen = new javax.swing.JTextField();
        NumberRewind = new javax.swing.JCheckBox();
        SeqCSV = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        csvFilePath = new javax.swing.JTextField();
        csvParamAdd = new javax.swing.JButton();
        csvParamDel = new javax.swing.JButton();
        csvParamUP = new javax.swing.JButton();
        csvParamDOWN = new javax.swing.JButton();
        csvParamRegexTest = new javax.swing.JButton();
        csvTargetURL = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        csvParamTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        CSVrewind = new javax.swing.JCheckBox();
        CSVSkipLine = new javax.swing.JTextField();
        SeqResponse = new javax.swing.JPanel();
        nParamAdd4 = new javax.swing.JButton();
        nParamDel12 = new javax.swing.JButton();
        nParamDel13 = new javax.swing.JButton();
        nParamDel14 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        trackTable = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        trackTargetURL = new javax.swing.JTextField();
        SeqRandom = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tamperTargetURL = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        tamperTable = new javax.swing.JTable();
        addTamper = new javax.swing.JButton();
        upTamper = new javax.swing.JButton();
        delTamper = new javax.swing.JButton();
        downTamper = new javax.swing.JButton();
        modTamper = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        AttackPatternFile = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        SaveParm = new javax.swing.JButton();
        CancelParm = new javax.swing.JButton();
        RequestSelectBtn = new javax.swing.JButton();
        ResReqTabs = new javax.swing.JTabbedPane();
        ReqPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        RequestArea = new javax.swing.JTextPane();
        selected_requestURL = new javax.swing.JTextField();
        ResPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ResponseArea = new javax.swing.JTextPane();
        selected_responseURL = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ParmGen編集画面");

        ModelTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ModelTabsStateChanged(evt);
            }
        });

        NumberRegexTest.setText("正規表現テスト");
        NumberRegexTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumberRegexTestActionPerformed(evt);
            }
        });

        nParamDel.setText("削除");
        nParamDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamDelActionPerformed(evt);
            }
        });

        nParamUP.setText("▲UP  ");
        nParamUP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamUPActionPerformed(evt);
            }
        });

        nParamDOWN.setText("▼DOWN");
        nParamDOWN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamDOWNActionPerformed(evt);
            }
        });

        nParamTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "置換箇所", "置換しない", "置換パターン", "インクリメント"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        nParamTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        nParamTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(nParamTable);
        if (nParamTable.getColumnModel().getColumnCount() > 0) {
            nParamTable.getColumnModel().getColumn(0).setPreferredWidth(60);
            nParamTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            nParamTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        }

        nParamAdd.setText("追加");
        nParamAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamAddActionPerformed(evt);
            }
        });

        numberTargetURL.setText(".*/input.php.*");
        numberTargetURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberTargetURLActionPerformed(evt);
            }
        });

        jLabel5.setText("置換対象パス");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("カウンタ初期値"));

        jLabel2.setText("初期値");

        NumberInit.setText("1");
        NumberInit.setToolTipText("最大2147483647まで");
        NumberInit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumberInitActionPerformed(evt);
            }
        });

        jLabel3.setText("桁数");

        NumberLen.setText("4");
        NumberLen.setToolTipText("最大10ケタまで");
        NumberLen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumberLenActionPerformed(evt);
            }
        });

        NumberRewind.setSelected(true);
        NumberRewind.setText("カウンタを初期化する");
        NumberRewind.setToolTipText("<HTML>カウンタを初期値で初期化する場合は、チェック");
        NumberRewind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumberRewindActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NumberInit, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NumberLen, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NumberRewind)
                .addContainerGap(115, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(NumberInit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(NumberLen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NumberRewind))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout SeqNumberLayout = new javax.swing.GroupLayout(SeqNumber);
        SeqNumber.setLayout(SeqNumberLayout);
        SeqNumberLayout.setHorizontalGroup(
            SeqNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqNumberLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SeqNumberLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(19, 19, 19)
                        .addComponent(numberTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(SeqNumberLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(SeqNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nParamDel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nParamUP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nParamDOWN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(NumberRegexTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nParamAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57))
        );
        SeqNumberLayout.setVerticalGroup(
            SeqNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SeqNumberLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SeqNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SeqNumberLayout.createSequentialGroup()
                        .addComponent(nParamAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nParamDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nParamUP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nParamDOWN)
                        .addGap(47, 47, 47)
                        .addComponent(NumberRegexTest))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        ModelTabs.addTab("数値", SeqNumber);

        jButton6.setText("CSVファイル選択");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        csvFilePath.setText("C:\\windows\\sample.csv");
        csvFilePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvFilePathActionPerformed(evt);
            }
        });

        csvParamAdd.setText("追加");
        csvParamAdd.setMaximumSize(new java.awt.Dimension(107, 23));
        csvParamAdd.setMinimumSize(new java.awt.Dimension(107, 23));
        csvParamAdd.setPreferredSize(new java.awt.Dimension(107, 23));
        csvParamAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvParamAddActionPerformed(evt);
            }
        });

        csvParamDel.setText("削除");
        csvParamDel.setMaximumSize(new java.awt.Dimension(107, 23));
        csvParamDel.setMinimumSize(new java.awt.Dimension(107, 23));
        csvParamDel.setPreferredSize(new java.awt.Dimension(107, 23));
        csvParamDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvParamDelActionPerformed(evt);
            }
        });

        csvParamUP.setText("▲UP  ");
        csvParamUP.setMaximumSize(new java.awt.Dimension(107, 23));
        csvParamUP.setMinimumSize(new java.awt.Dimension(107, 23));
        csvParamUP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvParamUPActionPerformed(evt);
            }
        });

        csvParamDOWN.setText("▼DOWN");
        csvParamDOWN.setMaximumSize(new java.awt.Dimension(107, 23));
        csvParamDOWN.setMinimumSize(new java.awt.Dimension(107, 23));
        csvParamDOWN.setPreferredSize(new java.awt.Dimension(107, 23));
        csvParamDOWN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvParamDOWNActionPerformed(evt);
            }
        });

        csvParamRegexTest.setText("正規表現テスト");
        csvParamRegexTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvParamRegexTestActionPerformed(evt);
            }
        });

        csvTargetURL.setText(".*/input.php.*");
        csvTargetURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvTargetURLActionPerformed(evt);
            }
        });

        jLabel6.setText("置換対象パス");

        csvParamTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "置換位置", "置換しない", "ＣＳＶカラム", "置換パターン", "インクリメント"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        csvParamTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        csvParamTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(csvParamTable);
        if (csvParamTable.getColumnModel().getColumnCount() > 0) {
            csvParamTable.getColumnModel().getColumn(0).setPreferredWidth(60);
            csvParamTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            csvParamTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        }

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("カウンタ初期値（読み飛ばし行数）"));

        CSVrewind.setSelected(true);
        CSVrewind.setText("カウンタを初期化する");
        CSVrewind.setToolTipText("<HTML>カウンタを初期値で初期化する際は、チェック");
        CSVrewind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSVrewindActionPerformed(evt);
            }
        });

        CSVSkipLine.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(CSVSkipLine, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CSVrewind)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(CSVSkipLine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(CSVrewind))
        );

        javax.swing.GroupLayout SeqCSVLayout = new javax.swing.GroupLayout(SeqCSV);
        SeqCSV.setLayout(SeqCSVLayout);
        SeqCSVLayout.setHorizontalGroup(
            SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqCSVLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SeqCSVLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(31, 31, 31)
                        .addComponent(csvTargetURL))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(SeqCSVLayout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(csvFilePath))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE))
                .addGroup(SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SeqCSVLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(csvParamRegexTest))
                    .addGroup(SeqCSVLayout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(csvParamDOWN, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(csvParamUP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(csvParamDel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(csvParamAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        SeqCSVLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {csvParamAdd, csvParamDOWN, csvParamDel, csvParamRegexTest, csvParamUP});

        SeqCSVLayout.setVerticalGroup(
            SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqCSVLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6)
                    .addComponent(csvFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(csvTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(5, 5, 5)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SeqCSVLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SeqCSVLayout.createSequentialGroup()
                        .addComponent(csvParamAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(csvParamDel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(csvParamUP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(csvParamDOWN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(csvParamRegexTest))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        ModelTabs.addTab("ＣＳＶ", SeqCSV);

        nParamAdd4.setText("追加");
        nParamAdd4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamAdd4ActionPerformed(evt);
            }
        });

        nParamDel12.setText("削除");
        nParamDel12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamDel12ActionPerformed(evt);
            }
        });

        nParamDel13.setText("▲UP  ");
        nParamDel13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamDel13ActionPerformed(evt);
            }
        });

        nParamDel14.setText("▼DOWN");
        nParamDel14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nParamDel14ActionPerformed(evt);
            }
        });

        jButton10.setText("正規表現テスト");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        trackTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "置換箇所", "置換しない", "置換正規表現", "追跡URL", "追跡正規表現", "追跡箇所", "追跡位置", "追跡NAME値", "URLencodeする", "追跡from", "追跡to", "tokentype"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        trackTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        trackTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(trackTable);
        if (trackTable.getColumnModel().getColumnCount() > 0) {
            trackTable.getColumnModel().getColumn(0).setPreferredWidth(60);
            trackTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            trackTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        }

        jLabel9.setText("置換対象パス");

        trackTargetURL.setText(".*/input.php.*");
        trackTargetURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackTargetURLActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SeqResponseLayout = new javax.swing.GroupLayout(SeqResponse);
        SeqResponse.setLayout(SeqResponseLayout);
        SeqResponseLayout.setHorizontalGroup(
            SeqResponseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqResponseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqResponseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SeqResponseLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(trackTargetURL))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SeqResponseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                    .addComponent(nParamDel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nParamDel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nParamDel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nParamAdd4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        SeqResponseLayout.setVerticalGroup(
            SeqResponseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqResponseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqResponseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trackTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(7, 7, 7)
                .addGroup(SeqResponseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SeqResponseLayout.createSequentialGroup()
                        .addComponent(nParamAdd4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nParamDel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nParamDel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nParamDel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );

        ModelTabs.addTab("追跡", SeqResponse);

        jLabel1.setText("対象パス");

        tamperTargetURL.setText("jTextField1");

        tamperTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"post", null, null, "param1", "SQL injection", null, "add", null},
                {"", null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "置換位置", "置換しない", "Value", "Name", "Attack", "Advance", "Position", "ＵＲＬencode"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tamperTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(tamperTable);

        addTamper.setText("追加");
        addTamper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTamperActionPerformed(evt);
            }
        });

        upTamper.setText("▲");
        upTamper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upTamperActionPerformed(evt);
            }
        });

        delTamper.setText("削除");
        delTamper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delTamperActionPerformed(evt);
            }
        });

        downTamper.setText("▼");
        downTamper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downTamperActionPerformed(evt);
            }
        });

        modTamper.setText("編集");
        modTamper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modTamperActionPerformed(evt);
            }
        });

        jLabel4.setText("パターン");

        AttackPatternFile.setText("jTextField1");

        javax.swing.GroupLayout SeqRandomLayout = new javax.swing.GroupLayout(SeqRandom);
        SeqRandom.setLayout(SeqRandomLayout);
        SeqRandomLayout.setHorizontalGroup(
            SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqRandomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SeqRandomLayout.createSequentialGroup()
                        .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5)
                            .addGroup(SeqRandomLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tamperTargetURL)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(addTamper, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(upTamper, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(delTamper, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(downTamper, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(modTamper, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(58, 58, 58))
                    .addGroup(SeqRandomLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(AttackPatternFile)
                        .addGap(164, 164, 164))))
        );
        SeqRandomLayout.setVerticalGroup(
            SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SeqRandomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tamperTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(AttackPatternFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(SeqRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(SeqRandomLayout.createSequentialGroup()
                        .addComponent(addTamper)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modTamper)
                        .addGap(15, 15, 15)
                        .addComponent(delTamper)
                        .addGap(11, 11, 11)
                        .addComponent(upTamper)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(downTamper)))
                .addContainerGap())
        );

        ModelTabs.addTab("Tamper", SeqRandom);

        SaveParm.setText("保存");
        SaveParm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveParmActionPerformed(evt);
            }
        });

        CancelParm.setText("取消");
        CancelParm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelParmActionPerformed(evt);
            }
        });

        RequestSelectBtn.setText("リクエスト/レスポンス選択する");
        RequestSelectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RequestSelectBtnActionPerformed(evt);
            }
        });

        ResReqTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ResReqTabsStateChanged(evt);
            }
        });

        RequestArea.setText("POST /input.php?opt=1\n\nname=123&passwd=secret&tel=456&addr1=789");
        jScrollPane1.setViewportView(RequestArea);

        selected_requestURL.setText("http://www.tms.co.jp");
        selected_requestURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selected_requestURLActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ReqPanelLayout = new javax.swing.GroupLayout(ReqPanel);
        ReqPanel.setLayout(ReqPanelLayout);
        ReqPanelLayout.setHorizontalGroup(
            ReqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReqPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selected_requestURL, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(ReqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ReqPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        ReqPanelLayout.setVerticalGroup(
            ReqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReqPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selected_requestURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(246, Short.MAX_VALUE))
            .addGroup(ReqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ReqPanelLayout.createSequentialGroup()
                    .addGap(47, 47, 47)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        ResReqTabs.addTab("選択リクエスト", ReqPanel);

        ResponseArea.setText("HTTP/1.1 200 OK\nSet-Cookie: Formp=deleted; path=/; expires=Thu, 01-Jan-1970 00:00:01 GMT; secure\nContent-Length: 2969\nDate: Fri, 20 Jan 2012 06:48:25 GMT\nServer: Apache\nExpires: Thu, 19 Nov 1981 08:52:00 GMT\nCache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0\nPragma: no-cache\nKeep-Alive: timeout=3, max=100\nConnection: Keep-Alive\nContent-Type: text/html\n\n<sample>123</sample>\n<value>sfkdlajfklsdjfklas234234</value>");
        jScrollPane2.setViewportView(ResponseArea);

        selected_responseURL.setText("http://www.tms.co.jp");
        selected_responseURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selected_responseURLActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ResPanelLayout = new javax.swing.GroupLayout(ResPanel);
        ResPanel.setLayout(ResPanelLayout);
        ResPanelLayout.setHorizontalGroup(
            ResPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ResPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selected_responseURL, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(ResPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ResPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        ResPanelLayout.setVerticalGroup(
            ResPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ResPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selected_responseURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(246, Short.MAX_VALUE))
            .addGroup(ResPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ResPanelLayout.createSequentialGroup()
                    .addGap(47, 47, 47)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        ResReqTabs.addTab("選択レスポンス", ResPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(ResReqTabs))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(RequestSelectBtn)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(SaveParm)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(CancelParm))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ModelTabs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jSeparator1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(RequestSelectBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ResReqTabs)
                .addGap(18, 18, 18)
                .addComponent(ModelTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SaveParm, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(CancelParm, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selected_requestURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selected_requestURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selected_requestURLActionPerformed

    private void NumberInitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumberInitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NumberInitActionPerformed

    private void csvFilePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvFilePathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_csvFilePathActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        JFileChooser jfc = new JFileChooser();
        if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
            //code to handle choosed file here. 
            File file = jfc.getSelectedFile();
            String name = file.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
            csvFilePath.setText(name);
        } 
    }//GEN-LAST:event_jButton6ActionPerformed

    private void csvParamAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvParamAddActionPerformed
        // TODO add your handling code here:
        //セッションクリア
        ParmVars.session.clear();
        ParmGenCSVLoader csvloader = new ParmGenCSVLoader(this,csvFilePath.getText());
        if(csvloader.readOneLine()){
            csvloader.setVisible(true);
        }else{
             csvloader.dispose();
        }
        
    }//GEN-LAST:event_csvParamAddActionPerformed

    private void CancelParmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelParmActionPerformed
        // TODO add your handling code here:
        // Destroy own JFrame window.
        parentwin.refreshRowDisp(false);
        dispose();
    }//GEN-LAST:event_CancelParmActionPerformed

    private void nParamAdd4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamAdd4ActionPerformed
        // TODO add your handling code here:
        //セッションクリア
        ParmVars.session.clear();
        //new ResponseTracker(this).setVisible(true);
       //new SelectRequest("レスポンス選択", this, new ResponseTracker(this), ParmGenNew.P_RESPONSETAB).setVisible(true);
        new SelectRequest("レスポンス選択", this, new ParmGenAutoTrack(this), ParmGenNew.P_RESPONSETAB).setVisible(true);
    }//GEN-LAST:event_nParamAdd4ActionPerformed

    private void numberTargetURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberTargetURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_numberTargetURLActionPerformed

    private void csvTargetURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvTargetURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_csvTargetURLActionPerformed

    private void trackTargetURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackTargetURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_trackTargetURLActionPerformed

    private void NumberRewindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumberRewindActionPerformed
        // TODO add your handling code here:
        if (NumberRewind.isSelected()){
           
        }
    }//GEN-LAST:event_NumberRewindActionPerformed

    private void RequestSelectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RequestSelectBtnActionPerformed
        // TODO add your handling code here:
        new SelectRequest("リクエスト選択", this, null, -1).setVisible(true);
    }//GEN-LAST:event_RequestSelectBtnActionPerformed

    private void SaveParmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveParmActionPerformed
        // TODO add your handling code here:
        // 保存処理実行。
        switch(current_model){
            case P_NUMBERMODEL:
                rec.setType(AppParmsIni.T_NUMBER_NAME);
                rec.url = numberTargetURL.getText();
                rec.len = ParmGenUtil.parseMaxInt(NumberLen.getText());
                if(rec.len>10){
                    rec.len = 10;
                }else if(rec.len<1){
                    rec.len = 1;
                }
                rec.inival = ParmGenUtil.parseMaxInt(NumberInit.getText());
                if(NumberRewind.isSelected()){
                    rec.updateCurrentValue(rec.inival);
                }
                break;
            case P_CSVMODEL:
                rec.setType(AppParmsIni.T_CSV_NAME);
                rec.url = csvTargetURL.getText();
                rec.frl = new FileReadLine(csvFilePath.getText(), true);
                if(CSVrewind.isSelected()){
                    rec.inival = ParmGenUtil.parseMinInt(CSVSkipLine.getText());
                    rec.updateCurrentValue(rec.inival);
                }
                break;
            case P_TRACKMODEL:
                rec.setType(AppParmsIni.T_TRACK_NAME);
                rec.url = trackTargetURL.getText();
                rec.inival = AppParmsIni.T_TRACK_AVCNT;
                break;
            case P_RANDOMMODEL:
                rec.setType(AppParmsIni.T_RANDOM_NAME);
                break;
            default:
                break;
        }
        
        DefaultTableModel model = ParamTableModels[current_model];
        int rcnt = model.getRowCount();
        rec.clearAppValues();
        for(int i = 0 ; i < rcnt; i++){
            String type = (String)model.getValueAt(i, 0);
            boolean nomodify = Boolean.parseBoolean(model.getValueAt(i, 1).toString());
            String value;AppValue app = null;
            boolean increment;
            switch(current_model){
                case P_NUMBERMODEL:
                    value = (String)model.getValueAt(i, 2);
                    increment = Boolean.parseBoolean(model.getValueAt(i,3).toString());
                    app = new AppValue(type, nomodify, value, increment);
                    break;
                case P_CSVMODEL:
                    int csvpos = Integer.parseInt(model.getValueAt(i, 2).toString());
                    value = (String)model.getValueAt(i, 3);
                    increment = Boolean.parseBoolean(model.getValueAt(i,4).toString());
                    app = new AppValue(type, nomodify, csvpos, value, increment);
                    break;
                case P_TRACKMODEL:
                    value = (String)model.getValueAt(i, 2);
                    String _resURL = (String)model.getValueAt(i, 3);
                    String _resRegex = (String)model.getValueAt(i, 4);
                    String _resPartType = (String)model.getValueAt(i, 5);
                    String _resRegexPos = (String)model.getValueAt(i, 6);
                    String _token = (String)model.getValueAt(i, 7);
                    boolean _trackreq = Boolean.parseBoolean(model.getValueAt(i, 8).toString());
                    int fromStepNo = 0;
                    try{
                        fromStepNo = (int)model.getValueAt(i, 9);
                    }catch(Exception e){
                        //
                        fromStepNo = -1;
                    }
                    int toStepNo = 0;
                    try{
                        toStepNo = (int)model.getValueAt(i, 10);
                    }catch(Exception e){
                        //
                        toStepNo = 0;
                    }
                    int tktype;

                    String tktypename = (String)model.getValueAt(i, 11);
                    

                    app = new AppValue(type, nomodify, value, _resURL, _resRegex, _resPartType, _resRegexPos, _token, _trackreq, fromStepNo, toStepNo,tktypename);
                    break;
                case P_RANDOMMODEL:
                    value = (String)model.getValueAt(i, 2);
                    break;
                default:
                    value = (String)model.getValueAt(i, 2);
                    break;
            }
            
            
           if(app!=null)rec.addAppValue(app);
        }
        Boolean lastEntryNocount = true;
        if(addrec==null){
            //更新
            lastEntryNocount = false;
        }
        rec.crtGenFormat(lastEntryNocount);
        parentwin.updateRowDisp(addrec);
        parentwin.Modified();
        dispose();
        
    }//GEN-LAST:event_SaveParmActionPerformed

    private void nParamAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamAddActionPerformed
        // TODO add your handling code here:
        //セッションクリア
        ParmVars.session.clear();
        new ParmGenAddParms(this, false).setVisible(true);
    }//GEN-LAST:event_nParamAddActionPerformed

    private void NumberRegexTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumberRegexTestActionPerformed
        // TODO add your handling code here
        int[] rowsSelected = nParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablecolidx = 2;
            current_tablerowidx = rowsSelected[0];
            new ParmGenRegex(this).setVisible(true);
        }
        
    }//GEN-LAST:event_NumberRegexTestActionPerformed

    private void nParamDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamDelActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = nParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            ParamTableModels[P_NUMBERMODEL].removeRow(current_tablerowidx);
        }
    }//GEN-LAST:event_nParamDelActionPerformed

    private void ModelTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ModelTabsStateChanged
        // TODO add your handling code here:
        int i = ModelTabs.getSelectedIndex();
        if ( i!=-1){
            current_model = i;
            switch(current_model){
                case P_TRACKMODEL:
                    ResReqTabs.add("レスポンス", ResPanel);
                    ResReqTabs.setSelectedIndex(P_REQUESTTAB);
                    break;
                default:
                    ResReqTabs.remove(ResPanel);
                    break;
            }
        }
    }//GEN-LAST:event_ModelTabsStateChanged

    private void nParamUPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamUPActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = nParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            int to = current_tablerowidx - 1;
            if ( to >= 0){
                ParamTableModels[P_NUMBERMODEL].moveRow(current_tablerowidx, current_tablerowidx, to);
            }
        }
    }//GEN-LAST:event_nParamUPActionPerformed

    private void nParamDOWNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamDOWNActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = nParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            int to = current_tablerowidx + 1;
            int rowcnt = ParamTableModels[P_NUMBERMODEL].getRowCount();
            if ( to < rowcnt){
                ParamTableModels[P_NUMBERMODEL].moveRow(current_tablerowidx, current_tablerowidx, to);
            }
        }
    }//GEN-LAST:event_nParamDOWNActionPerformed

    private void selected_responseURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selected_responseURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selected_responseURLActionPerformed

    private void ResReqTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ResReqTabsStateChanged
        // TODO add your handling code here:
        int i = ResReqTabs.getSelectedIndex();
        if ( i!=-1){
            current_reqrespanel = i;
        }
    }//GEN-LAST:event_ResReqTabsStateChanged

    private void nParamDel12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamDel12ActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = trackTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            ParamTableModels[P_TRACKMODEL].removeRow(current_tablerowidx);
        }
    }//GEN-LAST:event_nParamDel12ActionPerformed

    private void nParamDel13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamDel13ActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = trackTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            int to = current_tablerowidx - 1;
            if ( to >= 0){
                ParamTableModels[P_TRACKMODEL].moveRow(current_tablerowidx, current_tablerowidx, to);
            }
        }
    }//GEN-LAST:event_nParamDel13ActionPerformed

    private void nParamDel14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nParamDel14ActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = trackTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            int to = current_tablerowidx + 1;
            int rowcnt = ParamTableModels[P_TRACKMODEL].getRowCount();
            if ( to < rowcnt){
                ParamTableModels[P_TRACKMODEL].moveRow(current_tablerowidx, current_tablerowidx, to);
            }
        }
    }//GEN-LAST:event_nParamDel14ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = trackTable.getSelectedRows();
        int[] colsSelected = trackTable.getSelectedColumns();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];current_tablecolidx = 2;
            if (colsSelected.length > 0){
                if(colsSelected[0]>2){
                    current_tablecolidx = 4;
                }
            }
            new ParmGenRegex(this).setVisible(true);
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void csvParamDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvParamDelActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = csvParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            ParamTableModels[P_CSVMODEL].removeRow(current_tablerowidx);
        }
    }//GEN-LAST:event_csvParamDelActionPerformed

    private void csvParamUPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvParamUPActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        int[] rowsSelected = csvParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            int to = current_tablerowidx - 1;
            if ( to >= 0){
                ParamTableModels[P_CSVMODEL].moveRow(current_tablerowidx, current_tablerowidx, to);
            }
        }
    }//GEN-LAST:event_csvParamUPActionPerformed

    private void csvParamDOWNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvParamDOWNActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = csvParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablerowidx = rowsSelected[0];
            int to = current_tablerowidx + 1;
            int rowcnt = ParamTableModels[P_CSVMODEL].getRowCount();
            if ( to < rowcnt){
                ParamTableModels[P_CSVMODEL].moveRow(current_tablerowidx, current_tablerowidx, to);
            }
        }
    }//GEN-LAST:event_csvParamDOWNActionPerformed

    private void csvParamRegexTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvParamRegexTestActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = csvParamTable.getSelectedRows();
        if (rowsSelected.length > 0){
            current_tablecolidx = 3;
            current_tablerowidx = rowsSelected[0];
            new ParmGenRegex(this).setVisible(true);
        }
    }//GEN-LAST:event_csvParamRegexTestActionPerformed

    private void CSVrewindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSVrewindActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CSVrewindActionPerformed

    private void NumberLenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumberLenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NumberLenActionPerformed

    private void addTamperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTamperActionPerformed
        // TODO add your handling code here:
        //Tamper追加
        //セッションクリア
        ParmVars.session.clear();
        //new ParmGenAddParms(this, false).setVisible(true);
        new ParmGenTamperOpt(this).setVisible(true);
    }//GEN-LAST:event_addTamperActionPerformed

    private void upTamperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upTamperActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_upTamperActionPerformed

    private void delTamperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTamperActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_delTamperActionPerformed

    private void downTamperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downTamperActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_downTamperActionPerformed

    private void modTamperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modTamperActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modTamperActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ParmGenNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ParmGenNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ParmGenNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ParmGenNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ParmGenNew(null, null).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AttackPatternFile;
    private javax.swing.JTextField CSVSkipLine;
    private javax.swing.JCheckBox CSVrewind;
    private javax.swing.JButton CancelParm;
    private javax.swing.JTabbedPane ModelTabs;
    private javax.swing.JTextField NumberInit;
    private javax.swing.JTextField NumberLen;
    private javax.swing.JButton NumberRegexTest;
    private javax.swing.JCheckBox NumberRewind;
    private javax.swing.JPanel ReqPanel;
    private javax.swing.JTextPane RequestArea;
    private javax.swing.JButton RequestSelectBtn;
    private javax.swing.JPanel ResPanel;
    private javax.swing.JTabbedPane ResReqTabs;
    private javax.swing.JTextPane ResponseArea;
    private javax.swing.JButton SaveParm;
    private javax.swing.JPanel SeqCSV;
    private javax.swing.JPanel SeqNumber;
    private javax.swing.JPanel SeqRandom;
    private javax.swing.JPanel SeqResponse;
    private javax.swing.JButton addTamper;
    private javax.swing.JTextField csvFilePath;
    private javax.swing.JButton csvParamAdd;
    private javax.swing.JButton csvParamDOWN;
    private javax.swing.JButton csvParamDel;
    private javax.swing.JButton csvParamRegexTest;
    private javax.swing.JTable csvParamTable;
    private javax.swing.JButton csvParamUP;
    private javax.swing.JTextField csvTargetURL;
    private javax.swing.JButton delTamper;
    private javax.swing.JButton downTamper;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton modTamper;
    private javax.swing.JButton nParamAdd;
    private javax.swing.JButton nParamAdd4;
    private javax.swing.JButton nParamDOWN;
    private javax.swing.JButton nParamDel;
    private javax.swing.JButton nParamDel12;
    private javax.swing.JButton nParamDel13;
    private javax.swing.JButton nParamDel14;
    private javax.swing.JTable nParamTable;
    private javax.swing.JButton nParamUP;
    private javax.swing.JTextField numberTargetURL;
    private javax.swing.JTextField selected_requestURL;
    private javax.swing.JTextField selected_responseURL;
    private javax.swing.JTable tamperTable;
    private javax.swing.JTextField tamperTargetURL;
    private javax.swing.JTable trackTable;
    private javax.swing.JTextField trackTargetURL;
    private javax.swing.JButton upTamper;
    // End of variables declaration//GEN-END:variables

    @Override
    public void update() {
        //NOP
    }
}
