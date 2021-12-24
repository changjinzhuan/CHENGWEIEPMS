package cn.kcrxorg.chengweiepms.locktools;

import android.util.Log;

import com.rscja.deviceapi.Module;
import com.rscja.utility.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.security.auth.callback.Callback;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.psamtools.PsamTool;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;

public class LockUtilRx {
    private final String TAG="LockUtilRx";
    private Module mInstance;
    public Module getmInstance() {
        return mInstance;
    }
    PsamTool psamTool;

    public LockUtilRx() throws Exception{
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
    }

    public boolean openCOM()
    {
        boolean flag=false;
        mInstance.powerOn(4);
        if(mInstance!=null&&mInstance.openSerail("/dev/ttyHSL1",115200,8,1,0))
        {
            flag=true;
        }else
        {
            Log.e("LockUtil","串口打开失败!");
        }
        return  flag;
    }
    public boolean closeCOM()
    {
            if(mInstance.closeSerail())
            {
                Log.e("test","开关锁串口关闭:成功");
                return true;
            }else
            {
                Log.e("test","开关锁串口关闭:失败");
            }

        return false;
    }
    public String readEpc()
    {
        return   sendAndRs("0204303030343030373303",5000);
    }

    public String sendAndRs(String hexcmd, final long timeout)
    {
        if (StringUtility.isEmpty(hexcmd)) {
            Log.e("LockUtil","串口命令为空，不发送");
            return "";
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
        return "";//超时返回Null
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

        int calcCrc16 = CRC.calcCrc16(allcmdb);
        String crc = String.format("%04x", calcCrc16).toUpperCase();
        Log.e("LockUtil","crc="+crc);
        byte[] crcb = crc.getBytes();
        String crchex = StringUtility.bytes2HexString(crcb);
        String cmddata = cmdstart + CloseWriteElsCmdHex + crchex + "03";
        Log.e("LockUtil","cmddata="+cmddata);
        return  sendAndRs(cmddata,20000);
    }


    public Observable<LockResult> operateLockGetrs(List<String> tagidlist,String operatorstr, String auditorstr, int num, boolean lock)
    {
       return Observable.create(emitter -> {
           TagEpcData tagEpcData=null;
           String hexepc=null;
           try
           {
               if(!openCOM())
               {
                   emitter.onError(new Exception("串口打开失败"));
               }
               String rs="";
               rs=readEpc();
               if(rs==null||rs.isEmpty())
               {
                   closeCOM();
                   rs="";
                   emitter.onError(new Exception("未读取到电子签封"));
                   return;
               }
               if(rs.startsWith("0200")&&rs.endsWith("03"))//有线读取EPC消息
               {
                   String epcdata=rs.substring(12,60);
                   Log.e(TAG,"epcdata="+epcdata);
                   byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
                   hexepc=new String(epcbyte);
                   Log.e(TAG,"hexepc="+hexepc);
                   tagEpcData = EpcReader.readEpc(hexepc);
                   Log.e(TAG,"包号:"+tagEpcData.getTagid()+" 锁状态:"+tagEpcData.getLockstuts()+ " 工作状态:"+tagEpcData.getJobstuts());
                   if(!checkCMDList(tagEpcData.getTagid(),tagidlist))
                   {
                       closeCOM();
                       emitter.onError(new Exception("签封锁"+tagEpcData.getTagid()+"不在任务列表"));
                   }
                   psamTool =new PsamTool();
                   if(!psamTool.pasmInit())
                   {
                       emitter.onError(new Exception("Psam卡上电失败"));
                   }
                   String resetRs=psamTool.reSet();
                   if(resetRs==null)
                   {
                       emitter.onError(new Exception("Psam卡reset失败"));
                       psamTool.close();
                   }
                   if(!psamTool.verifyUser())
                   {
                       emitter.onError(new Exception("Psam卡登录失败"));
                   }
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
                   if(!psamTool.close())
                   {
                       emitter.onError(new Exception("Psam卡关闭失败"));
                   }
                   openCOM();
                   String closers=CloseWriteEls(genCloseWriteElsCmd.substring(0,genCloseWriteElsCmd.length()-4));
                   Log.e(TAG,"CloseCmdRs="+closers);
                   if(closers==null)
                   {
                       emitter.onError(new Exception("读取到操作结果：操作失败"));
                   }
                   if(closers.startsWith("0200"))//开关锁成功
                   {
                       emitter.onNext(new LockResult(tagEpcData.getTagid()+":锁状态正常:true","0"));
                       emitter.onComplete();
                   }else
                   {
                       emitter.onError(new Exception("读取到操作结果：操作失败"));
                   }
               }else
               {
                   emitter.onError(new Exception("读取到电子签封非法:"+rs));
               }
           }catch (Exception e)
           {
               emitter.onError(e);
           }
       });
    }
    private boolean checkCMDList(long tagid, List<String> tagidlist)
    {
        if(tagidlist.size()==1){
            if(tagidlist.get(0).equals("0000000000")) return true;
        }
        for(String tagidstr:tagidlist)
        {
            if(tagid==Long.parseLong(tagidstr))
            {
                return true;
            }
        }
        return false;
    }
    public class LockResult
    {
        String msg;
        String code;
        public  LockResult(String msg, String code){
            this.msg=msg;
            this.code=code;
        }
        public String getMsg() {
            return msg;
        }

        public String getCode() {
            return code;
        }
    }

}
