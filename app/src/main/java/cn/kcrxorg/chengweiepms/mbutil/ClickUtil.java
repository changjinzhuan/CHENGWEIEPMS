package cn.kcrxorg.chengweiepms.mbutil;

import android.util.Log;

public class ClickUtil {
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        Log.e("test","检查快速点击="+flag);
        return flag;
    }
}
