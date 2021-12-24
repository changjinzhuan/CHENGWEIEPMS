package cn.kcrxorg.chengweiepms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
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
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EnterScanCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.paymentSack;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stackInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.EnterScanData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.enterScanPackInfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;


public class EnterScanActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;
    Spinner sp_stackInfo;//库间选择


    EnterScanCMD enterScanCMD;
    EnterScanData enterScanData;

    List<enterScanPackInfo> enterScanPackInfoList;

    String businessid="";
    int isgood=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        allcarddata=new ArrayList<>();
        String cmddata=getIntent().getStringExtra("cmddata");
        businessid=getIntent().getStringExtra("businessid");

        enterScanCMD= JSONObject.parseObject(cmddata, EnterScanCMD.class);

        enterScanData=new EnterScanData();
        enterScanData.setCode(163843);
        enterScanData.setError("");
        enterScanPackInfoList=new ArrayList<enterScanPackInfo>();


        LinearLayout.LayoutParams paramsline=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsline.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);
        String  cmdinfo="任务信息:\r\n";

        cmdinfo+="需扫描入库 "+enterScanCMD.getPaymentSackList().length+"袋";
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(paramsline);
        scro_businfo.addView(tv_cmdinfo);

        saninfo.setText("已扫描:0袋");
        saninfo.setTextSize(18);
        saninfo.setLayoutParams(paramsline);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);




        //库间列表选择
        sp_stackInfo=new Spinner(this);

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight=1;
        sp_stackInfo.setLayoutParams(params);
        List<String> stackInfoList=new ArrayList<String>();
        for(cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stackInfo stackInfo:enterScanCMD.getStackInfoList())
        {
            stackInfoList.add(stackInfo.getSstackCode()+":"+stackInfo.getSstackName());
        }
        String[] stackInfos=new String[stackInfoList.size()];
        stackInfoList.toArray(stackInfos);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,R.layout.sp_items,R.id.tv_stackInfo,stackInfos);
        sp_stackInfo.setAdapter(aa);

        line_kun.addView(sp_stackInfo);

      //  mylog.Write("当前选择的是="+sp_stackInfo.getSelectedItem().toString());

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
                            mylog.Write("读取到本系统标签epc="+tagmessage+" 袋号:"+tagEpcData.getTagid());
                            if (!tagEpcData.getLockstuts().equals("Lock"))
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为"+tagEpcData.getLockstuts()+"，不可入库",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为"+tagEpcData.getLockstuts()+"，不可入库");
                                //Util.playErr();
                                return;
                            }
                            if (!checkPaymentSackList(tagEpcData))
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"不在任务列表中,不可入库",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"不在任务列表中,不可入库");
                             //   Util.playErr();
                                return;
                            }
                            isgood++;
                            paymentSack nowpaymentSack=selectpaymentSackList(tagEpcData);
                            addRsinfo("读取到封签:" + tagEpcData.getTagid() + "入库成功",true);
                            mylog.Write("读取到封签:" + tagEpcData.getTagid() + "入库成功");
                        //    Util.playOk();
                            saninfo.setText("已扫描"+isgood+"袋");

                            enterScanPackInfo enterScanPackInfo = new enterScanPackInfo();
                            enterScanPackInfo.setSackNo(tagEpcData.getTagid()+"");
                            enterScanPackInfo.setVoucherTypeID(nowpaymentSack.getVoucherTypeID());
                            enterScanPackInfo.setVoucherTypeName(nowpaymentSack.getVoucherTypeName());
                            enterScanPackInfo.setPaperTypeID(nowpaymentSack.getPaperTypeID());
                            enterScanPackInfo.setPaperTypeName(nowpaymentSack.getPaperTypeName());
                            enterScanPackInfo.setSackMoney(nowpaymentSack.getSackMoney());
                            enterScanPackInfo.setVal(nowpaymentSack.getVal());
                            enterScanPackInfo.setBundles(nowpaymentSack.getBundles());
                            enterScanPackInfo.setTie(nowpaymentSack.getTie());
                            enterScanPackInfo.setOprDT(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
                            enterScanPackInfo.setSstackCode(sp_stackInfo.getSelectedItem().toString().split(":")[0]);
                            enterScanPackInfo.setSstackName(sp_stackInfo.getSelectedItem().toString().split(":")[1]);
                            enterScanPackInfoList.add(enterScanPackInfo);
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
    public void initView()
    {
        tv_header.setCenterString("电子签封入库");
        tv_operinfo.setText("请先选择入库库间,按【扫描】进行入库扫描或按【取消】结束任务");
        tv_footer.setText("请先选择入库库间,按【扫描】进行入库扫描或按【取消】结束任务");
        line_kun.removeAllViews();//清除捆数显示，准备显示库间列表
    }
    private paymentSack selectpaymentSackList(TagEpcData tagEpcData) {
        for (int i = 0; i < enterScanCMD.getPaymentSackList().length; i++)
        {
            if ((tagEpcData.getTagid()+"").equals(enterScanCMD.getPaymentSackList()[i].getSackNo()))
            {
                return enterScanCMD.getPaymentSackList()[i];
            }
        }
        return null;
    }

    private boolean checkPaymentSackList(TagEpcData tagEpcData) {
        for (int i = 0; i < enterScanCMD.getPaymentSackList().length; i++)
        {
            if ((tagEpcData.getTagid()+"").equals(enterScanCMD.getPaymentSackList()[i].getSackNo()))
            {
                return true;
            }
        }
        return false;
    }



    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(enterScanPackInfoList.size()>0)//业务数据存在，生成业务文件
            {
                enterScanPackInfo[] enterScanPackinfos=new enterScanPackInfo[enterScanPackInfoList.size()];
                enterScanPackInfoList.toArray(enterScanPackinfos);
                enterScanData.setPackInfoList(enterScanPackinfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(enterScanData);

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
