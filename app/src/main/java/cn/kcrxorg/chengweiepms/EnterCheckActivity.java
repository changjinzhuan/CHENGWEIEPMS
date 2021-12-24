package cn.kcrxorg.chengweiepms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EnterCheckCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EnterCheckPackInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.EnterCheckData;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;


public class EnterCheckActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;
    EnterCheckCMD enterCheckCMD;
    EnterCheckData enterCheckData;

    List<EnterCheckPackInfo> enterCheckPackInfoList;
    Spinner sp_stackInfo;//库间选择
    String businessid = "";

    int isgood = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        allcarddata = new ArrayList<>();

        TXTReader tr = new TXTReader();
        businessid=getIntent().getStringExtra("businessid");
        String cmddata = tr.getCmdById(EnterCheckActivity.this, businessid);

        enterCheckCMD = JSONObject.parseObject(cmddata, EnterCheckCMD.class);

        enterCheckData = new EnterCheckData();
        enterCheckData.setCode(163849);
        enterCheckData.setError("");

        enterCheckPackInfoList = new ArrayList<EnterCheckPackInfo>();

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        tv_cmdinfo = new TextView(this);
        saninfo = new TextView(this);
        String cmdinfo = "任务信息:\r\n";

        cmdinfo += "待扫描" + enterCheckCMD.getPackInfoList().length + "包";

        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        // tv_cmdinfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

        saninfo.setText("已扫描:0袋");
        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);


        //初始化查看任务列表
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(EnterCheckPackInfo stockPackInfo:enterCheckCMD.getPackInfoList())
        {
            ViewCmdInfo viewCmdInfo=new ViewCmdInfo();
            viewCmdInfo.setSackNo(stockPackInfo.getSackNo());
            viewCmdInfo.setPaperTypeName(stockPackInfo.getPaperTypeName());
            viewCmdInfo.setVoucherTypeName(stockPackInfo.getVoucherTypeName());
            viewCmdInfo.setVal(stockPackInfo.getVal());
            viewCmdInfoList.add(viewCmdInfo);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1://扫描到包号
                        String tagmessage = msg.getData().getString("tagmessage");
                        if (!checkrepeat(tagmessage))//重复过滤
                        {
                            break;
                        }
                        allcarddata.add(tagmessage);
                        TagEpcData tagEpcData = EpcReader.readEpc(tagmessage);
                        if(tagEpcData==null)
                        {
                            Util.playErr();
                            break;
                        }
                        if (tagEpcData != null && tagEpcData.getTagid() > 1000000000L)//如果可以读取未报错
                        {
                            EnterCheckPackInfo thispackinfo = getPackInfo(tagEpcData.getTagid());
                            if (thispackinfo == null) {
                                addRsinfo("包号:" + tagEpcData.getTagid() + "不在任务列表", false);
                                mylog.Write("包号:" + tagEpcData.getTagid() + "不在任务列表");
                                Util.playErr();
                                break;
                            }
                            isgood++;
                            addRsinfo("核对封签:" + tagEpcData.getTagid() + "完成", true);
                            saninfo.setText("已扫描" + isgood + "袋");
                            Util.playOk();
                             setGoodViewCmdInfo( tagEpcData.getTagid()+"");
                            thispackinfo.setOprDT(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                            enterCheckPackInfoList.add(thispackinfo);
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
            }
        };
    }

    private EnterCheckPackInfo getPackInfo(long tagid) {
        for (EnterCheckPackInfo enterCheckPackInfo : enterCheckCMD.getPackInfoList()) {
            if (tagid == Long.parseLong(enterCheckPackInfo.getSackNo())) {
                return enterCheckPackInfo;
            }
        }
        return null;
    }

    public void initView() {
        tv_header.setCenterString("入库核对");
        tv_operinfo.setText("请按【扫描】进行核对或按【取消】结束任务");
        tv_footer.setText("请按【扫描】进行核对或按【取消】结束任务");
        line_kun.removeAllViews();//清除捆数栏，准备添加库间选择列表
    }

    //两次退出退出任务
    private static final int TIME_EXIT = 2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
        super.onBackPressed();
        if (enterCheckPackInfoList.size() > 0)//业务数据存在，生成业务文件
        {
            EnterCheckPackInfo[] enterCheckPackInfos = new EnterCheckPackInfo[enterCheckPackInfoList.size()];
            enterCheckPackInfoList.toArray(enterCheckPackInfos);
            enterCheckData.setPackInfoList(enterCheckPackInfos);

            TXTWriter tw = new TXTWriter();
            String datajson = JSON.toJSONString(enterCheckData);

            long timestamp = new Date().getTime();
            String timestampstr = DecimalTool.addZeroForNum(Long.toHexString(timestamp), 16).toUpperCase();
            String datafilename = "Data_" + businessid + "_" + timestampstr + ".json";
            mylog.Write("退出业务,生成任务数据文件=" + datafilename);
            mylog.Write("生成任务数据=" + datajson);
            try {
                tw.writeDataFile(this, datafilename, datajson.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                mylog.Write("生成数据文件失败,原因:" + e.getMessage());
                Toast.makeText(this, "生成数据文件失败,原因:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }else

    {
        Toast.makeText(this, "再点击一次返回退出本业务", Toast.LENGTH_SHORT).show();
        mBackPressed = System.currentTimeMillis();
    }
}

}
