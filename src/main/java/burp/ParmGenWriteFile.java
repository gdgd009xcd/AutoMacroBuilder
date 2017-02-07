/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.*;

/**
 *
 * @author daike
 */
public class ParmGenWriteFile {
    PrintWriter pw;
    String fileName;
    String charSet;
    boolean append;
    boolean auto_flush;
    
    ParmGenWriteFile(String _fileName) throws UnsupportedEncodingException, FileNotFoundException{
        fileName   = _fileName; // ファイル名
        charSet    = "utf-8";        // 文字コードセット
        append     = false;          // 追加モード
        auto_flush = true;           // 自動フラッシュ
        open();
    }
    
    final void open() throws UnsupportedEncodingException, FileNotFoundException{

            pw = new PrintWriter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(
                                            new File(fileName)
                                            ,append)
                                        ,charSet)) // 省略するとシステム標準
                                ,auto_flush);
            //...
            
        
    }
    
    void truncate(){
        close();
        try{
            open();
        }catch(Exception ex){
            ParmVars.plog.printException(ex);
        }
    }
    
    void print(String rec){
        if(pw!=null){
            pw.println(rec);
        }
    }
    void close(){
        if (pw != null){
            pw.close();
            pw = null;
        }
    }
    PrintWriter getPrintWriter(){
        return pw;
    }
    
}
