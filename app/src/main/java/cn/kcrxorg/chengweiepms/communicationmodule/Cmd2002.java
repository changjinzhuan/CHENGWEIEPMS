package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;


public class Cmd2002 extends BaseCmd {
    String id;
    String timestamp;
    String data;

    public Cmd2002(BaseCmd baseCmd, String skey,String mkey)throws Exception
    {
        if(baseCmd.getLength().startsWith("7"))
        {
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
                setId("0"+baseCmd.getPayloads().substring(1,8));
                setTimestamp(baseCmd.getPayloads().substring(8,24));
                setData(baseCmd.getPayloads().substring(24));
            }else if(MyApp.handlertype==1)
            {
                String Payloads= DESHelper.decryptStr(mPayloads,skey);
                Log.e("kcrx","解密后数据"+Payloads);
                baseCmd.setPayloads(Payloads);
                setId("0"+baseCmd.getPayloads().substring(1,8));
                setTimestamp(baseCmd.getPayloads().substring(8,24));
                setData(baseCmd.getPayloads().substring(24));
            }else
            {
                throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
            }
        }else
        {
            setId("0"+baseCmd.getPayloads().substring(1,8));
            setTimestamp(baseCmd.getPayloads().substring(8,24));
            setData(baseCmd.getPayloads().substring(24));
        }

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
