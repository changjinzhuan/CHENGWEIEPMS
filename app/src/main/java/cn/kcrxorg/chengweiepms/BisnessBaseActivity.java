package cn.kcrxorg.chengweiepms;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.warkiz.widget.IndicatorSeekBar;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.dialog.materialdialog.GravityEnum;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.textview.autofit.AutoFitTextView;
import com.xuexiang.xui.widget.textview.supertextview.SuperButton;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import com.xuexiang.xui.widget.toast.XToast;

import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.chengweiepms.locktools.LockUtil;
import cn.kcrxorg.chengweiepms.mbutil.ClickUtil;
import cn.kcrxorg.chengweiepms.mbutil.MyLog;
import cn.kcrxorg.chengweiepms.mbutil.XToastUtils;
import cn.kcrxorg.chengweiepms.views.CmdInfoListView;

public class BisnessBaseActivity extends AppCompatActivity {

    SuperTextView tv_header;
    AutoFitTextView tv_operinfo;
    TextView tv_footer;
    TextView tv_kuncount;

    LinearLayout line_businfo;
    LinearLayout line_kun;
    LinearLayout line_rsinfo;
    LinearLayout line_power;
    ScrollView scro_businfo;
    ScrollView line_scorllrsinfo;

    SuperButton btn_lock;
    SuperButton btn_unlock;
    LinearLayout line_lockopbtn;

    Spinner sp_stackInfo;//????????????
    //??????
    private TextToSpeech textToSpeech;
    public MyLog mylog;

    public Handler mHandler;//????????????
    public static final int uhfwhat=1;//????????????
    public static final int INFO_MES = 0;//????????????
    public static final int LOCK_WHAT=2;//???????????????
    public static final int LOCK_LOG_WHAT=3;//????????????
    //UHF??????
 //   public UHFHelper uhfHelper;
  //  public LockHelper lockHelper;
    public LockUtil lockHelper;
    public RFIDWithUHFUART mReader;//??????

    //???????????????????????????;
    public List<String> tagidlist;
    //??????????????????
    public  List<String> allcarddata;

    //??????
    public int kuncount=20;

    //????????????
    private IndicatorSeekBar sbpower;//????????????
    public SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    //??????????????????
    List<ViewCmdInfo> viewCmdInfoList;
    ProgressDialog mypDialog;
    
    //?????????????????????
   // boolean isOpLock=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bisness_base);

        tv_header=findViewById(R.id.tv_header);
        tv_operinfo=findViewById(R.id.tv_operinfo);
        tv_footer=findViewById(R.id.tv_footer);
        tv_kuncount=findViewById(R.id.tv_kuncout);
        scro_businfo=findViewById(R.id.scro_businfo);

        line_businfo=findViewById(R.id.line_businfo);
        line_kun=findViewById(R.id.line_kun);
        line_rsinfo=findViewById(R.id.line_rsinfo);
        line_scorllrsinfo=findViewById(R.id.line_scorllrsinfo);

        //????????????
        line_power=findViewById(R.id.line_power);
        sbpower=findViewById(R.id.seekBar_power);
        mSharedPreferences = getSharedPreferences("UHF",MODE_PRIVATE);

        btn_lock=findViewById(R.id.btn_lock);
        btn_unlock=findViewById(R.id.btn_unlock);
        line_lockopbtn=findViewById(R.id.line_lockopbtn);

        btn_lock.setOnClickListener(view -> {
            mylog.Write("??????????????????");
            closeLock();
        });
        btn_unlock.setOnClickListener(view->{
            mylog.Write("??????????????????");
            openLock();
        });

        //???????????????
        Util.initSoundPool(this);
        //????????????UHF??????????????????????????????psam
        try {
            lockHelper=new LockUtil();
        } catch (Exception e) {
            addRsinfo("???????????????????????????????????????",false);
            e.printStackTrace();
        }
        //initUHF();
        //???????????????
        mylog=new MyLog(this,10000,1);
        //mylog.Write(this.getClass()+"???????????????*****************************");

