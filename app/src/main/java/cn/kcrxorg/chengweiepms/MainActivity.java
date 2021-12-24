package cn.kcrxorg.chengweiepms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.alibaba.fastjson.JSONObject;
import com.android.rfid.DevSettings;
import com.tencent.mmkv.MMKV;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;
import com.vilyever.socketclient.util.CharsetUtil;

import org.apache.ftpserver.ftplet.FtpException;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.User;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.UserCommand;
import cn.kcrxorg.chengweiepms.businessmodule.mapper.UserMapper;
import cn.kcrxorg.chengweiepms.communicationmodule.BaseCmd;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd1001;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd1002;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd1004;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd1005;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd2002;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd2003;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd2004;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd2005;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd4002;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd8001;
import cn.kcrxorg.chengweiepms.communicationmodule.Cmd8002;
import cn.kcrxorg.chengweiepms.communicationmodule.CmdSelector;
import cn.kcrxorg.chengweiepms.communicationmodule.L4vtype;
import cn.kcrxorg.chengweiepms.communicationmodule.Replay2004;
import cn.kcrxorg.chengweiepms.communicationmodule.Replay4002;
import cn.kcrxorg.chengweiepms.communicationmodule.Replay4003;
import cn.kcrxorg.chengweiepms.communicationmodule.Replay8001;
import cn.kcrxorg.chengweiepms.communicationmodule.Replay8002;
import cn.kcrxorg.chengweiepms.communicationmodule.Reply1001;
import cn.kcrxorg.chengweiepms.mbutil.Config;
import cn.kcrxorg.chengweiepms.mbutil.DESHelper;
import cn.kcrxorg.chengweiepms.mbutil.DateTimeUtil;
import cn.kcrxorg.chengweiepms.mbutil.HexUtil;
import cn.kcrxorg.chengweiepms.mbutil.MyLog;
import cn.kcrxorg.chengweiepms.mbutil.MyTools;
import cn.kcrxorg.chengweiepms.mbutil.NetChecker;
import cn.kcrxorg.chengweiepms.mbutil.SHA1Util;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;
import cn.kcrxorg.chengweiepms.mbutil.TXTWriter;
import cn.kcrxorg.chengweiepms.mbutil.UserLGtool;
import cn.kcrxorg.chengweiepms.mbutil.XToastUtils;
import cn.kcrxorg.chengweiepms.networkutil.NetStateChangeObserver;
import cn.kcrxorg.chengweiepms.networkutil.NetStateChangeReceiver;
import cn.kcrxorg.chengweiepms.networkutil.NetworkType;
import cn.kcrxorg.chengweiepms.networkutil.NetworkUtil;
import cn.kcrxorg.chengweiepms.psamtools.PsamTool;
import cn.kcrxorg.chengweiepms.views.BatteryView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NetStateChangeObserver {

    Button btn_connect;
    Button btn_select;
    Button btn_dobis;//执行待办
    Button btn_sacktrace;//签封追溯
    Button btn_locktest;//签封测试

    TextView tv_connectstate;
    TextView tv_loginstate;

    BatteryView mBatteryView;

    private SocketClient socketClient;
    private SocketClientDelegate delegate;

    private MyLog mylog;

//    private PsamCmdUtil psam;//pasm卡
//    private int psamCard = 1;//PSAM卡座号

    //默认加解密key
    private String skey = "817340064B866F15196F5934BC930462";//存储解密
    private String mkey = "DA8D9AC02FB413BD9E4CAD626850473F";//存储加密
    private String session = "91";
    private String sequence = "0000000000000000";
    //任务数据文件目录
    private static final String CmdDir = "/Cmd/";
    private static final String DataDir = "/Data/";

    File CmdDirF;
    File DataDirF;

    //本地数据库
    UserMapper userMapper;

    //登录的用户
    String operator = "00000000";
    String auditor = "00000000";

    //业务状态
    int businessstate = 0;//0 等待命令状态 1 提交状态

    //FTP服务
    UserLGtool userLGtool;

    //handler
    Handler mHandler;
    Config config;

    PsamTool psam;

    boolean netstate = false;//默认网络连接状态

    int recontimes = 0;

    NetChecker netChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        //初始化日志
        mylog = new MyLog(this, 10000, 1);
        mylog.Write("用户已登录！*****************************");
        Util.initSoundPool(this);

        intView();
        //启动ftp服务
        userLGtool = new UserLGtool();
        try {
            mylog.Write("正在启动ftp服务...");
            userLGtool.startFtpServer(this);
            mylog.Write("ftp服务启动成功！");
        } catch (FtpException e) {
            mylog.Write("ftp服务启动失败!" + e.getMessage());
            // e.printStackTrace();
        }
        //配置文件读取
        config = new Config(this);
        config.load();

        //生成数据文件目录
        File logRoot = getFilesDir();
        CmdDirF = new File(logRoot.getPath() + CmdDir);
        DataDirF = new File(logRoot.getPath() + DataDir);

        if (!CmdDirF.exists()) {
            CmdDirF.mkdir();
        }
        if (!DataDirF.exists()) {
            DataDirF.mkdir();
        }

        //初始化数据库
        userMapper = new UserMapper(this);
        //获取登录用户 UID
        String operatorCID = getIntent().getStringExtra("operator");
        String auditorCID = getIntent().getStringExtra("auditor");
        //   String operatorCID ="0000000003";
        //   String auditorCID = "0000000003";

        User operatorUser = userMapper.getUseruid(operatorCID);
        User auditorUser = userMapper.getUseruid(auditorCID);

        if (operatorUser != null && auditorUser != null) {
            operator = operatorUser.getUid();
            auditor = auditorUser.getUid();
        }


        mylog.Write("operator=" + operator);
        mylog.Write("auditor=" + auditor);
        if (!operator.equals("00000000")) {
            tv_loginstate.setText("操作员:" + operatorUser.getName() + " 已登录\r\n复核员:" + auditorUser.getName() + " 已登录");
            tv_loginstate.setTextColor(getResources().getColor(R.color.DarkGreen));
        }


        //启动网络调试
        Runtime mRuntime = Runtime.getRuntime();
        try {
            mRuntime.exec("setprop service.adb.tcp.port 5555 ");
            mRuntime.exec("stop adbd ");
            mRuntime.exec("start adbd ");
            Log.e("kcrx", "网络调试已经打开");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        mylog.Write("socket已断开,尝试重新连接..");
                        connect();
                        break;
                    case 7://提交消息
                        onClick(btn_select);
                        break;
                    case 8://Textview日志设置
                        String tvmsg = msg.getData().getString("tvmsg");
                        mylog.Write("通讯组件连接状态消息:" + tvmsg);
                        tv_connectstate.setText(tvmsg);
                        break;
                    case 9:
                        autoUpdata();
                        break;
                    case 100:
                        mylog.Write("网路状态:" + msg.getData().getBoolean("netstate"));
                        if (msg.getData().getBoolean("netstate")) {
                            btn_connect.setEnabled(true);
                            btn_select.setEnabled(true);
                            netstate = true;
                            recontimes = 0;
                            connect();
                            break;
                        }
                        netstate = false;
                        recontimes = 0;
                        btn_connect.setEnabled(false);
                        btn_select.setEnabled(false);
                        tv_connectstate.setText("网络已断开...");
                        tv_connectstate.setTextColor(getResources().getColor(R.color.red));
                        // mylog.Write("socket连接断开:"+client.getState());
                        //  tv_connectstate.setText("通讯组件未连接,请检查连接...");

                        //  btn_connect.setEnabled(true);
                        Util.playbreak();
                        break;
                }
            }
        };

