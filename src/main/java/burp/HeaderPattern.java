/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.regex.Pattern;

/**
 *
 * @author daike
 */
public class HeaderPattern {
    private String upperheadername;
    private String tkname_regexformat;
    private Pattern tkname_regpattern = null;
    private String tkname_regex = null;
    private String tkvalue_regexformat;
    private Pattern tkvalue_regpattern = null;
    private String tkvalue_regex = null;
    
    HeaderPattern(String uhname, String name_regex, String value_regex){
        upperheadername = uhname.toUpperCase();
        tkname_regexformat = name_regex;
        tkvalue_regexformat = value_regex;
    }
    
    public String getUpperHeaderName(){
        return upperheadername;
    }
    
    
    
    public Pattern getTokenName_RegexPattern(String tkvalue){
        String escdtkvalue =  ParmGenUtil.escapeRegexChars(tkvalue);
        tkname_regex = String.format(tkname_regexformat, escdtkvalue);
        tkname_regpattern = ParmGenUtil.Pattern_compile(tkname_regex);//String.format("xxxx (name)=%s xxxx" , tkvalue)
        return tkname_regpattern;
    }
    
    
    
    public Pattern getTokenValue_RegexPattern(String tkname){
        String escdtkname = ParmGenUtil.escapeRegexChars(tkname);
        tkvalue_regex = String.format(tkvalue_regexformat, escdtkname);
        tkvalue_regpattern = ParmGenUtil.Pattern_compile(tkvalue_regex);//String.format("xxxx %s=(value) xxxx", tkname)
        return tkvalue_regpattern;
    }
    
    public String getTokenValueRegex(){
        return tkvalue_regex;
    }
    
    public String getTokenNameRegex(){
        return tkname_regex;
    }
}
