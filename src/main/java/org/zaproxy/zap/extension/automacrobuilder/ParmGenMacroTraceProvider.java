/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author daike
 */
public class ParmGenMacroTraceProvider {
    
    private static ParmGenMacroTrace pmt = new ParmGenMacroTrace();
    private static Map<Long, ParmGenMacroTrace> pmtmap = new ConcurrentHashMap<>();
    
    /**
     * get original ParmGenMacroTrace base instance for configuration ( for GUI ) 
     * 
     * @return 
     */
    public static ParmGenMacroTrace getOriginalBase(){
        return pmt;
    }
    
    /**
     * get new instance of ParmGenMacroTrace for scan
     * @param selectpos
     * @return 
     */
    public static ParmGenMacroTrace getNewParmGenMacroTraceInstance(long tid){
        ParmGenMacroTrace newpmt = pmt.getScanInstance(tid);
        pmtmap.put(tid, newpmt);
        return newpmt;
    }
    
    public static ParmGenMacroTrace getRunningInstance(long tid){
        return pmtmap.get(tid);
    }
    
    public static void removeEndInstance(long tid){
        pmtmap.remove(tid);
    }
    
}
