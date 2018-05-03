/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tms783
 */
public class ParmGenAutoTrack extends javax.swing.JFrame implements InterfaceRegex, interfaceParmGenWin{

    private static final ResourceBundle bundle = ResourceBundle.getBundle("burp/Bundle");
    ParmGenNew parentwin;
    boolean valueexistonly = false;

    /**
     * Creates new form ParmGenAutoTrack
     */
    public ParmGenAutoTrack(ParmGenNew _pwin) {
        parentwin = _pwin;//親ウィンドウ
        initComponents();


    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        TokenTablePane = new javax.swing.JScrollPane();
        TokenTable = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        NextBtn = new javax.swing.JButton();
        CancelBtn = new javax.swing.JButton();
        RegexBtn = new javax.swing.JButton();
        valuefilter = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("ParmGenAutoTrack.title.text")); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("burp/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("ParmGenAutoTrack.jLabel1.text")); // NOI18N

        TokenTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "種類", "tokentype", "出現順序", "name", "value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TokenTable.getTableHeader().setReorderingAllowed(false);
        TokenTablePane.setViewportView(TokenTable);
        if (TokenTable.getColumnModel().getColumnCount() > 0) {
            TokenTable.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("ParmGenAutoTrack.title0.part.text")); // NOI18N
            TokenTable.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("ParmGenAutoTrack.title2.order.text")); // NOI18N
        }

        NextBtn.setText(bundle.getString("ParmGenAutoTrack.NextBtn.text")); // NOI18N
        NextBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextBtnActionPerformed(evt);
            }
        });

        CancelBtn.setText(bundle.getString("ParmGenAutoTrack.CancelBtn.text")); // NOI18N
        CancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelBtnActionPerformed(evt);
            }
        });

        RegexBtn.setText(bundle.getString("ParmGenAutoTrack.RegexBtn.text")); // NOI18N
        RegexBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RegexBtnActionPerformed(evt);
            }
        });

        valuefilter.setText(bundle.getString("ParmGenAutoTrack.valuefilter.text")); // NOI18N
        valuefilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valuefilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(NextBtn)
                                .addGap(50, 50, 50)
                                .addComponent(RegexBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(CancelBtn))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(TokenTablePane))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(valuefilter)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(valuefilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(TokenTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NextBtn)
                    .addComponent(RegexBtn)
                    .addComponent(CancelBtn))
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void NextBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextBtnActionPerformed
        // TODO add your handling code here:
        //選択したパラメータをセッションに格納。
        AppValue ap = new AppValue();
        int[] rowsSelected = TokenTable.getSelectedRows();
        DefaultTableModel model = (DefaultTableModel)TokenTable.getModel();
        for (int i = 0;i < rowsSelected.length;i++ ){
            String respart = (String)model.getValueAt(rowsSelected[i], 0);//種類
            String tktype = (String)model.getValueAt(rowsSelected[i], 1);//token種類
            String num = (String)model.getValueAt(rowsSelected[i], 2);//出現順序
            String name = (String)model.getValueAt(rowsSelected[i], 3);//name
            String value = (String)model.getValueAt(rowsSelected[i], 4);//value
            ParmVars.session.put(i, ParmGenSession.K_RESPONSEREGEX, "");
            ParmVars.session.put(i, ParmGenSession.K_RESPONSEPART, respart);
            ParmVars.session.put(i, ParmGenSession.K_RESPONSEPOSITION, num);
            ParmVars.session.put(i, ParmGenSession.K_TOKEN, name);
            ParmVars.session.put(i, ParmGenSession.K_TOKENTYPE, tktype);

            int parsedrespart = ap.parseValPartType(respart);
            if (parsedrespart==AppValue.V_AUTOTRACKBODY) {
                //引き継ぎ元レスポンスのボディを引き継ぐ場合は,リクエストセット時URLENCODEする。
                ParmVars.session.put(i, ParmGenSession.K_URLENCODE, "true");
            }
        }
        //リクエスト選択へ。
        dispose();
        new SelectRequest(bundle.getString("ParmGenAutoTrack.リクエスト選択.text"), parentwin, new ParmGenAddParms(parentwin, true), ParmGenNew.P_REQUESTTAB).setVisible(true);
    }//GEN-LAST:event_NextBtnActionPerformed

    private void RegexBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegexBtnActionPerformed
        // TODO add your handling code here:
        ResponseTracker rtrack = new ResponseTracker(parentwin);
        rtrack.update();
        dispose();
        rtrack.setVisible(true);
    }//GEN-LAST:event_RegexBtnActionPerformed

    private void CancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelBtnActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_CancelBtnActionPerformed

    private void valuefilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valuefilterActionPerformed
        // value値有のみ表示チェックボックス
        if(valuefilter.isSelected()){
            valueexistonly = true;
        }else{
            valueexistonly = false;
        }
        update();
    }//GEN-LAST:event_valuefilterActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ParmGenAutoTrack.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ParmGenAutoTrack.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ParmGenAutoTrack.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ParmGenAutoTrack.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new ParmGenAutoTrack(null).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelBtn;
    private javax.swing.JButton NextBtn;
    private javax.swing.JButton RegexBtn;
    private javax.swing.JTable TokenTable;
    private javax.swing.JScrollPane TokenTablePane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox valuefilter;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getRegex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getOriginal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRegex(String regex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateMessageAreaInSelectedModel(int panel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update() {
         if ( ParmGenCSV.selected_messages.size()>0){
            DefaultTableModel model = (DefaultTableModel)TokenTable.getModel();
            while(model.getRowCount()>0){//table row全削除
                model.removeRow(0);
            }
            PRequestResponse rs = ParmGenCSV.selected_messages.get(0);
            String body = rs.response.getBody();
            AppValue ap = new AppValue();
            //Locationパラメータ取得
            ParmGenArrayList tklist = new ParmGenArrayList();
            InterfaceCollection<ParmGenToken> ic = rs.response.getLocationTokens(tklist);
            if(ic!=null){
            	for(ParmGenToken tkn : ic){
            		if(tkn!=null){
            		ParmGenTokenKey tkey = tkn.getTokenKey();
                        ParmGenTokenValue tval = tkn.getTokenValue();
                        String name = tkey.GetName();
                        String value = tval.getValue();
                        AppValue.TokenTypeNames _tktype = tkey.GetTokenType();
                        int npos = 0;
                        if(valueexistonly==true&&(value==null||value.isEmpty())){
                            //value値の無いパラメータは対象外
                        }else{
                            model.addRow(new Object[]{ap.getValPart(AppValue.V_HEADER),_tktype.name() ,Integer.toString(npos), name, value});
                        }
            		}

            	}
            }
            //responseパラメータ取得
            ParmGenParser pgser = new ParmGenParser(body);
            HashMap<String,Integer> namepos = new HashMap<String,Integer>();
            ArrayList<ParmGenToken> lst = pgser.getNameValues();
            ParmGenJSONDecoder jdec = new ParmGenJSONDecoder(body);
            ArrayList<ParmGenToken> jlst = jdec.parseJSON2Token();
            lst.addAll(jlst);

            for(Iterator<ParmGenToken> it = lst.iterator();it.hasNext();){
                ParmGenToken tkn = it.next();


                if(tkn!=null){
                    ParmGenTokenKey tkey = tkn.getTokenKey();
                    ParmGenTokenValue tval = tkn.getTokenValue();
                    String name = tkey.GetName();
                    String value = tval.getValue();
                    AppValue.TokenTypeNames _tktype = tkey.GetTokenType();
                    int npos = tkey.GetFcnt();
                    if(valueexistonly==true&&(value==null||value.isEmpty())){
                        //value値の無いパラメータは対象外
                    }else{
                        model.addRow(new Object[]{ap.getValPart(AppValue.V_AUTOTRACKBODY),_tktype.name() ,Integer.toString(npos), name, value});
                    }
                }
            }
            //requestパラメータ取得
            namepos.clear();
            Iterator<String> pit = rs.request.pathparams.iterator();
            int ppos = 1;
            while(pit.hasNext()){
                int npos = 0;
                String name = Integer.toString(ppos);
                if(namepos.containsKey(name)){
                    npos = namepos.get(name);
                    npos++;
                }
                namepos.put(name, npos);                
                model.addRow(new Object[]{ap.getValPart(AppValue.V_REQTRACKPATH), "", Integer.toString(npos),Integer.toString(ppos), pit.next()});
                ppos++;
            }

            namepos.clear();
            Iterator<String[]> it = rs.request.queryparams.iterator();
            int rcnt = 0;
            while(it.hasNext()){
                rcnt++;
                String[] nv = it.next();
                int npos = 0;
                String name = nv[0];
                if(namepos.containsKey(name)){
                    npos = namepos.get(name);
                    npos++;
                }
                namepos.put(name, npos);
                model.addRow(new Object[]{ap.getValPart(AppValue.V_REQTRACKQUERY),"" ,Integer.toString(npos),nv[0], nv[1]});
            }
            Iterator<String[]> itb = rs.request.getBodyParams().iterator();

            namepos.clear();
            while(itb.hasNext()){
                rcnt++;
                String[] nv = itb.next();
                int npos = 0;
                String name = nv[0];
                if(namepos.containsKey(name)){
                    npos = namepos.get(name);
                    npos++;
                }
                namepos.put(name, npos);
                model.addRow(new Object[]{ap.getValPart(AppValue.V_REQTRACKBODY), "",Integer.toString(npos),nv[0], nv[1]});
            }

         }
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
