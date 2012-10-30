package br.ufmg.dcc.vod.spiderpig.common;

/**
 * Class used to "unsign" values. This project only uses this for storing 
 * numbers. Arithmetic with unsigned values is a more complex task. 
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
 */
public class SignedUtils {

	/**
	 * Returns an integer which represents this byte as unsigned.
	 * 
	 * @param b byte
	 * @return Unsigned value (integer)
	 */
	public static int unsignedByte(byte b) {
        return b & 0xFF;
    }

	/**
	 * Returns an byte which represents this integer (which is a unsigned byte) 
	 * as signed.
	 * 
	 * @param i integer
	 * @return Signed value (byte)
	 */
    public static byte signedByte(int i) {
        return (byte) i;
    }
}
