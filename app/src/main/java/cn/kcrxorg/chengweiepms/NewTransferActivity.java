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

        line_businfo.setOrientation(LinearLayout.VERTICAL);//??????????????????
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);

        String  cmdinfo="????????????:\r\n";
        cmdinfo+="?????????: "+transferCMD.getScanSortList().length+"???";
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(18);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);
        String saninfostr="?????????:  0???";
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
                    case 1://???????????????
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//????????????
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
                        if(tagEpcData.getTagid()>0)//???????????????????????????
                        {
                            //??????????????????????????????????????????????????????
//                            if (!tagEpcData.getLockstuts().equals("Lock"))
//                            {
//                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"??????????????????????????????",false);
//                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"??????????????????????????????");
//                                Util.playErr();
//                                return;
//                            }
//                            if(tagEpcData.getLockeEx())
//                            {
//                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"???????????????????????????",false);
//                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"???????????????????????????");
//                                Util.playErr();
//                                return;
//                            }
                            scanSort thisscanSort = checkScanSortList(tagEpcData);
                            if(thisscanSort==null)//????????????
                            // if (!checkPaymentSackList(tagEpcData))//????????????
                            {
                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"?????????????????????,????????????",false);
                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"?????????????????????,????????????");
                                Util.playErr();
                                return;
                            }
                            isgood++;
                            addRsinfo("???????????????:" + tagEpcData.getTagid() + "????????????",true);
                            mylog.Write("???????????????:" + tagEpcData.getTagid() + "????????????");
                            Util.playOk();
                            saninfo.setText("?????????"+isgood+"???");
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
                            transferPackInfo.setBankCode(transferCMD.getOutDetailList()[0].getOrganID());//????????????????????????ID
                            transferPackInfo.setBankName(transferCMD.getOutDetailList()[0].getOrganName());
                            transferPackInfo.setEditionCode("");
                            transferPackInfo.setEditionName("");

                            transferPackInfoList.add(transferPackInfo);

                        }else
                        {
                            mylog.Write("?????????????????????epc="+tagmessage);
                        }
                        break;
                    case INFO_MES:
                        String msgstr=msg.getData().getString("message").toString();
                        mylog.Write(msgstr);
                        if(msgstr.equals("notag"))
                        {
                            addRsinfo("??????????????????",false);
                            Util.playErr();
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
        mylog.Write("???????????????");

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
        tv_header.setCenterString("??????????????????");
        tv_operinfo.setText("??????????????????????????????????????????????????????????????????");
        tv_footer.setText("??????????????????????????????????????????????????????????????????");
        line_kun.setVisibility(View.GONE);
    }
    //????????????????????????
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(transferPackInfoList.size()>0)//???????????????????????????????????????
            {
                transferPackInfo[] transferDatas=new transferPackInfo[transferPackInfoList.size()];
                transferPackInfoList.toArray(transferDatas);
                transferTransferData.setPackInfoList(transferDatas);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(transferTransferData);

                long timestamp=new Date().getTime();
                String timestampstr= DecimalTool.addZeroForNum(Long.toHexString(timestamp),16).toUpperCase();
                String datafilename="Data_"+businessid+"_"+timestampstr+".json";
                mylog.Write("????????????,????????????????????????="+datafilename);
                mylog.Write("??????????????????="+datajson);
                try {
                    tw.writeDataFile(this,datafilename,datajson.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    mylog.Write("????????????????????????,??????:"+e.getMessage());
                    Toast.makeText(this,"????????????????????????,??????:"+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
            finish();
        }else{
            Toast.makeText(this,"????????????????????????????????????",Toast.LENGTH_SHORT).show();
            mBackPressed=System.currentTimeMillis();
        }
    }

}