//        NetStateChangeReceiver.registerReceiver(this);
//        mylog.Write("新的网络监控已经启动");
        //注册电池
        registerBattery();
//        //启动后就尝试连接...
//        if(NetworkUtil.getNetworkType(this)== NetworkType.NETWORK_NO)
//        {
//            mylog.Write("网络未连接...");
//            netstate=false;
//        }else
//        {
//            netstate=true;
//            connect();
//        }
    }

    private void intView() {
        btn_connect = findViewById(R.id.btn_connect);
        btn_select = findViewById(R.id.btn_select);
        btn_dobis = findViewById(R.id.btn_dobis);
        btn_sacktrace = findViewById(R.id.btn_sacktrace);
        btn_locktest = findViewById(R.id.btn_locktest);
        //隐藏锁测试按钮
       // btn_locktest.setVisibility(View.GONE);


        tv_connectstate = findViewById(R.id.tv_connectstate);
        tv_loginstate = findViewById(R.id.tv_loginstate);

        mBatteryView = findViewById(R.id.batteryView);

        btn_connect.setOnClickListener(this);
        btn_select.setOnClickListener(this);
        btn_dobis.setOnClickListener(this);
        btn_sacktrace.setOnClickListener(this);
        btn_locktest.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mylog.Write(this.getLocalClassName() + "已关闭!");
        closeGameSocket();
        if (socketClient != null) {
            socketClient.disconnect();
        }
        if (userLGtool != null) {
            userLGtool.stop();
        }
        unregisterBattery();
        //  NetStateChangeReceiver.unRegisterReceiver(MainActivity.this);
    }

    @Override
    protected void onPause() {
        mylog.Write("主界面已暂停!");

        closeGameSocket();
        if (socketClient != null) {
            if (socketClient.isConnected()) {
                socketClient.disconnect();
            }
        }
        netChecker.pause();
        mylog.Write("网络监控已停止...");
        //  NetStateChangeReceiver.unRegisterObserver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //启动网络检查
        netChecker = new NetChecker(mHandler, mylog);
        netChecker.start();
        mylog.Write("网络监控已经启动");
        //  NetStateChangeReceiver.registerObserver(this);
    }

    public void connect() {
        String serverip = MMKV.defaultMMKV().decodeString("serverurl", MyApp.DEFAULT_SERVER_URL);
        String serverport = MMKV.defaultMMKV().decodeString("serverport", MyApp.DEFAULT_SERVER_PORT);
        String info = serverip + "_" + serverport;
        try {
            if (socketClient != null) {
                if (socketClient.isConnecting()) {
                    mylog.Write("socket已经连接，不再重连");
                    return;
                }
                closeGameSocket();
            }
            mylog.Write("正在连接通讯组件:" + "ip=" + info);
            startSocketClient(info);
            tv_connectstate.setText(R.string.connecting);
            mylog.Write(getResources().getString(R.string.connecting));
        } catch (IOException e) {
            tv_connectstate.setText("连接失败！");
            mylog.Write("连接失败!:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect://连接按钮
                mylog.Write("手动点击连接按钮...");
                if (!socketClient.isDisconnected()) {
                    XToastUtils.error("通讯组件正在连接请稍后再试...");
                    mylog.Write("通讯组件正在连接请稍后再试...");
                    break;
                }
                mylog.Write("通讯组件未连接，手动连接重试...");
                connect();
                break;
            case R.id.btn_select://提交按钮
                if (operator.equals("00000000")) {
                    showToast("操作员未登录，不可办理业务！请操作员登录...");
                    break;
                }
                if (socketClient == null) {
                    showToast("通讯组件未连接，无法提交！请先连接通讯组件");
                    break;
                }
                if (!checkDatas()) {
                    break;
                }
                businessstate = 1;
                if (businessstate == 1)//如果是业务完成状态
                {
                    TXTReader tr = new TXTReader();
                    try {
                        //  tr.findLastFile(this,DataDir);
                        String data = tr.findLastFile(MainActivity.this, DataDir);
                        if (data == null) {
                            showToast("没有任务需要提交!");
                            break;
                        }
                        if (!data.equals(""))//如果最近任务存在
                        {
                            mylog.Write("任务结果data=" + data);
                            String cmdhex = data.substring(24);
                            byte[] cmdb = Tools.HexString2Bytes(cmdhex);
                            String cmd = new String(cmdb);
                            JSONObject jsonObject = JSONObject.parseObject(cmd);
                            int cmdcode = jsonObject.getIntValue("code");
                            mylog.Write("查找到业务数据code=" + cmdcode);
                            //测试提交刷卡
                            if (cmdcode == 163842)//如果出库任务
                            {
                                mylog.Write("是出入库业务准备提交刷卡信息！");
                                upDataCard();
                                setTv_connectstate("用户卡号数据提交中....");
                                //   tv_connectstate.setText("用户卡号数据提交中....");
                                Thread.sleep(1500);
                                mylog.Write("用户卡号数据提交中...等待1.5秒后再提交业务数据!");
                            }

                            String id = data.substring(0, 8);
                            Random rnd = new Random();
                            int fragmentint = rnd.nextInt(999999);
                            String fragment = String.format("%08d", fragmentint);
                            String timestamp = data.substring(8, 24);
                            data = data.substring(24);
                            Replay4002 replay4002 = new Replay4002();
                            int oper = Integer.parseInt(operator);
                            int aduit = Integer.parseInt(auditor);

                            String operhex = Tools.Bytes2HexString(HexUtil.intToBytes(oper), HexUtil.intToBytes(oper).length);
                            String aduithex = Tools.Bytes2HexString(HexUtil.intToBytes(aduit), HexUtil.intToBytes(aduit).length);
                            ;
                            mylog.Write("operhex=" + operhex);
                            mylog.Write("aduithex=" + aduithex);

                            String payloads = id + fragment + timestamp + new L4vtype(data) + operhex + aduithex;
                            mylog.Write("加密前数据" + payloads);
                            String mPayloads = DESHelper.encryptStr(payloads, skey);
                            String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                            String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                            mPayloads = mPayloads + MAC;

                            replay4002.setPayloads(mPayloads);

                            int len = replay4002.getPayloads().length() / 2;
                            String hex = Integer.toHexString(len).toUpperCase();
                            replay4002.setLength("F" + MyTools.addZeroForNum(hex, 7));
                            replay4002.setSequence(sequence);//
                            String reply4002data = replay4002.toDataString().toUpperCase();
                            mylog.Write("replay4002=" + reply4002data);
                            mylog.Write("replay4002长度检查=" + replay4002.checkLenth());
                            socketClient.sendData(Tools.HexString2Bytes(reply4002data));
                            mylog.Write("发送replay4002");
                            showToast("任务提交完成!id=" + id);

                        } else {
                            showToast("没有任务需要提交!");
                        }

                    } catch (Exception e) {
                        mylog.Write("提交失败！" + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
                break;
            case R.id.btn_dobis:
                //操作员过滤
                if (operator.equals("00000000")) {
                    showToast("操作员未登录，不可办理业务！请操作员登录...");
                    return;
                }
                mylog.Write("正在查找最近的业务文件");
                TXTReader tr = new TXTReader();
                try {
                    String cmddata = tr.findLastFile(this, CmdDir);//找到最近的任务
                    mylog.Write("找到最近的代办业务数据=" + cmddata);
                    String id = cmddata.substring(0, 8);
                    mylog.Write("收到待办业务指令，查找到待办业务ID=" + id);
                    doBusiness(id);
                } catch (Exception e) {
                    showToast("无待办业务!");
                    Util.playErr();
                    mylog.Write("无待办业务!执行失败" + e.toString());
                    //  e.printStackTrace();
                }
                break;
            case R.id.btn_sacktrace://测试页面
                //操作员过滤
                if (operator.equals("00000000")) {
                    showToast("操作员未登录，不可办理业务！请操作员登录...");
                    return;
                }
                Intent sacktraceintent = new Intent(MainActivity.this, SackTraceActivity.class);
                startActivity(sacktraceintent);

                break;
            case R.id.btn_locktest:
                Intent testintent = new Intent(MainActivity.this, ComActivity.class);
                startActivity(testintent);
                break;
            default:
                break;
        }
    }

    //socket通信
    private void startSocketClient(String info) throws IOException {
        String[] array = info.split("_");
        socketClient = new SocketClient();
        socketClient.getAddress().setRemoteIP(array[0]);//设置IP,这里设置的是本地IP
        socketClient.getAddress().setRemotePort(Integer.parseInt(array[1]));//设置端口
        socketClient.getAddress().setConnectionTimeout(15 * 1000);//设置超时时间
        socketClient.setCharsetName(CharsetUtil.UTF_8);//设置编码格式，默认为UTF-8
        socketClient.connect(); // 连接，异步进行

        socketClient.registerSocketClientDelegate(delegate = new SocketClientDelegate() {
            @Override
            public void onConnected(SocketClient client) {
                tv_connectstate.setText(R.string.connecting);
                //  tv_connectstate.setTextColor(getResources().getColor(R.color.green));
                mylog.Write("socket连接成功=" + client.getState());
                recontimes = 0;
                btn_connect.setEnabled(false);
            }

            @Override
            public void onDisconnected(SocketClient client) {

                mylog.Write("socket连接断开:" + client.getState());
                tv_connectstate.setText("通讯组件未连接,请检查连接...");
                tv_connectstate.setTextColor(getResources().getColor(R.color.red));
                btn_connect.setEnabled(true);
//                //重新连接
//                if(netstate)
//                {
//                    btn_connect.setEnabled(false);
//                    recontimes++;
//                    mylog.Write("网络已接通，尝试重新连接通讯组件...第"+recontimes+"次");
//                    tv_connectstate.setText("网络已接通，尝试重新连接通讯组件...第"+recontimes+"次");
//                    //延时
//                    new Thread(new Runnable(){
//
//                        public void run(){
//                            try {
//                                Thread.sleep(5000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                          sendTimeoutMessage();//延时5秒重新连接
//                        }
//                    }).start();
//                }else
//                {
//                    mylog.Write("网络未接通，不再尝试重新连接通讯组件...");
//                    tv_connectstate.setText("网络未接通...");
//                }
            }

            @Override
            public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                byte[] data = responsePacket.getData();
                mylog.Write("recv<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                mylog.Write("收到通讯组件data=" + TXTReader.byteArrToHex(data));
                BaseCmd baseCmd = CmdSelector.makeCmd(TXTReader.byteArrToHex(data));
                if (baseCmd != null) {
                    mylog.Write("收到通讯组件命令:" + baseCmd.getTransport());
                    if (!baseCmd.checkLenth())//命令检查未通过
                    {
                        mylog.Write("长度检查:命令长度错误");
                        if (true)//所有长度有问题命令都检查粘包
                        {
                            mylog.Write("可能粘包，拆分任务...");
                            try {
                                String hexdata = TXTReader.byteArrToHex(data);
                                int lenpayload4002 = Integer.parseInt(hexdata.substring(1, 8), 16);
                                String dataone = hexdata.substring(0, 28 + lenpayload4002 * 2);
                                String datatwo = hexdata.substring(28 + lenpayload4002 * 2);
                                mylog.Write("dataone=" + dataone);
                                mylog.Write("datatwo=" + datatwo);
                                BaseCmd cmdone = CmdSelector.makeCmd(dataone);
                                BaseCmd cmdtwo = CmdSelector.makeCmd(datatwo);
                                mylog.Write("cmdone=" + cmdone.getTransport());
                                mylog.Write("cmdtwo=" + cmdtwo.getTransport());

                                if (cmdone.checkLenth()) {
                                    executeCmd(cmdone, dataone);
                                }

                                if (cmdtwo.checkLenth()) {
                                    executeCmd(cmdtwo, datatwo);
                                }

                            } catch (Exception e) {
                                // mylog.Write("执行命令失败:" + Tools.Bytes2HexString(data, data.length));
                                mylog.Write("执行命令失败:" + e.getStackTrace());
                                showToast("执行命令失败!");
                            }

                        } else {
                            mylog.Write("长度检查:" + baseCmd.checkLenth());
                            showToast("通讯组件下发命令有误：数据长度错误");
                            closeGameSocket();
                        }
                    }
                    //命令长度检查通过，开始执行
                    else {
                        try {
                            executeCmd(baseCmd, TXTReader.byteArrToHex(data));
                        } catch (Exception e) {
                            mylog.Write("执行命令失败:" + TXTReader.byteArrToHex(data));
                            mylog.Write("执行命令失败原因:" + e.getMessage());
                            //  Log.e("kcrx", "执行命令失败+", e);
                            showToast("执行命令失败!");
                            closeGameSocket();
                        }
                    }
                } else {
                    mylog.Write("通讯组件下发命令有误！basecmd=null");
                    showToast("通讯组件下发命令有误:数据长度错误");
                    closeGameSocket();
                }
            }
        });
    }

    private void executeCmd(BaseCmd basecmd, String cmddata) throws Exception {
        int transport = Integer.parseInt(basecmd.getTransport());
        switch (transport) {
            case 1001://1001
                mylog.Write("收到1001指令:" + cmddata);
                Cmd1001 cmd1001 = new Cmd1001(basecmd);
                mylog.Write("1001版本号:" + cmd1001.getVersion());
                //开始PSAM操作
                psam = new PsamTool();
                if (!psam.pasmInit()) {
                    mylog.Write("PSAM卡上电失败");
                    pasmError();
                    break;
                }
                mylog.Write("PSAM卡上电成功");
                String result = null;
                //复位
                result = psam.reSet();
                if (result != null && !result.equals("F1")) {
                    mylog.Write("复位PSAM卡：" + result);
                } else {
                    mylog.Write("复位PSAM卡失败");
                    pasmError();
                    break;
                    //  Util.play(3, 0);
                }
                String psaminfo = "";
                result = psam.getPSAMinfo();
                if (result != null) {
                    psaminfo = result;
                    byte[] psaminfob = psaminfo.getBytes("utf-8");
                    psaminfo = Tools.Bytes2HexString(psaminfob, psaminfob.length);
                    mylog.Write("获取PSAM卡信息：" + psaminfo);
                } else {
                    mylog.Write("获取PSAM卡信息失败");
                }
                //   psam.closeRfid();
                mylog.Write("PSAM不下电");
                Reply1001 reply1001 = new Reply1001(psaminfo, Tools.Bytes2HexString(cmd1001.getVersion().getBytes(), cmd1001.getVersion().getBytes().length));
                String reply1001data = reply1001.getReplydata();
                mylog.Write("reply1001=" + reply1001data);
                mylog.Write("reply1001长度检查=" + reply1001.checkLenth());
                socketClient.sendData(Tools.HexString2Bytes(reply1001data));
                mylog.Write("发送reply1001");
                setTv_connectstate("初始化连接完成,等待通讯组件命令...");
                // tv_connectstate.setText("初始化连接完成,等待通讯组件命令...");
                break;
            case 1002:
                mylog.Write("收到1002指令:" + cmddata);
                Cmd1002 cmd1002 = new Cmd1002(basecmd);
                if (cmd1002.getLevel().equals("02")) {
                    setTv_connectstate("错误:" + cmd1002.getMessage());
                    //  tv_connectstate.setText("错误:" + cmd1002.getMessage());
                    mylog.Write("错误:" + cmd1002.getMessage());
                } else if (cmd1002.getLevel().equals("01")) {
                    setTv_connectstate("警告:" + cmd1002.getMessage());
                    //  tv_connectstate.setText("警告:" + cmd1002.getMessage());
                    mylog.Write("警告:" + cmd1002.getMessage());
                } else//正常
                {
                    mylog.Write("2001任务：正常");
                }
                break;
            case 4002:
                mylog.Write("收到4002响应:" + cmddata);
                Cmd4002 cmd4002 = new Cmd4002(basecmd, skey, mkey);
                mylog.Write("收到4002响应error=" + cmd4002.getError());
                if (cmd4002.getError().equals("") || cmd4002.getError() == null) {
                    setTv_connectstate("任务数据提交完成...");
                    // tv_connectstate.setText("任务数据提交完成...");
                    mylog.Write("任务数据提交完成...");
                } else if (cmd4002.getError().contains("提交id错误")) {
                    mylog.Write("提交响应:" + cmd4002.getError());
                    setTv_connectstate("提交响应:" + cmd4002.getError() + ",等待通讯组件命令...");
                    //  tv_connectstate.setText("提交响应:" + cmd4002.getError() + ",等待通讯组件命令...");
                } else {
                    mylog.Write("提交响应:" + cmd4002.getError());
                    setTv_connectstate("提交响应:" + cmd4002.getError());
                }
                break;
            case 4003:
                mylog.Write("收到4003响应:" + cmddata);
                Cmd4002 cmd4003 = new Cmd4002(basecmd, skey, mkey);
                String error4003 = cmd4003.getError().split("详细信息")[0];
                if (error4003.equals("") || error4003 == null) {
                    mylog.Write("收到4003响应error为空，刷卡验证通过");
                    setTv_connectstate("提交用户刷卡数据成功！");
                    //  tv_connectstate.setText("提交用户刷卡数据成功！");
                } else {
                    mylog.Write("收到4003响应error=" + cmd4003.getError());
                    setTv_connectstate("提交用户卡号响应:" + cmd4003.getError().split("详细信息")[0]);
                    // tv_connectstate.setText("提交用户卡号响应:" + cmd4003.getError().split("详细信息")[0]);
                }
                break;
            case 8001:
                mylog.Write("收到8001指令:" + cmddata);
                Cmd8001 cmd8001 = new Cmd8001(basecmd, skey, mkey);
                if (cmd8001.getAction().equals("01"))//上电
                {
                    mylog.Write("上电指令");
                    //开始PSAM操作
                    psam = new PsamTool();
                    if (!psam.pasmInit()) {
                        mylog.Write("PSAM卡上电失败");
                        pasmError();
                        break;
                    }
                    mylog.Write("PSAM卡上电成功");

                    String result8001 = null;
                    //复位
                    result8001 = psam.reSet();
                    if (result8001 != null && !result8001.equals("F1")) {
                        mylog.Write("复位PSAM卡：" + result8001);
                    } else {
                        mylog.Write("复位PSAM卡失败");
                        pasmError();
                        break;
                    }
                    if (basecmd.getLength().startsWith("7"))//加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setAtr(result8001);
                        replay8001.setSequence(basecmd.getSequence());
                        String payloads = "";
                        if ((replay8001.getAtr().length() / 2) <= 16)//如果ATR长度小于等于16
                        {
                            payloads = replay8001.getError() + MyTools.addZeroForNum(Integer.toHexString(replay8001.getAtr().length() / 2), 2) + replay8001.getAtr();
                        } else {
                            payloads = replay8001.getError() + Integer.toHexString(replay8001.getAtr().length() / 2) + replay8001.getAtr();
                        }
                        String mPayloads = DESHelper.encryptStr(payloads, skey);
                        String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                        String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                        mPayloads = mPayloads + MAC;
                        replay8001.setPayloads(mPayloads);
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("F" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                    } else//不加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setAtr(result8001);
                        replay8001.setSequence(basecmd.getSequence());

                        String payloads = "";
                        if ((replay8001.getAtr().length() / 2) <= 16)//如果ATR长度小于等于16
                        {
                            payloads = replay8001.getError() + MyTools.addZeroForNum(Integer.toHexString(replay8001.getAtr().length() / 2), 2) + replay8001.getAtr();
                        } else {
                            payloads = replay8001.getError() + Integer.toHexString(replay8001.getAtr().length() / 2) + replay8001.getAtr();
                        }
                        replay8001.setPayloads(payloads);
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("8" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                    }

                } else if (cmd8001.getAction().equals("02"))//下电
                {
                    mylog.Write("下电指令");
                    if (psam.close()) {
                        mylog.Write("下电指令完成");
                    } else {
                        mylog.Write("下电指令执行失败.");
                    }
                    if (basecmd.getLength().startsWith("7"))//加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setSequence(basecmd.getSequence());
                        replay8001.setAtr("");
                        replay8001.setPayloads(replay8001.getError() + "00");//atr L1-V为空
                        mylog.Write("下电加密前payloads=" + replay8001.getError() + "00");//atr L1-V为空
                        String mPayloads = DESHelper.encryptStr(replay8001.getPayloads(), skey);
                        String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                        String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                        mPayloads = mPayloads + MAC;
                        replay8001.setPayloads(mPayloads);
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("F" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                    } else//不加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setSequence(basecmd.getSequence());
                        replay8001.setAtr("");
                        replay8001.setPayloads(replay8001.getError() + "00");//atr L1-V为空
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("8" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                        setTv_connectstate("提交安全令牌验证完成,等待通讯组件命令...");
                        //tv_connectstate.setText("提交安全令牌验证完成,等待通讯组件命令...");
                    }

                } else {
                    throw new Exception("8001命令解析失败！");
                }

                break;
            case 8002:
                mylog.Write("收到8002指令:" + cmddata);
                Cmd8002 cmd8002 = new Cmd8002(basecmd, skey, mkey);
                mylog.Write("APDU指令:" + cmd8002.getCAPDU());
                String result8002 = null;
                String rapdu = "";
                if (cmd8002.getCAPDU().equals("8010020000"))//证书第一部分
                {
                    result8002 = psam.getPSAMCertifiPart1();
                    if (result8002 != null) {
                        mylog.Write("8002证书第一部分：" + result8002);
                        rapdu = result8002;
                    } else {
                        mylog.Write("8002证书第一部分执行失败");
                    }
                } else if (cmd8002.getCAPDU().equals("8010020100"))//证书第二部分
                {
                    result8002 = psam.getPSAMCertifiPart2();
                    if (result8002 != null) {
                        mylog.Write("8002证书第二部分：" + result8002);
                        rapdu = result8002;
                    } else {
                        mylog.Write("8002证书第二部分执行失败");
                    }
                } else if (cmd8002.getCAPDU().startsWith("8023"))//在此处获取key，实际环境测试
                {
                    String cmdstr = cmd8002.getCAPDU();
                    rapdu = psam.excute(cmdstr);
                    if (rapdu.endsWith("9000"))//获取成功
                    {
                        if (rapdu.startsWith("01")) {
                            MyApp.handlertype = 1;
                            mylog.Write("获取到密文格式handlertype=0" + MyApp.handlertype);
                        } else {
                            MyApp.handlertype = 2;
                            mylog.Write("获取到密文格式handlertype=0" + MyApp.handlertype);
                        }
                        session = rapdu.substring(2, 4);
                        skey = rapdu.substring(4, 36);
                        mkey = rapdu.substring(36, 68);
                        mylog.Write("获取到session=" + session);
                        mylog.Write("获取到skey=" + skey);
                        mylog.Write("获取到mkey=" + mkey);
                    }
                } else if (cmd8002.getCAPDU().startsWith("8032"))//SHA-1运算回复 控制数据处理
                {
                    //这里需要寻找最近的任务文件数据 id+timestamp+data(文件内容)+oper1+oper2
                    if (businessstate == 0)//领取任务状态
                    {
                        mylog.Write("APDU8032指令" + "业务状态:" + businessstate);
                        TXTReader tr = new TXTReader();
                        String data = tr.findLastFile(MainActivity.this, CmdDir);
                        if (!data.equals(""))//如果最近任务存在
                        {
                            String id = data.substring(0, 8);
                            String fragment = cmd8002.getCAPDU().substring(18, 26);
                            String timestamp = data.substring(8, 24);
                            data = data.substring(24);

                            //operator="00000000";
                            // auditor="00000000";
                            mylog.Write("sha1源数据=" + id + fragment + timestamp + new L4vtype(data).toString() + "00000000" + "00000000");
                            String sha1 = SHA1Util.shaEncode(Tools.HexString2Bytes(id + fragment + timestamp + new L4vtype(data).toString() + "00000000" + "00000000"));
                            mylog.Write("sha1=" + sha1);
                            String cmdstr = cmd8002.getCAPDU().substring(0, 34) + sha1;
                            mylog.Write("cmdstr=" + cmdstr);
                            rapdu = psam.excute(cmdstr);
                        } else {
                            throw new Exception("任务不存在！无法完成控制数据处理");
                        }
                    } else if (businessstate == 1)//提交任务状态
                    {
                        mylog.Write("APDU8032指令" + "业务状态:" + businessstate);
                        TXTReader tr = new TXTReader();
                        String data = tr.findLastFile(MainActivity.this, DataDir);
                        if (!data.equals(""))//如果最近任务存在
                        {
                            String id = data.substring(0, 8);
                            String fragment = cmd8002.getCAPDU().substring(18, 26);//fragment
                            String timestamp = data.substring(8, 24);
                            data = data.substring(24);
                            int oper = Integer.parseInt(operator);
                            int aduit = Integer.parseInt(auditor);
                            String operhex = Tools.Bytes2HexString(HexUtil.intToBytes(oper), HexUtil.intToBytes(oper).length);
                            String aduithex = Tools.Bytes2HexString(HexUtil.intToBytes(aduit), HexUtil.intToBytes(aduit).length);
                            mylog.Write("sha1源数据=" + id + fragment + timestamp + new L4vtype(data).toString() + operhex + aduithex);
                            String sha1 = SHA1Util.shaEncode(Tools.HexString2Bytes(id + fragment + timestamp + new L4vtype(data).toString() + operhex + aduithex));
                            mylog.Write("sha1=" + sha1);
                            String cmdstr = cmd8002.getCAPDU().substring(0, 34) + sha1;
                            mylog.Write("cmdstr=" + cmdstr);
                            rapdu = psam.excute(cmdstr);
                        } else {
                            throw new Exception("任务不存在！无法完成控制数据处理");
                        }
                    }
                } else//其他命令直接执行返回
                {
                    String cmdstr = cmd8002.getCAPDU();
                    rapdu = psam.excute(cmdstr);
                }
                //回复通讯组件
                if (basecmd.getLength().startsWith("7"))//加密回复
                {
                    Replay8002 replay8002 = new Replay8002();
                    replay8002.setrAPDU(rapdu);
                    replay8002.setSequence(basecmd.getSequence());
                    replay8002.setPayloads(replay8002.getError() + new L4vtype(replay8002.getrAPDU()).toString());
                    String mPayloads = DESHelper.encryptStr(replay8002.getPayloads(), skey);
                    String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                    String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                    mPayloads = mPayloads + MAC;
                    replay8002.setPayloads(mPayloads);
                    int len = replay8002.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay8002.setLength("F" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay8002.toDataString().toUpperCase();
                    ;
                    mylog.Write("reply8002=" + reply8002data);
                    mylog.Write("reply8002长度检查=" + replay8002.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply8002完成");

                } else//不加密回复
                {
                    Replay8002 replay8002 = new Replay8002();
                    replay8002.setrAPDU(rapdu);
                    replay8002.setSequence(basecmd.getSequence());
                    replay8002.setPayloads(replay8002.getError() + new L4vtype(replay8002.getrAPDU()).toString());
                    int len = replay8002.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay8002.setLength("8" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay8002.toDataString();
                    mylog.Write("reply8002=" + reply8002data);
                    mylog.Write("reply8002长度检查=" + replay8002.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply8002完成");
                }
                setTv_connectstate("提交8002进行安全令牌验证,等待通讯组件命令...");
                // tv_connectstate.setText("提交8002进行安全令牌验证,等待通讯组件命令...");
                break;
            case 1004:
                mylog.Write("收到1004指令:" + cmddata);
                setTv_connectstate("提交8002进行安全令牌验证,等待通讯组件命令...");
                //  tv_connectstate.setText("正在进行检查环境....");
                Cmd1004 cmd1004 = new Cmd1004(basecmd, skey);
                if (cmd1004.getMessage().trim() != "") {
                    setTv_connectstate("检查环境失败,原因"+cmd1004.getMessage());
                    //  tv_connectstate.setText("检查环境失败,详细请查看日志...");

                }
                mylog.Write("1004指令信息:" + cmd1004.getMessage());
                String replay1004data = "80000004" + basecmd.getSequence() + "100400000000";
                socketClient.sendData(Tools.HexString2Bytes(replay1004data));
                mylog.Write("reply1004=" + replay1004data);
                mylog.Write("发送reply1004完成");
                setTv_connectstate("提交检查环境完成,等待通讯组件命令...");
                // tv_connectstate.setText("提交检查环境完成,等待通讯组件命令...");
                break;
            case 1005://同步时间
                mylog.Write("收到1005指令:" + cmddata);

                Cmd1005 cmd1005 = new Cmd1005(basecmd, skey, mkey);
                //   DevSettings dev = new DevSettings(MainActivity.this);
                DateTimeUtil dateTimeUtil = new DateTimeUtil();
                Date serverdt = new Date(cmd1005.getDatetime());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                mylog.Write("服务器时间为:" + sdf.format(serverdt));
                mylog.Write("同步服务器时间结果:" + dateTimeUtil.setSystemTime(MainActivity.this, sdf.format(serverdt)));

                String replay1005data = "80000004" + basecmd.getSequence() + "100500000000";
                socketClient.sendData(Tools.HexString2Bytes(replay1005data));
                mylog.Write("reply1005=" + replay1005data);
                mylog.Write("发送reply1005完成");
                setTv_connectstate("进行时间同步完成,等待通讯组件命令...");
                //tv_connectstate.setText("进行时间同步完成,等待通讯组件命令...");
                break;
            case 2002://接收任务数据
                mylog.Write("收到2002指令:" + cmddata);
                setTv_connectstate("正在进行接收任务数据....");
                //tv_connectstate.setText("正在进行接收任务数据....");
                Cmd2002 cmd2002 = new Cmd2002(basecmd, skey, mkey);
                String cmdjason = new String(Tools.HexString2Bytes(cmd2002.getData().substring(8)), "utf-8");
                TXTWriter.writeBinFile(CmdDirF + "/" + "CMD_" + cmd2002.getId() + "_" + cmd2002.getTimestamp() + ".json", cmdjason.getBytes());
                mylog.Write("生成任务文件=" + "CMD_" + cmd2002.getId() + "_" + cmd2002.getTimestamp() + ".json");
                mylog.Write("写入任务文件内容=" + cmdjason);
                Date date = new Date(Long.parseLong(cmd2002.getTimestamp(), 16));//时间戳计算
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
                mylog.Write("任务时间戳=" + sdf1.format(date));
                String replay2002data = "80000004" + basecmd.getSequence() + "200200000000";
                socketClient.sendData(Tools.HexString2Bytes(replay2002data));
                mylog.Write("reply2002=" + replay2002data);
                // mylog.Write("reply1004长度检查="+replay8002.checkLenth());
                mylog.Write("发送reply2002完成");
                setTv_connectstate("接收任务数据完成,等待通讯组件开始命令...");
                // tv_connectstate.setText("接收任务数据完成,等待通讯组件开始命令...");
                break;
            case 2003:
                mylog.Write("收到2003指令:" + cmddata);
                Cmd2003 cmd2003 = new Cmd2003(basecmd, skey, mkey);
                mylog.Write("要求执行ID为" + cmd2003.getId() + "的任务");
                String cmd2003payloads = "00000000";
                String cmd2003mPayloads = DESHelper.encryptStr(cmd2003payloads, skey);
                String cmd2003allMAC = DESHelper.encryptStr(cmd2003mPayloads, mkey);
                String cmd2003MAC = cmd2003allMAC.substring(cmd2003allMAC.length() - 16, cmd2003allMAC.length() - 8);
                cmd2003mPayloads = cmd2003mPayloads + cmd2003MAC;
                String replay2003data = "7000000C" + basecmd.getSequence() + "2003" + cmd2003mPayloads;
                socketClient.sendData(Tools.HexString2Bytes(replay2003data));
                mylog.Write("replay2003data=" + replay2003data);
                mylog.Write("发送replay2003完成");
                setTv_connectstate("收到执行任务命令,任务id:" + cmd2003.getId());
                //  tv_connectstate.setText("收到执行任务命令,任务id:" + cmd2003.getId());
                //先不等待
//                if (socketClient != null) {
//                    socketClient.disconnect();
//                    mylog.Write("正在准备执行任务..."+cmd2003.getId()+"通讯组件已断开");
//                }
                //这里开始执行业务任务
                doBusiness(cmd2003.getId());
                break;
            case 2004:
                mylog.Write("收到2004指令:" + cmddata);
                Cmd2004 cmd2004 = new Cmd2004(basecmd, skey, mkey);
                mylog.Write("收到2004指令" + cmd2004.getId() + "任务已经" + cmd2004.getComplete());
                //这里可以删除任务及数据文件
                if (cmd2004.getComplete().equals("8000"))//要求删除
                {
                    TXTReader tr = new TXTReader();
                    boolean rs = tr.delCmdDataFile(MainActivity.this, cmd2004.getId());
                    mylog.Write("删除任务=" + cmd2004.getId() + "的任务与结果文件" + rs);
                } else {
                    mylog.Write("任务未完成,不要求删除任务文件");
                }
                if (cmd2004.getComplete().equals("0000")) {
                    businessstate = 0;//提交完成,修改业务状态为等待任务
                    setTv_connectstate(cmd2004.getId() + "业务已完成...");
                    // tv_connectstate.setText(cmd2004.getId() + "业务已完成...");

                    mylog.Write(cmd2004.getId() + "业务已完成...");
                    mylog.Write("businessstate=" + businessstate);
                }
                //回复通讯组件
                if (basecmd.getLength().startsWith("7"))//加密回复
                {
                    Replay2004 replay2004 = new Replay2004();
                    replay2004.setSequence(basecmd.getSequence());
                    replay2004.setPayloads("00000000");//error
                    String mPayloads = DESHelper.encryptStr(replay2004.getPayloads(), skey);
                    String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                    String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                    mPayloads = mPayloads + MAC;
                    replay2004.setPayloads(mPayloads);
                    int len = replay2004.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay2004.setLength("F" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay2004.toDataString().toUpperCase();
                    mylog.Write("reply2004=" + reply8002data);
                    mylog.Write("reply2004长度检查=" + replay2004.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply2004完成");

                } else//不加密回复
                {
                    Replay2004 replay2004 = new Replay2004();
                    replay2004.setSequence(basecmd.getSequence());
                    replay2004.setPayloads("00000000");//error
                    int len = replay2004.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay2004.setLength("8" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay2004.toDataString().toUpperCase();
                    mylog.Write("reply2004=" + reply8002data);
                    mylog.Write("reply2004长度检查=" + replay2004.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply2004完成");
                }
                break;
            case 2005://元数据同步，获取用户
                mylog.Write("收到2005指令:" + cmddata);
                businessstate = 0;//元数据同步完成,修改业务状态为等待
                Cmd2005 cmd2005 = new Cmd2005(basecmd, skey, mkey);
                String usercmdstr = new String(Tools.HexString2Bytes(cmd2005.getMetadata()), "utf-8");
                mylog.Write("收到2005用户数据:" + usercmdstr);
                UserCommand userCommand = JSONObject.parseObject(usercmdstr, UserCommand.class);
                setTv_connectstate("正在元数据同步...");
                //tv_connectstate.setText("正在元数据同步...");
                if (userCommand.getUserInfoList().length > 0)//如果存在用户数据
                {
                    mylog.Write("用户数量:" + userCommand.getUserInfoList().length);
                    userMapper.delete();
                    for (User user : userCommand.getUserInfoList()) {
                        userMapper.insert(user);
                        mylog.Write("存储用户数据:" + user.getUid());

                    }
                }
                setTv_connectstate("正在元数据同步完成...");
                //tv_connectstate.setText("正在元数据同步完成...");
                sequence = basecmd.getSequence();
                String replay2005data = "80000004" + basecmd.getSequence() + "200500000000";
                socketClient.sendData(Tools.HexString2Bytes(replay2005data));
                mylog.Write("reply2005=" + replay2005data);
                // mylog.Write("reply1004长度检查="+replay8002.checkLenth());
                mylog.Write("发送reply2005完成");
                setTv_connectstate("通讯组件已接入...");
                // tv_connectstate.setText("通讯组件已接入...");
                // tv_connectstate.setTextColor(getResources().getColor(R.color.DarkGreen));
                Util.playconnect();
                if (checkDatas()) {
                    try {
                        mylog.Write("显示提交数据提示框，准备提交数据....");
                        sendAutoUpdate();
                        //autoUpdata();
                    } catch (Exception e) {
                        setTv_connectstate("自动提交失败，请手动提交再试...");
                        // tv_connectstate.setText("自动提交失败，请手动提交再试...");
                        mylog.Write("自动提交失败，请手动提交再试..." + e.getMessage());
                    }
                }
                break;
        }
    }

    private boolean checkDatas() {
        TXTReader tr = new TXTReader();
        try {
            //  tr.findLastFile(this,DataDir);
            String data = tr.findLastFile(MainActivity.this, DataDir);
            //   mylog.Write("************结果数据:"+data.substring(0,8));
            String cmddata = tr.findLastFile(MainActivity.this, CmdDir);
            //    mylog.Write("************任务数据:"+cmddata.substring(0,8));

            if (data == null || cmddata == null) {
                showToast("没有任务需要提交!");
                return false;
            }
            int dataid = HexUtil.byteArrayToInt(HexUtil.hexStringToBytes(data.substring(0, 8)));
            int cmddataid = HexUtil.byteArrayToInt(HexUtil.hexStringToBytes(cmddata.substring(0, 8)));
            mylog.Write("结果数据id=" + dataid + "**业务任务id=" + cmddataid);

            if (cmddataid != dataid)//业务id与数据id不匹配
            {
                showToast("提交失败，请检查是否已有新业务下发");
                tv_connectstate.setText("错误:" + dataid + "号数据提交失败，请点击执行待办办理" + cmddataid + "号新业务");
                tv_connectstate.setTextColor(getResources().getColor(R.color.red));
                return false;
            }
            return true;
        } catch (Exception e) {
            mylog.Write("检查任务文件失败，原因:" + e.toString());
            return false;
        }
    }

    private void pasmError() {
        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setMessage("PSAM卡启动失败,请重启手持机再试...")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mylog.Write("PSAM启动失败，需要重启..");
                        System.exit(0);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        //修改 确定取消 按钮的字体大小
        builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
        builder.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(18);

        try {
            //获取mAlert对象
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(builder);

            //获取mTitleView并设置大小颜色
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextSize(18);
            mTitleView.setTextColor(getResources().getColor(R.color.DarkGreen));

            //获取mMessageView并设置大小颜色
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(getResources().getColor(R.color.Black));
            mMessageView.setTextSize(18);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void autoUpdata() {
//        AlertDialog.Builder autoUpdataDialog=new AlertDialog.Builder(this);
//
//        autoUpdataDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mHandler.sendEmptyMessage(7);//提交数据消息
//            }
//        });
//        autoUpdataDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        autoUpdataDialog.setMessage("当前有未提交的数据文件,是否提交？");
//        autoUpdataDialog.setTitle("提示");
//        autoUpdataDialog.show();

        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("提示:")
                .setMessage("当前有未提交的数据文件,是否提交？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.sendEmptyMessage(7);//提交数据消息
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        //修改 确定取消 按钮的字体大小
        builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
        builder.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(18);

        try {
            //获取mAlert对象
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(builder);

            //获取mTitleView并设置大小颜色
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextSize(18);
            mTitleView.setTextColor(getResources().getColor(R.color.DarkGreen));

            //获取mMessageView并设置大小颜色
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(getResources().getColor(R.color.Black));
            mMessageView.setTextSize(18);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void doBusiness(String businessid) {
        if (operator.equals("00000000")) {
            showToast("操作员未登录，不可办理业务！请操作员登录...");
            return;
        }
        mylog.Write("开始办理业务ID=" + businessid);
        //首先删除所有data文件
        Util.playSound(1, 0);//收到业务开始提醒
        TXTReader tr = new TXTReader();
        boolean deldatars = tr.delDataFile(this);
        mylog.Write("开始清除历史数据文件=" + deldatars);

        List<String> oldfilenames = tr.clearCmdFile(this);
        if (oldfilenames.size() > 0) {
            for (String s : oldfilenames) {
                mylog.Write("清除过期任务文件" + s);
            }
        }
        String cmd = tr.getCmdById(MainActivity.this, businessid);
        mylog.Write("查找到业务数据=" + cmd);
        JSONObject jsonObject = JSONObject.parseObject(cmd);
        int cmdcode = jsonObject.getIntValue("code");
        mylog.Write("查找到业务数据code=" + cmdcode);
        //主动断开通讯组件
        closeGameSocket();

        MyApp.ScanLock = false;//设置所有业务不锁定
        switch (cmdcode) {
            case 131073://电子签封登记业务
                if (jsonObject.containsKey("packInfoList"))//这是1.04以上版本协议
                {
                    Intent superscanintent104 = new Intent(MainActivity.this, SuperScanActivty104.class);
                    superscanintent104.putExtra("operator", operator);
                    superscanintent104.putExtra("auditor", auditor);
                    superscanintent104.putExtra("cmddata", cmd);
                    superscanintent104.putExtra("businessid", businessid);
                    mylog.Write("启动电子签封登记业务界面,协议版本1.04+:");
                    startActivity(superscanintent104);
                } else if (jsonObject.containsKey("stackInfoList"))//这是1.03协议
                {

                    Intent superscanintent = new Intent(MainActivity.this, SuperScanActivty.class);
                    superscanintent.putExtra("operator", operator);
                    superscanintent.putExtra("auditor", auditor);
                    superscanintent.putExtra("cmddata", cmd);
                    superscanintent.putExtra("businessid", businessid);
                    mylog.Write("启动电子签封登记业务界面,协议版本1.03:");
                    startActivity(superscanintent);
                }
                break;
            case 131074://扫描出库业务
                Intent outScanintent = new Intent(MainActivity.this, NewOutScanActivity.class);
                outScanintent.putExtra("operator", operator);
                outScanintent.putExtra("auditor", auditor);
                //  outScanintent.putExtra("cmddata", cmd);
                outScanintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封扫描出库界面:");
                startActivity(outScanintent);
                break;
            case 131075://扫描入库业务
                Intent enterScanintent = new Intent(MainActivity.this, NewEnterScanActivity.class);
                enterScanintent.putExtra("operator", operator);
                enterScanintent.putExtra("auditor", auditor);
                //  enterScanintent.putExtra("cmddata", cmd);
                enterScanintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封扫描入库界面:");
                startActivity(enterScanintent);
                break;
            case 131076://签封交接
                Intent transferintent = new Intent(MainActivity.this, NewTransferActivity.class);
                transferintent.putExtra("operator", operator);
                transferintent.putExtra("auditor", auditor);
                //   transferintent.putExtra("cmddata", cmd);
                transferintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封签封交接界面:");
                startActivity(transferintent);
                break;
            case 131077://签封分配
                MyApp.ScanLock = true;//拆封分配任务锁定扫描键
                Intent allotUnPackintent = new Intent(MainActivity.this, NewAllotUnPackActivity.class);
                allotUnPackintent.putExtra("operator", operator);
                allotUnPackintent.putExtra("auditor", auditor);
                //    allotUnPackintent.putExtra("cmddata", cmd);
                allotUnPackintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封签封核对界面:");
                startActivity(allotUnPackintent);
                break;
            case 131078://签封核对
                Intent dailyCheckintent = new Intent(MainActivity.this, NewDailyCheckActivity.class);
                dailyCheckintent.putExtra("operator", operator);
                dailyCheckintent.putExtra("auditor", auditor);
                //   dailyCheckintent.putExtra("cmddata", cmd);
                dailyCheckintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封签封核对界面:");
                startActivity(dailyCheckintent);
                break;
            case 131080://空电子签封入库
                Intent emptyEnterScanintent = new Intent(MainActivity.this, EmptyEnterScanActivity.class);
                emptyEnterScanintent.putExtra("operator", operator);
                emptyEnterScanintent.putExtra("auditor", auditor);
                //   emptyEnterScanintent.putExtra("cmddata", cmd);
                emptyEnterScanintent.putExtra("businessid", businessid);
                mylog.Write("启动空电子签封入库界面:");
                startActivity(emptyEnterScanintent);
                break;
            case 131081://签封入库核对
                Intent entercheckintent = new Intent(MainActivity.this, EnterCheckActivity.class);
                entercheckintent.putExtra("operator", operator);
                entercheckintent.putExtra("auditor", auditor);
                //   entercheckintent.putExtra("cmddata", cmd);
                entercheckintent.putExtra("businessid", businessid);
                mylog.Write("启动签封入库核对界面:");
                startActivity(entercheckintent);
                break;
            case 65537://封装业务
                MyApp.ScanLock = true;//封装业务锁定扫描键
                Intent packageintent = new Intent(MainActivity.this, PackageActivity.class);
                packageintent.putExtra("operator", operator);
                packageintent.putExtra("auditor", auditor);
                packageintent.putExtra("cmddata", cmd);
                packageintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封封装业务界面:");
                startActivity(packageintent);
                break;
            case 65538://拆封业务
                MyApp.ScanLock = true;//拆封业务锁定扫描键
                Intent unpackageintent = new Intent(MainActivity.this, UnPackageActivity.class);
                unpackageintent.putExtra("operator", operator);
                unpackageintent.putExtra("auditor", auditor);
                //   unpackageintent.putExtra("cmddata", cmd);
                unpackageintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封拆封业务界面:");
                startActivity(unpackageintent);
                break;
        }
    }

    //发送消息
    private void socketSendMessage(String info) {
        String status = String.valueOf(socketClient.getState());
        Log.e("kcrx", "status=" + status);
        if (socketClient != null && status == "Connected") {
            mylog.Write("send>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            socketClient.sendData(info.getBytes()); // 发送byte[]消息
        }
    }

    //前台请求socket连接状态
    private void requestGameSocketConnectFlg() {
        String status = String.valueOf(socketClient.getState());
        // launcher.callExternalInterface("gameSocketFlg", status);
    }

    //前台主动断开gamesocket
    private void closeGameSocket() {
        if (socketClient != null) {
            socketClient.removeSocketClientDelegate(delegate);
            socketClient.disconnect();
            mylog.Write("通讯组件socket已主动断开...");
        }

    }

    //    private void reConnect()
//    {
//        int retimes=20;
//        while(retimes>0) {
//            if (!netstate) {
//                mylog.Write("网络已断开，不再重试...");
//                return;
//            }
//            if (socketClient.isDisconnected()) {
//                try {
//                    tv_connectstate.setText("通讯组件已断开,稍后自动重试...");
//                    mylog.Write("通讯组件已断开,稍后自动重试...");
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                retimes--;
//                mylog.Write("通讯组件已断开，正在重试连接..." + retimes);
//                tv_connectstate.setText("通讯组件已断开，正在重试连接...");
//                connect();
//            } else
//            {
//                mylog.Write("通讯组件重新连接成功...");
//                return;
//            }
//        }
//        mylog.Write("通讯组件多次重新连接失败,请手动连接再试...");
//        tv_connectstate.setText("通讯组件多次重新连接失败,请手动连接再试...");
//    }
    //电池显示
    private void registerBattery() {
        registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void unregisterBattery() {
        unregisterReceiver(batteryChangedReceiver);
    }

    // 接受广播
    private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int power = level * 100 / scale;
                Log.d("Deom", "电池电量：:" + power);
                mBatteryView.setPower(power);
            }
        }
    };
    //两次退出返回登录
    private static final int TIME_EXIT = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_EXIT > System.currentTimeMillis()) {
            super.onBackPressed();
//            Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(loginintent);
            //这里清空业务数据，重新登录后可重新接入通讯组件
            TXTReader tr = new TXTReader();
            boolean b = tr.delDataFile(MainActivity.this);
            mylog.Write("主动退出登录,清除业务数据...");
            finish();
        } else {
            Toast.makeText(this, "再点击一次返回退出登录", Toast.LENGTH_SHORT).show();
            mBackPressed = System.currentTimeMillis();
        }
    }

    //show tips
    private Toast toast;

    private void showToast(String info) {
        if (toast == null) toast = Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT);
        else toast.setText(info);
        toast.show();
    }

    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((byte) chars[i]);
            } else {
                sbu.append((byte) chars[i]);
            }
        }
        return sbu.toString();
    }

    private void upDataCard() {
        try {
            Replay4003 replay4003 = new Replay4003();
            //   String aid=config.getValue("stackCode");
            String aid = "72203811010101";
            int productclass = MMKV.defaultMMKV().getInt("produckid", MyApp.DEFAULT_PRODUCKCLASS);
            if (productclass == 0)//当前为人行端
            {
                mylog.Write("当前为人行端，不提交刷卡信息");
                return;
            }
            mylog.Write("stackCode库间编号" + aid);
            UserMapper userMapper = new UserMapper(this);

            List<User> userList = userMapper.getUsers();
            if (userList.size() == 0) {
                mylog.Write("未同步用户信息，不提交刷卡信息");
                return;
            }
            // String users=config.getValue("users");//卡号冒号隔开
            String useruid = userList.get(0).getUid();
            mylog.Write("users用户UID信息" + useruid);
            int cardnums = Integer.valueOf(useruid);
            String cardnumhexs = Tools.Bytes2HexString(MyTools.intToByteArray(cardnums), MyTools.intToByteArray(cardnums).length);

            String payloads = new L4vtype(Tools.Bytes2HexString(aid.getBytes(), aid.getBytes().length))
                    + "0001"
                    + cardnumhexs;

            mylog.Write("4003加密前数据" + payloads);
            String mPayloads = DESHelper.encryptStr(payloads, skey);
            String allMAC = DESHelper.encryptStr(mPayloads, mkey);
            String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
            mPayloads = mPayloads + MAC;

            replay4003.setPayloads(mPayloads);

            int len = replay4003.getPayloads().length() / 2;
            String hex = Integer.toHexString(len).toUpperCase();
            replay4003.setLength("F" + MyTools.addZeroForNum(hex, 7));
            replay4003.setSequence(sequence);//
            String reply4002data = replay4003.toDataString().toUpperCase();
            mylog.Write("replay4003=" + reply4002data);
            mylog.Write("replay4003长度检查=" + replay4003.checkLenth());
            socketClient.sendData(Tools.HexString2Bytes(reply4002data));
            mylog.Write("发送replay4003刷卡信息");

        } catch (Exception e) {
            mylog.Write("4003提交刷卡信息失败：" + e.getMessage());
        }

    }

    @Override
    public void onNetDisconnected() {
        mylog.Write("新的网络检查网络已断开");
        btn_connect.setEnabled(false);
        btn_select.setEnabled(false);
        tv_connectstate.setText("网络已断开...");
        netstate = false;
        recontimes = 0;
        Util.playbreak();
    }

    @Override
    public void onNetConnected(NetworkType networkType) {
        mylog.Write("新的网络检查网络已接通:" + networkType.toString());
        //   Util.playconnect();
        btn_connect.setEnabled(true);
        btn_select.setEnabled(true);
        netstate = true;
        recontimes = 0;
        connect();
    }

    public void setTv_connectstate(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("tvmsg", msg);
        message.setData(bundle);
        message.what = 8;//tv消息
        mHandler.sendMessage(message);
    }

    public void sendAutoUpdate() {
        Message message = new Message();
        message.what = 9;//提交数据提示框消息
        mHandler.sendMessage(message);
    }

    public void sendTimeoutMessage() {
        Message message = new Message();
        message.what = 1;//延时消息
        mHandler.sendMessage(message);
    }
}
