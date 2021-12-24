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

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.TransferCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.scanSort;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.TransferTransferData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.transferPackInfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;


public class NewTransferActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;

    String businessid="";
    int isgood=0;

    TransferCMD transferCMD;
    TransferTransferData transferTransferData;

    List<transferPackInfo> transferPackInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
   //     String cmddata=getIntent().getStringExtra("cmddata");
        TXTReader tr = new TXTReader();
        businessid=getIntent().getStringExtra("businessid");
        String cmddata = tr.getCmdById(NewTransferActivity.this, businessid);

        transferCMD= JSONObject.parseObject(cmddata, TransferCMD.class);
        transferTransferData=new TransferTransferData();
        transferTransferData.setCode(163844);
        transferTransferData.setError("");

        transferPackInfoList = new ArrayList<transferPackInfo>();
        allcarddata = new ArrayList<String>();

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);

        String  cmdinfo="任务信息:\r\n";
        cmdinfo+="待交接: "+transferCMD.getScanSortList().length+"袋";
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);
        String saninfostr="已扫描:  0袋";
        saninfo.setText(saninfostr);
        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);
        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(scanSort stockPackInfo:transferCMD.getScanSortList())
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
                switch (msg.what) {
                    case 1://扫描到包号
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//重复过滤
                        {
                            return;
                        }
                        allcarddata.add(tagmessage);
                        TagEpcData tagEpcData= EpcReader.readEpc(tagmessage);
                        if(tagEpcData==null)
                        {
                            Util.playErr();
                            break;
                        }
                        if(tagEpcData.getTagid()>0)//如果可以读取未报错
                        {
                            //交接业务不再查看锁状态，直接核对袋号
//                            if (!tagEpcData.getLockstuts().equals("Lock"))
//                            {
//                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为开锁，不可交接",false);
//                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为开锁，不可交接");
//                                Util.playErr();
//                                return;
//                            }
//                            if(tagEpcData.getLockeEx())
//                            {
//                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"状态为异常不可交接",false);
//                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"状态为异常不可交接");
//                                Util.playErr();
//                                return;
//                            }
                            scanSort thisscanSort = checkScanSortList(tagEpcData);
                            if(thisscanSort==null)//检查任务
                            // if (!checkPaymentSackList(tagEpcData))//检查任务
                            {
                                addRsinfo("读取到封签:"+tagEpcData.getTagid()+"不在任务列表中,不可交接",false);
                                mylog.Write("读取到封签:"+tagEpcData.getTagid()+"不在任务列表中,不可交接");
                                Util.playErr();
                                return;
                            }
                            isgood++;
                            addRsinfo("读取到封签:" + tagEpcData.getTagid() + "交接完成",true);
                            mylog.Write("读取到封签:" + tagEpcData.getTagid() + "交接成功");
                            Util.playOk();
                            saninfo.setText("已交接"+isgood+"袋");
                            setGoodViewCmdInfo(tagEpcData.getTagid()+"");
                            transferPackInfo transferPackInfo = new transferPackInfo();
                            transferPackInfo.setSackNo(tagEpcData.getTagid()+"");
                            transferPackInfo.setPaperTypeID(thisscanSort.getPaperTypeID());
                            transferPackInfo.setPaperTypeName(thisscanSort.getPaperTypeName());
                            transferPackInfo.setVoucherTypeID(thisscanSort.getVoucherTypeID());
                            transferPackInfo.setVoucherTypeName(thisscanSort.getVoucherTypeName());
                            transferPackInfo.setVal(thisscanSort.getVal());
                            transferPackInfo.setSackMoney(thisscanSort.getSackMoney()+"");
                            transferPackInfo.setBundles(thisscanSort.getBundles());
                            transferPackInfo.setOprDT(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
                            transferPackInfo.setBankCode(transferCMD.getOutDetailList()[0].getOrganID());//获取第一家银行的ID
                            transferPackInfo.setBankName(transferCMD.getOutDetailList()[0].getOrganName());
                            transferPackInfo.setEditionCode("");
                            transferPackInfo.setEditionName("");

                            transferPackInfoList.add(transferPackInfo);

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

    private  scanSort  checkScanSortList(TagEpcData tagEpcData) {
        for (int i = 0; i < transferCMD.getScanSortList().length; i++)
        {
            if ((tagEpcData.getTagid()+"").equals(transferCMD.getScanSortList()[i].getSackNo()))
            {
                return transferCMD.getScanSortList()[i];
            }
        }
        return null;
    }

    private void initView() {
        tv_header.setCenterString("电子签封交接");
        tv_operinfo.setText("请按【扫描】进行交接扫描或按【取消】结束任务");
        tv_footer.setText("请按【扫描】进行交接扫描或按【取消】结束任务");
        line_kun.setVisibility(View.GONE);
    }
    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(transferPackInfoList.size()>0)//业务数据存在，生成业务文件
            {
                transferPackInfo[] transferDatas=new transferPackInfo[transferPackInfoList.size()];
                transferPackInfoList.toArray(transferDatas);
                transferTransferData.setPackInfoList(transferDatas);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(transferTransferData);

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
