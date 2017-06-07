/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

/**
 *
 * @author daike
 */
public class test {

    public static void main(String[] args){
        //ParmGenCSV(String _filename, String _lang)
        ParmVars.parmfile = "xxx.csv";
        String seq = new String("1ABABABCxD123ABCD4");
        String key = new String("ABCx");

        byte[] seqbin = seq.getBytes();
        byte[] keybin = key.getBytes();

        int startpos = 2;


        System.out.println("String.indexof=" + seq.indexOf(key, startpos));

        	int j = 0;
        	int idx = 0;
        	boolean found = false;
        	for(int i = startpos; i<seqbin.length; i++){
        		System.out.println("  i,j="  + i + "," + j);
        		if(seqbin[i] != keybin[j]){
        			j = 0;
        		}
        		if(seqbin[i] == keybin[j]){
        			if(j == 0){
        				idx = i;
        			}
        			if(++j == keybin.length ){
        				found = true;
        				System.out.println(" result idx,i,j=" + idx+ "," + i + "," + j);
        				break;
        			}
        		}else{
        			j = 0;
        		}
        	}
        	if(!found){
        		idx = -1;
        	}
        	System.out.println(" result idx=" + idx);
    }

}
