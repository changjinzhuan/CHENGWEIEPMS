package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;


public class Cmd2004 extends BaseCmd {
       String id;//已经存储的控制数据id
       String fragment;
       String complete;

    public Cmd2004(BaseCmd baseCmd,String skey,String mkey) throws Exception {

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
                setId(Payloads.substring(0,8));
                setFragment(Payloads.substring(8,16));
                setComplete(Payloads.substring(16));
            }else if(MyApp.handlertype==1)
            {
                String Payloads= DESHelper.decryptStr(mPayloads,skey);
                Log.e("kcrx","解密后数据"+Payloads);
                baseCmd.setPayloads(Payloads);
                setId(Payloads.substring(0,8));
                setFragment(Payloads.substring(8,16));
                setComplete(Payloads.substring(16));
            }else
            {
                throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
            }
        }else
        {

            String mPayloads=baseCmd.getPayloads();
            Log.e("kcrx","不加密包"+mPayloads);
            setId(mPayloads.substring(0,8));
            setFragment(mPayloads.substring(8,16));
            setComplete(mPayloads.substring(16));
        }


    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getComplete() {
        return complete;
    }

    public void setComplete(String complete) {
        this.complete = complete;
    }
}