        //????????????
        //??????????????????
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.rfid.FUN_KEY");
         // registerReceiver(keyReceiver,filter);

        //???????????????
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE){
                       mylog.Write( "TTS???????????????????????????????????????");
                    }
                }
            }

        });
        tv_operinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(tv_operinfo.getText().toString(),
                        TextToSpeech.QUEUE_FLUSH, null);
            }
        });

       if(MyApp.ScanLock)
        {
            tv_header.setLeftIcon(R.mipmap.scanlock_foreground);
        }else
        {
            tv_header.setLeftIcon(R.mipmap.scanunlock_foreground);
        }
        tv_header.setLeftImageViewClickListener(new SuperTextView.OnLeftImageViewClickListener() {
            @Override
            public void onClick(ImageView imageView) {
                if(MyApp.ScanLock)
                {
                    MyApp.ScanLock =false;
                    XToastUtils.success("?????????????????????....");
                    tv_header.setLeftIcon(R.mipmap.scanunlock_foreground);
                }else
                {
                    MyApp.ScanLock =true;
                    XToastUtils.error("??????????????????...");
                    tv_header.setLeftIcon(R.mipmap.scanlock_foreground);
                }

            }
        });
        tv_header.setRightTvClickListener(new SuperTextView.OnRightTvClickListener() {
            @Override
            public void onClick(TextView textView) {
                if(viewCmdInfoList==null)
                {
                    XToastUtils.error("?????????????????????????????????!");
                    return;
                }
                String[] cmdstrs=new String[viewCmdInfoList.size()];
                if(viewCmdInfoList.size()>0)
                {
                    CharSequence[] items=new CharSequence[viewCmdInfoList.size()];
                    int i=0;
                    for(ViewCmdInfo viewCmdInfo:viewCmdInfoList)
                    {
                       // items[i]=("??????:"+viewCmdInfo.getSackNo()+"\n??????:"+viewCmdInfo.getPaperTypeName()+"\n??????:"+viewCmdInfo.getVoucherTypeName());
                        items[i]= Html.fromHtml("<b>"+"??????:"+viewCmdInfo.getSackNo()+"</b><br>??????:"+viewCmdInfo.getPaperTypeName()+"<br>??????:"+viewCmdInfo.getVoucherTypeName());
                        i++;
                    }
//                    AlertDialog.Builder builder = new AlertDialog.Builder(BisnessBaseActivity.this);
//                    builder.setItems(items,null);
//                    builder.setTitle("??????????????????"+"??????:"+viewCmdInfoList.size());
//                    builder.setNegativeButton("??????",null);
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
                    CmdInfoListView cmdInfoListView=new CmdInfoListView(BisnessBaseActivity.this,viewCmdInfoList);
                    new MaterialDialog.Builder(BisnessBaseActivity.this)
                            .titleGravity(GravityEnum.CENTER)
                            .titleColorRes(R.color.xpage_default_actionbar_color)
                            .customView(cmdInfoListView,false)
                            .title("??????????????????"+"??????:"+viewCmdInfoList.size())
                            .positiveText("??????")
                            .show();
                }
                else
                {
                    XToast.error(BisnessBaseActivity.this,"?????????????????????");
                }
            }
        });
    }

    //key receiver
    private  long startTime = 0 ;
    private boolean keyUpFalg= true;
    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            //   if (f1hidden) return;
            int keyCode = intent.getIntExtra("keyCode", 0) ;
            if(keyCode == 0){//H941
                keyCode = intent.getIntExtra("keycode", 0) ;
            }
            Log.e("key ","keyCode = " + keyCode) ;
            boolean keyDown = intent.getBooleanExtra("keydown", false);
