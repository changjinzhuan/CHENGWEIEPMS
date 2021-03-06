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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.DailyCheckCMD;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.dailyCheckStock;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.DailyCheckData;
import cn.kcrxorg.chengweiepms.businessmodule.datainfo.DailyCheckPackInfo;
import cn.kcrxorg.chengweiepms.mbutil.DecimalTool;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;


public class NewDailyCheckActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;

    String businessid="";
    int isgood=0;

    DailyCheckCMD dailyCheckCMD;
    DailyCheckData dailyCheckData;

    List<DailyCheckPackInfo> dailyCheckPackInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        TXTReader tr = new TXTReader();
        businessid=getIntent().getStringExtra("businessid");
        String cmddata = tr.getCmdById(NewDailyCheckActivity.this, businessid);

        dailyCheckCMD= JSONObject.parseObject(cmddata, DailyCheckCMD.class);
        dailyCheckData=new DailyCheckData();
        dailyCheckData.setCode(163846);
        dailyCheckData.setError("");

        dailyCheckPackInfoList=new ArrayList<DailyCheckPackInfo>();
        allcarddata = new ArrayList<String>();

        line_businfo.setOrientation(LinearLayout.VERTICAL);//??????????????????
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);

        String  cmdinfo="????????????:\r\n";
        cmdinfo+="?????????: "+dailyCheckCMD.getStockList().length+"???";
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

        //???????????????????????????
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(dailyCheckStock stockPackInfo:dailyCheckCMD.getStockList())
        {
            //   tagidlist.add(stockPackInfo.getSackNo());
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
                            //?????????????????????
//                            if (!tagEpcData.getLockstuts().equals("Lock"))
//                            {
//                                addRsinfo("???????????????:"+tagEpcData.getTagid()+"?????????????????????????????????",false);
//                                mylog.Write("???????????????:"+tagEpcData.getTagid()+"?????????????????????????????????");
//                                Util.playErr();
//                                return;
//                            }
                            dailyCheckStock thisdailyCheckStock=checkJobInfo(tagEpcData);
                            if(thisdailyCheckStock==null)
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
                            DailyCheckPackInfo dailyCheckPackInfo = new DailyCheckPackInfo();
                            dailyCheckPackInfo.setSackNo(tagEpcData.getTagid()+"");
                            dailyCheckPackInfo.setVal(thisdailyCheckStock.getVal());
                            dailyCheckPackInfo.setPaperTypeID(thisdailyCheckStock.getPaperTypeID());
                            dailyCheckPackInfo.setPaperTypeName(thisdailyCheckStock.getPaperTypeName());
                            dailyCheckPackInfo.setVoucherTypeID(thisdailyCheckStock.getVoucherTypeID());
                            dailyCheckPackInfo.setVoucherTypeName(thisdailyCheckStock.getVoucherTypeName());
                            dailyCheckPackInfo.setBundles(thisdailyCheckStock.getBundles());
                            dailyCheckPackInfo.setTie(thisdailyCheckStock.getTie());
                            dailyCheckPackInfo.setSackMoney(thisdailyCheckStock.getSackMoney());
                            dailyCheckPackInfo.setSstackCode(thisdailyCheckStock.getSstackCode());
                            dailyCheckPackInfo.setSstackName(thisdailyCheckStock.getSstackName());
                            dailyCheckPackInfo.setStatus(tagEpcData.getLockstuts().equals("Lock")?"1":"0");

                            dailyCheckPackInfoList.add(dailyCheckPackInfo);

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

    private dailyCheckStock checkJobInfo(TagEpcData tagEpcData) {
        for(dailyCheckStock d:dailyCheckCMD.getStockList())
        {
            if(d.getSackNo().equals(tagEpcData.getTagid()+""))
            {
                return d;
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

            if(dailyCheckPackInfoList.size()>0)//???????????????????????????????????????
            {
                DailyCheckPackInfo[] dailyCheckPackInfos=new DailyCheckPackInfo[dailyCheckPackInfoList.size()];
                dailyCheckPackInfoList.toArray(dailyCheckPackInfos);
                dailyCheckData.setPackInfoList(dailyCheckPackInfos);

                TXTWriter tw=new TXTWriter();
                String datajson= JSON.toJSONString(dailyCheckData);

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
