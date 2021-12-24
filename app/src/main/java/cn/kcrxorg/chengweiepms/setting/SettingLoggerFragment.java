package cn.kcrxorg.chengweiepms.setting;

import android.view.View;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xui.widget.textview.LoggerTextView;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.kcrxorg.chengweiepms.R;
import cn.kcrxorg.chengweiepms.mbutil.TXTReader;

@Page(name = "日志查看")
public class SettingLoggerFragment extends XPageFragment {
    private static final String LogDir = "/Log/";
    @BindView(R.id.logger)
    LoggerTextView logger;

    @Override
    protected int getLayoutId() {
        return R.layout.frament_setting_logger;
    }

    @Override
    protected void initViews() {


    }

    @Override
    protected void initListeners() {

    }
    protected void setLog()
    {
        File logRoot = getContext().getFilesDir();
        File logDir = new File(logRoot.getPath() + LogDir);

        //创建日志文件
        SimpleDateFormat mytime = new SimpleDateFormat("yyyy_MM_dd");
        Date curDate = new Date(System.currentTimeMillis());
        String mLogPath = logDir.getPath() + "/" + mytime.format(curDate) + ".log";

        File logFile = new File(mLogPath);

        if (logFile.exists()) {
            TXTReader tr = new TXTReader();
            List<String> stringList= tr.readLog(logFile);
          //  String[] logss=logs.split("\r\n");

            //        logger.logError("测试一下下");
            for(String s:stringList)
            {
                logger.logError(s.replace("*",""));
                //   Log.e("kcrx",s+"\r\n");
            }
        }
    }
    @OnClick(R.id.btn_normal)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_normal:
              //  logger.logNormal("这是一条普通日志！");
                setLog();
                break;
            default:
                break;
        }
    }
}
