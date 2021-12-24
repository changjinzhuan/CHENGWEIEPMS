package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;


public class Cmd8002 extends BaseCmd {
    String Chandler;
    String CAPDU;
    String Rhandler;
    public Cmd8002(BaseCmd baseCmd, String skey,String mkey) throws Exception {
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
              String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
              Log.e("kcrx","解密后数据"+Payloads);
              baseCmd.setPayloads(Payloads);
          }else if(MyApp.handlertype==1)
          {
              String Payloads= DESHelper.decryptStr(mPayloads,skey);
              Log.e("kcrx","解密后数据"+Payloads);
              baseCmd.setPayloads(Payloads);
          }else
          {
              throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
          }

      }
        setChandler(baseCmd.getPayloads().substring(0,4));
        String apdulenstr=baseCmd.getPayloads().substring(4,12);
        int apduLen=Integer.parseInt(apdulenstr,16);
        int apdustart=4+8;
        int apduend=baseCmd.getPayloads().length()-4;
        setCAPDU(baseCmd.getPayloads().substring(apdustart,apduend));
        setRhandler(baseCmd.getPayloads().substring(baseCmd.getPayloads().length()-4));
    }


    public String getChandler() {
        return Chandler;
    }

    public void setChandler(String chandler) {
        Chandler = chandler;
    }

    public String getCAPDU() {
        return CAPDU;
    }

    public void setCAPDU(String CAPDU) {
        this.CAPDU = CAPDU;
    }

    public String getRhandler() {
        return Rhandler;
    }

    public void setRhandler(String rhandler) {
        Rhandler = rhandler;
    }


}
