/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author daike
 */
public class ParmVars {
	// ã°ã­ã¼ãã«ãã©ã¡ã¼ã¿
	static String projectdir;
	static String parmfile =  "";
	public static PLog plog;
	public static Encode enc;
	static String formdataenc;//iso8859-1 encoding is fully  mapped binaries for form-data binaries.
	// Proxy Authentication
	// Basic username:password(base64 encoded)
	//String ProxyAuth = "Basic Y2hpa2FyYV8xLmRhaWtlOjdyOXR5QDRxMQ==";
	static String ProxyAuth;
	static ParmGenSession session;
        static int displaylength = 10000;// JTextArea/JTextPaneç­swingã®è¡¨ç¤ºãã¤ãæ°
        private static boolean issaved = false;
        static String fileSep = "/";//maybe unix filesystem.
        static String Version = "";// loaded JSON format version
        final static int TOSTEPANY = 2147483647;//StepTo number means any value
        static List<String> ExcludeMimeTypes = null;
        private static List<Pattern> ExcludeMimeTypesPatterns = null;
        private static org.apache.log4j.Logger logger4j;
	//
	// staticå¤æ°åæå
	//
	static {
            File log4jdir = new File(System.getProperty("user.home"), ".BurpSuite");//.ZAP or .BurpSuite
            String fileName = "log4j.properties";
            File logFile = new File(log4jdir, fileName);
            if (!logFile.exists()) {
                try {
                    ParmGenUtil.copyFileToHome(
                            logFile.toPath(), "xml/" + fileName, "/burp/" + fileName);
                } catch (IOException ex) {
                    
                }
            }
            
            System.out.println("log4j:" + logFile.getPath());
            PropertyConfigurator.configure(logFile.getPath());
            
            logger4j = org.apache.log4j.Logger.getLogger(ParmVars.class);
            
            setExcludeMimeTypes(Arrays.asList("image/.*","application/pdf"));//default Content-Types that exclude ParseResponse function
            
            fileSep = System.getProperty("file.separator");
            formdataenc = "ISO-8859-1";
            
            File desktop = new File(System.getProperty("user.home"), "Desktop");
            if (! desktop.exists()){
                    projectdir = System.getenv("HOMEDRIVE") + fileSep + System.getenv("HOMEPATH") + fileSep + "\u30c7\u30b9\u30af\u30c8\u30c3\u30d7";
                    desktop = new File(projectdir);
                    if (! desktop.exists()){
                            projectdir = System.getenv("HOMEDRIVE") + fileSep + System.getenv("HOMEPATH") + fileSep + "Desktop";
                    }
            }else{
                    projectdir = desktop.getAbsolutePath();
            }
            desktop = null;
            
            parmfile = projectdir + fileSep + "MacroBuilder.json";
            plog = new PLog(projectdir);
            enc = Encode.UTF_8;// default encoding.
            ProxyAuth = "";
            session = new ParmGenSession();
	}
        
        public static boolean isSaved(){
            return issaved;
        }
        
        public static void Saved(){
            issaved = true;
        }
        
        private static void setRegexPatternExcludeMimeType(List<String> excludeMimeTypes){
            Pattern compiledregex = null;
            Matcher m = null;
            ExcludeMimeTypesPatterns = new ArrayList<>();
            int flags = 0;

            flags |= Pattern.MULTILINE;

            flags |= Pattern.CASE_INSENSITIVE;
            
            for(String regex: excludeMimeTypes){
                try{
                    ExcludeMimeTypesPatterns.add(ParmGenUtil.Pattern_compile(regex, flags));
                }catch(Exception e){

                }
            }
        }
        
        public static void setExcludeMimeTypes(List<String> extypes){
            if(extypes!=null&&extypes.size()>0){
                ExcludeMimeTypes = extypes;
                setRegexPatternExcludeMimeType(ExcludeMimeTypes);
            }
        }
        
        public static void clearExcludeMimeType(){
            ExcludeMimeTypes = new ArrayList<>();
            ExcludeMimeTypesPatterns = null;
        }
        
        public static void addExcludeMimeType(String exttype){
            ExcludeMimeTypes.add(exttype);
        }
        
        
        public static boolean isMimeTypeExcluded(String MimeType){
            for(Pattern pt:ExcludeMimeTypesPatterns){
                Matcher m = pt.matcher(MimeType);
                if(m.find()){
                    return true;
                }
            }
            return false;
        }
        
        public static String getFileSep(){
            return fileSep;
        }
        
	//
	//
	// HTTP Request parser
	//
	//
}
