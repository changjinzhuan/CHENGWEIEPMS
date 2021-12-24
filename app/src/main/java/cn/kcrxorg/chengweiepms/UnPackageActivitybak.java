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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.UnPackageCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stockPackInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.UnPackageData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.unpackinfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;


public class UnPackageActivitybak extends BisnessBaseActivity {
    TextView tv_cmdinfo;
    TextView saninfo;

    UnPackageCMD unPackageCMD;
    UnPackageData unPackageData;

    String businessid="";
    String operator;
    String auditor;

    int waitunpack = 0;
    int isgood = 0;

    List<unpackinfo> unpackinfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        tagidlist=new ArrayList<String>();//获取可执行锁列表
        for(cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stockPackInfo stockPackInfo:unPackageCMD.getStockPackInfoList())
        {
            tagidlist.add(stockPackInfo.getSackNo());
        }
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
                            //Util.playErr();
                        }else//操作成功
                        {
                           // Util.playOk();
                            String cardnum=lockrs.split(":")[0];
                            String oplock=lockrs.split(":")[2].equals("Lock")?"关锁":"开锁";
                            if(lockrs.split(":")[2].equals("Lock"))
                            {
                                //关锁先不处理
                            }else
                            {
                              //  addRsinfo(cardnum+oplock+"完成!",true);
                                isgood++;
                                saninfo.setText("已拆封: "+isgood+"袋");
                                stockPackInfo thisstockPackInfo = getstockPackInfo(cardnum);
                                addRsinfo("签封" + cardnum + "拆封成功!",true);

                                unpackinfo unpackinfo = new unpackinfo();
                                unpackinfo.setSackNo(cardnum);
                                unpackinfo.setVal(thisstockPackInfo.getVal());
                                unpackinfo.setVoucherTypeID(thisstockPackInfo.getVoucherTypeID());
                                unpackinfo.setVoucherTypeName(thisstockPackInfo.getVoucherTypeName());
                                unpackinfo.setPaperTypeID(thisstockPackInfo.getPaperTypeID());
                                unpackinfo.setPaperTypeName(thisstockPackInfo.getPaperTypeName());
                                unpackinfo.setSackMoney(thisstockPackInfo.getSackMoney());
                                unpackinfo.setBundles(thisstockPackInfo.getBundles());
                                unpackinfo.setTie(thisstockPackInfo.getTie());
                                unpackinfo.setOprDT(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

                                unpackinfoList.add(unpackinfo);
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
        line_kun.setVisibility(View.GONE);
        //开始处理业务数据
        String cmddata=getIntent().getStringExtra("cmddata");
        businessid=getIntent().getStringExtra("businessid");
        operator=getIntent().getStringExtra("operator");
        auditor=getIntent().getStringExtra("auditor");
        unPackageCMD= JSONObject.parseObject(cmddata, UnPackageCMD.class);

        unPackageData = new UnPackageData();
        unPackageData.setCode(98306);
        unPackageData.setError("");
        unpackinfoList=new ArrayList<unpackinfo>();

        waitunpack=unPackageCMD.getStockPackInfoList().length;

        tv_header.setCenterString("拆封");
        tv_operinfo.setText("请按【开锁】进行拆封，或按【取消】结束任务");
        tv_footer.setText("请按【开锁】进行拆封，或按【取消】结束任务");

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        String  cmdinfo="任务信息:\r\n";

        cmdinfo+="待拆封: "+waitunpack+"袋";
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

        saninfo.setText("已拆封: 0袋");
        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);
        //  saninfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);


    }
    public void operateLockGetrs(final boolean lock)
    {
        if(lock)//如果是关锁，后续增加异常处理流程
        {
            //lockHelper=new LockUtil();
            lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,20000,lock);

        }
        else//
        {
            addRsinfo("正在开锁请稍后...",true);
            new Thread(){
                public void run(){
                    //lockHelper=new LockUtil();
                    lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,0,lock);
                }
            }.start();
        }
    }

    private stockPackInfo getstockPackInfo(String cardnum)
    {
        for (int i = 0; i < unPackageCMD.getStockPackInfoList().length; i++)
        {
            if (unPackageCMD.getStockPackInfoList()[i].getSackNo() .equals(cardnum))
            {
                return unPackageCMD.getStockPackInfoList()[i];
            }
        }
        return null;
    }
    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(unpackinfoList.size()>0)//业务数据存在，生成业务文件
            {
                unpackinfo[] unpackinfos=new unpackinfo[unpackinfoList.size()];
                unpackinfoList.toArray(unpackinfos);
                unPackageData.setUnPackInfoList(unpackinfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(unPackageData);

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
