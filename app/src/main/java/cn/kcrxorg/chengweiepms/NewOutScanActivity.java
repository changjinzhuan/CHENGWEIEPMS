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

        line_businfo.setOrientation(LinearLayout.VERTICAL);//??????????????????
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        params.topMargin=2;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);

        String  cmdinfo="????????????:\r\n";

        for(outScanBusiInfo outScanBusiInfo:outScanCMD.getBusiInfoList())
        {
            cmdinfo+=outScanBusiInfo.getPaperTypeName()+" "+outScanBusiInfo.getVoucherTypeName()+" "+ DecimalTool.formatTosepara(outScanBusiInfo.getTotalMoney())+"???\r\n";
            outScanBusiInfo isout=new outScanBusiInfo();
            isout.setTotalMoney(new BigDecimal(0));
            isout.setPaperTypeID(outScanBusiInfo.getPaperTypeID());
            isout.setPaperTypeName(outScanBusiInfo.getPaperTypeName());
            isout.setVal(outScanBusiInfo.getVal());
            isout.setVoucherTypeID(outScanBusiInfo.getVoucherTypeID());
            isout.setVoucherTypeName(outScanBusiInfo.getVoucherTypeName());
            isScanoutScanBusiInfoList.add(isout);
        }
       //?????????????????????
        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        params1.weight=1;

        boolean visible=false;
        btn_mustout=new Button(this);
        btn_mustout.setText("?????????????????????");
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
                    if(s.getMustOutFlag().equals("1"))//??????????????????
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
                    builder.setMessage("????????????!");
                }
                builder.setTitle("???????????????");
                builder.setNegativeButton("??????",null);
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

        String saninfostr="?????????:  0???";

        saninfo.setText(saninfostr);

        saninfo.setTextSize(18);
        saninfo.setLayoutParams(params);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);
//???????????????????????????
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
                    case 1://???????????????
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//????????????
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
                        if(tagEpcData!=null&&tagEpcData.getTagid()>0)//???????????????????????????
                        {
                            mylog.Write("????????????????????????epc="+tagmessage+" ??????:"+tagEpcData.getTagid());
                            if (!tagEpcData.getLockstuts().equals("Lock"))
                            {
                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"?????????"+ LockStutsHelper.getStutsName(tagEpcData.getLockstuts())+"????????????",false);
                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"?????????"+LockStutsHelper.getStutsName(tagEpcData.getLockstuts())+"????????????");
                                Util.playErr();
                                return;
                            }
                            if(tagEpcData.getLockeEx())
                            {
                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"???????????????????????????",false);
                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"???????????????????????????");
                                Util.playErr();
                                return;
                            }
                            if(!checkStockList(tagEpcData))//????????????
                            {
                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"?????????????????????,????????????",false);
                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"?????????????????????,????????????");
                                Util.playErr();
                                return;
                            }
                            outScanStockPackInfo outScanStockPackInfo = selectScanStockPackInfo(tagEpcData);
                            outScanBusiInfo thisoutScanBusiInfo=  getoutScanBusiInfo(outScanStockPackInfo);
                            if(thisoutScanBusiInfo==null)
                            {
                                addRsinfo("???????????????:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"??????????????????",false);
                                mylog.Write("???????????????:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"??????????????????");
                                Util.playErr();
                                return;
                            }
//                            Log.e("kcrx","thisoutScanBusiInfo.getTotalMoney()="+thisoutScanBusiInfo.getTotalMoney());
//                            Log.e("kcrx","outScanStockPackInfo.getSackMoney()="+outScanStockPackInfo.getSackMoney());
//                            Log.e("kcrx","compareTo="+thisoutScanBusiInfo.getTotalMoney().compareTo(outScanStockPackInfo.getSackMoney()));

                            //???????????????????????????
                            int sackmoneyrs=thisoutScanBusiInfo.getTotalMoney().compareTo(outScanStockPackInfo.getSackMoney());
                            if(sackmoneyrs==-1)
                            {
                                addRsinfo("???????????????:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"??????????????????",false);
                                mylog.Write("???????????????:"+tagEpcData.getTagid()+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName()+"??????????????????");
                                Util.playErr();
                                return;
                            }
                            if(outScanStockPackInfo.getMustOutFlag().equals("0"))//??????????????????
                            {
                                mylog.Write("???????????????????????????????????????????????????");
                               if(thisTypeHasMustOut(outScanStockPackInfo))//?????????????????????????????????
                               {
                                   mylog.Write("???????????????:"+tagEpcData.getTagid()+" "+outScanStockPackInfo.getPaperTypeName()+outScanStockPackInfo.getVoucherTypeName()+"????????????????????????????????????????????????");
                                   addRsinfo("???????????????:"+tagEpcData.getTagid()+" "+outScanStockPackInfo.getPaperTypeName()+outScanStockPackInfo.getVoucherTypeName()+"????????????????????????????????????????????????",false);
                                   Util.playErr();
                                   return;
                               }
                            }

                            BigDecimal cmdSackMoney=thisoutScanBusiInfo.getTotalMoney().subtract(outScanStockPackInfo.getSackMoney());
                            //?????????????????????
                            setBusiInfoSackMoney(thisoutScanBusiInfo,cmdSackMoney);
                            isgood++;
                            addRsinfo("???????????????:" + tagEpcData.getTagid() + "????????????: "+outScanStockPackInfo.getPaperTypeName()+" "+outScanStockPackInfo.getVoucherTypeName(),true);
                            mylog.Write("???????????????:" + tagEpcData.getTagid() + "????????????");
                            Util.playOk();
                            saninfo.setText("?????????"+isgood+"???");

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

                            //?????????????????????????????????????????????
                            mylog.Write("???????????????????????????????????????");
                            mylog.Write("???????????????????????????"+outScanCMD.getStockPackInfoList().length);
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

                            mylog.Write("???????????????????????????"+outScanCMD.getStockPackInfoList().length);
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
    private void initView() {
        tv_header.setCenterString("??????????????????");
        tv_operinfo.setText("??????????????????????????????????????????????????????????????????");
        tv_footer.setText("??????????????????????????????????????????????????????????????????");
        line_kun.removeAllViews();
    }
    private boolean thisTypeHasMustOut(outScanStockPackInfo outScanStockPackInfo) {
        for(outScanStockPackInfo os:outScanCMD.getStockPackInfoList())
        {
            //??????????????????????????????????????????
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
            //?????????????????????????????????????????????
            if(outScanBusiInfo.getPaperTypeID().equals(thisoutScanBusiInfo.getPaperTypeID())&&outScanBusiInfo.getVoucherTypeID().equals(thisoutScanBusiInfo.getVoucherTypeID()))
            {
                outScanBusiInfo.setTotalMoney(cmdSackMoney);
            }
        }
        //????????????
        String  cmdinfo="????????????:\r\n";

        for(outScanBusiInfo outScanBusiInfo:outScanCMD.getBusiInfoList())
        {
            cmdinfo+=outScanBusiInfo.getPaperTypeName()+" "+outScanBusiInfo.getVoucherTypeName()+" "+ DecimalTool.formatTosepara(outScanBusiInfo.getTotalMoney())+"???\r\n";
        }

        tv_cmdinfo.setText(cmdinfo);
    }


    //????????????????????????
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(transferDataList.size()>0)//???????????????????????????????????????
            {
                transferData[] enterScanPackinfos=new transferData[transferDataList.size()];
                transferDataList.toArray(enterScanPackinfos);
                outScanData.setTransferDataList(enterScanPackinfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(outScanData);

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
