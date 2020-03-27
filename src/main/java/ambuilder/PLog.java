package ambuilder;


import ambuilder.ParmVars;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author daike
 */
//
//Logger
//

public class PLog {
	//ã­ã°ã¬ãã«
	// 	0 - INFO
	// 1 - DEBUG
	// 2 - DETAIL
	// 3 - ALL
	public static final int C_INFO = 0;
	public static final int C_DEBUG = 1;
	public static final int C_DETAIL = 2;
	public static final int C_ALL = 3;

	int LogLevel = C_INFO;
	String logname = null;
	String comments = "";
	boolean LogfileOn;
	boolean iserror=false;
	PrintWriter Stdout = null;
	PrintWriter Stderr = null;

	PLog(String projectdir){

		// è¨­å®ãã¡ã¤ã«
		//    ãã¡ã¤ã«ãã©ã¼ããã
		//    "ãã¹ï¼æ­£è¦è¡¨ç¾ï¼", æ¡æ°(number)|ä½ç½®(csv), å¤ã®ç¨®é¡randï¼ä¹±æ°ï¼/numberï¼æé æ°å¤ï¼/csvï¼ãã¡ã¤ã«ï¼/track(ã¬ã¹ãã³ã¹), åæå¤(æé æ°å¤)[:æå¤§å¤]/csvãã¡ã¤ã«ãã¹ï¼ï¼£ï¼³ï¼¶ï¼,"path/query/body/loc[-]","å¤[æ­£è¦è¡¨ç¾(\w+)ã§æå®ãã]"....
		//
		//    ä¾ï¼".*/project/index.php.*", 4, number, 1,"body", "myname(\w+)","query", "myvalue(\w+)", "path", "id/\/(\w+)\/"
		//

		logname = projectdir + ParmVars.getFileSep() +"AppScanPermGen.log";
		LogfileOn = false;// default disable file output
		File logfile = new File(logname);
		if ( ! logfile.exists()){
			debuglog(1, "started: projectdir=" + projectdir);
		}
		logfile = null;
		comments = "";//no null
		iserror = false;//==true then error

	}

	public void SetBurpPrintStreams(PrintWriter stdout, PrintWriter stderr){
		Stdout = stdout;
		Stderr = stderr;
	}

	private void StdoutPrintln(String v){
		if(Stdout!=null){
			Stdout.println(v);
		}
		System.out.println(v);
	}
	private void StderrPrintln(String v){
		if(Stderr!=null){
			Stderr.println(v);
		}
		System.err.println(v);
	}

	public String getLogname() { return logname; }

	private String getDateTimeStr(){
		Date date1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		return sdf1.format(date1);
	}


	public void printLF(){
		try {
			String v = "";
			String line = "";
			boolean append = true;
			if (LogLevel >= 0){

				line = v + "\n";
				StdoutPrintln(line);
				if(LogfileOn){
					FileWriter filewriter = new FileWriter(logname, append);
					filewriter.write(v + "\r\n");
					filewriter.close();
				}
			}
		}catch (Exception e){
			printException(e);
		}
	}

	public void printlog(String v, boolean append){
		try {
			v = getDateTimeStr() + " " + v;
			if (LogLevel >= 0){


				StdoutPrintln(v);
				if(LogfileOn){
					FileWriter filewriter = new FileWriter(logname, append);
					filewriter.write(v + "\r\n");
					filewriter.close();
				}
			}
		}catch (Exception e){
			printException(e);
		}
	}

	public void InitPrint(String v){
		printlog(v, false);
	}

	public void AppendPrint(String v){
		printlog(v, true);
	}

	boolean isLogfileOn(){
		return LogfileOn;
	}

	public void LogfileOn(boolean _on){
		LogfileOn = _on;
	}

	public void printException(Exception e){
            StringWriter sw = null;
            PrintWriter  pw = null;

            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String trace = sw.toString();
            printlog(e.toString(), true);
            printlog(trace, true);

            try {
                if ( sw != null ) {
                    sw.flush();
                    sw.close();
                }
                if ( pw != null ) {
                    pw.flush();
                    pw.close();
                }
            } catch (IOException ignore){}
	}

       public void printError(String v){
           if(v==null){
               v = "";
           }
           printlog("ERROR: " + v, true);
       }

	public void debuglog(int l, String v){
		if ( l <= LogLevel ){
			printlog(v, true);
		}
	}

        void clearComments(){
            comments = "";//no null
        }

        void addComments(String _v){
            comments += _v + "\n";
        }

        void setComments(String _v){
            comments = _v;
        }

        String getComments(){
            return comments;
        }

        void setError(boolean _b){
            iserror = _b;
        }

        boolean isError(){
            return iserror;
        }
}

