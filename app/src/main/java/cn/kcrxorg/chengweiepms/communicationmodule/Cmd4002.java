package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import com.BRMicro.Tools;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;


public class Cmd4002 extends BaseCmd {

    public Cmd4002(BaseCmd baseCmd, String skey,String mkey) throws Exception {
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
            setError(new String(Tools.HexString2Bytes(Payloads.substring(8)),"UTF-8"));
        }else if(MyApp.handlertype==1)
        {
            String Payloads= DESHelper.decryptStr(mPayloads,skey);
            Log.e("kcrx","解密后数据"+Payloads);
            setError(new String(Tools.HexString2Bytes(Payloads.substring(8)),"UTF-8"));
        }else
        {
            throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
        }
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    String error;
}
