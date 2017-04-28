/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 *
 * @author tms783
 */

public class ParmFileFilter extends FileFilter{

  public boolean accept(File f){
    /* ディレクトリなら無条件で表示する */
    if (f.isDirectory()){
      return true;
    }

    /* 拡張子を取り出し、csvだったら表示する */
    String ext = getExtension(f);
    if (ext != null){
      if (ext.equals("json") ){
        return true;
      }else{
        return false;
      }
    }

    return false;
  }

  public String getDescription(){
    return "ParmGen設定";
  }

  /* 拡張子を取り出す */
  private String getExtension(File f){
    String ext = null;
    String filename = f.getName();
    int dotIndex = filename.lastIndexOf('.');

    if ((dotIndex > 0) && (dotIndex < filename.length() - 1)){
      ext = filename.substring(dotIndex + 1).toLowerCase();
    }
      
    return ext;
  }

  

}
