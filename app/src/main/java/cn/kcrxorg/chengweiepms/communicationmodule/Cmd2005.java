package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;


public class Cmd2005 extends BaseCmd {


    public Cmd2005(BaseCmd baseCmd,String skey,String mkey) throws Exception {

        String mPayloads=baseCmd.getPayloads();
        Log.e("kcrx","当前密文格式为"+ MyApp.handlertype);
        if(MyApp.handlertype==2)
        {
            if(!DESHelper.checkMAC(mPayloads,mkey))
            {
                throw new Exception("密文MAC校验失败...");
            }
            String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
            Log.e("kcrx","解密后数据"+Payloads);
            baseCmd.setPayloads(Payloads);
            int userdatalen=Integer.parseInt(baseCmd.getPayloads().substring(0,8),16);
            setMetadata(baseCmd.getPayloads().substring(8,8+userdatalen*2));
            setMetamark(baseCmd.getPayloads().substring(8+userdatalen*2));
        }else if(MyApp.handlertype==1)
        {
            String Payloads= DESHelper.decryptStr(mPayloads,skey);
            Log.e("kcrx","解密后数据"+Payloads);
            baseCmd.setPayloads(Payloads);
            int userdatalen=Integer.parseInt(baseCmd.getPayloads().substring(0,8),16);
            setMetadata(baseCmd.getPayloads().substring(8,8+userdatalen*2));
            setMetamark(baseCmd.getPayloads().substring(8+userdatalen*2));
        }else
        {
            throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
        }
    }

    String metadata;
    String metamark;

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getMetamark() {
        return metamark;
    }

    public void setMetamark(String metamark) {
        this.metamark = metamark;
    }
}
