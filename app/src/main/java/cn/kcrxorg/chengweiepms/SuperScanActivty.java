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
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.SuperScanCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.superScanBusiInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.SuperScanData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.superScanPackinfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;
import cn.kcrxorg.chengweiepms.rfidtool.PervalueHelper;

public class SuperScanActivty extends BisnessBaseActivity {


    TextView tv_cmdinfo;
    TextView saninfo;
    SuperScanCMD superScanCMD;
    SuperScanData superScanData;

    Spinner sp_vercharInfo;//券别选择
    Spinner sp_stackInfo;//库间选择


    List<superScanPackinfo> superScanPackinfoList;
    String nowVoucherTypeId;
    String nowVoucherTypeName;
    String businessid="";

    int isgood=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        //开始处理业务数据
        String cmddata=getIntent().getStringExtra("cmddata");
        businessid=getIntent().getStringExtra("businessid");
        superScanCMD= JSONObject.parseObject(cmddata,SuperScanCMD.class);

        superScanData=new SuperScanData();
        superScanData.setCode(163841);
        superScanData.setError("");

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;


        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);
        String  cmdinfo="任务信息:\r\n";
        for(superScanBusiInfo superScanBusiInfo:superScanCMD.getBusiInfoList())
        {
            cmdinfo+=superScanBusiInfo.getVoucherTypeName()+"\r\n";
        }
        superScanPackinfoList=new ArrayList<superScanPackinfo>();
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
       // tv_cmdinfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

        saninfo.setText("已扫描:0袋");
        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);
      //  saninfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);
//初始化查看任务列表
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(superScanBusiInfo stockPackInfo:superScanCMD.getBusiInfoList())
        {
            ViewCmdInfo viewCmdInfo=new ViewCmdInfo();
            viewCmdInfo.setSackNo("无");
            viewCmdInfo.setPaperTypeName("无");
            viewCmdInfo.setVoucherTypeName(stockPackInfo.getVoucherTypeName());
            viewCmdInfo.setVal(stockPackInfo.getVal());
            viewCmdInfoList.add(viewCmdInfo);
        }


        line_kun.setOrientation(LinearLayout.VERTICAL);
        if (superScanCMD.getStackInfoList().length > 0)//如果需要选择库间的话
        {
            if(superScanCMD.getStackInfoList()[0].getSstackName()!=null)//如果集合不是空再添加选择
            {
                sp_stackInfo=new Spinner(this);//库间选择
                sp_stackInfo.setLayoutParams(params);
                List<String> stackInfoList=new ArrayList<String>();
                for(cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stackInfo stackInfo:superScanCMD.getStackInfoList())
                {
                    stackInfoList.add(stackInfo.getSstackCode()+":"+stackInfo.getSstackName());
                }
                String[] stackInfos=new String[stackInfoList.size()];
                stackInfoList.toArray(stackInfos);

                ArrayAdapter<String> aa = new ArrayAdapter<String>(this,R.layout.sp_items,R.id.tv_stackInfo,stackInfos);
                sp_stackInfo.setAdapter(aa);
                sp_stackInfo.setBackgroundResource(R.drawable.tv_border);
                line_kun.addView(sp_stackInfo);
            }

        }

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
                          if(CheckJobInfo(PervalueHelper.getVal(tagEpcData.getPervalueid())))//如果在券别列表中
                          {
                              isgood++;
                              addRsinfo("读取到封签:" + tagEpcData.getTagid()+ " 面值:" + nowVoucherTypeName,true);
                              saninfo.setText("已扫描"+isgood+"袋");
                              Util.playOk();
                              superScanPackinfo superScanPackinfo = new superScanPackinfo();
                              superScanPackinfo.setSackNo(tagEpcData.getTagid()+"");
                              superScanPackinfo.setVal(PervalueHelper.getVal(tagEpcData.getPervalueid()));
                              superScanPackinfo.setVoucherTypeID(nowVoucherTypeId);
                              superScanPackinfo.setVoucherTypeName(nowVoucherTypeName);
                              superScanPackinfo.setOprDT(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
                              superScanPackinfo.setStackCode("");

                              if(superScanCMD.getStackInfoList().length>0)
                              {
                                  if(superScanCMD.getStackInfoList()[0].getSstackName()!=null)
                                  {
                                      superScanPackinfo.setStackCode(sp_stackInfo.getSelectedItem().toString().split(":")[0]);
                                  }

                              }
                              superScanPackinfoList.add(superScanPackinfo);
                          }
                          else
                          {
                              addRsinfo("读取到封签:" + tagEpcData.getTagid()+ " 面值:" +PervalueHelper.getVal(tagEpcData.getPervalueid())+"不在任务列表中",false);
                              Util.playErr();
                          }
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
        tv_header.setCenterString("电子签封登记");
        tv_operinfo.setText("请按【扫描】进行登记或按【取消】结束任务");
        tv_footer.setText("请按【扫描】进行登记或按【取消】结束任务");
        line_kun.removeAllViews();//清除捆数栏，准备添加库间选择列表
    }
    private Boolean CheckJobInfo(String Val)
    {
        for (int i = 0; i < superScanCMD.getBusiInfoList().length; i++)
        {
            if (Float.parseFloat(Val) == Float.parseFloat(superScanCMD.getBusiInfoList()[i].getVal()))
            {
                nowVoucherTypeId = superScanCMD.getBusiInfoList()[i].getVoucherTypeID();
                nowVoucherTypeName =superScanCMD.getBusiInfoList()[i].getVoucherTypeName();
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

            if(superScanPackinfoList.size()>0)//业务数据存在，生成业务文件
            {
                superScanPackinfo[] superScanPackinfos=new superScanPackinfo[superScanPackinfoList.size()];
                superScanPackinfoList.toArray(superScanPackinfos);
                superScanData.setPackInfoList(superScanPackinfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(superScanData);

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
