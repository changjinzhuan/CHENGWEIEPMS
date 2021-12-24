package cn.kcrxorg.chengweiepms.locktools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.rscja.deviceapi.Module;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.mbutil.HexUtil;
import cn.kcrxorg.chengweiepms.psamtools.PsamTool;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;

public class LockUtil{
    private final String TAG="LockUtil";
    private Module mInstance;
    private boolean threadStop = true;
    boolean isOpened=false;

    private final int LOCK_WHAT=2;
    private final int LOCK_LOG_WHAT=3;

    public Module getmInstance() {
        return mInstance;
    }
    PsamTool psamTool;
    public LockUtil() throws Exception {
            mInstance=Module.getInstance();//获取串口
            psamTool =new PsamTool();
            if(!psamTool.pasmInit())
            {
                Log.e ("LockUtil","PSAM卡上电失败");
                return  ;
            }
            String resetRs=psamTool.reSet();
            if(resetRs==null)
            {
                Log.e ("LockUtil","psam初始化失败");
                psamTool.close();
                return ;
            }
            Log.e("LockUtil", "resetRs="+resetRs);
           // openCOM();
    }
    public boolean openCOM()
    {
        boolean flag=false;
        mInstance.powerOn(4);
        if(mInstance!=null&&mInstance.openSerail("/dev/ttyHSL1",115200,8,1,0))
        {
            flag=true;
            isOpened=true;
        }else
        {
            Log.e("LockUtil","串口打开失败!");
        }
        return  flag;
    }
    public String readEpc()
    {
       return   sendAndRs("0204303030343030373303",5000);
    }
    public String openWriteEls(String OpenWriteElsCmd)
    {
        String cmdstart="020530303143";
        String lenHexStr="001C";
        byte[] CloseWriteElsCmdB=OpenWriteElsCmd.getBytes();
        String CloseWriteElsCmdHex=StringUtility.bytes2HexString(CloseWriteElsCmdB);
        String allcmd=lenHexStr+OpenWriteElsCmd;
        Log.e("LockUtil","allcmd="+allcmd);
        byte[] allcmdb=StringUtility.hexString2Bytes(allcmd);

        //       int[] allcmdints=new int[allcmdb.length];
//      for(int i=0;i<allcmdb.length;i++)
//      {
//          allcmdints[i]=(int)allcmdb[i];
//      }
//      String crc=CRC.crc16(allcmdints);
        int calcCrc16 = CRC.calcCrc16(allcmdb);
        String crc = String.format("%04x", calcCrc16).toUpperCase();
        Log.e("LockUtil","crc="+crc);
        byte[] crcb = crc.getBytes();
        String crchex = StringUtility.bytes2HexString(crcb);
        String cmddata = cmdstart + CloseWriteElsCmdHex + crchex + "03";
        Log.e("LockUtil","cmddata="+cmddata);
        return  sendAndRs(cmddata,20000);
    }
    public String CloseWriteEls(String CloseWriteElsCmd)
    {
       String cmdstart="020530303143";
       String lenHexStr="001C";
       byte[] CloseWriteElsCmdB=CloseWriteElsCmd.getBytes();
       String CloseWriteElsCmdHex=StringUtility.bytes2HexString(CloseWriteElsCmdB);
       String allcmd=lenHexStr+CloseWriteElsCmd;

       byte[] allcmdb=StringUtility.hexString2Bytes(allcmd);

//       int[] allcmdints=new int[allcmdb.length];
//      for(int i=0;i<allcmdb.length;i++)
//      {
//          allcmdints[i]=(int)allcmdb[i];
//      }
//      String crc=CRC.crc16(allcmdints);
        int calcCrc16 = CRC.calcCrc16(allcmdb);
        String crc = String.format("%04x", calcCrc16).toUpperCase();
        Log.e("LockUtil","crc="+crc);
        byte[] crcb = crc.getBytes();
        String crchex = StringUtility.bytes2HexString(crcb);
        String cmddata = cmdstart + CloseWriteElsCmdHex + crchex + "03";
        Log.e("LockUtil","cmddata="+cmddata);
      return  sendAndRs(cmddata,20000);
    }
    public String sendAndRs(String hexcmd, final long timeout)
    {

        if (StringUtility.isEmpty(hexcmd)) {
            Log.e("LockUtil","串口命令为空，不发送");
            return null;
        }
        byte[] sendBytes=null;
        sendBytes= StringUtility.hexString2Bytes(hexcmd);
        Log.e("LockUtil","串口命令发送:"+hexcmd);
        mInstance.send(sendBytes);

        long startTime=new Date().getTime();
        byte[] reBytes = new byte[128];

        long endTime=new Date().getTime();
        while((endTime-startTime)<=timeout)//如果未超时
        {
            Log.e("LockUtil", "开始读取!");
            reBytes = mInstance.receive();
            if (reBytes == null || reBytes.length == 0) {
                endTime=new Date().getTime();
                continue;
            }
            return StringUtility.bytes2HexString(reBytes, reBytes.length);
        }
        return null;//超时返回Null
    }
    public boolean send(String hexcmd)
        {
            if (StringUtility.isEmpty(hexcmd)) {
                Log.e("LockUtil","串口命令为空，不发送");
                return false;
            }
            byte[] sendBytes=null;
            sendBytes= StringUtility.hexString2Bytes(hexcmd);

            if (mInstance.send(sendBytes))
            {
                return true;
            }
            return false;
    }
    public boolean close()
    {
        threadStop = true;
        if(isOpened)
        {
            if(mInstance.closeSerail())
            {
                Log.e("test","开关锁串口关闭:成功");
            }else
            {
                Log.e("test","开关锁串口关闭:失败");
            }
        }
        return false;
    }
    class ReceiveThread extends Thread {
        byte[] data;
        Message msg;
        @Override
        public void run() {
            do {
                data = mInstance.receive();
                msg = new Message();
                if (data == null || data.length == 0) {
                    continue;
                } else {
                    msg.what = 11;
                    msg.obj = StringUtility.bytes2HexString(data, data.length);
                }
            //    mhandler.sendMessage(msg);
            } while (!threadStop);
        }
    }

