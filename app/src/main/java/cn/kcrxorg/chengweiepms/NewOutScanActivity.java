package cn.kcrxorg.chengweiepms;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.OutScanCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.outScanBusiInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.outScanStockPackInfo;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.OutScanData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.transferData;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;
import cn.kcrxorg.chengweiepms.rfidtool.LockStutsHelper;


public class NewOutScanActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;
    Button btn_mustout;

    String businessid="";
    int isgood=0;

    OutScanCMD outScanCMD;
    OutScanData outScanData;

    List<transferData> transferDataList;
    List<outScanBusiInfo> isScanoutScanBusiInfoList;

    BigDecimal watiOutSackMoney;

    int mustOut = 0;
    int notmustOut = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        TXTReader tr = new TXTReader();
        businessid=getIntent().getStringExtra("businessid");
        String cmddata = tr.getCmdById(NewOutScanActivity.this, businessid);
    //    String cmddata=getIntent().getStringExtra("cmddata");


        outScanCMD= JSONObject.parseObject(cmddata, OutScanCMD.class);

        outScanData = new OutScanData();
        outScanData.setCode(163842);
        outScanData.setError("");

        transferDataList=new ArrayList<transferData>();
        isScanoutScanBusiInfoList=new ArrayList<outScanBusiInfo>();

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        params.topMargin=2;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);

        String  cmdinfo="任务信息:\r\n";

        for(outScanBusiInfo outScanBusiInfo:outScanCMD.getBusiInfoList())
        {
            cmdinfo+=outScanBusiInfo.getPaperTypeName()+" "+outScanBusiInfo.getVoucherTypeName()+" "+ DecimalTool.formatTosepara(outScanBusiInfo.getTotalMoney())+"元\r\n";
            outScanBusiInfo isout=new outScanBusiInfo();
            isout.setTotalMoney(new BigDecimal(0));
            isout.setPaperTypeID(outScanBusiInfo.getPaperTypeID());
            isout.setPaperTypeName(outScanBusiInfo.getPaperTypeName());
            isout.setVal(outScanBusiInfo.getVal());
            isout.setVoucherTypeID(outScanBusiInfo.getVoucherTypeID());
            isout.setVoucherTypeName(outScanBusiInfo.getVoucherTypeName());
            isScanoutScanBusiInfoList.add(isout);
        }
       //查看必出袋明细
        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        params1.weight=1;

        boolean visible=false;
        btn_mustout=new Button(this);
        btn_mustout.setText("查看必出袋明细");
        btn_mustout.setBackgroundResource(R.drawable.button_selector);
   //     btn_mustout.setTextColor(getResources().getColor(R.color.TextWhite,null));
        btn_mustout.setLayoutParams(params1);
        btn_mustout.setTextSize(18);
        btn_mustout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> mustoutlist=new ArrayList<String>();
                AlertDialog.Builder builder = new AlertDialog.Builder(NewOutScanActivity.this);

                for(outScanStockPackInfo s:outScanCMD.getStockPackInfoList())
                {
                    if(s.getMustOutFlag().equals("1"))//如果是必出袋
                    {
                        mustoutlist.add(s.getSackNo()+" "+s.getPaperTypeName()+" "+s.getVoucherTypeName());
                    }
                }
                if(mustoutlist.size()>0)
                {
                    String[] mustouts=new String[mustoutlist.size()];
                    mustoutlist.toArray(mustouts);
                    builder.setItems(mustouts,null);

                }else
                {
                    builder.setMessage("无必出袋!");
                }
                builder.setTitle("必出袋明细");
                builder.setNegativeButton("确认",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);
        line_kun.addView(btn_mustout);

        String saninfostr="已扫描:  0袋";

        saninfo.setText(saninfostr);

        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);
