package cn.kcrxorg.chengweiepms.mbutil;

public class MyTools {
    public static String addZeroForNum(String str, int strLength) {

        int strLen = str.length();

        if (strLen < strLength) {

            while (strLen < strLength) {

                StringBuffer sb = new StringBuffer();

                sb.append("0").append(str);// 左补0

                // sb.append(str).append("0");//右补0

                str = sb.toString();

                strLen = str.length();

            }

        }
        return str;
    }
    public static byte[] intToByteArray(int i) {

        byte[] result = new byte[4];

        result[0] = (byte)((i >> 24) & 0xFF);

        result[1] = (byte)((i >> 16) & 0xFF);

        result[2] = (byte)((i >> 8) & 0xFF);

        result[3] = (byte)(i & 0xFF);

        return result;

    }
}
