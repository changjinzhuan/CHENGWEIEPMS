package cn.kcrxorg.chengweiepms;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.BRMicro.Tools;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.deviceapi.interfaces.IUHF;

import java.util.ArrayList;
import java.util.List;

import cn.kcrxorg.chengweiepms.adapter.TraceDataAdapter;
import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.bean.TagUserdata;
import cn.kcrxorg.chengweiepms.bean.UserTraceData;
import cn.kcrxorg.chengweiepms.mbutil.HexUtil;
import cn.kcrxorg.chengweiepms.psamtools.PsamTool;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;
import cn.kcrxorg.chengweiepms.rfidtool.LockStutsHelper;
import cn.kcrxorg.chengweiepms.rfidtool.PervalueHelper;
import cn.kcrxorg.chengweiepms.rfidtool.UserReader;


public class SackTraceActivity extends BisnessBaseActivity{
    TextView tv_cmdinfo;
    ListView listViewTraceData;
    ScrollView line_scorllrsinfo;

    TraceDataAdapter traceDataAdapter ;
    UserTraceData[] userTraceDatas;
    List<UserTraceData> userTraceDataList;
    PsamTool psamTool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        tv_cmdinfo=new TextView(this);
      //  listViewTraceData=new ListView(this);
        line_scorllrsinfo=findViewById(R.id.line_scorllrsinfo);
        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
      //  tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setGravity(Gravity.CENTER);
        // tv_cmdinfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        line_businfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

//        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,150);
//        params1.weight=1;
//       // userTraceDatas=new UserTraceData[8];


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://扫描到包号
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//重复过滤
                        {
                            break;
                        }
                        allcarddata.add(tagmessage);
                        TagEpcData tagEpcData= EpcReader.readEpc(tagmessage);

                        if(tagEpcData!=null&&tagEpcData.getTagid()>1000000000)//如果可以读取未报错
                        {
                            mylog.Write("追溯读取到包号："+tagEpcData.getTagid());
                            tv_cmdinfo.setText("读取到签封："+tagEpcData.getTagid()+"\r\n"+
                                    "券别:"+ PervalueHelper.getVal(tagEpcData.getPervalueid())+"\r\n"+
                                    "锁状态:"+ LockStutsHelper.getStutsName(tagEpcData.getLockstuts()));
                            String resetRs=psamTool.reSet();
                            if(resetRs==null)
                            {
                                setLog("psam重置失败");
                                psamTool.close();
                                break;
                            }
                            if( !psamTool.verifyUser())
                            {
                                setLog("Psam登录失败...");
                                break;
                            }
                            String rfidPassword= psamTool.getRFIDPassword(tagmessage).replace("9000","");
                            if(rfidPassword==null||rfidPassword.equals(""))
                            {
                                setLog("RFID密码获取失败...");
                                break;
                            }
//                            Log.e("test","password="+rfidPassword);
//                            Log.e("test","filterbank="+IUHF.Bank_EPC);
//                            Log.e("test","filterPtr="+32);
//                            Log.e("test","filterCnt="+96);
//                            Log.e("test","filterData="+tagmessage);
//                            Log.e("test","bank="+IUHF.Bank_USER);
//                            Log.e("test","ptr="+0);
//                            Log.e("test","cnt="+96);
                            String userdata= mReader.readData(rfidPassword, IUHF.Bank_EPC,32,96,tagmessage,IUHF.Bank_USER,0,96);
                            setLog("userdata="+userdata);
                            if(mReader!=null)
                                mReader.free();
                            if(userdata==null||userdata.equals(""))
                            {
                                line_rsinfo.removeAllViews();
                                addRsinfo("未追溯到操作记录,请重试...",false);
                                break;
                            }
                            TagUserdata tagUserdata= UserReader.readTagUser(HexUtil.hexStringToBytes(userdata));
                            userTraceDatas = tagUserdata.getUserTraceData();
                            if(tagUserdata==null||userTraceDatas==null)
                            {
                                addRsinfo("未追溯到操作记录出错",false);
                                break;
                            }
                            line_rsinfo.removeAllViews();
                            for(int i=0;i<userTraceDatas.length;i++)
                            {
                                if(userTraceDatas[i].getCommandid().toUpperCase().startsWith("B"))
                                {
                                    setLog((i+1)+":操作:"+(userTraceDatas[i].getCommandid().toLowerCase().equals("b4")?"开袋":"关袋")+" 操作员:"+userTraceDatas[i].getOperator1()+" 复核员:"+userTraceDatas[i].getOperator2()+" 时间:20"+userTraceDatas[i].getOpdatetime());
                                    addRsinfo((i+1)+":操作:"+(userTraceDatas[i].getCommandid().toLowerCase().equals("b4")?"开袋":"关袋")+" 操作员:"+userTraceDatas[i].getOperator1()+" 复核员:"+userTraceDatas[i].getOperator2()+" 时间:20"+userTraceDatas[i].getOpdatetime(),true);
                                }else
                                {
                                    setLog((i+1)+":"+"操作:"+userTraceDatas[i].getCommandid());
                                    addRsinfo((i+1)+":"+"操作:"+userTraceDatas[i].getCommandid(),true);
                                }
                            }
                            Util.playOk();
                        }
                        break;
                    case INFO_MES:
                          String msgstr=msg.getData().getString("message").toString();
                        mylog.Write(msgstr);
                        if(msgstr.equals("notag"))
                        {
                            addRsinfo("未读取到标签",false);
                            Util.playErr();
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        //psam
        psamTool=new PsamTool();
        if(!psamTool.pasmInit())
        {
            sendInfoMes(mHandler,"PSAM卡上电失败",LOCK_LOG_WHAT);
        }
        mylog.Write("签封追溯界面初始化完成");
    }