//			Log.e("key ", "down = " + keyDown);
            if(keyUpFalg&&keyDown && System.currentTimeMillis() - startTime > 500){
                keyUpFalg = false;
                startTime = System.currentTimeMillis() ;
                mylog.Write("????????????keyCode="+keyCode);
                if (keyCode == KeyEvent.KEYCODE_F3) {
                    if(MyApp.ScanLock)
                    {
                        Util.playErr();
                        XToastUtils.error("?????????????????????????????????????????????...");
                        return;
                    }
                    readCard();
                }
                if(keyCode==KeyEvent.KEYCODE_F1)
                {
                   // addRsinfo("??????????????????...",true);
                    if(tagidlist==null)
                    {
                        addRsinfo("???????????????????????????",false);
                        return;
                    }
                    new operateLockTask().execute(true);
                 //  operateLockGetrs(true);
                }
                if(keyCode==KeyEvent.KEYCODE_F2)
                {
                   // addRsinfo("??????????????????...",true);
                    if(tagidlist==null)
                    {
                        addRsinfo("???????????????????????????",false);
                        return;
                    }
                    new operateLockTask().execute(false);
                   // operateLockGetrs(false);
                }
                return ;
            }else if (keyDown){
                startTime = System.currentTimeMillis() ;
            }else {
                keyUpFalg = true;
            }

        }
    } ;
    public void setLog(String log)
    {
        mylog.Write(log);
        Log.e(this.getLocalClassName(),log);
    }
    public void readCard() {
          //??????????????????...
        new ReadTagTask().execute();
    }
    public void stopInventory()//????????????
    {
        if (mReader.stopInventory()) {
            mylog.Write("???????????????");
        }
    }
    public class operateLockTask extends AsyncTask<Boolean, Integer, Boolean>
    {
        ProgressDialog mypDialog;
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean)
            {
                mypDialog.setMessage("???????????????!");
            }else
            {
                mypDialog.setMessage("??????????????????");
            }
            mypDialog.cancel();
        }


        @Override
        protected Boolean doInBackground(Boolean... booleans) {
          //  mylog.Write("???????????????="+booleans[0]);
            try {
                operateLockGetrs(booleans[0]);
                return true;
            }catch (Exception e)
            {
               mylog.Write("???????????????"+e.getMessage());
               return false;
            }


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mypDialog = new ProgressDialog(BisnessBaseActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("????????????????????????...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
    public class ReadTagTask extends AsyncTask<String, Integer, Boolean>
    {
        ProgressDialog mypDialog;
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mypDialog.cancel();
            if(mReader!=null)
                mReader.free();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mReader = RFIDWithUHFUART.getInstance();
            } catch (ConfigurationException e) {
                mylog.Write("mReader.getInstance().error="+e.toString());
                e.printStackTrace();
            }
            mylog.Write("mReader.init()="+mReader.init());
            int nowpower=mSharedPreferences.getInt("readPower",15);
            mReader.setPower(nowpower);
            mylog.Write("UHF????????????....???????????????:"+nowpower);
            if(!mReader.startInventoryTag())
            {
                mylog.Write("UHF??????????????????");
                mReader.stopInventory();
                return false;
            }
            mylog.Write("????????????????????????...");
            boolean hastag=false;
            if(isinventoryFlag)
            {
                addRsinfo("????????????,????????????????????????",false);
                return false;
            }
            isinventoryFlag=true;
            String strEPC;
            UHFTAGInfo res = null;
            int times=0;
            while(times<100)
            {
                delay(10);
                times++;
                res = mReader.readTagFromBuffer();
                if (res != null)
                {
                    strEPC = res.getEPC();
                    mylog.Write("??????????????????:" + strEPC);
                    Message message = new Message();
                    message.what = 1;
                    Bundle data = new Bundle();
                    data.putString("tagmessage", strEPC);
                    message.setData(data);
                    mHandler.sendMessage(message);
                    hastag=true;
                }
            }
            if(hastag==false)
            {
                Message message = new Message();
                message.what = 0;
                Bundle data = new Bundle();
                data.putString("message", "notag");
                message.setData(data);
                mHandler.sendMessage(message);
            }
            isinventoryFlag=false;
            stopInventory();
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mypDialog = new ProgressDialog(BisnessBaseActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("?????????????????????...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
    boolean isinventoryFlag=false;
    //??????????????????...
    class TagThread extends Thread
    {
        @Override
        public void run() {
            super.run();
            mylog.Write("????????????????????????...");
            boolean hastag=false;
            if(isinventoryFlag)
            {
               addRsinfo("????????????,????????????????????????",false);
               return;
            }
            isinventoryFlag=true;
            String strEPC;
            UHFTAGInfo res = null;
            int times=0;
            while(times<100)
            {
                delay(10);
                times++;
                res = mReader.readTagFromBuffer();
                if (res != null)
                {
                    strEPC = res.getEPC();
                    mylog.Write("??????????????????:" + strEPC);
                    Message message = new Message();
                    message.what = 1;
                    Bundle data = new Bundle();
                    data.putString("tagmessage", strEPC);
                    message.setData(data);
                    mHandler.sendMessage(message);
                    hastag=true;
                }
            }
            if(hastag==false)
            {
                Message message = new Message();
                message.what = 0;
                Bundle data = new Bundle();
                data.putString("message", "notag");
                message.setData(data);
                mHandler.sendMessage(message);
            }
            isinventoryFlag=false;
            stopInventory();
        }
    }
    public void delay(int delay)
    {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //show tips
    private Toast toast;
    private void showToast(String info) {
        if (toast==null) toast =  Toast.makeText(this, info, Toast.LENGTH_SHORT);
        else toast.setText(info);
        toast.show();
    }
    public void addRsinfo(final String message, boolean rs)
    {

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        AlwaysMarqueeTextView rstv=new AlwaysMarqueeTextView(this);
        rstv.setText(message);
        rstv.setTextSize(18);
        rstv.setMarqueeRepeatLimit(Integer.MAX_VALUE);
//        rstv.setFocusable(true);
        rstv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        rstv.setSingleLine();
//        rstv.setFocusableInTouchMode(true);
//        rstv.setHorizontallyScrolling(true);

        rstv.setLayoutParams(params);
        rstv.setAnimation(AnimationUtils.makeInAnimation(this,true));
        if(rs)
        {
            rstv.setBackground(getDrawable(R.drawable.tv_goodinfo));

        }else
        {
            rstv.setBackground(getDrawable(R.drawable.tv_badinfo));

        }
        //??????????????????
        rstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(message,
                        TextToSpeech.QUEUE_ADD, null);
            }
        });
        line_rsinfo.addView(rstv);
       // line_scorllrsinfo.fullScroll(ScrollView.FOCUS_DOWN);

        line_scorllrsinfo.post(new Runnable() {
            public void run() {
                line_scorllrsinfo.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    public void operateLockGetrs(boolean lock)
    {

    }
    public  Boolean checkrepeat(String nowcarddata)
    {
        for (String s : allcarddata)
        {
            if (s == nowcarddata || s.equals(nowcarddata))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReader!=null)
        {
            mReader=null;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mylog.Write("????????????keyCode="+keyCode);
        switch (keyCode)
        {
            case 136://?????????????????????
                int nowpower=mSharedPreferences.getInt("readPower",10);
                if(line_power.getVisibility()==View.GONE)
                {
                    line_power.setVisibility(View.VISIBLE);
                    sbpower.setProgress(nowpower);
                    showToast("???????????????????????????????????????????????????????????????");
                }else
                {
                    //int nowpower=mSharedPreferences.getInt("readPower",10);
                    showToast("?????????????????????:"+sbpower.getProgress());
                    mEditor=mSharedPreferences.edit();
                    mEditor.putInt("readPower", sbpower.getProgress());
                    mEditor.commit();
                    line_power.setVisibility(View.GONE);
                }
                break;
            case 139://????????????
                if(MyApp.ScanLock)
                {
                    Util.playErr();
                    XToastUtils.error("?????????????????????????????????????????????...");
                    break;
                }
                readCard();
                break;
            case 230://???????????? ??????
                closeLock();
                break;
            case 231://???????????? ??????
                openLock();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                kuncount--;
                mylog.Write("????????????");
                tv_kuncount.setText(kuncount+"");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                kuncount++;
                mylog.Write("????????????");
                tv_kuncount.setText(kuncount+"");
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            if(sp_stackInfo!=null)
            {
                sp_stackInfo.setSelection(sp_stackInfo.getSelectedItemPosition()-1);
            }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(sp_stackInfo!=null)
                {
                    sp_stackInfo.setSelection(sp_stackInfo.getSelectedItemPosition()+1);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            mylog.Write(this.getClass() + "????????????:" + ex.getMessage());
            addRsinfo(ex.getMessage(),false);
            return;
        }
        if (mReader != null) {
            new InitTask().execute();
        }
    }

    /**
     * ??????????????????????????
     *
     * @author liuruifeng
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
           // return true;
            return mReader.init();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (!result) {
                addRsinfo("???????????????",false);
                mylog.Write("???????????????");
            } else {
                int nowpower=mSharedPreferences.getInt("readPower",10);
                mReader.setPower(nowpower);
                showToast("???????????????");
                mylog.Write("???????????????");
              //  addRsinfo("???????????????",false);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(BisnessBaseActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("???????????????...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
  public void sendLockMes(android.os.Handler mHandler, String mes, int what) {
        // mylog.Write???mes);
        Message message = new Message();
        message.what = what;
        Bundle data = new Bundle();
        data.putString("lockmessage", mes);
        message.setData(data);
        mHandler.sendMessage(message);
    }public void setGoodViewCmdInfo(String sackno)
    {
        for(ViewCmdInfo v:viewCmdInfoList)
        {
            if(v.getSackNo().equals(sackno))
            {
                v.setDone(true);
            }
        }
    }

    private void closeLock()
    {
        if(MyApp.isOpLock)
        {
            XToastUtils.error("???????????????????????????....");
            return;
        }
        if(mReader!=null)
        {
            mylog.Write("?????????UHF="+  mReader.free());
        }
        MyApp.isOpLock=true;
        mylog.Write("???????????????:isOpLock="+ MyApp.isOpLock);
      //  btn_lock.setEnabled(false);
        if(ClickUtil.isFastClick())//????????????????????????
        {
            mylog.Write("???????????????....");
            return;
        }
        if (tagidlist == null) {
            addRsinfo("???????????????????????????", false);
            MyApp.isOpLock=false;
            return;
        }
        try
        {
            operateLockGetrs(true);
        }catch (Exception e)
        {
            mylog.Write("??????????????????:"+e);
            addRsinfo("????????????", false);
            MyApp.isOpLock=false;
        }
    }

    private void openLock()
    {
        if(MyApp.isOpLock)
        {
            XToastUtils.error("???????????????????????????....");
            return;
        }
        if(mReader!=null)
        {
            mylog.Write("?????????UHF="+  mReader.free());
        }
        MyApp.isOpLock=true;
        mylog.Write("???????????????:isOpLock="+ MyApp.isOpLock);
       // btn_unlock.setEnabled(false);
        if(ClickUtil.isFastClick())//????????????????????????
        {
            mylog.Write("?????????????????????????????????...");
            return;
        }
        if (tagidlist == null) {
            addRsinfo("???????????????????????????", false);
            MyApp.isOpLock=false;
            return;
        }
        try {
            operateLockGetrs(false);
        }catch (Exception e)
        {
            mylog.Write("??????????????????:"+e);
            addRsinfo("????????????...", false);
            MyApp.isOpLock=false;
        }
    }

}
