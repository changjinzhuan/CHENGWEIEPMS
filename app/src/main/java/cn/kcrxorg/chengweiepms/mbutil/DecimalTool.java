package cn.kcrxorg.chengweiepms.mbutil;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DecimalTool {
    public static String formatTosepara(BigDecimal data) {
        if(data.intValue()==0)
        {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(data);
    }
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < strLength) {
            sb = new StringBuffer();
            sb.append("0").append(str);// 左补0
            // sb.append(str).append("0");//右补0
            str = sb.toString();
            strLen = str.length();
        }
        return str;
    }
}
