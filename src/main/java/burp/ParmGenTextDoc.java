/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 *
 * @author youtube
 */
public class ParmGenTextDoc{
    private  JTextComponent tcompo;
    
    ParmGenTextDoc(JTextComponent tc){
        tcompo = tc;
    }
    
    void setdatadoc(){
        JTextComponent editor = tcompo;
        String filestring= "";
        byte[] b = new byte[4096]; int readByte = 0, totalByte = 0;
        try {
            DataInputStream dataInStream = new DataInputStream( new BufferedInputStream( new FileInputStream("C:\\temp\\bindata.txt")));
            //File rfile = new File("C:\\temp\\text.jpg");
            while(-1 != (readByte = dataInStream.read(b))){ 
                try{
                    filestring = filestring + new String(b, "ISO8859-1"); 
                }catch(UnsupportedEncodingException e){
                    filestring += "unsupported.\n";
                }
                totalByte += readByte; 
                //System.out.println("Read: " + readByte + " Total: " + totalByte); 
            }
        } catch (IOException ex) {
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("before LFinsert");
        //filestring = filestring.substring(0, 1024);
        String display = ParmGenUtil.LFinsert(filestring);
        System.out.println("LFinsert done. before reader");
        Document blank = new DefaultStyledDocument();
        Document doc = editor.getDocument();
        editor.setDocument(blank);
        try {
            //Editor.setPage(rfile.toURI().toURL());
            ParmVars.plog.debuglog(0, "before insert ");
            doc.insertString(0, display, null);
            ParmVars.plog.debuglog(0,"insert done");
            editor.setDocument(doc);
            //TextArea.setText(filestring);
            //Editor.setText(filestring);
 
        } catch (Exception ex) {
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public  void setText(String text){
        Document doc = null;
        if(tcompo!=null){
            Document blank = new DefaultStyledDocument();
            doc = tcompo.getDocument();
            
            tcompo.setDocument(blank);
            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException ex) {
                Logger.getLogger(ParmGenTextDoc.class.getName()).log(Level.SEVERE, null, ex);
            }
                        
            try {
                ParmVars.plog.debuglog(0, "before blank insert");
                doc.insertString(0, text, null);
                ParmVars.plog.debuglog(0, "blank insert done");
            } catch (BadLocationException ex) {
                Logger.getLogger(ParmGenTextDoc.class.getName()).log(Level.SEVERE, null, ex);
            }
            tcompo.setDocument(doc);
            
        }
        
    }
    
}