    public void initView()
    {
        tv_header.setCenterString("签封追溯");
        tv_operinfo.setText("请按【扫描】进行追溯或按【取消】退出");
        tv_footer.setText("请按【扫描】进行追溯或按【取消】退出");
        line_kun.removeAllViews();//清除捆数栏，准备添加库间选择列表
    }
      //重写追溯
    public void readCard() {
//        int readpower = 15;
//        Log.e("kcrx", "开始扫描款包,读取功率：" + readpower);
//        mylog.Write("UHF开始扫描....启动功率为:" + readpower);
//        mReader.setPower(readpower);
//        if (!mReader.startInventoryTag()) {
//            mylog.Write("UHF开始扫描失败");
//            mReader.stopInventory();
//            return;
//        }
        new ReadTagTask().execute();
    }
    public void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int index = 0, len = listAdapter.getCount(); index < len; index++) {
            View listViewItem = listAdapter.getView(index , null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
    private void sendInfoMes(Handler mHandler, String mes, int what) {
        // mylog.Write（mes);
        Message message = new Message();
        message.what = 0;
        Bundle data = new Bundle();
        data.putString("message", mes);
        message.setData(data);
        mHandler.sendMessage(message);
    }

    public class ReadTagTask extends AsyncTask<String, Integer, Boolean>
    {
        ProgressDialog mypDialog;
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mypDialog.cancel();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mReader = RFIDWithUHFUART.getInstance();
            } catch (ConfigurationException e) {
                mylog.Write("mReader.getInstance().error="+e.toString());
                e.printStackTrace();
            }
            mylog.Write("mReader.init()="+mReader.init());
            // Log.e("test","读取才初始化天线...:"+  mReader.init());
            int nowpower=mSharedPreferences.getInt("readPower",15);
            mReader.setPower(nowpower);
            mylog.Write("UHF开始扫描....启动功率为:"+nowpower);
            if(!mReader.startInventoryTag())
            {
                mylog.Write("UHF开始扫描失败");
                mReader.stopInventory();
                return false;
            }
            mylog.Write("读取线程已经启动...");
            boolean hastag=false;
            if(isinventoryFlag)
            {
                addRsinfo("正在读取,请勿重复按扫描键",false);
                return false;
            }
            isinventoryFlag=true;
            String strEPC;
            UHFTAGInfo res = null;
            int times=0;
            while(times<100)
            {
                delay(10);
                times++;
                res = mReader.readTagFromBuffer();
                if (res != null)
                {
                    strEPC = res.getEPC();
                    mylog.Write("扫描到标签号:" + strEPC);
                    Message message = new Message();
                    message.what = 1;
                    Bundle data = new Bundle();
                    data.putString("tagmessage", strEPC);
                    message.setData(data);
                    mHandler.sendMessage(message);
                    hastag=true;
                }
            }
            if(hastag==false)
            {
                Message message = new Message();
                message.what = 0;
                Bundle data = new Bundle();
                data.putString("message", "notag");
                message.setData(data);
                mHandler.sendMessage(message);
            }
            isinventoryFlag=false;
            stopInventory();
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mypDialog = new ProgressDialog(SackTraceActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在读取请稍后...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
}
