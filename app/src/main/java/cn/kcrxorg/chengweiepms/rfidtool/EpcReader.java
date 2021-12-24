package cn.kcrxorg.chengweiepms.rfidtool;


import android.util.Log;

import com.rscja.utility.StringUtility;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;

public class EpcReader {

    public static TagEpcData readEpc(String epcstr)
    {
        try {
            if(epcstr.length()!=24)//长度有误
            {
                return null;
            }
            TagEpcData ted = new TagEpcData();
            //包号
            String tedidstr= epcstr.substring(0,8);
            Long tedid= Long.parseLong(tedidstr,16);
            ted.setTagid(tedid);
            //版本号
            String versionidstr=epcstr.substring(8,9);
            int versionid=Integer.parseInt(versionidstr);
            ted.setVersionid(versionid);
            //券别
            String pervalueidstr=epcstr.substring(9,10);
            int pervalueid=Integer.parseInt(pervalueidstr);
            ted.setPervalueid(pervalueid);
            //张数
            String amountstr=epcstr.substring(10,14);
            int amount=Integer.parseInt(amountstr,16);
            ted.setAmount(amount);
            //随机数
            String randomstr=epcstr.substring(14,16);
            ted.setRandom(randomstr);
            //操作数
            String operatecountstr=epcstr.substring(16,19);
            int operatecount=Integer.parseInt(operatecountstr,16);
            ted.setOperatecount(operatecount);
            //校验码
            String checkcodestr=epcstr.substring(19,22);
            ted.setCheckcode(checkcodestr);
            //状态字
            String statusstr=epcstr.substring(22);
            System.out.println("statusstr="+statusstr);
            int status=Integer.parseInt(statusstr,16);
          //  System.out.println(Integer.toBinaryString(status));
            statusstr=String.format("%08d",Integer.parseInt(Integer.toBinaryString(status)));;
            System.out.println(statusstr);
            //状态字判断
            String jobstutsstr=statusstr.substring(0,1);
            if(jobstutsstr.equals("0"))
            {
                ted.setJobstuts(false);
            }else
            {
                ted.setJobstuts(true);
            }
            String hasElecstr=statusstr.substring(1,2);
            if(hasElecstr.equals("0"))
            {
                ted.setHasElec(false);
            }else
            {
                ted.setHasElec(true);
            }
            String epcExstr=statusstr.substring(4,5);
            if(epcExstr.equals("0"))
            {
                ted.setEpcEx(false);
            }else
            {
                ted.setEpcEx(true);
            }
            String lockeExstr=statusstr.substring(5,6);
            if(lockeExstr.equals("0"))
            {
                ted.setLockeEx(false);
            }else
            {
                ted.setLockeEx(true);
            }
            String lockstutsstr=statusstr.substring(6,8);
            if(lockstutsstr.equals("00"))
            {
                ted.setLockstuts("unLock");
            }else if(lockstutsstr.equals("01"))
            {
                ted.setLockstuts("Lock");
            }else if(lockstutsstr.equals("11"))
            {
               // ted.setLockstuts("Lock");
                ted.setLockstuts("unKnown");
            }else
            {
                //ted.setLockstuts("Lock");
                 ted.setLockstuts("unlawful");
            }
            if(1000000000L>ted.getTagid()||ted.getTagid()>1268435455)//ID号过滤
            {
                Log.e("test","ID号过滤 "+ted.getTagid());
                return null;
            }
            if(ted.getPervalueid()>6)//券别过滤
            {
                Log.e("test","券别过滤 "+ted.getPervalueid());
                return null;
            }
            if(ted.getOperatecount()>402||ted.getOperatecount()<0)//操作次数过滤
            {
                Log.e("test","操作次数 "+ted.getOperatecount());
                return null;
            }
            return ted;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String desCheck()
    {
       String EpcStartData="3B9CD92A114E20A6";
       String opcount="03A"+"0000000000000";
       String tid="E280114020002100F5D10001";
       String lowtid="20002100F5D10001";

       byte[] EpcStartDataB= StringUtility.hexStringToBytes(EpcStartData);//A
       byte[] opcountB=StringUtility.hexStringToBytes(opcount);//B
       byte[] lowtidB=StringUtility.hexStringToBytes(lowtid);//C

        String xorCA=xorHexString(lowtid,EpcStartData);
        String xorCB=xorHexString(xorCA,opcount);

        System.out.println("xorCB="+xorCB);
        return xorCB;
    }
    private static String xorHexString(String string1,String string2)
    {
        String[] dateArr1 = new String[8];
        String[] dateArr2 = new String[8];
        try
        {
            dateArr1[0]=string1.substring(0,2);
            dateArr2[0]=string2.substring(0,2);
            dateArr1[1]=string1.substring(2,4);
            dateArr2[1]=string2.substring(2,4);
            dateArr1[2]=string1.substring(4,6);
            dateArr2[2]=string2.substring(4,6);
            dateArr1[3]=string1.substring(6,8);
            dateArr2[3]=string2.substring(6,8);
            dateArr1[4]=string1.substring(8,10);
            dateArr2[4]=string2.substring(8,10);
            dateArr1[5]=string1.substring(10,12);
            dateArr2[5]=string2.substring(10,12);
            dateArr1[6]=string1.substring(12,14);
            dateArr2[6]=string2.substring(12,14);
            dateArr1[7]=string1.substring(14,16);
            dateArr2[7]=string2.substring(14,16);

        }catch (Exception e)
        {

        }
        String code = "";
        for (int i = 0; i < dateArr1.length-1; i++) {

                code += xor(dateArr1[i], dateArr2[i]);

        }
        return code;
    }
    private static String xor(String strHex_X,String strHex_Y){
//将x、y转成二进制形式
        String anotherBinary=Integer.toBinaryString(Integer.valueOf(strHex_X,16));
        String thisBinary=Integer.toBinaryString(Integer.valueOf(strHex_Y,16));
        String result = "";
//判断是否为8位二进制，否则左补零
        if(anotherBinary.length() != 8){
            for (int i = anotherBinary.length(); i <8; i++) {
                anotherBinary = "0"+anotherBinary;
            }
        }
        if(thisBinary.length() != 8){
            for (int i = thisBinary.length(); i <8; i++) {
                thisBinary = "0"+thisBinary;
            }
        }
//异或运算
        for (int i=0;i<anotherBinary.length();i++){
//如果相同位置数相同，则补0，否则补1
            if(thisBinary.charAt(i)==anotherBinary.charAt(i))
                result+="0"; else{
                result+="1";
            }
        }
     //   Log.e("code",result);
        return Integer.toHexString(Integer.parseInt(result, 2));
    }

//    public static List<UHfData.InventoryTagMap> checkHasElc(List<UHfData.InventoryTagMap> alltag)
//    {
//        for(int i=0;i<alltag.size();i++)
//        {
//            TagEpcData tegdata=readEpc(alltag.get(i).strEPC);
//            if(!tegdata.getHasElec())
//            {
//                alltag.remove(i);
//            }
//        }
//        return alltag;
//    }

    public static void main(String[] args) {

    //    desCheck();
        //3B9D8CAA1200002E00DD3E450
        TagEpcData ted= readEpc("481F5451114E204A09387141");
        System.out.println("tagid="+ted.getTagid());
        System.out.println("versionid="+ted.getVersionid());
        System.out.println("pervalueid="+ted.getPervalueid());
        System.out.println("amount="+ted.getAmount());
        System.out.println("randomstr="+ted.getRandom());
        System.out.println("operatecount="+ted.getOperatecount());
        System.out.println("checkcodestr="+ted.getCheckcode());

        //00开锁下电40开锁上电01关锁下电41关锁上电
        System.out.println("lockstuts="+ted.getLockstuts());
        System.out.println("lockeEx="+ted.getLockeEx());
        System.out.println("epcEx="+ted.getEpcEx());
        System.out.println("hasElec="+ted.getHasElec());
        System.out.println("jobstuts="+ted.getJobstuts());
        System.out.println("tagid="+ted.getTagid());

    }
    /* c 要填充的字符
     *  length 填充后字符串的总长度
     *  content 要格式化的字符串
     *  格式化字符串，左对齐
     * */
    public static String flushLeft(char c, long length, String content){
        String str = "";
        long cl = 0;
        String cs = "";
        if (content.length() > length){
            str = content;
        }else{
            for (int i = 0; i < length - content.length(); i++){
                cs = cs + c;
            }
        }
        str = content + cs;
        return str;
    }
    public static String reverse1(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}
