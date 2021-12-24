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
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.AllotUnPackCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.stock;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.team;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.AllotUnPackData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.elePackInfo;
import cn.kcrxorg.chengweiepms.locktools.LockUtil;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;

public class NewAllotUnPackActivity extends BisnessBaseActivity {
    TextView tv_cmdinfo;
    TextView saninfo;
    Spinner sp_stackInfo;//小组选择

    AllotUnPackCMD allotUnPackCMD;
    AllotUnPackData allotUnPackData;

    String businessid="";
    String operator;
    String auditor;

    List<elePackInfo> elePackInfoList;

    int waitunpack = 0;
    int isgood = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        //开始处理业务数据
     //   String cmddata=getIntent().getStringExtra("cmddata");
        TXTReader tr = new TXTReader();
        businessid=getIntent().getStringExtra("businessid");
        String cmddata = tr.getCmdById(NewAllotUnPackActivity.this, businessid);
        operator=getIntent().getStringExtra("operator");
        auditor=getIntent().getStringExtra("auditor");
        allotUnPackCMD= JSONObject.parseObject(cmddata, AllotUnPackCMD.class);

        allotUnPackData = new AllotUnPackData();
        allotUnPackData.setCode(163845);
        allotUnPackData.setError("");
        elePackInfoList=new ArrayList<elePackInfo>();

        waitunpack = allotUnPackCMD.getStockList().length;

        tagidlist=new ArrayList<String>();//获取可执行锁列表

        for(stock stockPackInfo:allotUnPackCMD.getStockList())
        {
            tagidlist.add(stockPackInfo.getSackNo());
        }

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        String  cmdinfo="任务信息:\r\n";

        cmdinfo+="待拆封分配: "+waitunpack+"袋";
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

        line_kun.removeAllViews();//清除捆数显示，准备显示小组列表
        line_kun.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        TextView sp_info=new TextView(this);
        sp_info.setText("请选择分配小组:");
        sp_info.setTextSize(18);
        line_kun.addView(sp_info);
        sp_stackInfo=new Spinner(this);//小组选择
        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        params1.weight=1;
        sp_stackInfo.setLayoutParams(params1);

