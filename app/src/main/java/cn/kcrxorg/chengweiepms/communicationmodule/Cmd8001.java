package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;

public class Cmd8001 extends BaseCmd {


     public Cmd8001(BaseCmd baseCmd, String skey,String mkey) throws Exception {
         if(baseCmd.getLength().startsWith("7"))//加密包
         {
             String mPayloads=baseCmd.getPayloads();
             Log.e("kcrx","当前密文格式为"+ MyApp.handlertype);
             if(MyApp.handlertype==2)
             {
                 if(!DESHelper.checkMAC(mPayloads,mkey))
                 {
                     throw new Exception("密文MAC校验失败...");
                 }
                 String Payloads=DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
                 Log.e("kcrx","解密后数据"+Payloads);
                 baseCmd.setPayloads(Payloads);
                 setAction(baseCmd.getPayloads().substring(0,2));
                 setFlags(baseCmd.getPayloads().substring(2));
             }else if(MyApp.handlertype==1)
             {
                 String Payloads=DESHelper.decryptStr(mPayloads,skey);
                 Log.e("kcrx","解密后数据"+Payloads);
                 baseCmd.setPayloads(Payloads);
                 setAction(baseCmd.getPayloads().substring(0,2));
                 setFlags(baseCmd.getPayloads().substring(2));
             }else
             {
                 throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
             }
         }else//未加密包
         {
             setAction(baseCmd.getPayloads().substring(0,2));
             setFlags(baseCmd.getPayloads().substring(2));
         }
     }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    String action;
    String flags;


}
