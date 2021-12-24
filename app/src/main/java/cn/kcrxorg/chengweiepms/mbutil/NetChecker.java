package cn.kcrxorg.chengweiepms.mbutil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NetChecker extends Thread {
    private boolean isstart=false;
    private boolean isconnect=false;
    private Handler mHandler;
    MyLog myLog;
    public NetChecker(Handler handler,MyLog log)
    {
        mHandler=handler;
        myLog=log;
    }
    public void pause()
    {
        isstart=false;
    }
    @Override
    public void run() {
        isstart=true;
        while(isstart)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean nowstate=isWirePluggedIn();
            if(nowstate==true)
            {
                delay(3000);
                boolean laststate=isWirePluggedIn();
                if(nowstate!=laststate)
                {
                    myLog.Write("网络状态第一次检查:"+nowstate+"\r\n网络状态第二次检查:"+laststate+"\r\n网络状态不稳定，不上报...");
                    continue;
                }
            }
            if(nowstate!=isconnect)
            {
                isconnect=nowstate;

                Message msg = new Message();
                msg.what = 100;//网络消息
                Bundle b = new Bundle();
                b.putBoolean("netstate",isWirePluggedIn());
                msg.setData(b);
                if(isFastSennd())
                {
                    myLog.Write("发送网络状态太快了，过滤....");
                    continue;
                }
                if(isstart)
                {
                    mHandler.sendMessage(msg);
                }

            }

        }
    }
    private static final int MIN_CLICK_DELAY_TIME =10000;//10秒内只报一次网络状态
    private static long lastClickTime;
    public static boolean isFastSennd() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) <=MIN_CLICK_DELAY_TIME) {
         Log.e("test","curClickTime - lastClickTime="+(curClickTime - lastClickTime));
            flag = true;
        }
        lastClickTime = curClickTime;
        Log.e("test","return:"+flag);
        return flag;
    }
    public void delay(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public String execCommand(String command) {
        Runtime runtime;
        Process proc = null;
        StringBuffer stringBuffer = null;
        try {
            runtime = Runtime.getRuntime();
            proc = runtime.exec(command);
            stringBuffer = new StringBuffer();
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));

            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }

        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }
        return stringBuffer.toString();
    }
    //判断网线拔插状态
    //通过命令cat /sys/class/net/eth0/carrier，如果插有网线的话，读取到的值是1，否则为0
    public boolean isWirePluggedIn(){
        //String state= execCommand("cat /sys/class/net/wlan0/carrier");
        String state= execCommand("cat /sys/class/net/eth0/carrier");
          String wifistate=execCommand("cat /sys/class/net/wlan0/carrier");
        //  if(state.trim().equals("1")||wifistate.trim().equals("1")){  //有网线插入时返回1，拔出时返回0
        if(state.trim().equals("1")||wifistate.trim().equals("1")){
            return true;
        }
        return false;
    }
}
