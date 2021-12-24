package cn.kcrxorg.chengweiepms;

import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.locktools.LockUtil;
import cn.kcrxorg.chengweiepms.locktools.LockUtilRx;
import cn.kcrxorg.chengweiepms.psamtools.PsamTool;
import cn.kcrxorg.chengweiepms.rfidtool.EpcReader;
import cn.kcrxorg.chengweiepms.setting.NewSettingActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rscja.utility.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class ComActivity extends AppCompatActivity  implements View.OnClickListener {
    private final static String TAG = "ComActivity";

    private Button btn_Start;
    private Button btn_Clear;
    private Button btn_psamreset;

    private Button btn_rx_open;
    private Button btn_rx_close;

    private Button btn_packcheck_test;

    private EditText et_fasong;
    private TextView tv_jieshou;
    private CheckBox cbHex;

    private Handler handler;
    private Thread thread;
    private ScrollView svResult;

    LockUtil lockUtil;
    LockUtilRx lockUtilRx;
    String hexepc="";

    PsamTool psamTool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com);
        init();
        Util.initSoundPool(this);

        try {
            lockUtil=new LockUtil();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        btn_Start =  findViewById(R.id.btn_Start);
        btn_Clear =  findViewById(R.id.btn_Clear);
        btn_psamreset=findViewById(R.id.btn_psamreset);

        btn_rx_open=findViewById(R.id.btn_rx_open);
        btn_rx_close=findViewById(R.id.btn_rx_close);

        btn_packcheck_test=findViewById(R.id.btn_packcheck_test);

        et_fasong =  findViewById(R.id.et_fasong);
        svResult =  findViewById(R.id.scrollView1);
      //  cbHex =  findViewById(R.id.cbHex);
        tv_jieshou = findViewById(R.id.tv_jieshou);

        btn_Start.setOnClickListener(this);
        btn_Clear.setOnClickListener(this);
        btn_psamreset.setOnClickListener(this);
        btn_rx_open.setOnClickListener(this);
        btn_rx_close.setOnClickListener(this);
        btn_packcheck_test.setOnClickListener(this);

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg != null) {
                    switch (msg.what)
                    {

                    }

                }
            }

        };


    }

    @Override
    public void onClick(View v) {
         switch (v.getId())
         {
             case R.id.btn_Start:
                 long startTime = System.currentTimeMillis();
//                 try {
//                     lockUtil=new LockUtil();
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
                 if(lockUtil.onlyopLock(handler,"31323334","31323334",20000,true))
                 {
                     tv_jieshou.setText(tv_jieshou.getText()+"关锁成功\r\n");
                     Util.playOk();
                 }else
                 {
                     tv_jieshou.setText(tv_jieshou.getText()+"关锁失败\r\n");
                     Util.playErr();
                 }
                 long endTime = System.currentTimeMillis();
                 tv_jieshou.setText(tv_jieshou.getText()+"全部操作用时：" + ((float) (endTime - startTime) / 1000) + "秒\r\n");

                 break;
             case R.id.btn_psamreset:
                 long startTime2 = System.currentTimeMillis();
//                 try {
//                     lockUtil=new LockUtil();
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//                 lockUtil.openCOM();
                 String epcdata=lockUtil.readEpc().substring(12,60);
                 Log.e(TAG,"epcdata="+epcdata);
                 byte[] epcbyte=StringUtility.hexStringToBytes(epcdata);
                 hexepc=new String(epcbyte);
                 Log.e(TAG,"hexepc="+hexepc);
                 TagEpcData tagEpcData = EpcReader.readEpc(hexepc);
                 if(tagEpcData!=null)
                 {
                     tv_jieshou.setText(tv_jieshou.getText()+"读取成功,包号："+tagEpcData.getTagid()+" 锁状态:"+tagEpcData.getLockstuts()+" 异常状态:"+tagEpcData.getLockeEx()+" 工作状态:"+tagEpcData.getJobstuts());
                     Log.e(TAG,tv_jieshou.getText()+"读取成功,包号："+tagEpcData.getTagid()+" 锁状态:"+tagEpcData.getLockstuts()+" 异常状态:"+tagEpcData.getLockeEx()+" 工作状态:"+tagEpcData.getJobstuts());
                     Util.playOk();
                 }else
                 {
                     tv_jieshou.setText(tv_jieshou.getText()+"读取失败\r\n");
                     Util.playErr();
                 }
                 lockUtil.close();
                 long endTime2 = System.currentTimeMillis();
                 tv_jieshou.setText(tv_jieshou.getText()+"全部操作用时：" + ((float) (endTime2 - startTime2) / 1000) + "秒\r\n");

//                 Log.e(TAG,"4、获取psam证书part1.....................");
//                String psamCertifiPart1 = psamTool.getPSAMCertifiPart1();
//                Log.e(TAG,"psamCertifiPart1="+psamCertifiPart1);
//                 Log.e(TAG,"5、获取psam证书part2.....................");
//                 String psamCertifiPart2 = psamTool.getPSAMCertifiPart2();
//                 Log.e(TAG,"psamCertifiPart2="+psamCertifiPart2);

                // String epc="481F4EAF110000DF00627740";
//                 Log.e(TAG,"6、获取RFID密码.....................");
//                 String password= psamTool.getRFIDPassword(hexepc);
//                 Log.e(TAG,"password="+password);
                 break;
             case R.id.btn_Clear:
                 long startTime1 = System.currentTimeMillis();
//                 try {
//                     lockUtil=new LockUtil();
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
                 if(lockUtil.onlyopLock(handler,"31323334","31323334",0,false))
                 {
                     tv_jieshou.setText(tv_jieshou.getText()+"开锁成功\r\n");
                     Util.playOk();
                 }else
                 {
                     tv_jieshou.setText(tv_jieshou.getText()+"开锁失败\r\n");
                     Util.playErr();
                 }
                 long endTime1 = System.currentTimeMillis();
                 tv_jieshou.setText(tv_jieshou.getText()+"全部操作用时：" + ((float) (endTime1 - startTime1) / 1000) + "秒\r\n");
                 break;
             case R.id.btn_rx_open:
                 try {
                     LockUtilRx lockUtilRx=new LockUtilRx();
                     List taglist=new ArrayList<String>();
                     taglist.add("0000000000");
                     Observable<LockUtilRx.LockResult> observable = lockUtilRx.operateLockGetrs(taglist, "31313131", "32323232", 0, false);
                     observable.subscribeOn(Schedulers.io())
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe(new Observer<LockUtilRx.LockResult>() {
                         @Override
                         public void onSubscribe(@NonNull Disposable d) {
                             Log.e("test","onSubscribe");
                         }
                         @Override
                         public void onNext(LockUtilRx.LockResult lockResult) {
                             Log.e("test","onNext:"+lockResult.getMsg());
                             tv_jieshou.setText(tv_jieshou.getText()+lockResult.getMsg()+"成功\r\n");
                             Util.playOk();
                         }
                         @Override
                         public void onError(@NonNull Throwable e) {
                             Log.e("test","onError:"+e.getMessage());
                             tv_jieshou.setText(tv_jieshou.getText()+"锁操作失败\r\n");
                             Util.playErr();
                         }
                         @Override
                         public void onComplete() {
                             Log.e("test","onComplete");
                         }
                     });

                 } catch (Exception e) {
                     e.printStackTrace();
                 }

                 break;
             case R.id.btn_rx_close:
                 try {
                     LockUtilRx lockUtilRx=new LockUtilRx();
                     List taglist=new ArrayList<String>();
                     taglist.add("0000000000");
                     Observable<LockUtilRx.LockResult> observable = lockUtilRx.operateLockGetrs(taglist, "31313131", "32323232", 0, true);
                     observable.subscribeOn(Schedulers.io())
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe(new Observer<LockUtilRx.LockResult>() {
                                 @Override
                                 public void onSubscribe(@NonNull Disposable d) {
                                     Log.e("test","onSubscribe");
                                 }
                                 @Override
                                 public void onNext(LockUtilRx.LockResult lockResult) {
                                     Log.e("test","onNext:"+lockResult.getMsg());
                                     tv_jieshou.setText(tv_jieshou.getText()+lockResult.getMsg()+"成功\r\n");
                                     Util.playOk();
                                 }
                                 @Override
                                 public void onError(@NonNull Throwable e) {
                                     Log.e("test","onError:"+e.getMessage());
                                     tv_jieshou.setText(tv_jieshou.getText()+"锁操作失败\r\n");
                                     Util.playErr();
                                 }
                                 @Override
                                 public void onComplete() {
                                     Log.e("test","onComplete");
                                 }
                             });

                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 break;

             case R.id.btn_packcheck_test:
                 Intent packagecheckintent = new Intent(this, PackageCheckActivity.class);
                 startActivity(packagecheckintent);
                 break;
         }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(lockUtil!=null)
        {
            lockUtil.close();
        }
        if(psamTool!=null)
        {
            if(psamTool.close())
            {
                Log.e(TAG,"psamTool.close成功");
            }
        }
    }
}
