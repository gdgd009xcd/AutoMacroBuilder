/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.io.File;
import java.util.Optional;

/**
 *
 * @author daike
 */
public class FileInfo {
    
    private String dirname;
    private String basename;
    private String dirsep;
    private String suffix;
    private String dot;
    private String prefix;
    
    FileInfo(String fullfilename){
        File file = new File(fullfilename);
        
        String  fileSep = file.separator;
        
        dirname = getNoNullString(file.getParent());
        basename = getNoNullString(file.getName());
        suffix = getExtensionByStringHandling(basename);
        prefix = getPrefixByStringHandling(basename);
        
        dirsep = dirname.length()>0?fileSep:"";
        
        dot = basename.contains(".")?".":"";
        
        
        
        
    }
    
    public String getDirName(){
        return dirname;
    }
    
    public String getBaseName(){
        return basename;
    }
    
    public String getPrefix(){
        return prefix;
    }
    
    public String getSuffix(){
        return suffix;
    }
    
    public String getFullFileName(){
        String fullname = dirname + dirsep + prefix + dot + suffix;
        
        return fullname;
    }
    
    public void setPrefix(String pstr){
        Optional<String> optstr = Optional.ofNullable(pstr);
        prefix = optstr.orElse("");
    }
    
    public void setSuffix(String pstr){
        Optional<String> optstr = Optional.ofNullable(pstr);
        suffix = optstr.orElse("");
    }
    
    private String getNoNullString(String maybenullstr){
        Optional<String> ostr = Optional.ofNullable(maybenullstr);
        return ostr.orElse("");
    }
    
    private  String getExtensionByStringHandling(String filename) {
        //1) ofNullable() : if filename is not null, then filter method is called. otherwise return Optional.empty().
        //2) .filter(): if f.contains(".") is true, then map method called. otherwise return Optional.empty().
        //3) .map():  if f is not null then call f.substring == prefix return.
        Optional<String> optsuffix = Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
        
        String suffix = optsuffix.orElse("");
        return suffix;
        
    }
    
    private  String getPrefixByStringHandling(String filename) {
        
        Optional<String> optprefix =  Optional.ofNullable(filename)
        
        .map(f -> f.substring(0,filename.lastIndexOf(".")>0?filename.lastIndexOf("."):filename.length()))
        .filter(f -> f.contains(".")==false);
        
        String prefix = optprefix.orElse("");
        return prefix;
        
    }
    
    
}