//初始化查看任务列表
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(outScanStockPackInfo stockPackInfo:outScanCMD.getStockPackInfoList())
        {
            //    tagidlist.add(stockPackInfo.getSackNo());
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
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为"+ LockStutsHelper.getStutsName(tagEpcData.getLockstuts())+"不可出库",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为"+LockStutsHelper.getStutsName(tagEpcData.getLockstuts())+"不可出库");
                                Util.playErr();
                                return;
                            }
                            if(tagEpcData.getLockeEx())
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为异常不可出库",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为异常不可出库");
                                Util.playErr();
                                return;
                            }
                            if(!checkStockList(tagEpcData))//检查任务
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"不在任务列表中,不可出库",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"不在任务列表中,不可出库");
                                Util.playErr();
                                return;
                            }
                            outScanStockPackInfo outScanStockPackInfo = selectScanStockPackInfo(tagEpcData);
                            outScanBusiInfo thisoutScanBusiInfo=  getoutScanBusiInfo(outScanStockPackInfo);
                            if(thisoutScanBusiInfo==null)
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"任务金额不足",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"任务金额不足");
                                Util.playErr();
                                return;
                            }
//                            Log.e("kcrx","thisoutScanBusiInfo.getTotalMoney()="+thisoutScanBusiInfo.getTotalMoney());
//                            Log.e("kcrx","outScanStockPackInfo.getSackMoney()="+outScanStockPackInfo.getSackMoney());
//                            Log.e("kcrx","compareTo="+thisoutScanBusiInfo.getTotalMoney().compareTo(outScanStockPackInfo.getSackMoney()));

                            //本币种任务金额检查
                            int sackmoneyrs=thisoutScanBusiInfo.getTotalMoney().compareTo(outScanStockPackInfo.getSackMoney());
                            if(sackmoneyrs==-1)
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"任务金额不足",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"任务金额不足");
                                Util.playErr();
                                return;
                            }
                            if(outScanStockPackInfo.getMustOutFlag().equals("0"))//必出先出判断
                            {
                                mylog.Write("本袋为非必出袋，判断是否还有必出袋");
                               if(thisTypeHasMustOut(outScanStockPackInfo))//如果本类型币种仍有必出
                               {
                                   mylog.Write("读取到封签:"+tagEpcData.getTagid()+" "+outScanStockPackInfo.getPaperTypeName()+outScanStockPackInfo.getVoucherTypeName()+"仍然有必出袋，请先扫描必出袋出库");
                                   addRsinfo("读取到封签:"+tagEpcData.getTagid()+" "+outScanStockPackInfo.getPaperTypeName()+outScanStockPackInfo.getVoucherTypeName()+"仍然有必出袋，请先扫描必出袋出库",false);
                                   Util.playErr();
                                   return;
                               }
                            }

                            BigDecimal cmdSackMoney=thisoutScanBusiInfo.getTotalMoney().subtract(outScanStockPackInfo.getSackMoney());
                            //减掉本币种金额
                            setBusiInfoSackMoney(thisoutScanBusiInfo,cmdSackMoney);
                            isgood++;
                            addRsinfo("读取到封签:" + tagEpcData.getTagid() + "出库成功: "+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName(),true);
                            mylog.Write("读取到封签:" + tagEpcData.getTagid() + "出库成功");
                            Util.playOk();
                            saninfo.setText("已出库"+isgood+"袋");

                            setGoodViewCmdInfo(tagEpcData.getTagid()+"");
                            transferData transferData=new transferData();
                            transferData.setSackNo(tagEpcData.getTagid()+"");
                            transferData.setVoucherTypeID(outScanStockPackInfo.getVoucherTypeID());
                            transferData.setVoucherTypeName(outScanStockPackInfo.getVoucherTypeName());
                            transferData.setPaperTypeID(outScanStockPackInfo.getPaperTypeID());
                            transferData.setPaperTypeName(outScanStockPackInfo.getPaperTypeName());
                            transferData.setVal(outScanStockPackInfo.getVal());
                            transferData.setBundles(outScanStockPackInfo.getBundles());
                            transferData.setTie(outScanStockPackInfo.getTie());
                            transferData.setMustOutFlag(outScanStockPackInfo.getMustOutFlag());
                            transferData.setOprDT(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
                            transferData.setStackCode(outScanStockPackInfo.getSstackCode());

                            transferData.setSackMoney(outScanStockPackInfo.getSackMoney());
                            transferDataList.add(transferData);

                            //如果可以出，删除任务列表中本袋
                            mylog.Write("可以出，删除库存列表中本袋");
                            mylog.Write("删除前库存列表大小"+outScanCMD.getStockPackInfoList().length);
                            List<outScanStockPackInfo> outScanStockPackInfolist=  new ArrayList<>(Arrays.asList(outScanCMD.getStockPackInfoList()));

                            for(int i=outScanStockPackInfolist.size()-1;i>=0;i--)
                            {
                                if(outScanStockPackInfolist.get(i).getSackNo().equals(outScanStockPackInfo.getSackNo()))
                                {
                                    outScanStockPackInfolist.remove(i);
                                }
                            }


                            outScanStockPackInfo[] outScanStockPackInfos=new outScanStockPackInfo[outScanStockPackInfolist.size()];

                            outScanStockPackInfolist.toArray(outScanStockPackInfos);

                            outScanCMD.setStockPackInfoList(outScanStockPackInfos);

                            mylog.Write("删除后库存列表大小"+outScanCMD.getStockPackInfoList().length);
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
    private void initView() {
        tv_header.setCenterString("电子签封出库");
        tv_operinfo.setText("请按【扫描】进行出库扫描或按【取消】结束任务");
        tv_footer.setText("请按【扫描】进行出库扫描或按【取消】结束任务");
        line_kun.removeAllViews();
    }
    private boolean thisTypeHasMustOut(outScanStockPackInfo outScanStockPackInfo) {
        for(outScanStockPackInfo os:outScanCMD.getStockPackInfoList())
        {
            //如果本币种金额仍然有必出存在
            if(os.getPaperTypeID().equals(outScanStockPackInfo.getPaperTypeID())&&os.getVoucherTypeID().equals(outScanStockPackInfo.getVoucherTypeID())&&os.getMustOutFlag().equals("1"))
            {
                return true;
            }
        }
        return false;
    }


    private outScanStockPackInfo selectScanStockPackInfo(TagEpcData tagEpcData) {

        for(outScanStockPackInfo os:outScanCMD.getStockPackInfoList())
        {
            if((tagEpcData.getTagid()+"").equals(os.getSackNo()))
            {
                return os;
            }
        }
        return null;
    }

    private boolean checkStockList(TagEpcData tagEpcData) {
        for(outScanStockPackInfo outScanStockPackInfo:outScanCMD.getStockPackInfoList())
        {
            if((tagEpcData.getTagid()+"").equals(outScanStockPackInfo.getSackNo()))
            {
                return true;
            }
        }
        return false;
    }
    private outScanBusiInfo getoutScanBusiInfo(outScanStockPackInfo outScanStockPackInfo)
    {
        for(outScanBusiInfo outScanBusiInfo:outScanCMD.getBusiInfoList())
        {
            if(outScanBusiInfo.getPaperTypeID().equals(outScanStockPackInfo.getPaperTypeID())&&outScanBusiInfo.getVoucherTypeID().equals(outScanStockPackInfo.getVoucherTypeID()))
            {
                return outScanBusiInfo;
            }
        }
        return null;
    }
    private void setBusiInfoSackMoney(outScanBusiInfo thisoutScanBusiInfo,BigDecimal cmdSackMoney)
    {
        for(outScanBusiInfo outScanBusiInfo:outScanCMD.getBusiInfoList())
        {
            //如果类型及币种一致，则重设金额
            if(outScanBusiInfo.getPaperTypeID().equals(thisoutScanBusiInfo.getPaperTypeID())&&outScanBusiInfo.getVoucherTypeID().equals(thisoutScanBusiInfo.getVoucherTypeID()))
            {
                outScanBusiInfo.setTotalMoney(cmdSackMoney);
            }
        }
        //刷新任务
        String  cmdinfo="任务信息:\r\n";

        for(outScanBusiInfo outScanBusiInfo:outScanCMD.getBusiInfoList())
        {
            cmdinfo+=outScanBusiInfo.getPaperTypeName()+" "+outScanBusiInfo.getVoucherTypeName()+" "+ DecimalTool.formatTosepara(outScanBusiInfo.getTotalMoney())+"元\r\n";
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

            if(transferDataList.size()>0)//业务数据存在，生成业务文件
            {
                transferData[] enterScanPackinfos=new transferData[transferDataList.size()];
                transferDataList.toArray(enterScanPackinfos);
                outScanData.setTransferDataList(enterScanPackinfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(outScanData);

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
