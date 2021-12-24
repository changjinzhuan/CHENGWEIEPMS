package cn.kcrxorg.chengweiepms.mbutil;

import java.security.MessageDigest;


public class SHA1Util {
	public static String shaEncode(byte[] data) throws Exception {

        MessageDigest sha = null;

        try {

            sha = MessageDigest.getInstance("SHA");

        } catch (Exception e) {

            System.out.println(e.toString());

            e.printStackTrace();

            return "";

        }

 

       

        byte[] md5Bytes = sha.digest(data);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {

            int val = ((int) md5Bytes[i]) & 0xff;

            if (val < 16) {

                hexValue.append("0");

            }

            hexValue.append(Integer.toHexString(val));

        }

        return hexValue.toString();

    }

 

    public static void main(String args[]) throws Exception {

    	String msg = SHA1Util.shaEncode(HexUtil.hexStringToBytes("000000270000000000000171CA0F5FC3000000BF7B22636F6465223A3133313037332C2262757369496E666F4C697374223A5B7B22766F7563686572547970654944223A22313031303031222C22766F7563686572547970654E616D65223A22E7BAB8313030E58583222C2276616C223A223130302E30227D2C7B22766F7563686572547970654944223A22313031303032222C22766F7563686572547970654E616D65223A22E7BAB83530E58583222C2276616C223A2235302E30227D5D2C22737461636B496E666F4C697374223A5B5D7D0000000000000000"));
    	System.out.println("msg"+msg.toUpperCase());
    }
}

