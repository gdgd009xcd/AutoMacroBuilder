/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ambuilder;

import ambuilder.ParmVars;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author daike
 */
public class ParmGenReadFile {
    
    FileReader fr = null;
    BufferedReader br = null;
    
    ParmGenReadFile(String rfile) throws FileNotFoundException{
            fr = new FileReader(rfile);
            br = new BufferedReader(fr);
    }
    
    public String read(){
        
        String rdata;
        String alldata = null;
        try {
            if(br!=null){
                if((rdata = br.readLine()) != null) {
                    rdata = rdata.replace("\r","");
                    rdata = rdata.replace("\n","");
                    alldata = rdata;
                }
            }
        } catch (IOException ex) {
            ParmVars.plog.printException(ex);
        }
        return alldata;
    }
    
    void close(){
        if(br!=null) try {
            br.close();
        } catch (IOException ex) {
            ParmVars.plog.printException(ex);
        }
        if(fr!=null) try {
            fr.close();
        } catch (IOException ex) {
            ParmVars.plog.printException(ex);
        }
    }
    
}
