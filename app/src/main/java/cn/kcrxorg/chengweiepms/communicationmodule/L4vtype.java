package cn.kcrxorg.chengweiepms.communicationmodule;

public class L4vtype {
    String lenth;
    String data;
    public L4vtype(String l4data)
    {
        int len=l4data.length()/2;
        String hex= Integer.toHexString(len);
        setLenth(addZeroForNum(hex,8).toUpperCase());
        setData(l4data);
    }

    public String toString()
    {
        return lenth+data;
    }


    public String getLenth() {
        return lenth;
    }

    public void setLenth(String lenth) {
        this.lenth = lenth;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
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
}
