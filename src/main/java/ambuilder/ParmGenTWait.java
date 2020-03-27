/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import ambuilder.ParmVars;

/**
 *
 * @author daike
 */
public class ParmGenTWait {
    private long waittimer;
    //for log4j
    //private static final Logger LOGGER = Logger.getLogger(ParmGenTWait.class);
    
    ParmGenTWait(long wtimer){
        waittimer = wtimer;
    }
    
    synchronized void TWait(){
        if(waittimer>0){
            ParmVars.plog.debuglog(0, "....sleep Start:" + waittimer + "(msec)");
            try{
                wait(waittimer);
            }catch(Exception e){
                ParmVars.plog.debuglog(0, "....sleep Exception..");
            }
            ParmVars.plog.debuglog(0, "....sleep End.");
        }
    }
}