    public boolean onlyopLock(Handler mHandler, String operatorstr, String auditorstr, int num, boolean lock)
    {
        openCOM();
        String rs = readEpc();
        String hexepc="";
        sendLockMes(mHandler, "开始执行关锁*******",LOCK_LOG_WHAT);
        if(rs==null)
        {
            // Log.e(TAG,"超时未读取到结果");
            sendLockMes(mHandler, "未读取到电子签封",LOCK_WHAT);
            close();
            return false;
        }


        if(rs.startsWith("0200")&&rs.endsWith("03"))//有线读取EPC消息
        {
            String epcdata=rs.substring(12,60);
            Log.e(TAG,"epcdata="+epcdata);
            byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
            hexepc=new String(epcbyte);
            Log.e(TAG,"hexepc="+hexepc);
            TagEpcData tagEpcData = EpcReader.readEpc(hexepc);
            Log.e(TAG,"包号:"+tagEpcData.getTagid()+" 锁状态:"+tagEpcData.getLockstuts()+ " 工作状态:"+tagEpcData.getJobstuts());
//            if(tagEpcData.getLockstuts().equals("Lock"))
//            {
//                sendLockMes(mHandler, "签封状态为关锁，关锁失败",LOCK_WHAT);
//                close();
//                return false;
//            }
//            if(!checkCMDList(tagEpcData.getTagid(),tagidlist))
//            {
//                close();
//                sendLockMes(mHandler, "签封"+tagEpcData.getTagid()+"不在任务列表",LOCK_WHAT);
//                return false;
//            }
        }
        close();
        //psamTool =new PsamTool();
//        if(!psamTool.pasmInit())
//        {
//            sendLockMes(mHandler,"PSAM卡上电失败",LOCK_LOG_WHAT);
//            return  false;
//        }
        sendLockMes(mHandler,"1、重置psam........................",LOCK_LOG_WHAT);
        String resetRs=psamTool.reSet();
        if(resetRs==null)
        {
            sendLockMes(mHandler, "未读取到上电的标签",LOCK_WHAT);
            psamTool.close();
            return false;
        }
        sendLockMes(mHandler, "2、开始操作PSAM获命令",LOCK_LOG_WHAT);
        sendLockMes(mHandler, "resetRs="+resetRs,LOCK_LOG_WHAT);
        // Log.e(TAG,"2、获取psam信息.....................");
        // Log.e(TAG,"执行命令:"+"8010010015");
        // String infoRs= psamTool.getPSAMinfo();
        // Log.e(TAG,"getinfoRs="+infoRs);
        sendLockMes(mHandler, "3、psam用户登录",LOCK_LOG_WHAT);
        if(!psamTool.verifyUser())
        {
            sendLockMes(mHandler, "用户登录失败",LOCK_WHAT);
            return false;
        }
        sendLockMes(mHandler, "psam用户登录成功",LOCK_LOG_WHAT);
        Log.e(TAG,"7、获取关锁操作指令.....................");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());
        byte[] operator1strbytes = StringUtility.hexString2Bytes(operatorstr);
        byte[] operator2strbytes = StringUtility.hexString2Bytes(auditorstr);
        String genCloseWriteElsCmd="";
        if(lock)
        {
            genCloseWriteElsCmd = psamTool.genCloseWriteElsCmd(hexepc, num, operator1strbytes, operator2strbytes, DateTimeStr);
        }else
        {
            genCloseWriteElsCmd = psamTool.genOpenWriteElsCmd(hexepc, num, operator1strbytes, operator2strbytes, DateTimeStr);
        }

