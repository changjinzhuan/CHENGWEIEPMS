package cn.kcrxorg.chengweiepms.communicationmodule;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;


public class Cmd1005 extends BaseCmd {

public Cmd1005(BaseCmd baseCmd,String skey,String mkey)throws Exception
{
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
            setSession(baseCmd.getPayloads().substring(0,2));
            Log.e("kcrx","UTC时间为："+Long.parseLong(baseCmd.getPayloads().substring(2),16));
            setDatetime(Long.parseLong(baseCmd.getPayloads().substring(2),16));
        }else if(MyApp.handlertype==1)
        {
            String Payloads= DESHelper.decryptStr(mPayloads,skey);
            Log.e("kcrx","解密后数据"+Payloads);
            baseCmd.setPayloads(Payloads);
            setSession(baseCmd.getPayloads().substring(0,2));
            Log.e("kcrx","UTC时间为："+Long.parseLong(baseCmd.getPayloads().substring(2),16));
            setDatetime(Long.parseLong(baseCmd.getPayloads().substring(2),16));
        }else
        {
            throw new Exception("密文格式未知异常,handlertype=0"+MyApp.handlertype);
        }
    }else
    {
        setSession(baseCmd.getPayloads().substring(0,2));
        setDatetime(Long.parseLong(baseCmd.getPayloads().substring(2),16));
    }
}

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Long getDatetime() {
        return datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    private String session;
   private Long datetime;

    /**
     * utc时间转成local时间
     * @param utcTime
     * @return
     */
    public static Date utcToLocal(String utcTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate = null;
        try {
            utcDate = sdf.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.setTimeZone(TimeZone.getDefault());
        Date locatlDate = null;
        String localTime = sdf.format(utcDate.getTime());
        try {
            locatlDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return locatlDate;
    }
}
