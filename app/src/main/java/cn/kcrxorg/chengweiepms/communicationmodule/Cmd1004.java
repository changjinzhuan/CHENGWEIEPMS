package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import com.BRMicro.Tools;

import java.io.UnsupportedEncodingException;

import cn.kcrxorg.chengweiepms.mbutil.DESHelper;

public class Cmd1004 extends BaseCmd {


    public  Cmd1004(BaseCmd baseCmd,String skey) throws Exception {
        if(baseCmd.getLength().startsWith("7"))
        {
            String mPayloads=baseCmd.getPayloads();
            String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
            Log.e("kcrx","解密后数据"+Payloads);
            baseCmd.setPayloads(Payloads);
            byte[] messagesb= Tools.HexString2Bytes(baseCmd.getPayloads().substring(2));
            String messagestr=new String(messagesb,"utf-8");
            setMessage(messagestr);
            if(Payloads.equals("00000000"))
            {
                setMessage("");
            }
        }else
        {
            byte[] messagesb= Tools.HexString2Bytes(baseCmd.getPayloads().substring(2));
            String messagestr=new String(messagesb,"utf-8");
            setMessage(messagestr);
            if(baseCmd.getPayloads().equals("00000000"))
            {
                setMessage("");
            }
        }

    }
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
