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

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.PackageCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.busiInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.packInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.PackageData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.packinfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;


public class PackageActivity extends BisnessBaseActivity {

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
    int isgood = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
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

        saninfo.setText("已封装: 0袋");
        saninfo.setTextSize(18);

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


                for(packInfo packInfo:packageCMD.getPackinfoList())
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

        //初始化查看任务列表
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(packInfo stockPackInfo:packageCMD.getPackinfoList())
        {
            //     tagidlist.add(stockPackInfo.getSackNo());
            ViewCmdInfo viewCmdInfo=new ViewCmdInfo();
            viewCmdInfo.setSackNo(stockPackInfo.getSackNo());
            viewCmdInfo.setPaperTypeName("无");
            viewCmdInfo.setVoucherTypeName(stockPackInfo.getVoucherTypeName());
            viewCmdInfo.setVal(stockPackInfo.getVal());
            viewCmdInfoList.add(viewCmdInfo);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://扫描到包号,这里应该是补登后续开发
                       // String tagmessage=msg.getData().getString("tagmessage");
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//重复过滤
                        {
                            break;
                        }
                        mylog.Write("封装补登读取到EPC："+tagmessage);
                        allcarddata.add(tagmessage);
                        TagEpcData tagEpcData= EpcReader.readEpc(tagmessage);
                        if(tagEpcData==null)
                        {
                            Util.playErr();
                            break;
                        }
                        if(tagEpcData!=null&&tagEpcData.getTagid()>0)//如果可以读取未报错
                        {
                            String cardnum=tagEpcData.getTagid()+"";
                            if(!tagidlist.contains(cardnum))
                            {
                                Util.playErr();
                                addRsinfo(cardnum+"不在任务列表不可补登",false);
                                mylog.Write(cardnum+"不在任务列表不可补登");
                                break;
                            }
                            if(!tagEpcData.getLockstuts().equals("Lock"))//未关锁不可以补登
                            {
                                Util.playErr();
                                addRsinfo(cardnum+"未关锁不可补登，锁状态："+tagEpcData.getLockstuts(),false);
                                mylog.Write(cardnum+"未关锁不可补登，锁状态："+tagEpcData.getLockstuts());
                                break;
                            }
                            if(tagEpcData.getLockeEx())
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为异常不可补登",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为异常不可补登");
                                Util.playErr();
                                return;
                            }
                            if(!checkDataList(cardnum))//这里需要补登判断是否已经在任务列表
                            {
                                Util.playErr();
                                addRsinfo(cardnum+"已登记，不可补登",false);
                                mylog.Write(cardnum+"已登记，不可补登");
                                break;
                            }
                            if(thisbusiInfo.getSackMoney().compareTo(new BigDecimal(kuncount*1000*val))<0)//剩余金额不够封装
                            {
                                Util.playErr();
                                addRsinfo(thisbusiInfo.getVoucherTypeName()+" "+cardnum+"待封装金额不足，不可补登",false);
                                mylog.Write(thisbusiInfo.getVoucherTypeName()+" "+cardnum+"待封装金额不足，不可补登");
                                break;
                            }
                            //减掉本币种金额
                            BigDecimal cmdSackMoney=thisbusiInfo.getSackMoney().subtract(new BigDecimal(kuncount*1000*val));
                            setBusiInfoSackMoney(thisbusiInfo,cmdSackMoney);
                            setGoodViewCmdInfo(tagEpcData.getTagid()+"");
                            isgood++;
                            saninfo.setText("已封装: "+isgood+"袋");
                         //   packInfo thispackInfo = getpackinfofromcardnum(cardnum);
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
                            Util.playOk();
                            addRsinfo(cardnum+"封装补登"+"完成!补登金额:"+kuncount*1000*val+"元",true);
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
                    case LOCK_LOG_WHAT://锁操作日志
                        mylog.Write(msg.getData().getString("lockmessage").toString());
                       // addRsinfo(msg.getData().getString("lockmessage").toString(),false);
                        break;
                    case LOCK_WHAT://操作锁结果
                        MyApp.isOpLock=false;
                        mylog.Write("操作锁结束:isOpLock="+MyApp.isOpLock);
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
                            String oplock=lockrs.split(":")[2].equals("true")?"关锁":"开锁";

                            if(lockrs.split(":")[2].equals("true"))
                            {
                                addRsinfo(cardnum+oplock+"完成!封装金额:"+kuncount*1000*val+"元",true);
                                //减掉本币种金额
                                BigDecimal cmdSackMoney=thisbusiInfo.getSackMoney().subtract(new BigDecimal(kuncount*1000*val));
                                setBusiInfoSackMoney(thisbusiInfo,cmdSackMoney);
                                setGoodViewCmdInfo(cardnum);
                                isgood++;
                                saninfo.setText("已封装: "+isgood+"袋");
                           //     packInfo thispackInfo = getpackinfofromcardnum(cardnum);
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

    public boolean checkDataList(String tagid)//查看补登的是否已经在数据列表中
    {
        for (packinfo p:packinfoList)
        {
            if(p.getSackNo().equals(tagid))
            {
                return false;
            }
        }
        return true;
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
     //line_kun.removeAllViews();//清除捆数显示，准备显示券别
        line_businfo.removeAllViews();
        //封袋业务显示虚拟开关袋按钮
        line_lockopbtn.setVisibility(View.VISIBLE);

    }

    public void operateLockGetrs(final boolean lock)
    {
        if (thisbusiInfo.getSackMoney().compareTo(new BigDecimal(kuncount*1000*val))<0)
        {
            Util.playErr();
            addRsinfo(thisbusiInfo.getVoucherTypeName()+"待封装金额不足，不可封装",false);
            mylog.Write(thisbusiInfo.getVoucherTypeName()+"待封装金额不足，不可封装");
            MyApp.isOpLock=false;
            return;
        }

            if(lock)
            {
                if(thisbusiInfo.getSackMoney().intValue()>0)
                {
                  //  lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,kuncount*1000,lock);
                    addRsinfo("正在关锁请稍后....",true);
                    new Thread() {
                        @Override
                        public void run() {
                            lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,kuncount*1000,lock);
                        }
                    }.start();
                }else
                {
                  new Thread(){
                      @Override
                      public void run() {
                          super.run();
                          addRsinfo("待封装金额不足，不可封装",false);
                      }
                  }.start();
                }
            }
            else//如果是开锁，后续增加异常处理流程
            {
             new Thread(){
                 @Override
                 public void run() {
                     super.run();
                     lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,0,lock);
                 }
             }.start();
            }
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
