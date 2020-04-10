/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automacrobuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author youtube
 */
//
// ByteArray
//
class ParmGenBinUtil {

	private ByteArrayOutputStream bstream= null;

	public ParmGenBinUtil(){
            bstream= new ByteArrayOutputStream();
	}

	public ParmGenBinUtil(byte[] bin){
            initParmGenBinUtil(bin);
	}

        public void initParmGenBinUtil(byte[] bin){
            bstream = new ByteArrayOutputStream();
            concat(bin);
        }




	public int length(){
		return bstream.size();
	}



	/**
	 * streamにバイトを追加
	 */
	public boolean concat(byte[] bin){


		if ((bin== null)){
			return false;
		}

		try {
			bstream.write(bin);
		}
		catch (IOException e) {

                    return false;
		}
		return true;
	}



	public byte[] getBytes(){
		if (bstream== null){
			return null;
		}
		return bstream.toByteArray();
	}

	/**
	 substring のバイナリ版
         * org[beginIndex] - org[endIndex-1]
         * length = endIndex - beginIndex > 0
	 */
	public byte[] subBytes(int beginIndex, int endIndex){


		int length= endIndex - beginIndex;		// 戻り値配列の要素数
                if(length>0&&beginIndex>=0&&length()>=endIndex){
                    byte[] org= getBytes();
                    byte[] result= new byte[length];
                    System.arraycopy(org, beginIndex, result, 0, length);
                    return result;
                }

		return null;
	}

	/**
	  org[beginIndex] to last
	 */
	public byte[] subBytes(int beginIndex){
		return subBytes(beginIndex, length());
	}

	/**
	indexOf
	 */
	public int indexOf(byte[] dest, int startpos){
            int idx = -1;
            byte[] seqbin = getBytes();
            byte[] keybin = dest;

            int endpos = seqbin.length - keybin.length +1;

            if(endpos>0&&startpos<endpos){
        	for(int i = startpos; i<endpos; i++){
        		for(int j=0; j<keybin.length; j++){
	        		//System.out.println("  i,j="  + i + "," + j);

	        		if(seqbin[i+j] == keybin[j]){
	        			if(j == keybin.length -1 ){
	        				idx = i;
	        				//System.out.println(" result idx,i,j=" + idx+ "," + i + "," + j);
	        				break;
	        			}

	        		}else{
	        			break;
	        		}
        		}
        		if(idx!=-1)break;
        	}
            }


            return idx;
	}

	/**

	 */
	public int indexOf(byte[] dest){
		return indexOf(dest, 0);
	}


	public int indexOf(byte dest){
		byte[] b= {dest};
		return indexOf(b, 0);
	}











}