        List<String> teamList=new ArrayList<String>();
        for(team t:allotUnPackCMD.getTeamList())
        {
            teamList.add(t.getTeamId()+":"+t.getTeamName());
        }
        String[] teamInfos=new String[teamList.size()];
        teamList.toArray(teamInfos);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,R.layout.sp_items,R.id.tv_stackInfo,teamInfos);
        sp_stackInfo.setAdapter(aa);
      //  sp_stackInfo.setBackgroundResource(R.drawable.button_selector);

        line_kun.addView(sp_stackInfo);
        //初始化查看任务列表
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(stock stockPackInfo:allotUnPackCMD.getStockList())
        {
            tagidlist.add(stockPackInfo.getSackNo());
            ViewCmdInfo viewCmdInfo=new ViewCmdInfo();
            viewCmdInfo.setSackNo(stockPackInfo.getSackNo());
            viewCmdInfo.setPaperTypeName(stockPackInfo.getPaperTypeName());
            viewCmdInfo.setVoucherTypeName(stockPackInfo.getVoucherTypeName());
            viewCmdInfo.setVal(stockPackInfo.getVal());
            viewCmdInfoList.add(viewCmdInfo);
        }
        allcarddata=new ArrayList<String>();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://扫描到包号,这里应该是补登后续开发
                        String tagmessage=msg.getData().getString("tagmessage");
                       // mylog.Write("拆封分配补登读取到EPC："+tagmessage);
                        if(!checkrepeat(tagmessage))//重复过滤
                        {
                            break;
                        }
                        mylog.Write("拆封分配补登读取到EPC："+tagmessage);
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
                            if(!tagEpcData.getLockstuts().equals("unLock")||tagEpcData.getHasElec()==true)//关锁不可以补登
                            {
                                Util.playErr();
                                addRsinfo(cardnum+"关锁不可补登，锁状态："+tagEpcData.getLockstuts(),false);
                                mylog.Write(cardnum+"关锁不可补登，锁状态："+tagEpcData.getLockstuts());
                                break;
                            }
                            if(!checkDataList(cardnum))
                            {
                                Util.playErr();
                                addRsinfo(cardnum+"已登记，不可补登",false);
                                mylog.Write(cardnum+"已登记，不可补登");
                                break;
                            }
                            team thisteam=new team();
                            thisteam.setTeamId(sp_stackInfo.getSelectedItem().toString().split(":")[0]);
                            thisteam.setTeamName(sp_stackInfo.getSelectedItem().toString().split(":")[1]);
                            isgood++;
                            saninfo.setText("已拆封分配: "+isgood+"袋");
                            addRsinfo("签封" + cardnum + "拆封补登成功!分配给小组"+thisteam.getTeamName(),true);

                            stock thisstock = getStock(cardnum);

                            setGoodViewCmdInfo(tagEpcData.getTagid()+"");//设置任务列表

                            elePackInfo elePackInfo = new elePackInfo();
                            elePackInfo.setSackNo(cardnum);
                            elePackInfo.setVoucherTypeID(thisstock.getVoucherTypeID());
                            elePackInfo.setVoucherTypeName(thisstock.getVoucherTypeName());
                            elePackInfo.setPaperTypeID(thisstock.getPaperTypeID());
                            elePackInfo.setPaperTypeName(thisstock.getPaperTypeName());
                            elePackInfo.setEditionCode("");
                            elePackInfo.setEditionName("");
                            elePackInfo.setSeriesCode("");
                            elePackInfo.setSeriesName("");
                            elePackInfo.setVal(thisstock.getVal());
                            elePackInfo.setBundles(thisstock.getBundles());
                            elePackInfo.setSackMoney(thisstock.getSackMoney());
                            elePackInfo.setTeamId(thisteam.getTeamId());
                            elePackInfo.setTeamName(thisteam.getTeamName());
                            elePackInfo.setScanUser("");
                            elePackInfo.setOprDT(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                            elePackInfo.setHavaBoxInfo("0");
                            elePackInfoList.add(elePackInfo);
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
                        break;
                    case LOCK_WHAT://操作锁结果
                        MyApp.isOpLock=false;

                        mylog.Write("操作锁结束:isOpLock="+MyApp.isOpLock);
                        mylog.Write(msg.getData().getString("lockmessage").toString());
                        String lockrs=msg.getData().getString("lockmessage").toString();
                    //    mylog.Write("操作锁结束:重新初始化天线");

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
                                //关锁先不处理
                            }else
                            {
                                //  addRsinfo(cardnum+oplock+"完成!",true);
                                team thisteam=new team();
                                thisteam.setTeamId(sp_stackInfo.getSelectedItem().toString().split(":")[0]);
                                thisteam.setTeamName(sp_stackInfo.getSelectedItem().toString().split(":")[1]);
                                isgood++;
                                saninfo.setText("已拆封分配: "+isgood+"袋");
                                addRsinfo("签封" + cardnum + "拆封成功!分配给小组"+thisteam.getTeamName(),true);
                                setGoodViewCmdInfo(cardnum);
                                stock thisstock = getStock(cardnum);
                                elePackInfo elePackInfo = new elePackInfo();
                                elePackInfo.setSackNo(cardnum);
                                elePackInfo.setVoucherTypeID(thisstock.getVoucherTypeID());
                                elePackInfo.setVoucherTypeName(thisstock.getVoucherTypeName());
                                elePackInfo.setPaperTypeID(thisstock.getPaperTypeID());
                                elePackInfo.setPaperTypeName(thisstock.getPaperTypeName());
                                elePackInfo.setEditionCode("");
                                elePackInfo.setEditionName("");
                                elePackInfo.setSeriesCode("");
                                elePackInfo.setSeriesName("");
                                elePackInfo.setVal(thisstock.getVal());
                                elePackInfo.setBundles(thisstock.getBundles());
                                elePackInfo.setSackMoney(thisstock.getSackMoney());
                                elePackInfo.setTeamId(thisteam.getTeamId());
                                elePackInfo.setTeamName(thisteam.getTeamName());
                                elePackInfo.setScanUser("");
                                elePackInfo.setOprDT(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                                elePackInfo.setHavaBoxInfo("0");

                                elePackInfoList.add(elePackInfo);
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private stock getStock(String cardnum) {
        for (int i = 0; i < allotUnPackCMD.getStockList().length; i++)
        {
            if (allotUnPackCMD.getStockList()[i].getSackNo().equals(cardnum) )
            {
                return allotUnPackCMD.getStockList()[i];
            }
        }
        return null;
    }
    public void operateLockGetrs(final boolean lock)
    {
        if(lock)//如果是关锁，后续增加异常处理流程
        {
      //      lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,20000,lock);
            new Thread(){
                @Override
                public void run() {
                    lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,20000,lock);
                }
            }.start();

        }
        else//
        {
      //      lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,0,lock);
            addRsinfo("正在开锁请稍后...",true);
            new Thread(){
                public void run(){
                    lockHelper.operateLockGetrs(mHandler,tagidlist,operator,auditor,0,lock);
                }
            }.start();
        }
    }
    public void initView()
    {
        tv_header.setCenterString("拆封分配");
        tv_operinfo.setText("请按【开锁】进行拆封分配，或按【取消】结束任务");
        tv_footer.setText("请按【开锁】进行拆封分配，或按【取消】结束任务");
    }
    private boolean checkDataList(String cardnum) {
        for(elePackInfo elePackInfo:elePackInfoList)
        {
            if(elePackInfo.getSackNo().equals(cardnum))
            {
                return false;
            }
        }
        return  true;
    }
    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(elePackInfoList.size()>0)//业务数据存在，生成业务文件
            {
                elePackInfo[] elePackInfos=new elePackInfo[elePackInfoList.size()];
                elePackInfoList.toArray(elePackInfos);
                allotUnPackData.setElePackInfoList(elePackInfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(allotUnPackData);

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
