/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author daike
 */
public class MacroBuilderUI extends javax.swing.JPanel {

    ArrayList <PRequestResponse> rlist = null;
    ParmGenMacroTrace pmt = null;

    DefaultListModel<String> CSRFListModel = null;
    DefaultListModel<String> RequestListModel = null;

    /**
     * Creates new form MacroBuilderUI
     */
    public MacroBuilderUI(ParmGenMacroTrace _pmt) {
        pmt = _pmt;
        initComponents();
        RequestList.setCellRenderer(new MacroBuilderUIRequestListRender(this));
        CSRFListModel = new DefaultListModel();
        CSRFListModel.clear();
        CSRFList.setModel(CSRFListModel);
        RequestListModel = new DefaultListModel();
        RequestListModel.clear();
        RequestList.setModel(RequestListModel);

        pmt.setUI(this);

        pmt.setMBExec(MBExec.isSelected());
        pmt.setMBCookieUpdate(MBCookieUpdate.isSelected());
        pmt.setMBCookieFromJar(MBCookieFromJar.isSelected());
        pmt.setMBFinalResponse(FinalResponse.isSelected());
        pmt.setMBResetToOriginal(MBResetToOriginal.isSelected());
        pmt.setMBdeletesetcookies(MBdeleteSetCookies.isSelected());

    }

    ParmGenMacroTrace getParmGenMacroTrace(){
        return pmt;
    }

    void addNewRequests(ArrayList <PRequestResponse> _rlist){
        DefaultListModel  lmodel = new DefaultListModel();
        AppParmsIni pini;
        if(_rlist!=null){
            rlist = _rlist;
            if(pmt!=null){
                pmt.setRecords(_rlist);
                pmt.ParseResponse();
            }
            Iterator<PRequestResponse> it = _rlist.iterator();
            while(it.hasNext()){

                //model.addRow(new Object[] {false, pini.url, pini.getIniValDsp(), pini.getLenDsp(), pini.getTypeValDsp(),pini.getAppValuesDsp(),pini.getCurrentValue()});
                PRequestResponse pqr = it.next();
                String url = pqr.request.url;
                lmodel.addElement((Object)(' ' +url));
            }
            RequestList.setModel(lmodel);
        }

    }

    void updateCurrentReqRes(){
        int cpos = pmt.getCurrentRequest();
        if(rlist!=null){
            PRequestResponse pqr = rlist.get(cpos);
            MacroRequest.setText(pqr.request.getMessage());
            MacroResponse.setText(pqr.response.getMessage());
            MacroComments.setText(pqr.getComments());
        }
    }







