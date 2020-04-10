/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automacrobuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author daike
 */
public class GSONSaveObject {
    public String VERSION;
    public String LANG;
    public boolean ProxyInScope;
    public boolean IntruderInScope;
    public boolean RepeaterInScope;
    public boolean ScannerInScope;
    public Collection<String> ExcludeMimeTypes;
    public Collection<AppParmsIni_List> AppParmsIni_List;
    
    GSONSaveObject(){
        ExcludeMimeTypes = new ArrayList<>();
        AppParmsIni_List = new ArrayList<>();
        PRequestResponse = new ArrayList<>();
    }
    
    // innner static classes
    static class AppParmsIni_List {
        public String URL;
        public int len;
        public int typeval;
        public int inival;
        public int maxval;
        public String csvname;
        public boolean pause;
        public int TrackFromStep;
        public int SetToStep;
        public String relativecntfilename;
        public Collection<AppValue_List> AppValue_List;
        
        AppParmsIni_List(){
            AppValue_List = new ArrayList<>();
        }
        
        
    }
    
    static class AppValue_List{
        public String valpart;
        public boolean isEnabled;
        public boolean isNoCount;
        public int csvpos;
        public String value;
        public String resURL;
        public String resRegex;
        public String resValpart;
        public int resRegexPos;
        public String token;
        public boolean urlencode;
        public int fromStepNo;
        public int toStepNo;
        public String TokenType;
    }
    
    public int CurrentRequest;
    public Collection<PRequestResponses> PRequestResponse;
    
    static class PRequestResponses{
        public String PRequest;
        public String PResponse;
        public String Host;
        public int Port;
        public boolean SSL;
        public String Comments;
        public boolean Disabled;
        public boolean Error;
    }
    
    
    
}