        Log.e(TAG,"genCloseWriteElsCmd="+genCloseWriteElsCmd);
//        if(psamTool.close())
//        {
//            Log.e(TAG,"psamTool.close成功");
//            sendLockMes(mHandler, "psamTool.close成功",LOCK_LOG_WHAT);
//        }
        openCOM();
        String closers=CloseWriteEls(genCloseWriteElsCmd.substring(0,genCloseWriteElsCmd.length()-4));
        Log.e(TAG,"CloseCmdRs="+closers);
        if(closers==null)
        {
            close();
            return false;
        }
        if(closers.startsWith("0200"))//开关锁成功
        {
            close();
            return true;
        }else
        {
            close();
            return false;
        }
    }
    public boolean closeLock(Handler mHandler, List<String> tagidlist,String operator1str,String operator2str,int num)
    {
        TagEpcData tagEpcData=null;
      //  long startTime = System.currentTimeMillis();
        if(!openCOM())
        {
            sendLockMes(mHandler, "打开串口失败，请检查连接线",LOCK_WHAT);
            close();
            return false;
        }
      //  long endTime = System.currentTimeMillis();
     //   sendLockMes(mHandler,"串口初始化用时:"+((float) (endTime - startTime) / 1000) + "秒",LOCK_LOG_WHAT);

        String rs = readEpc();
        String hexepc="";
      //  sendLockMes(mHandler, "开始执行关锁*******",LOCK_LOG_WHAT);
        if(rs==null)
        {
           // Log.e(TAG,"超时未读取到结果");
            sendLockMes(mHandler, "未读取到电子签封",LOCK_WHAT);
            close();
            return false;
        }
        if(rs.startsWith("0200")&&rs.endsWith("03"))//有线读取EPC消息
        {
            String epcdata=rs.substring(12,60);
            Log.e(TAG,"epcdata="+epcdata);
            byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
            hexepc=new String(epcbyte);
            Log.e(TAG,"hexepc="+hexepc);
            tagEpcData = EpcReader.readEpc(hexepc);
            if(tagEpcData==null)
            {
                sendLockMes(mHandler, "电子签封非法",LOCK_WHAT);
                close();
                return false;
            }
//            if(tagEpcData.getLockstuts().equals("Lock"))
//            {
//                sendLockMes(mHandler, "签封状态为关锁，关锁失败",LOCK_WHAT);
//                close();
//                return false;
//            }
            Log.e(TAG,"包号:"+tagEpcData.getTagid());
            if(!checkCMDList(tagEpcData.getTagid(),tagidlist))
            {
                close();
                sendLockMes(mHandler, "签封"+tagEpcData.getTagid()+"不在任务列表",LOCK_WHAT);
                return false;
            }
        }
        close();
     //   startTime = System.currentTimeMillis();
//        psamTool =new PsamTool();
//        if(!psamTool.pasmInit())
//        {
//            sendLockMes(mHandler,"PSAM卡上电失败",LOCK_LOG_WHAT);
//            return  false;
//        }
//        sendLockMes(mHandler,"1、重置psam........................",LOCK_LOG_WHAT);
//        String resetRs=psamTool.reSet();
//        if(resetRs==null)
//        {
//            sendLockMes(mHandler, "未读取到上电的标签",LOCK_WHAT);
//            psamTool.close();
//            return false;
//        }
      //  endTime = System.currentTimeMillis();
      //  sendLockMes(mHandler,"PSAM初始化用时:"+((float) (endTime - startTime) / 1000) + "秒",LOCK_LOG_WHAT);

        sendLockMes(mHandler, "2、开始操作PSAM获命令",LOCK_LOG_WHAT);

       // Log.e(TAG,"2、获取psam信息.....................");
        // Log.e(TAG,"执行命令:"+"8010010015");
       // String infoRs= psamTool.getPSAMinfo();
       // Log.e(TAG,"getinfoRs="+infoRs);
        sendLockMes(mHandler, "3、psam用户登录",LOCK_LOG_WHAT);
        if(!psamTool.verifyUser())
        {
            sendLockMes(mHandler, "用户登录失败",LOCK_WHAT);
            return false;
        }
        sendLockMes(mHandler, "psam用户登录成功",LOCK_LOG_WHAT);
        Log.e(TAG,"7、获取关锁操作指令.....................");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());
        byte[] operator1strbytes= HexUtil.intToBytes(Integer.parseInt(operator1str));

        byte[] operator2strbytes= HexUtil.intToBytes(Integer.parseInt(operator2str));
        String genCloseWriteElsCmd = psamTool.genCloseWriteElsCmd(hexepc, num, operator1strbytes, operator2strbytes, DateTimeStr);
        Log.e(TAG,"genCloseWriteElsCmd="+genCloseWriteElsCmd);
