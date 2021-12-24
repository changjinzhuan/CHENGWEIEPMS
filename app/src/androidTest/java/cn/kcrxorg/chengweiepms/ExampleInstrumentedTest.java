package cn.kcrxorg.chengweiepms;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.psamtools.PsamTool;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private Handler handler;
    @Test
    public void useAppContext() {
        final String TAG="Test";
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("cn.kcrxorg.chengweiepms", appContext.getPackageName());

        String data="30303034";


    }

    @Test
    public void test8023()
    {
        String apducmd="8023000080DF901BA4D1971145C4EA3C32C9713A90F9E0F61250F947D351F916141815F737E43BFB4B2FF58CCAAB8E959394A4E414EDB8436A42B75A64623FC6A6E37D436569044EF264D86597C3751E545A5824982EF5956B12C3F69ED5CF024B51B8B76857E8F0DACB2708E174E1A846120C63CECC15242B549BEA9B07BEBE49599AE8FC";
        //PsamError err = new PsamError();
        PsamTool psamTool=new PsamTool();
        Log.e("PsamCmdUtil","重置PSAM="+ psamTool.reSet()) ;
        String rs= psamTool.excute(apducmd);
        Log.e("PsamCmdUtil","执行命令="+ apducmd) ;
        Log.e("PsamCmdUtil","psam return:"+rs);
    }
    @Test
    public void psamTest()
    {
        PsamTool psamTool=new PsamTool();
        for(int i=0;i<1000;i++)
        {
            boolean a,b,c=false;
            Log.e("PsamCmdUtil",i+"上电："+psamTool.pasmInit());
            delay(100);
            Log.e("PsamCmdUtil",i+"复位："+psamTool.reSet());
            Log.e("PsamCmdUtil",i+"下电："+psamTool.close());
            delay(100);
          //  delay(50);
        }
    }

    public void delay(int second)
    {
        try {
            Thread.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void EpcReaderTest()
    {
        String epcstr="481F5451114E204A09387141";
        TagEpcData ted= EpcReader.readEpc(epcstr);

        Log.e("test","tagid="+ted.getTagid());
        Log.e("test","versionid="+ted.getVersionid());
        Log.e("test","pervalueid="+ted.getPervalueid());
        Log.e("test","amount="+ted.getAmount());
        Log.e("test","randomstr="+ted.getRandom());
        Log.e("test","operatecount="+ted.getOperatecount());
        Log.e("test","checkcodestr="+ted.getCheckcode());

        //00开锁下电40开锁上电01关锁下电41关锁上电
        Log.e("test","lockstuts="+ted.getLockstuts());
        Log.e("test","lockeEx="+ted.getLockeEx());
        Log.e("test","epcEx="+ted.getEpcEx());
        Log.e("test","hasElec="+ted.getHasElec());
        Log.e("test","jobstuts="+ted.getJobstuts());
        Log.e("test","tagid="+ted.getTagid());
    }
}
