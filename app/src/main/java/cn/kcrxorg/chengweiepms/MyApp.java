package cn.kcrxorg.chengweiepms;

import android.app.Application;
import android.content.Context;

import com.tencent.mmkv.MMKV;
import com.xuexiang.xpage.PageConfig;
import com.xuexiang.xpage.PageConfiguration;
import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xpage.config.AppPageConfig;
import com.xuexiang.xpage.model.PageInfo;
import com.xuexiang.xui.XUI;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;

import java.util.List;


import cn.kcrxorg.chengweiepms.mbutil.CrashHandler;
import cn.kcrxorg.chengweiepms.mbutil.MyCrashHandler;
import cn.kcrxorg.chengweiepms.mbutil.XToastUtils;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

public class MyApp extends Application {

    public static final String DEFAULT_SERVER_URL="192.168.1.10";
    public static final String DEFAULT_SERVER_PORT="50002";
    public static final int DEFAULT_SCANPOWER=15;
    public static final int DEFAULT_PRODUCKCLASS=0;
    public static boolean ScanLock=true;//扫描锁定开关
    public static int handlertype=0;
    public static boolean isOpLock=false;
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        initMMKV();
       initXPage();
       initXUpdate();

      //  MyCrashHandler handler = new MyCrashHandler();
      //  Thread.setDefaultUncaughtExceptionHandler(handler);
        context = getApplicationContext();
        XUI.init(this);

        CrashHandler.getInstance().init(context);
    }
    private void initMMKV() {
        String rootDir = MMKV.initialize(this);
    //    myLog.Write("初始化 mmkv root: " + rootDir);
    //    myLog.Write("获取到服务器地址:" + MMKV.defaultMMKV().getString("serverurl", DEFAULT_SERVER_URL));
    }
    private void initXPage() {
        PageConfig.getInstance()
                //页面注册
                .setPageConfiguration(new PageConfiguration() {
                    @Override
                    public List<PageInfo> registerPages(Context context) {
                        /*
                            自动注册页面,是编译时自动生成的，build一下就出来了。
                            如果你还没使用 @Page 的话，暂时是不会生成的。
                            另外注意注解编译器在 build.gradle 中的引入方式：
                            Java：
                                annotationProcessor 'com.github.xuexiangjys.XPage:xpage-compiler:3.0.0'
                            Kotlin：
                                kapt 'com.github.xuexiangjys.XPage:xpage-compiler:3.0.0'
                            引入方式不对应的话，编译时也不会自动生成的。
                         */
                        return AppPageConfig.getInstance().getPages();
                    }
                })
                //开启调试
                .debug("PageLog")
                //设置是否开启内存泄露监测
                //设置默认的容器Activity
                .setContainActivityClazz(XPageActivity.class)
                //初始化页面配置
                .init(this);
    }

    private void initXUpdate()
    {
        XUpdate.get()
                .debug(true)
                .isWifiOnly(true)                                               //默认设置只在wifi下检查版本更新
                .isGet(true)                                                    //默认设置使用get请求检查版本
                .isAutoMode(false)                                              //默认设置非自动模式，可根据具体使用配置
                .param("versionCode", UpdateUtils.getVersionCode(this))         //设置默认公共请求参数
                .param("appKey", getPackageName())
                .setOnUpdateFailureListener(new OnUpdateFailureListener() {     //设置版本更新出错的监听
                    @Override
                    public void onFailure(UpdateError error) {
                      //  Log.e("kcrx",+" "+error.getMessage());
                        if (error.getCode() != CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                            XToastUtils.error(error.getDetailMsg());
                        }else
                        {
                            XToastUtils.info(error.getMessage());
                        }

                    }
                })
                .supportSilentInstall(true)                                     //设置是否支持静默安装，默认是true
                .setIUpdateHttpService(new OKHttpUpdateHttpService())           //这个必须设置！实现网络请求功能。
                .init(this);                                                    //这个必须初始化
    }

    public static Context getContext() {
        return context;
    }
}