//        if(psamTool.close())
//        {
//            Log.e(TAG,"psamTool.close成功");
//            sendLockMes(mHandler, "psamTool.close成功",LOCK_LOG_WHAT);
//        }
        openCOM();
        String closers=CloseWriteEls(genCloseWriteElsCmd.substring(0,genCloseWriteElsCmd.length()-4));
        Log.e(TAG,"CloseCmdRs="+closers);
        if(closers==null)
        {
            sendLockMes(mHandler, "读取到操作结果：操作失败",LOCK_WHAT);
            close();
            return false;
        }
        if(closers.startsWith("0200"))//开关锁成功
        {
            sendLockMes(mHandler, tagEpcData.getTagid()+":锁状态正常:true",LOCK_WHAT);
            close();
            return true;
        }else
        {
            sendLockMes(mHandler, "读取到操作结果：操作失败",LOCK_WHAT);
            close();
            return false;
        }
//        if(closers!=null)
//        {
//            rs = readEpc();
//            if(rs.startsWith("0200")&&rs.endsWith("03"))//有线读取EPC消息
//            {
//                Log.e(TAG,"epcdatars="+rs);
//                String epcdata=rs.substring(12,60);
//                Log.e(TAG,"epcdata="+epcdata);
//                byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
//                hexepc=new String(epcbyte);
//                Log.e(TAG,"hexepc="+hexepc);
//                TagEpcData tagEpcData = EpcReader.readEpc(hexepc);
//                Log.e(TAG,"包号:"+tagEpcData.getTagid());
//                if(tagEpcData.getLockstuts().equals("Lock"))
//                {
//                    sendLockMes(mHandler, tagEpcData.getTagid()+":锁状态正常:true",LOCK_WHAT);
//                    close();
//                    return true;
//                }
//            }
//        }

    }
    public boolean openLock(Handler mHandler, List<String> tagidlist,String operator1str,String operator2str,int num) {

        if(!openCOM())
        {
            sendLockMes(mHandler, "打开串口失败，请检查连接线",LOCK_WHAT);
            close();
            return false;
        }
        String rs = readEpc();
        String hexepc="";
      //  sendLockMes(mHandler, "开始执行开锁*******",LOCK_LOG_WHAT);
        if(rs==null)
        {
            // Log.e(TAG,"超时未读取到结果");
            sendLockMes(mHandler, "未读取到电子签封",LOCK_WHAT);
            close();
            return false;
        }
        TagEpcData tagEpcData=null;
        if(rs.startsWith("0200")&&rs.endsWith("03"))//有线读取EPC消息
        {
            String epcdata=rs.substring(12,60);
            Log.e(TAG,"epcdata="+epcdata);
            byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
            hexepc=new String(epcbyte);
            Log.e(TAG,"hexepc="+hexepc);
            tagEpcData = EpcReader.readEpc(hexepc);

            if(tagEpcData==null)
            {
                sendLockMes(mHandler, "电子签封非法",LOCK_WHAT);
                close();
                return false;
            }
//            if(tagEpcData.getLockstuts().equals("unLock"))
//            {
//                sendLockMes(mHandler, "签封状态为开锁，开锁失败",LOCK_WHAT);
//                close();
//                return false;
//            }
            Log.e(TAG,"包号:"+tagEpcData.getTagid());
            if(!checkCMDList(tagEpcData.getTagid(),tagidlist))
            {
                close();
                sendLockMes(mHandler, "签封"+tagEpcData.getTagid()+"不在任务列表",LOCK_WHAT);
                return false;
            }
        }
        close();
//        psamTool =new PsamTool();
//        if(!psamTool.pasmInit())
//        {
//            sendLockMes(mHandler,"PSAM卡上电失败",LOCK_LOG_WHAT);
//            return  false;
//        }
//        sendLockMes(mHandler,"1、重置psam........................",LOCK_LOG_WHAT);
//        String resetRs=psamTool.reSet();
//        if(resetRs==null)
//        {
//            sendLockMes(mHandler, "未读取到上电的标签",LOCK_WHAT);
//            psamTool.close();
//            return false;
//        }
        sendLockMes(mHandler, "2、开始操作PSAM获命令",LOCK_LOG_WHAT);
       // sendLockMes(mHandler, "resetRs="+resetRs,LOCK_LOG_WHAT);
        // Log.e(TAG,"2、获取psam信息.....................");
        // Log.e(TAG,"执行命令:"+"8010010015");
        // String infoRs= psamTool.getPSAMinfo();
        // Log.e(TAG,"getinfoRs="+infoRs);
        sendLockMes(mHandler, "3、psam用户登录",LOCK_LOG_WHAT);
        if(!psamTool.verifyUser())
        {
            sendLockMes(mHandler, "用户登录失败",LOCK_WHAT);
            return false;
        }
        sendLockMes(mHandler, "psam用户登录成功",LOCK_LOG_WHAT);
        Log.e(TAG,"7、获取开锁操作指令.....................");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());
        byte[] operator1strbytes= HexUtil.intToBytes(Integer.parseInt(operator1str));
        byte[] operator2strbytes= HexUtil.intToBytes(Integer.parseInt(operator2str));
        String genCloseWriteElsCmd = psamTool.genOpenWriteElsCmd(hexepc, num, operator1strbytes, operator2strbytes, DateTimeStr);
        Log.e(TAG,"genCloseWriteElsCmd="+genCloseWriteElsCmd);
