/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.Objects;
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
    private String tkname = "";
    private int fcnt = 0;
    private String tkvalue_regexformat;
    private Pattern tkvalue_regpattern = null;
    private String tkvalue_regex = null;
    private ParmGenRequestTokenKey.RequestParamType rptype;
    private ParmGenRequestTokenKey.RequestParamSubType rpsubtype;
    
    HeaderPattern(String uhname, String name_regex, String value_regex, ParmGenRequestTokenKey.RequestParamType _rptype, ParmGenRequestTokenKey.RequestParamSubType _subtype){
        upperheadername = uhname.toUpperCase();
        tkname_regexformat = name_regex;
        tkvalue_regexformat = value_regex;
        rptype = _rptype;
        rpsubtype = _subtype;
    }
    
    HeaderPattern(HeaderPattern src){
        upperheadername = src.upperheadername;
        tkname_regexformat = src.tkname_regexformat;
        tkname_regpattern = src.tkname_regpattern;
        tkname_regex = src.tkname_regex;
        tkname = src.tkname;
        fcnt = src.fcnt;
        tkvalue_regexformat = src.tkvalue_regexformat;
        tkvalue_regpattern = src.tkvalue_regpattern;
        tkvalue_regex = src.tkvalue_regex;
        rptype = src.rptype;
        rpsubtype = src.rpsubtype;
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
    
    
    
    public Pattern getTokenValue_RegexPattern(String _tkname){
        String escdtkname = ParmGenUtil.escapeRegexChars(_tkname);
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
    
    public ParmGenRequestTokenKey.RequestParamType getRequestParamType(){
        return rptype;
    }
    
    public ParmGenRequestTokenKey.RequestParamSubType getRequestParamSubType(){
        return rpsubtype;
    }
    
    public void setTkName(String _tkname){
        tkname = _tkname;
    }
    
    public void setFcnt(int _fcnt){
        fcnt = _fcnt;
    }
    
    public ParmGenRequestToken getQToken(){
        //ParmGenRequestToken(ParmGenRequestTokenKey.RequestParamType _rptype, ParmGenRequestTokenKey.RequestParamSubType _subtype,String _name, String _value, int _fcnt)
        return new ParmGenRequestToken(rptype, rpsubtype, tkname, "", fcnt);
    }
    
    // this hash doesn't care about fcnt. because request has same rptype/subtype/tkname tokens.
    public int getSameTokenHash(){
        return Objects.hash(rptype,rpsubtype, tkname);
    }
    
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof HeaderPattern))
        return false;
      HeaderPattern that = (HeaderPattern) obj;
      return this.rptype == that.rptype && this.rpsubtype == that.rpsubtype && this.tkname.equals(that.tkname) && this.fcnt == that.fcnt;
    }

    @Override
    public int hashCode() {
        int hcode  = Objects.hash(this.rptype, this.rpsubtype, this.tkname, this.fcnt);
        
        return hcode;
    }
    
}
