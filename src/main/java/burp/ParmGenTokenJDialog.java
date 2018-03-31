/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author daike
 */
public class ParmGenTokenJDialog extends javax.swing.JDialog {
    List<AppParmsIni> newparms = null;
    ParmGenMacroTrace pmt = null;
    
    /**
     * Creates new form ParmGenTokenJDialog
     */
    public ParmGenTokenJDialog(java.awt.Frame parent, boolean modal, List<AppParmsIni> _newparms, ParmGenMacroTrace _pmt) {
        super(parent, modal);
        initComponents();
        newparms = _newparms;
        pmt = _pmt;
        //newparmsからtokenの一覧を取得し、テーブルに表示。
        HashMap<ParmGenTokenKey, ParmGenTokenValue> map = new HashMap<ParmGenTokenKey, ParmGenTokenValue>();
        ParmGenTokenKey tkey = null;
        ParmGenTokenValue tval = null;
        ParmGenToken token = null;
        for(AppParmsIni pini: newparms){
            if(pini.parmlist!=null){
                for(AppValue ap: pini.parmlist){
                    tkey = new ParmGenTokenKey(ap.tokentype, ap.token, ap.resRegexPos);
                    tval = new ParmGenTokenValue(ap.resURL, ap.resFetchedValue, ap.isEnabled());
                    map.put(tkey, tval);
                }
            }
        }
        
        //テーブルクリア
        DefaultTableModel model = (DefaultTableModel)TrackTkTable.getModel();
        while(model.getRowCount()>0){//table row全削除
            model.removeRow(0);
        }
        
        for(Map.Entry<ParmGenTokenKey, ParmGenTokenValue> entry : map.entrySet()) {
            tkey = entry.getKey();
            tval = entry.getValue();
            boolean enabled = tval.getBoolean();
            Object[] rec = new Object[] {tval.getBoolean(),"", tkey.GetTokenType().name(), Integer.toString(tkey.GetFcnt()),tkey.GetName(),tval.getValue()};
            model.addRow(rec);
        }

        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TrackTkTable = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        OK = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        TrackTkTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "track", "種類", "TokenType", "出現順序", "name", "value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        TrackTkTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(TrackTkTable);

        OK.setText("OK");
        OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("追跡するパラメータを指定してください。");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                        .addGap(1, 1, 1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(OK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 298, Short.MAX_VALUE)
                        .addComponent(jButton2))
                    .addComponent(jSeparator1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OK)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel)TrackTkTable.getModel();
        HashMap<ParmGenTokenKey, ParmGenTokenValue> map = new HashMap<ParmGenTokenKey, ParmGenTokenValue>();

        for (int i = 0;i < model.getRowCount();i++ ){
            boolean enabled = Boolean.parseBoolean(model.getValueAt(i, 0).toString());
            String resAttrName = (String)model.getValueAt(i, 1);//input type　Attribute
            String tokentypename = (String)model.getValueAt(i, 2);//tag name eg. input
            int fcnt = Integer.parseInt((String)model.getValueAt(i, 3));//出現位置　0start
            String tokenname = (String)model.getValueAt(i, 4);//token name
            String resFetchedValue = (String)model.getValueAt(i, 5);//token value
            ParmGenTokenKey tkey = new ParmGenTokenKey(AppValue.parseTokenTypeName(tokentypename),tokenname, fcnt);
            ParmGenTokenValue tval = new ParmGenTokenValue(resAttrName, resFetchedValue, enabled);
            map.put(tkey, tval);
        }
        
        
        if (newparms != null && !newparms.isEmpty()&& pmt!=null) {
            List<AppParmsIni> alist = newparms;
            ListIterator<AppParmsIni> appit = alist.listIterator();
            while(appit.hasNext()){
                AppParmsIni aini = appit.next();
                List<AppValue> apvlist = aini.parmlist;
                ListIterator<AppValue> apvit = null;
                if(apvlist!=null){
                    apvit = apvlist.listIterator();
                    while(apvit.hasNext()){
                        AppValue ap = apvit.next();
                        ParmGenTokenKey _tkey = new ParmGenTokenKey(ap.tokentype, ap.token, ap.resRegexPos);
                        if(map.containsKey(_tkey)){
                            ParmGenTokenValue _tval = map.get(_tkey);
                            ap.setEnabled(_tval.getBoolean());
                            apvit.set(ap);
                        }
                    }
                }
                appit.set(aini);
            }
            
            //トークン一覧のチェックON/OFFをnewparmsに反映
            ParmGenCSV csv = new ParmGenCSV(newparms, pmt);
            csv.jsonsave();
        }
        dispose();
    }//GEN-LAST:event_OKActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OK;
    private javax.swing.JTable TrackTkTable;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}