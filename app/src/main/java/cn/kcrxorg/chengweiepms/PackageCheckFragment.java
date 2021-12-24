package cn.kcrxorg.chengweiepms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.warkiz.widget.IndicatorSeekBar;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import cn.kcrxorg.chengweiepms.adapter.TagEpcDataAdapter;
import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.mbutil.MyLog;
import cn.kcrxorg.chengweiepms.mbutil.XToastUtils;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;

import static android.content.Context.MODE_PRIVATE;

@Page(name = "包装袋状态核查",anim = CoreAnim.none)
public class PackageCheckFragment extends XPageFragment {
    public RFIDWithUHFUART mReader;//读头
    public List<String> allcarddata;

    @BindView(R.id.rec_invertory)
    RecyclerView lv_invertory;

    public MyLog mylog;

    public Handler mHandler;//消息中心
    public static final int uhfwhat=1;//标签消息
    public static final int INFO_MES = 0;//其他消息
    public static final int LOCK_WHAT=2;//操作锁结果
    public static final int LOCK_LOG_WHAT=3;//日志消息

    //功率配置
    private IndicatorSeekBar sbpower;//功率设置
    public SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Context context;

    TagEpcDataAdapter tagEpcDataAdapter;
    List<TagEpcData> tagEpcDataList;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_package_check;
    }

    @Override
    protected void initViews() {
        initDev();
        allcarddata=new ArrayList<String>();
        tagEpcDataList = new ArrayList<TagEpcData>();
        tagEpcDataAdapter=new TagEpcDataAdapter(getContext(),tagEpcDataList);
        lv_invertory.setLayoutManager(new LinearLayoutManager(getContext()));
        lv_invertory.setAdapter(tagEpcDataAdapter);

        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what)
                {
                    case 1:
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//重复过滤
                        {
                            break;
                        }
                        allcarddata.add(tagmessage);
                        mylog.Write("读取到芯片EPC="+tagmessage);
                        TagEpcData tagEpcData= EpcReader.readEpc(tagmessage);
                        if(tagEpcData==null)
                        {
                            Util.playErr();
                            break;
                        }
                        if(tagEpcData!=null&&tagEpcData.getTagid()>1000000000)//如果可以读取未报错
                        {
                            mylog.Write("读取到本系统标签epc="+tagmessage+" 袋号:"+tagEpcData.getTagid()+"锁状态:"+tagEpcData.getLockstuts());
                            Util.playOk();
                            tagEpcDataList.add(tagEpcData);
                            tagEpcDataAdapter.notifyDataSetChanged();
                        }

                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    protected void initListeners() {

    }
    private void initDev()
    {
        context=getContext();
        //初始化声音
        Util.initSoundPool(context);
      //  initUHF();
        mylog=new MyLog(context,10000,1);
        //mylog.Write(this.getClass()+"业务启动！*****************************");

        //注册按钮
        //注册按键广播
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.rfid.FUN_KEY");

        mSharedPreferences = getActivity().getSharedPreferences("UHF",MODE_PRIVATE);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mylog.Write("按钮按下keyCode="+keyCode);
        switch (keyCode){
            case 139://读取扳机
                readCard();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            mylog.Write(this.getClass() + "启动失败:" + ex.getMessage());
            XToastUtils.error("天线初始化失败");
            mylog.Write("天线初始化失败");
            return;
        }
        if (mReader != null) {
            new InitTask().execute();
        }
    }

    /**
     * �豸�ϵ��첽��
     *
     * @author liuruifeng
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            // return true;
            return mReader.init();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (!result) {
                XToastUtils.error("天线初始化失败");
                mylog.Write("天线初始化失败");
            } else {
                int nowpower=mSharedPreferences.getInt("readPower",18);
                mReader.setPower(nowpower);
                XToastUtils.success("天线初始化成功");
                mylog.Write("天线初始化成功");
                //  addRsinfo("天线初始化",false);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(context);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在初始化...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }


    public void readCard() {
        //扫描线程启动...
        new ReadTagTask().execute();
    }
    public void stopInventory()//停止扫描
    {
        if (mReader.stopInventory()) {
            mylog.Write("识别已停止");
        }
    }
    boolean isinventoryFlag=false;
    public class ReadTagTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mypDialog = new ProgressDialog(context);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在读取请稍后...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mypDialog.cancel();
            if (mReader != null)
                mReader.free();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mReader = RFIDWithUHFUART.getInstance();
            } catch (ConfigurationException e) {
                mylog.Write("mReader.getInstance().error=" + e.toString());
                e.printStackTrace();
            }
            mylog.Write("mReader.init()=" + mReader.init());
            int nowpower = mSharedPreferences.getInt("readPower", 15);
            mReader.setPower(nowpower);
            mylog.Write("UHF开始扫描....启动功率为:" + nowpower);
            if (!mReader.startInventoryTag()) {
                mylog.Write("UHF开始扫描失败");
                mReader.stopInventory();
                return false;
            }
            mylog.Write("读取线程已经启动...");
            boolean hastag = false;
            if (isinventoryFlag) {
                mylog.Write("正在读取,请勿重复按扫描键");
                return false;
            }
            isinventoryFlag = true;
            String strEPC;
            UHFTAGInfo res = null;
            int times = 0;
            while (times < 100) {
                delay(10);
                times++;
                res = mReader.readTagFromBuffer();
                if (res != null) {
                    strEPC = res.getEPC();
                    mylog.Write("扫描到标签号:" + strEPC);
                    Message message = new Message();
                    message.what = 1;
                    Bundle data = new Bundle();
                    data.putString("tagmessage", strEPC);
                    message.setData(data);
                    mHandler.sendMessage(message);
                    hastag = true;
                }
            }
            if (hastag == false) {
                Message message = new Message();
                message.what = 0;
                Bundle data = new Bundle();
                data.putString("message", "notag");
                message.setData(data);
                mHandler.sendMessage(message);
            }
            isinventoryFlag = false;
            stopInventory();
            return true;
        }

    }

    public void delay(int delay)
    {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public  Boolean checkrepeat(String nowcarddata)
    {
        for (String s : allcarddata)
        {
            if (s == nowcarddata || s.equals(nowcarddata))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReader.isPowerOn())
        {
            mylog.Write("UHF已关闭");
            mReader.free();
        }
    }
}
