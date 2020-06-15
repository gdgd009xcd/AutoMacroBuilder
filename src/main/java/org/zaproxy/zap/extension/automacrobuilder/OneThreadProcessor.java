/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author daike
 */
public class OneThreadProcessor {
    private static org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();
    private long id;
    private ThreadManager tm;
    private boolean isend;
    private Thread th = null;
    private long starttime;
    private boolean isinterrupted = false;
    private boolean isaborted = false;
    private InterfaceDoAction doaction;
    private List<InterfaceAction> actionlist;
    private int stage = -1;
    private int seqno;
    private Object optdata = null;
    
   
    /**
     * Newly create doaction.
     * 
     *  it call void startAction(ThreadManager tm, OneThreadProcessor otp)
     * 
     * @param tm
     * @param th
     * @param doaction 
     */
    public OneThreadProcessor(ThreadManager tm, Thread th, InterfaceDoActionProvider provider){
        this.doaction = provider.getDoActionInstance();
        this.seqno = provider.getSequnceNo();
        this.th = th;
        this.id = th.getId();
        this.tm = tm;
        LOGGER4J.info("ProcessCreated:" + id);
        this.isend =false;
        this.isaborted = false;
        this.starttime = 0;
        
        this.isinterrupted = false;

        this.actionlist = doaction.startAction(tm, this);
        
        
     
        
        

    }
    
    public Thread getThread(){
        return this.th;
    }
    
    /**
     * set initial parameter for RECYCLE doaction.
     * 
     * it call void startAction(ThreadManager tm, OneThreadProcessor otp)
     * 
     * @param th 
     */
    public void setNewThread(Thread th, InterfaceDoActionProvider provider){
        this.doaction = provider.getDoActionInstance();
        this.seqno = provider.getSequnceNo();
        this.th = th;
        this.id = th.getId();
        this.tm = tm;
        LOGGER4J.info("ProcessReplaced:" + id);
        this.isend =false;
        this.isaborted = false;
        this.starttime = 0;
        
        this.isinterrupted = false;
        
        this.actionlist = this.doaction.startAction(tm, this);
        

    }
    
    public void startTimer(){
        this.starttime = System.currentTimeMillis ();
    }
    
    public void endTimer(){
        this.starttime = 0;
    }
    
    protected long howManyWaitTime(){
        long timecache = this.starttime;
        if ( timecache != 0 ) {
            // DO NOT USE this.starttime IN THIS BLOCK.
            // this.starttime parameter may be modified by other thread.
            long distc = System.currentTimeMillis() - timecache;
            return distc;
        }
        return 0;
    }
    

    public void setAborted(){
        this.isaborted = true;
    }
    
    public boolean isAborted(){
        return this.isaborted;
    }
    
    public void setInterrupt(){
        this.th.interrupt();
        this.isinterrupted = true;
    }
    
    public boolean wasInterrupted(){
        if(Thread.currentThread().isInterrupted()){
            this.isinterrupted = true;
        }
        return this.isinterrupted;
    }
        
    public void doProcess(int n){
        // ================== A) thread local zone start.
        boolean doendaction = false;
        this.stage = n;
        try{
            InterfaceAction action = actionlist.get(n);
            if ( action != null ) {
                doendaction = action.action(this.tm, this);
            }
        } catch (Exception ex){
            LOGGER4J.error("id:" + id , ex);
        } finally {
            terminated(doendaction);
        }
    }
    
    public long getid(){
        return this.id;
    }
    
    /**
     * run InterfaceEndAction endAction 
     * 
     * @param noendaction false 
     * @return 
     */
    public boolean terminated(boolean doendaction){
      // ================== A) thread local zone end.
        // This method should call the following function at the very end:
        boolean aborted = this.tm.endProcess(this, this.doaction, doendaction);
        LOGGER4J.info("Process terminated:" + id );
        // thread local zone ended..
        return aborted;
    }
    

    
    protected void Ended(){
        this.isend = true;
    }
    
    public boolean isEnd(){
        return this.isend;
    }
    
    public void addAction(InterfaceAction action){
        actionlist.add(action);
    }

    public int getStage(){
        return this.stage;
    }
    
    public int getSequenceNo(){
        return this.seqno;
    }
    
    public <T> void setOptData(T d){
        this.optdata = d;
    }
    
    public <T> T getOptData(){
        return CastUtils.castToType(this.optdata);
    }
}
