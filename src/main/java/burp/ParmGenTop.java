/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author tms783
 */

public class ParmGenTop extends javax.swing.JFrame {

    public ParmGenCSV csv;//CSVファイル
    DefaultTableModel model = null;
    int current_row;
    int default_rowheight;
    boolean ParmGenNew_Modified = false;
    ParmGenMacroTrace pmt;

    
    /**
     * Creates new form ParmGenTop
     */
    public ParmGenTop(ParmGenMacroTrace _pmt, ParmGenCSV _csv) {
        pmt = _pmt;
        ParmGenNew_Modified = false;
        csv = _csv;//リファレンスを格納
        initComponents();
        LogfileOnOff.setSelected(ParmVars.plog.isLogfileOn());
        TableColumnModel tcm = ParamTopList.getColumnModel();
        tcm.getColumn(5).setCellRenderer(new LineWrapRenderer());
        ParamTopList.setColumnModel(tcm);
        default_rowheight = ParamTopList.getRowHeight();
        model = (DefaultTableModel)ParamTopList.getModel();
        
        
        cleartables();
        LANGUAGE.setSelectedItem(csv.getLang());
        AppParmsIni pini;
        csv.rewindAppParmsIni();
        int ri = 0;
        while((pini=csv.getNextAppParmsIni())!=null){
            model.addRow(new Object[] {pini.pause, pini.url, pini.getIniValDsp(), pini.getLenDsp(), pini.getTypeValDsp(),pini.getAppValuesDsp(),pini.getCurrentValue()});
            ParamTopList.setRowHeight(ri++, default_rowheight * pini.getAppValuesLineCnt());
        }
        current_row = 0;
        ParmGen pg = new ParmGen(pmt);
        if(pg.ProxyInScope){
            ProxyScope.setSelected(true);
        }else{
            ProxyScope.setSelected(false);
        }
        if(pg.IntruderInScope){
            IntruderScope.setSelected(true);
        }else{
            IntruderScope.setSelected(false);
        }
        if(pg.RepeaterInScope){
            RepeaterScope.setSelected(true);
        }else{
            RepeaterScope.setSelected(false);
        }
        if(pg.ScannerInScope){
            ScannerScope.setSelected(true);
        }else{
            ScannerScope.setSelected(false);
        }
        ParamTopList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                JTable target = (JTable)e.getSource();
                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();
                // do some action if appropriate column
                Object cell =  target.getValueAt(row, column);
                String v = "";
                if ( cell instanceof String){
                    v = Integer.toString(row) + Integer.toString(column) + (String)cell;
                }else if(cell instanceof Boolean){
                    v = Integer.toString(row) + Integer.toString(column) + Boolean.toString((boolean)cell);
                    // column == 0 は、pauseボタン。
                    if ( column == 0){
                        ParmGen pglocal = new ParmGen(pmt);
                        AppParmsIni pini = pglocal.parmcsv.get(row);
                        if(pini!=null){
                            pini.setPause((boolean)cell);
                        }
                    }
                }
                ParmVars.plog.debuglog(0, v);
              }
            }
          });
    }
    
    public void refreshRowDisp(boolean reloadcsv){
        if(reloadcsv){
            csv.reloadParmGen(pmt);
            if(ParmGen.ProxyInScope){
                ProxyScope.setSelected(true);
            }else{
                ProxyScope.setSelected(false);
            }
            if(ParmGen.IntruderInScope){
                IntruderScope.setSelected(true);
            }else{
                IntruderScope.setSelected(false);
            }
            if(ParmGen.RepeaterInScope){
                RepeaterScope.setSelected(true);
            }else{
                RepeaterScope.setSelected(false);
            }
            if(ParmGen.ScannerInScope){
                ScannerScope.setSelected(true);
            }else{
                ScannerScope.setSelected(false);
            }
            csv.setLang(ParmVars.enc);
        }
        LANGUAGE.setSelectedItem(csv.getLang());
        AppParmsIni pini;
        csv.rewindAppParmsIni();
        int ri = 0;
        cleartables();
        while((pini=csv.getNextAppParmsIni())!=null){
            model.addRow(new Object[] {pini.pause, pini.url, pini.getIniValDsp(), pini.getLenDsp(), pini.getTypeValDsp(),pini.getAppValuesDsp(),pini.getCurrentValue()});
            ParamTopList.setRowHeight(ri++, default_rowheight * pini.getAppValuesLineCnt());
        }
    }
    
    public void updateRowDisp(AppParmsIni pini){

        if(pini != null){//新規
            csv.add(pini);
            //model.addRow(new Object[] {false, pini.url, pini.getIniValDsp(), pini.getLenDsp(), pini.getTypeValDsp(),pini.getAppValuesDsp(), pini.getCurrentValue()});
        }/****else{//更新
            pini = csv.getAppParmsIni(current_row);
            model.insertRow(current_row, new Object[] {false, pini.url, pini.getIniValDsp(), pini.getLenDsp(), pini.getTypeValDsp(),pini.getAppValuesDsp(), pini.getCurrentValue()});
            ParamTopList.setRowHeight(current_row, default_rowheight * pini.getAppValuesLineCnt());
            int rowcnt = model.getRowCount();
            if ( rowcnt > current_row+1){
                model.removeRow(current_row+1);
            }
        }***/
        refreshRowDisp(false);

    }
    public void Modified(){
        ParmGenNew_Modified = true;
    }
