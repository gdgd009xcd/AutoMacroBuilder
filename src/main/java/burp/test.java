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
        String seq = new String("AAAABBABABCxD123ABCD4");
        String key = new String("AAAB");

        byte[] seqbin = seq.getBytes();
        byte[] keybin = key.getBytes();

        int startpos = 0;
        int endpos = seqbin.length - keybin.length +1;


        System.out.println("String.indexof=" + seq.indexOf(key, startpos));

        	int idx = -1;
        	for(int i = startpos; i<endpos; i++){
        		for(int j=0; j<keybin.length; j++){
	        		System.out.println("  i,j="  + i + "," + j);
	        		
	        		if(seqbin[i+j] == keybin[j]){
	        			if(j == keybin.length -1 ){
	        				idx = i;
	        				System.out.println(" result idx,i,j=" + idx+ "," + i + "," + j);
	        				break;
	        			}

	        		}else{
	        			break;
	        		}
        		}
        		if(idx!=-1)break;
        	}
        	
        	System.out.println(" result idx=" + idx);
    }

}
