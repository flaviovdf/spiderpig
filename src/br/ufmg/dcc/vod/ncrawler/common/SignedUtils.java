package br.ufmg.dcc.vod.ncrawler.common;

public class SignedUtils {

	public static int unsignedByte(byte b) {
        return b & 0xFF;
    }
    
    public static byte signedByte(int i) {
        return (byte) i;
    }

	public static int unsignedInt(int b) {
        return b & 0xFF;
    }
    
}
