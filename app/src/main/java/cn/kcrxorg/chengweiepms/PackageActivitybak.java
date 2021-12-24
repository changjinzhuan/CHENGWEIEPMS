package cn.kcrxorg.chengweiepms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.PackageCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.busiInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.packInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.PackageData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.packinfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;


public class PackageActivitybak extends BisnessBaseActivity {

    ScrollView scro_cmdinfo;
    TextView tv_cmdinfo;
    TextView saninfo;
    Spinner sp_vercharInfo;//券别选择
    Spinner sp_stackInfo;//库间选择


    PackageCMD packageCMD;
    PackageData packageData;

    String businessid="";
    String operator;
    String auditor;

    busiInfo thisbusiInfo;

    static BigDecimal waitpackSackMoney ;
    static BigDecimal ispackSackMoney ;
    float val;

    List<packinfo> packinfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

     //   line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        scro_cmdinfo=new ScrollView(this);
        scro_cmdinfo.setLayoutParams(params);
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);
        String  cmdinfo="任务信息:\r\n";
        for(busiInfo b:packageCMD.getBusiinfoList())
        {
            cmdinfo+=b.getPaperTypeName()+" "+b.getVoucherTypeName()+" "+ DecimalTool.formatTosepara(b.getSackMoney())+"元\r\n";

        }

        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        scro_cmdinfo.addView(tv_cmdinfo);

        line_businfo.addView(scro_cmdinfo);


        line_kun.setOrientation(LinearLayout.VERTICAL);

        if (packageCMD.getStackInfoList().length > 0)//如果需要选择库间的话
        {
            if(packageCMD.getStackInfoList()[0].getSstackName()!=null)
            {
                sp_stackInfo=new Spinner(this);//库间选择
                sp_stackInfo.setLayoutParams(params);
                List<String> stackInfoList=new ArrayList<String>();
                for(cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stackInfo stackInfo:packageCMD.getStackInfoList())
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

        sp_vercharInfo=new Spinner(this);//券别选择
        sp_vercharInfo.setBackgroundResource(R.drawable.tv_border);
        sp_vercharInfo.setLayoutParams(params);
        List<String> vercharInfoList=new ArrayList<String>();//券别列表
        for(busiInfo b:packageCMD.getBusiinfoList())
        {
            vercharInfoList.add(b.getPaperTypeName()+":"+b.getVoucherTypeName());
        }
        String[] vercharInfos=new String[vercharInfoList.size()];
        vercharInfoList.toArray(vercharInfos);

        ArrayAdapter<String> bb= new ArrayAdapter<String>(this,R.layout.sp_items,R.id.tv_stackInfo,vercharInfos);
        sp_vercharInfo.setAdapter(bb);

        sp_vercharInfo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //mylog.Write("现在选择的是:"+position);
                thisbusiInfo= packageCMD.getBusiinfoList()[position];
                mylog.Write("任务列表是"+thisbusiInfo.getPaperTypeName()+"-"+thisbusiInfo.getVoucherTypeName());
                tv_operinfo.setText("请封装" +thisbusiInfo.getPaperTypeName() + "的" + thisbusiInfo.getVoucherTypeName());

                val=Float.parseFloat(packageCMD.getBusiinfoList()[position].getVal());
                tagidlist=new ArrayList<String>();//更换可执行锁列表


                for(cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.packInfo packInfo:packageCMD.getPackinfoList())
                {
                    float pv=Float.parseFloat(packInfo.getVal());
                    float bv=Float.parseFloat(thisbusiInfo.getVal());
                    if(pv==bv)//获取券别相同可操作
                    {
                        tagidlist.add(packInfo.getSackNo());
                    }

                }
                mylog.Write("可操作锁数量="+tagidlist.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        line_kun.addView(sp_vercharInfo);
        sp_vercharInfo.setFocusable(true);
        sp_vercharInfo.setFocusableInTouchMode(true);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://扫描到包号,这里应该是补登后续开发
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
                    case LOCK_LOG_WHAT://锁操作日志
                        mylog.Write(msg.getData().getString("lockmessage").toString());
                        break;
                    case LOCK_WHAT://操作锁结果
                        mylog.Write(msg.getData().getString("lockmessage").toString());
                        String lockrs=msg.getData().getString("lockmessage").toString();
                        if(!lockrs.contains("锁状态正常"))
                        {
                            addRsinfo(lockrs,false);
                            Util.playErr();
                        }else//操作成功
                        {
                            Util.playOk();
                            String cardnum=lockrs.split(":")[0];
                            String oplock=lockrs.split(":")[2].equals("Lock")?"关锁":"开锁";

                            if(lockrs.split(":")[2].equals("Lock"))
                            {
                                addRsinfo(cardnum+oplock+"完成!封装金额:"+kuncount*1000*val+"元",true);
                                //减掉本币种金额
                                BigDecimal cmdSackMoney=thisbusiInfo.getSackMoney().subtract(new BigDecimal(kuncount*1000*val));
                                setBusiInfoSackMoney(thisbusiInfo,cmdSackMoney);

                                packInfo thispackInfo = getpackinfofromcardnum(cardnum);
                                packinfo packinfo=new packinfo();
                                packinfo.setSackNo(cardnum);
                                packinfo.setVal(thisbusiInfo.getVal());
                                packinfo.setPaperTypeID(thisbusiInfo.getPaperTypeID());
                                packinfo.setPaperTypeName(thisbusiInfo.getPaperTypeName());
                                packinfo.setVoucherTypeID(thisbusiInfo.getVoucherTypeID());
                                packinfo.setVoucherTypeName(thisbusiInfo.getVoucherTypeName());
                                packinfo.setSackMoney(kuncount*1000*val+"");
                                packinfo.setOperatorID("0");
                                packinfo.setBundles(kuncount+"");
                                packinfo.setTie("0");
                                packinfo.setOprDT(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                                packinfo.setSstackCode("");
                                packinfo.setSstackName("");
                                if (packageCMD.getStackInfoList().length > 0)
                                {
                                    if(packageCMD.getStackInfoList()[0].getSstackName()!=null)
                                    {
                                        packinfo.setSstackCode(sp_stackInfo.getSelectedItem().toString().split(":")[0]);
                                        packinfo.setSstackName(sp_stackInfo.getSelectedItem().toString().split(":")[1]);
                                    }

                                }
                                packinfoList.add(packinfo);
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }
    public void initView()
    {
        //开始处理业务数据
        String cmddata=getIntent().getStringExtra("cmddata");
        businessid=getIntent().getStringExtra("businessid");
        operator=getIntent().getStringExtra("operator");
        auditor=getIntent().getStringExtra("auditor");
        packageCMD= JSONObject.parseObject(cmddata, PackageCMD.class);

        packageData=new PackageData();
        packageData.setCode(98305);
        packageData.setError("");
        packinfoList=new ArrayList<packinfo>();

        tv_header.setCenterString("封装");

        tv_footer.setText("请按【关锁】进行封装，或按【取消】结束任务");
        line_kun.removeAllViews();//清除捆数显示，准备显示券别
        line_businfo.removeAllViews();
    }
    public void operateLockGetrs(final boolean lock)
    {

        {
            if(lock)
            {
                if(thisbusiInfo.getSackMoney().intValue()>0)
                {
                    addRsinfo("正在关锁请稍后....",true);
                    new Thread() {
                        @Override
                        public void run() {
                           // lockHelper=new LockUtil();
                            lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,kuncount*1000,lock);
                        }
                    }.start();

                }else
                {
                    addRsinfo("待封装金额不足，不可封装",false);
                }
            }
            else//如果是开锁，后续增加异常处理流程
            {
                new Thread() {
                    @Override
                    public void run() {
                       // lockHelper=new LockUtil();
                        lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,0,lock);
                    }
                }.start();
            }


        }
        //封装页面开锁解除异常状态逻辑后续完成...目前不支持开锁

    }
    private packInfo getpackinfofromcardnum(String cardnum)
    {
        for (int i = 0; i < packageCMD.getPackinfoList().length; i++)
        {
            if (cardnum == packageCMD.getPackinfoList()[i].getSackNo())
            {
                return packageCMD.getPackinfoList()[i];
            }
        }
        return null;
    }
    private void setBusiInfoSackMoney(busiInfo thisoutScanBusiInfo,BigDecimal cmdSackMoney)
    {
        for(busiInfo outScanBusiInfo: packageCMD.getBusiinfoList())
        {
            //如果类型及币种一致，则重设金额
            if(outScanBusiInfo.getPaperTypeID().equals(thisoutScanBusiInfo.getPaperTypeID())&&outScanBusiInfo.getVoucherTypeID().equals(thisoutScanBusiInfo.getVoucherTypeID()))
            {
                outScanBusiInfo.setSackMoney(cmdSackMoney);
            }
        }
        //刷新任务
        String  cmdinfo="任务信息:\r\n";

        for(busiInfo outScanBusiInfo:packageCMD.getBusiinfoList())
        {
            cmdinfo+=outScanBusiInfo.getPaperTypeName()+" "+outScanBusiInfo.getVoucherTypeName()+" "+ DecimalTool.formatTosepara(outScanBusiInfo.getSackMoney())+"元\r\n";
        }

        tv_cmdinfo.setText(cmdinfo);
    }

    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(packinfoList.size()>0)//业务数据存在，生成业务文件
            {
                packinfo[] packinfos=new packinfo[packinfoList.size()];
                packinfoList.toArray(packinfos);
                packageData.setPackInfoList(packinfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(packageData);

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
