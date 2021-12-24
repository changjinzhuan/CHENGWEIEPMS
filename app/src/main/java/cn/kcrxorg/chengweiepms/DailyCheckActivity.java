package cn.kcrxorg.chengweiepms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.DailyCheckCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.dailyCheckStock;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.DailyCheckData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.DailyCheckPackInfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;


public class DailyCheckActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;

    String businessid="";
    int isgood=0;

    DailyCheckCMD dailyCheckCMD;
    DailyCheckData dailyCheckData;

    List<DailyCheckPackInfo> dailyCheckPackInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        String cmddata=getIntent().getStringExtra("cmddata");
        businessid=getIntent().getStringExtra("businessid");

        dailyCheckCMD= JSONObject.parseObject(cmddata, DailyCheckCMD.class);
        dailyCheckData=new DailyCheckData();
        dailyCheckData.setCode(163846);
        dailyCheckData.setError("");

        dailyCheckPackInfoList=new ArrayList<DailyCheckPackInfo>();
        allcarddata = new ArrayList<String>();

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);

        String  cmdinfo="任务信息:\r\n";
        cmdinfo+="待交接: "+dailyCheckCMD.getStockList().length+"袋";
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

        String saninfostr="已核对:  0袋";

        saninfo.setText(saninfostr);

        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);

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
                        if(tagEpcData==null)
                        {
                            Util.playErr();
                            break;
                        }
                        if(tagEpcData!=null&&tagEpcData.getTagid()>0)//如果可以读取未报错
                        {
                            //不判断锁状态？
//                            if (!tagEpcData.getLockstuts().equals("Lock"))
//                            {
//                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为开锁，核对失败！",false);
//                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为开锁，核对失败！");
//                                Util.playErr();
//                                return;
//                            }
                            dailyCheckStock thisdailyCheckStock=checkJobInfo(tagEpcData);
                            if(thisdailyCheckStock==null)
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"不在库存列表中,核对失败",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"不在库存列表中,核对失败");
                              //  Util.playErr();
                                return;
                            }
                            isgood++;
                            addRsinfo("读取到封签:" + tagEpcData.getTagid() + "核对成功",true);
                            mylog.Write("读取到封签:" + tagEpcData.getTagid() + "核对成功");
                            Util.playOk();
                            saninfo.setText("已核对"+isgood+"袋");

                            DailyCheckPackInfo dailyCheckPackInfo = new DailyCheckPackInfo();
                            dailyCheckPackInfo.setSackNo(tagEpcData.getTagid()+"");
                            dailyCheckPackInfo.setVal(thisdailyCheckStock.getVal());
                            dailyCheckPackInfo.setPaperTypeID(thisdailyCheckStock.getPaperTypeID());
                            dailyCheckPackInfo.setPaperTypeName(thisdailyCheckStock.getPaperTypeName());
                            dailyCheckPackInfo.setVoucherTypeID(thisdailyCheckStock.getVoucherTypeID());
                            dailyCheckPackInfo.setVoucherTypeName(thisdailyCheckStock.getVoucherTypeName());
                            dailyCheckPackInfo.setBundles(thisdailyCheckStock.getBundles());
                            dailyCheckPackInfo.setTie(thisdailyCheckStock.getTie());
                            dailyCheckPackInfo.setSackMoney(thisdailyCheckStock.getSackMoney());
                            dailyCheckPackInfo.setSstackCode(thisdailyCheckStock.getSstackCode());
                            dailyCheckPackInfo.setSstackName(thisdailyCheckStock.getSstackName());
                            dailyCheckPackInfo.setStatus(tagEpcData.getLockstuts().equals("Lock")?"1":"0");

                            dailyCheckPackInfoList.add(dailyCheckPackInfo);

                        }else
                        {
                            mylog.Write("读取到错误标签epc="+tagmessage);
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
        mylog.Write("初始化完成");
    }

    private dailyCheckStock checkJobInfo(TagEpcData tagEpcData) {
        for(dailyCheckStock d:dailyCheckCMD.getStockList())
        {
            if(d.getSackNo().equals(tagEpcData.getTagid()+""))
            {
                return d;
            }
        }
        return null;
    }

    private void initView() {
        tv_header.setCenterString("电子签封核对");
        tv_operinfo.setText("请按【扫描】进行核对扫描或按【取消】结束任务");
        tv_footer.setText("请按【扫描】进行核对扫描或按【取消】结束任务");
        line_kun.setVisibility(View.GONE);
    }

    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(dailyCheckPackInfoList.size()>0)//业务数据存在，生成业务文件
            {
                DailyCheckPackInfo[] dailyCheckPackInfos=new DailyCheckPackInfo[dailyCheckPackInfoList.size()];
                dailyCheckPackInfoList.toArray(dailyCheckPackInfos);
                dailyCheckData.setPackInfoList(dailyCheckPackInfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(dailyCheckData);

                long timestamp=new Date().getTime();
                String timestampstr= DecimalTool.addZeroForNum(Long.toHexString(timestamp),16).toUpperCase();
                String datafilename="Data_"+businessid+"_"+timestampstr+".json";
                mylog.Write("退出业务,生成任务数据文件="+datafilename);
                mylog.Write("生成任务数据="+datajson);
                try {
                    tw.writeDataFile(this,datafilename,datajson.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    mylog.Write("生成数据文件失败,原因:"+e.getMessage());
                    Toast.makeText(this,"生成数据文件失败,原因:"+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
            finish();
        }else{
            Toast.makeText(this,"再点击一次返回退出本业务",Toast.LENGTH_SHORT).show();
            mBackPressed=System.currentTimeMillis();
        }
    }
}