//        if(psamTool.close())
//        {
//            Log.e(TAG,"psamTool.close成功");
//            sendLockMes(mHandler, "psamTool.close成功",LOCK_LOG_WHAT);
//        }
        openCOM();
        String closers=openWriteEls(genCloseWriteElsCmd.substring(0,genCloseWriteElsCmd.length()-4));
        Log.e(TAG,"CloseCmdRs="+closers);
        if(closers==null)
        {
            sendLockMes(mHandler, "读取到操作结果：操作失败",LOCK_WHAT);
            close();
            return false;
        }
        if(closers.startsWith("0200"))//开关锁成功
        {
            sendLockMes(mHandler, tagEpcData.getTagid()+":锁状态正常:false",LOCK_WHAT);
            close();
            return true;
        }else
        {
            sendLockMes(mHandler, "读取到操作结果：操作失败",LOCK_WHAT);
            close();
            return false;
        }
//        if(closers!=null)
//        {
//            rs = readEpc();
//            if(rs.startsWith("0200")&&rs.endsWith("03"))//有线读取EPC消息
//            {
//                String epcdata=rs.substring(12,60);
//                Log.e(TAG,"epcdata="+epcdata);
//                byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
//                hexepc=new String(epcbyte);
//                Log.e(TAG,"hexepc="+hexepc);
//                TagEpcData tagEpcData = EpcReader.readEpc(hexepc);
//                Log.e(TAG,"包号:"+tagEpcData.getTagid());
//                if(tagEpcData.getLockstuts().equals("unLock"))
//                {
//                    sendLockMes(mHandler, tagEpcData.getTagid()+":锁状态正常:false",LOCK_WHAT);
//                    close();
//                    return true;
//                }
//            }
//        }
    }
  public void   operateLockGetrs(Handler mHandler, List<String> tagidlist, String operatorstr, String auditorstr, int num, boolean lock)
  {
      long startTime = System.currentTimeMillis();
      try {
          if (lock) {
              closeLock(mHandler, tagidlist, operatorstr, auditorstr, num);
          } else {
              openLock(mHandler, tagidlist, operatorstr, auditorstr, num);
          }
      }catch (Exception e)
      {
          sendLockMes(mHandler, "锁异常，请检查签封锁:"+e.getMessage(),LOCK_WHAT);
      }
      long endTime = System.currentTimeMillis();
      sendLockMes(mHandler,"用时:"+((float) (endTime - startTime) / 1000) + "秒",LOCK_LOG_WHAT);
  }
    private boolean checkCMDList(long tagid,List<String> tagidlist)
    {
        for(String tagidstr:tagidlist)
        {
            if(tagid==Long.parseLong(tagidstr))
            {
                return true;
            }
        }
        return false;
    }
    private void sendLockMes(android.os.Handler mHandler, String mes,int what) {
        // mylog.Write（mes);
        Message message = new Message();
        message.what = what;
        Bundle data = new Bundle();
        data.putString("lockmessage", mes);
        message.setData(data);
        mHandler.sendMessage(message);
    }
    public static enum LOCK_ERR {
        LOCK_OK(0),
        LOCK_NOEPC_ERR(1),
        LOCK_PSAM_ERR(2),
        LOCK_READUSER_ERR(3),
        LOCK_WRITEUSER_ERR(4),
        LOCK_LOCKRRS_ERR(5),
        LOCK_NOCMD_ERR(6);
        private int value = 0;

        private LOCK_ERR(int value) {
            this.value = value;
        }

        public static LOCK_ERR valueOf(int value) {
            switch (value) {
                case 0:
                    return LOCK_OK;
                case 1:
                    return LOCK_NOEPC_ERR;
                case 2:
                    return LOCK_PSAM_ERR;
                case 3:
                    return LOCK_READUSER_ERR;
                case 4:
                    return LOCK_WRITEUSER_ERR;
                case 5:
                    return LOCK_LOCKRRS_ERR;
                case 6:
                    return LOCK_NOCMD_ERR;
                default:
                    return LOCK_LOCKRRS_ERR;
            }
        }
        public int value() {
            return this.value;
        }
    }
}