    public void Redraw(){
        //ListModel cmodel = RequestList.getModel();
        //RequestList.setModel(cmodel);
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
        targetRequest = new javax.swing.JMenuItem();
        disableRequest = new javax.swing.JMenuItem();
        enableRequest = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        RequestList = new javax.swing.JList();
        paramlog = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        MacroRequest = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        MacroResponse = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        MacroComments = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        MBExec = new javax.swing.JCheckBox();
        MBCookieUpdate = new javax.swing.JCheckBox();
        MBCookieFromJar = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane4 = new javax.swing.JScrollPane();
        CSRFList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        CSRFParam = new javax.swing.JTextField();
        CSRFupdate = new javax.swing.JButton();
        CSRFdelete = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        waitsec = new javax.swing.JTextField();
        jCheckBox2 = new javax.swing.JCheckBox();
        FinalResponse = new javax.swing.JCheckBox();
        MBResetToOriginal = new javax.swing.JCheckBox();
        MBdeleteSetCookies = new javax.swing.JCheckBox();
        ParamTracking = new javax.swing.JButton();

        targetRequest.setText("targetRequest");
        targetRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetRequestActionPerformed(evt);
            }
        });
        jPopupMenu1.add(targetRequest);

        disableRequest.setText("disableRequest");
        disableRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableRequestActionPerformed(evt);
            }
        });
        jPopupMenu1.add(disableRequest);

        enableRequest.setText("enableRequest");
        enableRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableRequestActionPerformed(evt);
            }
        });
        jPopupMenu1.add(enableRequest);

        jScrollPane1.setAutoscrolls(true);

        RequestList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        RequestList.setAutoscrolls(false);
        RequestList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RequestListMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                RequestListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                RequestListMouseReleased(evt);
            }
        });
        RequestList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                RequestListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(RequestList);

        MacroRequest.setColumns(20);
        MacroRequest.setRows(5);
        jScrollPane2.setViewportView(MacroRequest);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        paramlog.addTab("リクエスト", jPanel1);

        MacroResponse.setColumns(20);
        MacroResponse.setRows(5);
        jScrollPane3.setViewportView(MacroResponse);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        paramlog.addTab("レスポンス", jPanel2);

        MacroComments.setColumns(20);
        MacroComments.setRows(5);
        jScrollPane5.setViewportView(MacroComments);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        paramlog.addTab("追跡", jPanel3);

        jButton2.setText("編集");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("削除");

        MBExec.setText("実行");
        MBExec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBExecActionPerformed(evt);
            }
        });

        MBCookieUpdate.setText("Cookie更新");
        MBCookieUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBCookieUpdateActionPerformed(evt);
            }
        });

        MBCookieFromJar.setText("開始時Cookie.jarから引き継ぐ");
        MBCookieFromJar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBCookieFromJarActionPerformed(evt);
            }
        });

        CSRFList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(CSRFList);

        jLabel1.setText("引き継ぎ（CSRF）パラメータ");

        CSRFParam.setText("jTextField1");

        CSRFupdate.setText("追加／更新");
        CSRFupdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSRFupdateActionPerformed(evt);
            }
        });

        CSRFdelete.setText("削除");
        CSRFdelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSRFdeleteActionPerformed(evt);
            }
        });

        jLabel2.setText("マクロリクエスト一覧");

        waitsec.setText("1");

        jCheckBox2.setText("WaitTimer(sec)");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        FinalResponse.setSelected(true);
        FinalResponse.setText("final response");
        FinalResponse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FinalResponseActionPerformed(evt);
            }
        });

        MBResetToOriginal.setSelected(true);
        MBResetToOriginal.setText("オリジナルにリセット");
        MBResetToOriginal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBResetToOriginalActionPerformed(evt);
            }
        });

        MBdeleteSetCookies.setSelected(true);
        MBdeleteSetCookies.setText("delete setcookies from Request");
        MBdeleteSetCookies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBdeleteSetCookiesActionPerformed(evt);
            }
        });

        ParamTracking.setText("追跡");
        ParamTracking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParamTrackingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ParamTracking, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(jSeparator1)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jCheckBox2)
                            .addGap(18, 18, 18)
                            .addComponent(waitsec, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(52, 52, 52)
                            .addComponent(FinalResponse))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(104, 104, 104)
                                .addComponent(MBdeleteSetCookies)
                                .addGap(68, 68, 68))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(MBExec)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(MBCookieUpdate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(MBCookieFromJar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(MBResetToOriginal))))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jSeparator2)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(CSRFParam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(CSRFupdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(CSRFdelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(paramlog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(MBdeleteSetCookies))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MBExec)
                    .addComponent(MBCookieUpdate)
                    .addComponent(MBCookieFromJar)
                    .addComponent(MBResetToOriginal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(waitsec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2)
                    .addComponent(FinalResponse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)
                        .addGap(9, 9, 9)
                        .addComponent(ParamTracking))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CSRFParam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CSRFupdate)))
                    .addComponent(CSRFdelete))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(paramlog, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_jButton2ActionPerformed

    private void RequestListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_RequestListValueChanged
        // TODO add your handling code here:

        int pos = RequestList.getSelectedIndex();
        if (pos != -1){

            if(rlist!=null&&rlist.size()>pos){
                //

                DefaultListModel  lmodel = new DefaultListModel();
                PRequestResponse pqr = rlist.get(pos);
                MacroRequest.setText(pqr.request.getMessage());
                MacroResponse.setText(pqr.response.getMessage());
                MacroComments.setText(pqr.getComments());


            }
        }

    }//GEN-LAST:event_RequestListValueChanged

    private void MBCookieUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBCookieUpdateActionPerformed
        // TODO add your handling code here:
        pmt.setMBCookieUpdate(MBCookieUpdate.isSelected());
    }//GEN-LAST:event_MBCookieUpdateActionPerformed

    private void MBCookieFromJarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBCookieFromJarActionPerformed
        // TODO add your handling code here:
        pmt.setMBCookieFromJar(MBCookieFromJar.isSelected());
    }//GEN-LAST:event_MBCookieFromJarActionPerformed

    private void MBExecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBExecActionPerformed
        // TODO add your handling code here:
        pmt.setMBExec(MBExec.isSelected());
    }//GEN-LAST:event_MBExecActionPerformed

    private void CSRFdeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSRFdeleteActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        int sidx = CSRFList.getSelectedIndex();
        int maxidx = CSRFListModel.getSize();
        if(sidx>=0&& sidx < maxidx){
            CSRFListModel.remove(sidx);
        }
    }//GEN-LAST:event_CSRFdeleteActionPerformed

    private void CSRFupdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSRFupdateActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        String pattern = CSRFParam.getText();
        int sidx = CSRFList.getSelectedIndex();
        int maxidx = CSRFListModel.getSize();
        if(maxidx<=0){//0件
            sidx = 0;
        }
        CSRFListModel.insertElementAt(pattern, sidx);

        if(sidx+2 < maxidx){
            CSRFListModel.remove(sidx+1);
        }
    }//GEN-LAST:event_CSRFupdateActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
        pmt.setWaitTimer(waitsec.getText());
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
        if(evt.isPopupTrigger()){
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_RequestListMousePressed

    private void disableRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableRequestActionPerformed
        // TODO add your handling code here:
        int pos = RequestList.getSelectedIndex();
        if( pos != -1){
            pmt.DisableRequest(pos);
        }
        Redraw();
    }//GEN-LAST:event_disableRequestActionPerformed

    private void enableRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableRequestActionPerformed
        // TODO add your handling code here:
        int pos = RequestList.getSelectedIndex();
        if( pos != -1){
            pmt.EnableRequest(pos);
        }
        Redraw();
    }//GEN-LAST:event_enableRequestActionPerformed

    private void RequestListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RequestListMouseClicked
        // TODO add your handling code here:
        if(evt.isPopupTrigger()){
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_RequestListMouseClicked

    private void RequestListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RequestListMouseReleased
        // TODO add your handling code here:
        if(evt.isPopupTrigger()){
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_RequestListMouseReleased

    private void targetRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetRequestActionPerformed
        // TODO add your handling code here:
        int pos = RequestList.getSelectedIndex();
        if( pos != -1){
            pmt.setCurrentRequest(pos);
        }
        Redraw();
    }//GEN-LAST:event_targetRequestActionPerformed

    private void MBdeleteSetCookiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MBdeleteSetCookiesActionPerformed
        // TODO add your handling code here:
        pmt.setMBdeletesetcookies(MBdeleteSetCookies.isSelected());
    }//GEN-LAST:event_MBdeleteSetCookiesActionPerformed

    private void ParamTrackingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ParamTrackingActionPerformed
        // TODO add your handling code here:
    	//fileChooser起動
    	JFileChooser jfc = new JFileChooser() {

    		@Override public void approveSelection() {
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
    	ParmFileFilter pFilter=new ParmFileFilter();
        jfc.setFileFilter(pFilter);
    	if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    		//code to handle choosed file here.
    		File file = jfc.getSelectedFile();
    		String name = file.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
        	//エンコードの設定
        	//ParmVars.encエンコードの決定
        	//先頭ページのレスポンスのcharsetを取得
        	PRequestResponse toppage = rlist.get(0);
        	String tcharset = toppage.response.getCharset();
        	ParmVars.enc = Encode.getEnum(tcharset);



        	String tknames[] = {
                    "PHPSESSID",
                    "JSESSIONID",
                    "SESID",
                    "TOKEN",
                    "_CSRF_TOKEN",
                    "authenticity_token"
            };

            //token追跡自動設定。。
            ArrayList<ParmGenToken> tracktokenlist = new ArrayList<ParmGenToken>();
            Pattern patternw32 = Pattern.compile("\\w{32}");
            ArrayList<AppParmsIni> newparms = new ArrayList<AppParmsIni>();//生成するパラメータ
            PRequestResponse srcpqrs;


            for(PRequestResponse pqrs : rlist){
                if(tracktokenlist!=null&&tracktokenlist.size()>0){//直前のレスポンスに追跡パラメータあり
                	//パラメータ生成



                }
                srcpqrs = pqrs;
                //レスポンストークン解析
                String body = pqrs.response.getBody();
                //レスポンスから追跡パラメータ抽出
                ParmGenParser pgparser = new ParmGenParser(body);
                ArrayList<ParmGenToken> tokenlist = pgparser.getNameValues();
                for(ParmGenToken token : tokenlist){
                    //PHPSESSID, token, SesID, jsessionid

                    String tokenname = token.getTokenKey().GetName();
                    boolean namematched = false;
                    for(String tkn : tknames){
                        if(tokenname.equalsIgnoreCase(tkn)){
                            tracktokenlist.add(token);
                            namematched = true;
                            break;
                        }
                    }

                    // value値が \w{32}に一致
                    if(!namematched){//nameはtknamesに一致しない
                        String tokenvalue = token.getTokenValue().getValue();
                        Matcher matcher = patternw32.matcher(tokenvalue);
                        if(matcher.matches()){
                            tracktokenlist.add(token);
                        }
                    }

                }
            }
    	}



    }//GEN-LAST:event_ParamTrackingActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList CSRFList;
    private javax.swing.JTextField CSRFParam;
    private javax.swing.JButton CSRFdelete;
    private javax.swing.JButton CSRFupdate;
    private javax.swing.JCheckBox FinalResponse;
    private javax.swing.JCheckBox MBCookieFromJar;
    private javax.swing.JCheckBox MBCookieUpdate;
    private javax.swing.JCheckBox MBExec;
    private javax.swing.JCheckBox MBResetToOriginal;
    private javax.swing.JCheckBox MBdeleteSetCookies;
    private javax.swing.JTextArea MacroComments;
    private javax.swing.JTextArea MacroRequest;
    private javax.swing.JTextArea MacroResponse;
    private javax.swing.JButton ParamTracking;
    private javax.swing.JList RequestList;
    private javax.swing.JMenuItem disableRequest;
    private javax.swing.JMenuItem enableRequest;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane paramlog;
    private javax.swing.JMenuItem targetRequest;
    private javax.swing.JTextField waitsec;
    // End of variables declaration//GEN-END:variables
}
