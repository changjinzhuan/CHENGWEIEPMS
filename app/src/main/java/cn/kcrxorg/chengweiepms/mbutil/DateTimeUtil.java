package cn.kcrxorg.chengweiepms.mbutil;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {
    //修改本地时间
    public boolean setSystemTime(Context context, String  time){
        if(time==null || time.isEmpty())return false;
        try{
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(time);
            c.setTime(date);

            int YEAR= c.get(Calendar.YEAR);    //获取年
            int MONTH= c.get(Calendar.MONTH);    //获取月
            int DAY_OF_MONTH=c.get(Calendar.DAY_OF_MONTH);    //获取日
            int HOUR_OF_DAY=c.get(Calendar.HOUR_OF_DAY);    //获取日
            int MINUTE=c.get(Calendar.MINUTE);    //获取时
            int SECOND=c.get(Calendar.SECOND);    //获取分
            int MILLISECOND=c.get(Calendar.MILLISECOND);    //获取秒
//            if(Debug.DEBUG) {
//                Debug.logD(TAG, "------------setSystemTime------------");
//                Debug.logD(TAG, "YEAR:" + YEAR);
//                Debug.logD(TAG, "MONTH:" + MONTH);
//                Debug.logD(TAG, "DAY_OF_MONTH:" + DAY_OF_MONTH);
//                Debug.logD(TAG, "HOUR_OF_DAY:" + HOUR_OF_DAY);
//                Debug.logD(TAG, "MINUTE:" + MINUTE);
//                Debug.logD(TAG, "SECOND:" + SECOND);
//                Debug.logD(TAG, "MILLISECOND:" + MILLISECOND);
//            }
            Intent intent = new Intent();
            intent.setAction("com.rscja.android.updateSystemTime");
            intent.putExtra("YEAR", YEAR);
            intent.putExtra("MONTH", MONTH);
            intent.putExtra("DAY_OF_MONTH", DAY_OF_MONTH);
            intent.putExtra("HOUR_OF_DAY", HOUR_OF_DAY);
            intent.putExtra("MINUTE", MINUTE);
            intent.putExtra("SECOND", SECOND);
            intent.putExtra("MILLISECOND", MILLISECOND);
            context.sendBroadcast(intent);
            return true;
        }catch (Exception ex){
            Log.e("DateTimeUtil","日期时间设置失败");
            return false;
        }
    }
}
