package cn.kcrxorg.chengweiepms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.xuexiang.xutil.app.AppUtils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.User;
import cn.kcrxorg.chengweiepms.businessmodule.mapper.UserMapper;
import cn.kcrxorg.chengweiepms.mbutil.MyLog;
import cn.kcrxorg.chengweiepms.mbutil.XToastUtils;
import cn.kcrxorg.chengweiepms.setting.NewSettingActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView username;
    TextView password;
    TextView tv_vision;
    Button login;
    UserMapper userMapper;
    MyLog myLog;
    public RFIDWithUHFUART mReader;//读头
    //扫描捆号过滤
    public List<String> allcarddata;

    Handler mHandler;
    ImageView iv_kcrx;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        login=findViewById(R.id.login);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        tv_vision=findViewById(R.id.tv_vision);
        userMapper=new UserMapper(this);
        myLog=new MyLog(this,10000,1);

        tv_vision.setText("V"+ AppUtils.getAppVersionName()+"Debug");
        login.setEnabled(true);
        login.setOnClickListener(this);

        //初始化声音
        Util.initSoundPool(this);
      //  initUHF();//启动天线

        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case 1://刷卡信息
                       String tagmessage= msg.getData().getString("tagmessage");
                        myLog.Write("读取到卡号:"+tagmessage);
                        myLog.Write("读取到卡号="+tagmessage);
                        if(tagmessage.length()<10)//不是工作证卡
                        {
                            XToastUtils.error(tagmessage+"不是工作证卡，请检查");
                            Util.playErr();
                            break;
                        }
                        if(username.getText().toString().equals(""))
                        {
                            username.setText(tagmessage.substring(0,10));
                            Util.playOk();
                        }else if(!username.getText().toString().equals(""))
                        {
                            String psw=tagmessage.substring(0,10);
                            myLog.Write("psw="+psw);
                            myLog.Write("username="+username.getText().toString().trim());
                            if(psw.equals(username.getText().toString().trim())||psw==username.getText().toString().trim())
                            {
                                XToastUtils.error(tagmessage.substring(0,10)+"重复刷卡!");
                                Util.playErr();
                              break;
                            }
                            password.setText(tagmessage.substring(0,10));
                            Util.playOk();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        iv_kcrx=findViewById(R.id.iv_kcrx);
        iv_kcrx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingintent = new Intent(LoginActivity.this, NewSettingActivity.class);
                startActivity(settingintent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.login:
                String usernamecid=username.getText()+"";
                String passwordcid=password.getText()+"";
                User oper=userMapper.getUseruid(usernamecid);
                User chkr=userMapper.getUseruid(passwordcid);
                  if(oper!=null&&chkr!=null)
                  {
                    myLog.Write("用户登录成功!");
                    String welcome = getString(R.string.welcome);
                    Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
                    Intent mainintent=new Intent(LoginActivity.this, MainActivity.class);
                    mainintent.putExtra("operator",oper.getCid());
                    mainintent.putExtra("auditor",chkr.getCid());
                    startActivity(mainintent);
                    }else
                    {
                        myLog.Write("游客登录成功!");
                        Intent mainintent=new Intent(LoginActivity.this, MainActivity.class);
                        mainintent.putExtra("operator","00000000");
                        mainintent.putExtra("auditor","00000000");
                        startActivity(mainintent);
                    }
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mReader!=null)
        {
            mReader.free();
        }
        username.setText("");
        password.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUHF();//启动天线

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReader!=null)
        {
            mReader.free();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case 139://读取扳机
                //mylog.Write("keyCode="+keyCode);
             //   XToastUtils.("扫描按钮按下", false);
                readCard();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void readCard() {
        int power=10;
        myLog.Write("UHF开始扫描....启动功率为:"+power);
        mReader.setPower(power);
        if(!mReader.startInventoryTag())
        {
            myLog.Write("UHF开始扫描失败");
            mReader.stopInventory();
            return;
        }
        //扫描线程启动...
        new TagThread().start();
    }
    private void stopInventory()//停止扫描
    {
        if (mReader.stopInventory()) {
            myLog.Write("识别已停止");
        }
    }
    boolean isinventoryFlag=false;
    //读取线程启动...
    class TagThread extends Thread
    {
        @Override
        public void run() {
            super.run();
            myLog.Write("读取线程已经启动...");
            if(isinventoryFlag)
            {
             //   addRsinfo("正在读取,请勿重复按扫描键",false);
                return;
            }
            isinventoryFlag=true;
            String strEPC;
            UHFTAGInfo res = null;
            int times=0;
            while(times<20)
            {
                //  mylog.Write("扫描中....");
                //    Log.e("kcrx","扫描中...");
                delay(10);
                times++;
                res = mReader.readTagFromBuffer();
                if (res != null)
                {
                    strEPC = res.getEPC();
                    myLog.Write("扫描到卡号:" + strEPC);
                    Message message = new Message();
                    message.what = 1;
                    Bundle data = new Bundle();
                    data.putString("tagmessage", strEPC);
                    message.setData(data);
                    mHandler.sendMessage(message);
                }
            }
            isinventoryFlag=false;
            stopInventory();
        }
    }
    private void delay(int delay)
    {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            myLog.Write(this.getClass() + "启动失败:" + ex.getMessage());
            XToastUtils.error(ex.getMessage());
            return;
        }

        if (mReader != null) {
            new InitTask().execute();
        }
    }

    /**
     * �豸�ϵ��첽��
     *
     * @author liuruifeng
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            return mReader.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (!result) {
              //  addRsinfo("天线初始化失败",false);
                XToastUtils.error("天线初始化失败");
                myLog.Write("天线初始化失败");
            } else {
              //  showToast("天线初始化成功");
                myLog.Write("天线初始化成功");
                XToastUtils.success("天线初始化成功");
                //  addRsinfo("天线初始化",false);
            }

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(LoginActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在初始化...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }
    //两次退出返回登录
    private static final int TIME_EXIT = 2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_EXIT > System.currentTimeMillis()) {
            super.onBackPressed();
//            Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(loginintent);
            finish();
        } else {
            Toast.makeText(this, "再点击一次返回退出登录", Toast.LENGTH_SHORT).show();
            mBackPressed = System.currentTimeMillis();
        }
    }
}