/*
 *  テーブル全削除
 */
    private void cleartables(){
        int rcnt = model.getRowCount();
        for(int i = 0; i< rcnt; i++){
            model.removeRow(0);//0行目を削除。
        }
    }
    
    public int getRowSize(){
        if(model==null)return 0;
        return model.getRowCount();
    }
    //
    // 
    //　
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        LANGUAGE = new javax.swing.JComboBox();
        Add = new javax.swing.JButton();
        Mod = new javax.swing.JButton();
        Del = new javax.swing.JButton();
        Save = new javax.swing.JButton();
        Cancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        ParamTopList = new javax.swing.JTable();
        LogfileOnOff = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        ProxyScope = new javax.swing.JCheckBox();
        IntruderScope = new javax.swing.JCheckBox();
        ScannerScope = new javax.swing.JCheckBox();
        RepeaterScope = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        Load = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ParmGenトップ画面");

        jLabel1.setText("文字コード");

        LANGUAGE.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SJIS", "EUC-JP", "UTF-8", "ISO8859-1" }));
        LANGUAGE.setToolTipText("文字コード");
        LANGUAGE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LANGUAGEActionPerformed(evt);
            }
        });

        Add.setText("新規");
        Add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddActionPerformed(evt);
            }
        });

        Mod.setText("修正");
        Mod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModActionPerformed(evt);
            }
        });

        Del.setText("削除");
        Del.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelActionPerformed(evt);
            }
        });

        Save.setText("保存");
        Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
            }
        });

        Cancel.setText("閉じる");
        Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelActionPerformed(evt);
            }
        });

        ParamTopList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, ".*/input.php.*", null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "停止", "置換対象パス", "初期値/CSVファイル", "桁数", "機能", "パターンリスト", "現在値"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        ParamTopList.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        ParamTopList.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(ParamTopList);
        if (ParamTopList.getColumnModel().getColumnCount() > 0) {
            ParamTopList.getColumnModel().getColumn(0).setPreferredWidth(35);
            ParamTopList.getColumnModel().getColumn(1).setPreferredWidth(200);
            ParamTopList.getColumnModel().getColumn(2).setPreferredWidth(150);
            ParamTopList.getColumnModel().getColumn(3).setPreferredWidth(40);
            ParamTopList.getColumnModel().getColumn(4).setPreferredWidth(60);
            ParamTopList.getColumnModel().getColumn(5).setPreferredWidth(200);
        }

        LogfileOnOff.setText("ログファイル出力");
        LogfileOnOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogfileOnOffActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("対象機能"));

        ProxyScope.setSelected(true);
        ProxyScope.setText("Proxy");
        ProxyScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProxyScopeActionPerformed(evt);
            }
        });

        IntruderScope.setSelected(true);
        IntruderScope.setText("intruder");
        IntruderScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IntruderScopeActionPerformed(evt);
            }
        });

        ScannerScope.setSelected(true);
        ScannerScope.setText("scanner");
        ScannerScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScannerScopeActionPerformed(evt);
            }
        });

        RepeaterScope.setSelected(true);
        RepeaterScope.setText("repeater");
        RepeaterScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RepeaterScopeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ProxyScope)
                .addGap(18, 18, 18)
                .addComponent(IntruderScope)
                .addGap(32, 32, 32)
                .addComponent(RepeaterScope)
                .addGap(34, 34, 34)
                .addComponent(ScannerScope)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(ProxyScope)
                .addComponent(IntruderScope)
                .addComponent(ScannerScope)
                .addComponent(RepeaterScope))
        );

        jLabel2.setText("注意：処理実行前に、この画面は保存または閉じるボタンで閉じてください。");

        Load.setText("ロード");
        Load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(Add)
                        .addGap(66, 66, 66)
                        .addComponent(Mod)
                        .addGap(66, 66, 66)
                        .addComponent(Del)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Load)
                        .addGap(33, 33, 33)
                        .addComponent(Save)
                        .addGap(46, 46, 46)
                        .addComponent(Cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(LANGUAGE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                        .addComponent(LogfileOnOff))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LANGUAGE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(LogfileOnOff)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Add)
                    .addComponent(Mod)
                    .addComponent(Del)
                    .addComponent(Save)
                    .addComponent(Cancel)
                    .addComponent(Load))
                .addGap(28, 28, 28))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            ParmVars.parmfile = name;
             csv.save();
             csv.jsonsave();
            //reset ParmGen
            ParmGen pgen = new ParmGen(pmt);

            pgen.reset();
            pgen.disposeTop();
        } 
       
    }//GEN-LAST:event_SaveActionPerformed

    private void ModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModActionPerformed
        // TODO add your handling code here:
        //テーブル内の選択したrowに対応するrecをここで渡す。
        int[] rowsSelected = ParamTopList.getSelectedRows();
        AppParmsIni rec = null;
        if ( rowsSelected.length> 0){
            current_row = rowsSelected[0];
            rec = csv.getAppParmsIni(current_row);
        }
        new ParmGenNew(this, rec).setVisible(true);
    }//GEN-LAST:event_ModActionPerformed

    private void AddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddActionPerformed
        // TODO add your handling code here:
        new ParmGenNew(this, null).setVisible(true);
    }//GEN-LAST:event_AddActionPerformed

    private void CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelActionPerformed
        // TODO add your handling code here:
        ParmGen pgen = new ParmGen(pmt);
        if(ParmGenNew_Modified){
            pgen.reset();
        }
        pgen.disposeTop();
    }//GEN-LAST:event_CancelActionPerformed

    private void LANGUAGEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LANGUAGEActionPerformed
        // TODO add your handling code here:
        int idx = LANGUAGE.getSelectedIndex();
        String str = (String)LANGUAGE.getSelectedItem();  //Object型で返された値をString型にｷｬｽﾄ
        ParmVars.enc = str;
        csv.setLang(str);
    }//GEN-LAST:event_LANGUAGEActionPerformed

    private void DelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelActionPerformed
        // TODO add your handling code here:
        int[] rowsSelected = ParamTopList.getSelectedRows();
        AppParmsIni rec = null;
        if ( rowsSelected.length> 0){
            current_row = rowsSelected[0];
            csv.del(current_row);
            model.removeRow(current_row);
            if(current_row>0){
                current_row--;
            }
        }
    }//GEN-LAST:event_DelActionPerformed

    private void LogfileOnOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogfileOnOffActionPerformed
        // TODO add your handling code here:
        ParmVars.plog.LogfileOn(LogfileOnOff.isSelected());
    }//GEN-LAST:event_LogfileOnOffActionPerformed

    private void ProxyScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProxyScopeActionPerformed
        // TODO add your handling code here:
        ParmGen pg = new ParmGen(pmt);
        if (ProxyScope.isSelected()){
            pg.ProxyInScope = true;
        }else{
            pg.ProxyInScope = false;
        }
    }//GEN-LAST:event_ProxyScopeActionPerformed

    private void IntruderScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IntruderScopeActionPerformed
        // TODO add your handling code here:
        ParmGen pg = new ParmGen(pmt);
        if (IntruderScope.isSelected()){
            pg.IntruderInScope = true;
        }else{
            pg.IntruderInScope = false;
        }
    }//GEN-LAST:event_IntruderScopeActionPerformed

    private void RepeaterScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RepeaterScopeActionPerformed
        // TODO add your handling code here:
        ParmGen pg = new ParmGen(pmt);
        if (RepeaterScope.isSelected()){
            pg.RepeaterInScope = true;
        }else{
            pg.RepeaterInScope = false;
        }
    }//GEN-LAST:event_RepeaterScopeActionPerformed

    private void ScannerScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScannerScopeActionPerformed
        // TODO add your handling code here:
        ParmGen pg = new ParmGen(pmt);
        if (ScannerScope.isSelected()){
            pg.ScannerInScope = true;
        }else{
            pg.ScannerInScope = false;
        }
    }//GEN-LAST:event_ScannerScopeActionPerformed

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
            ParmVars.parmfile = name;
            ParmGen pgen = new ParmGen(pmt);
            pgen.reset();//再読み込み
            refreshRowDisp(true);//表示更新
        } 
        
    }//GEN-LAST:event_LoadActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Add;
    private javax.swing.JButton Cancel;
    private javax.swing.JButton Del;
    private javax.swing.JCheckBox IntruderScope;
    private javax.swing.JComboBox LANGUAGE;
    private javax.swing.JButton Load;
    private javax.swing.JToggleButton LogfileOnOff;
    private javax.swing.JButton Mod;
    private javax.swing.JTable ParamTopList;
    private javax.swing.JCheckBox ProxyScope;
    private javax.swing.JCheckBox RepeaterScope;
    private javax.swing.JButton Save;
    private javax.swing.JCheckBox ScannerScope;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
