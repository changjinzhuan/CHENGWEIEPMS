package cn.kcrxorg.chengweiepms.psamtools;

import android.util.Log;

import com.rscja.deviceapi.PSAM;
import com.rscja.utility.StringUtility;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PsamTool {
    private final static String TAG = "PsamTool";
    private PSAM mPSAM;
    String card = "01";
    int baud = 9600;
    String param = "000" + baud;
    String cmdVoltage = "6D";
    private byte[] psamID = new byte[4];//00030DC8

    public PsamTool() {
        mPSAM = PSAM.getInstance();
    }
    public boolean pasmInit()
    {
        boolean rs=mPSAM.init();
        delay(1000);
        return rs;
    }
    public String reSet() {
        mPSAM.executeCmd("02", card);
        String result = mPSAM.executeCmd(cmdVoltage, param);
        return result;
    }

    public boolean close() {
        if (mPSAM != null) {
            boolean free = mPSAM.free();
           // delay(1000);
            return free;
        }
        return false;
    }
    public void delay(int second)
    {
        try {
            Thread.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //获取信息 GET_INFO  8010010015
    public String getPSAMinfo() {
        Log.d(TAG,"cmd="+"8010010015");
        return mPSAM.executeCmd("00", "8010010015").substring(0,8);
    }

    //用户验证(VERIFY_USER)   80200000+数据域长度+用户验证信息(用户ID+用户PIN码)
    public boolean verifyUser() {
        String userIDStr = "5053414D49303031";
        String userPINStr = "4D494D49535F5053414D5F55534552";
        byte[] userID = StringUtility.hexString2Bytes(userIDStr);
        byte[] userPIN = StringUtility.hexString2Bytes(userPINStr);
        byte[] length = new byte[1];
        length[0] = (byte) (userID.length + userPIN.length);

        String cmd = "80200000" + StringUtility.bytes2HexString(length, 1) + userIDStr + userPINStr;
        Log.e(TAG, "verifyUsercmd=" + cmd);
        String cmdrs = mPSAM.executeCmd("00", cmd);
        Log.e(TAG, "verifyUserRs=" + cmdrs);
        if (cmdrs == null) {
            return false;
        }
        if (cmdrs.endsWith("9000")) {
            return true;
        }
        return false;
    }

    //获取PSAM数据证书第一部分  8010020000
    public String getPSAMCertifiPart1() {
        String CertifiPart1 = "";
        String cmd = "8010020080";
        Log.e(TAG, "getPSAMCertifiPart1cmd=" + cmd);
        String cmdrs = mPSAM.executeCmd("00", cmd);
        if (cmdrs == null) {
            return null;
        }
        Log.e(TAG, "getPSAMCertifiPart1rs=" + cmdrs);
        if(cmdrs.endsWith("6180"))
        {
            CertifiPart1 = cmdrs.substring(0, cmdrs.length() - 4);
            cmdrs = getResponse(cmdrs.substring(cmdrs.length()-2,cmdrs.length()));
            if (cmdrs == null) {
                return null;
            }
            CertifiPart1 += cmdrs;
        }

//        if (cmdrs.endsWith("6180")) {
//            CertifiPart1 = cmdrs.substring(0, cmdrs.length() - 4);
//            cmd = "00C0000080";
//            cmdrs = mPSAM.executeCmd("00", cmd);
//            if (cmdrs == null) {
//                return null;
//            }
//            CertifiPart1 += cmdrs;
//        }
        return CertifiPart1;
    }

    public String getPSAMCertifiPart2() {
        String CertifiPart2 = "";
        String cmd = "8010020136";
        Log.e(TAG, "getPSAMCertifiPart2cmd=" + cmd);
        String cmdrs = mPSAM.executeCmd("00", cmd);
        Log.e(TAG, "getPSAMCertifiPart2rs=" + cmdrs);
        if (cmdrs == null) {
            return null;
        }

        if (cmdrs.endsWith("6134"))
        {
            CertifiPart2 = cmdrs.substring(0, cmdrs.length() - 4);
            cmdrs = getResponse(cmdrs.substring(cmdrs.length()-2,cmdrs.length()));
            if (cmdrs == null) {
                return null;
            }
            CertifiPart2 += cmdrs;
        }

//        if (cmdrs.endsWith("6134")) {
//            CertifiPart2 = cmdrs.substring(0, cmdrs.length() - 4);
//            cmd = "00C0000034";
//            cmdrs = mPSAM.executeCmd("00", cmd);
//            if (cmdrs == null) {
//                return null;
//            }
//            CertifiPart2 += cmdrs;
//        }
        return CertifiPart2;
    }
    //生成电子签封交互指令(GEN_ELS_CMD)
    //8030 + 交互指令 + 00 + 数据长度 + 数据(交互指令DATA)（EPC长度 + EPC + 交互指令数据长度 + 交互指令数据（交互指令 + 随机数））
    public String genElsCmd(String cmdType,String epc,String data)
    {
        byte[] epcb=StringUtility.hexString2Bytes(epc);
        byte[] datab=StringUtility.hexString2Bytes(data);
        byte[] epcLen = {(byte) epcb.length};
        byte[] dataLen = {(byte) datab.length};
        byte[] allLen = {(byte) (epcb.length + datab.length + 2)};

        String cmd="8030"+cmdType+"00"
                +StringUtility.bytes2HexString(allLen, 1)
                +StringUtility.bytes2HexString(epcLen, 1)
                +epc
                +StringUtility.bytes2HexString(dataLen, 1)
                +data;
                //+"00";
        Log.e(TAG,"genElsCmd="+cmd);
        String cmdrs = mPSAM.executeCmd("00", cmd);

        if (cmdrs == null) {
            return null;
        }
        Log.e(TAG,"genElsRs="+cmdrs);
        if(cmdrs.startsWith("61"))
        {
            cmdrs = getResponse(cmdrs.substring(2));
            if (cmdrs == null) {
                return null;
            }
        }
        return cmdrs;

    }

    public String excute(String cmd)
    {
       String cmdrs= mPSAM.executeCmd("00", cmd);
        {
            if(cmdrs.startsWith("61"))
            {
                cmdrs = getResponse(cmdrs.substring(2));
                if (cmdrs == null) {
                    return null;
                }
            }
        }
        return cmdrs;
    }
    public String getResponse(String len)
    {
        String cmd="00C00000"+len;
        Log.e(TAG,"getResponsecmd="+cmd);
        String cmdrs = mPSAM.executeCmd("00", cmd);
        Log.e(TAG,"getResponseRs="+cmdrs);
        if (cmdrs == null) {
            return null;
        }
         if(cmdrs.endsWith("9000"))
         {
             return cmdrs;
         }
         return cmdrs;
    }

    //开锁写物流指令0x05
    //数据(交互指令DATA):张数（2字节）+ 操作员1(4字节) + 操作员2(4字节) + 操作时间(4字节) + 手持机ID(PSAM卡ID后20bit) + 位置序号(4bit) + 随机数(4字节)+保留(1字节)
    public String genCloseWriteElsCmd(String epc,int number,byte[] operator1,byte[] operator2,String dateTime)
    {
        String dataStr ;
        byte[] data ;
        byte[] dateTimeByte ;
        byte[] numberBytes = new byte[2] ;
        String psamIDStr ;
        String deviceID ;
        String cmdType ;

        numberBytes[0] = (byte) (number/256);
        numberBytes[1] = (byte) (number%256);

        dateTimeByte = dateTimeToByte(dateTime) ;
        psamIDStr = getPSAMinfo().substring(3,8);
        psamIDStr =psamIDStr+"0";
        deviceID=psamIDStr;
        dataStr= StringUtility.bytes2HexString(numberBytes, 2)
                +StringUtility.bytes2HexString(operator1)
                +StringUtility.bytes2HexString(operator2)
                +StringUtility.bytes2HexString(dateTimeByte)
                +deviceID
                +"00000000"
                +"00";
        cmdType = "04";
        return genElsCmd(cmdType,epc,dataStr);

    }
    //开锁写物流指令0x04
    //数据(交互指令DATA):张数（2字节）+ 操作员1(4字节) + 操作员2(4字节) + 操作时间(4字节) + 手持机ID(PSAM卡ID后20bit) + 位置序号(4bit) + 随机数(4字节)+保留(1字节)
    public String genOpenWriteElsCmd(String epc,int number,byte[] operator1,byte[] operator2,String dateTime)
    {
        String dataStr ;
        byte[] data ;
        byte[] dateTimeByte ;
        byte[] numberBytes = new byte[2] ;
        String psamIDStr ;
        String deviceID ;
        String cmdType ;

        numberBytes[0] = (byte) (number/256);
        numberBytes[1] = (byte) (number%256);

        dateTimeByte = dateTimeToByte(dateTime) ;
        psamIDStr = getPSAMinfo().substring(3,8);
        psamIDStr =psamIDStr+"0";
        deviceID=psamIDStr;
        dataStr= StringUtility.bytes2HexString(numberBytes, 2)
                +StringUtility.bytes2HexString(operator1)
                +StringUtility.bytes2HexString(operator2)
                +StringUtility.bytes2HexString(dateTimeByte)
                +deviceID
                +"00000000"
                +"00";
        cmdType = "05";
        return genElsCmd(cmdType,epc,dataStr);

    }
    //关锁指令0x02
    //张数（2字节）+ 操作员1(4字节) + 操作员2(4字节) + 操作时间(4字节) + 手持机ID(PSAM卡ID后20bit) + 位置序号(4bit) + 随机数(4字节)+保留(1字节)
    public String genCloseElsCmd(String epc,int number,byte[] operator1,byte[] operator2,String dateTime)
    {
        String dataStr ;
        byte[] data ;
        byte[] dateTimeByte ;
        byte[] numberBytes = new byte[2] ;
        String psamIDStr ;
        String deviceID ;
        String cmdType ;

        numberBytes[0] = (byte) (number/256);
        numberBytes[1] = (byte) (number%256);

        dateTimeByte = dateTimeToByte(dateTime) ;
        psamIDStr = getPSAMinfo().substring(3,8);
        psamIDStr =psamIDStr+"0";
        deviceID=psamIDStr;
        dataStr= StringUtility.bytes2HexString(numberBytes, 2)
                +StringUtility.bytes2HexString(operator1)
                +StringUtility.bytes2HexString(operator2)
                +StringUtility.bytes2HexString(dateTimeByte)
                +deviceID
                +"00000000"
                +"00";
        cmdType = "02";
        return genElsCmd(cmdType,epc,dataStr);

    }
    //开锁指令0x03
    //数据(交互指令DATA):张数（2字节）+ 操作员1(4字节) + 操作员2(4字节) + 操作时间(4字节) + 手持机ID(PSAM卡ID后20bit) + 位置序号(4bit) + 随机数(4字节)+保留(1字节)
    public String genOpenElsCmd(String epc,int number,byte[] operator1,byte[] operator2,String dateTime)
    {
        String dataStr ;
        byte[] data ;
        byte[] dateTimeByte ;
        byte[] numberBytes = new byte[2] ;
        String psamIDStr ;
        String deviceID ;
        String cmdType ;

        numberBytes[0] = (byte) (number/256);
        numberBytes[1] = (byte) (number%256);

        dateTimeByte = dateTimeToByte(dateTime) ;
        psamIDStr = getPSAMinfo().substring(3,8);
        psamIDStr =psamIDStr+"0";
        deviceID=psamIDStr;
        dataStr= StringUtility.bytes2HexString(numberBytes, 2)
                +StringUtility.bytes2HexString(operator1)
                +StringUtility.bytes2HexString(operator2)
                +StringUtility.bytes2HexString(dateTimeByte)
                +deviceID
                +"00000000"
                +"00";
        cmdType = "03";
        return genElsCmd(cmdType,epc,dataStr);
    }

    //获取 RFID 访问密码(GET_RFID_PWD)   80360000 + 数据长度 + EPC数据 + 00
    public String getRFIDPassword(String epc)
    {
        byte[] length = new byte[1];
        length[0]=(byte)(epc.length()/2);

        String cmd="80360000"+StringUtility.bytes2HexString(length,1)+epc;
        Log.e(TAG,"getRFIDPasswordcmd="+cmd);
        String cmdrs = mPSAM.executeCmd("00", cmd);
        Log.e(TAG,"getRFIDPasswordrs="+cmdrs);
        if(cmdrs.startsWith("61"))
        {
            cmdrs = getResponse(cmdrs.substring(2));

            if (cmdrs == null) {
                return null;
            }
        }
        return cmdrs;

    }
    /* 日期字符串转字节 */
    public static byte[] dateTimeToByte(String dateStr) {
        Long time = 0L ;
        long year = 0 ;
        long month = 0 ;
        long day = 0 ;
        long hour = 0 ;
        long minute = 0 ;
        long second = 0 ;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(dateStr);
            year = date.getYear()%100 ;
            month = (date.getMonth() + 1) ;
            day = date.getDate() ;
            hour = date.getHours() ;
            minute = date.getMinutes() ;
            second = date.getSeconds() ;
        }catch (Exception e){

        }

        //将时间转成4个字节
        //2019-4-4 16:52:21   4D090D15
        time = ((year << 26) | (month << 22) | (day << 17) | (hour << 12) | (minute << 6) | second);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(time) ;
        byte[] t = buffer.array() ;
        byte[] result = new byte[4] ;
        System.arraycopy(t, 4, result, 0, 4);
        return result ;
    }
}
