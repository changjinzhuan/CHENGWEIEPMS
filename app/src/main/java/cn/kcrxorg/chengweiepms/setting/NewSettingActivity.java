package cn.kcrxorg.chengweiepms.setting;

import android.os.Bundle;
import android.view.Window;

import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xpage.core.PageOption;


public class NewSettingActivity extends XPageActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        PageOption.to(NewSettingFragment.class).open(this);
    }
}
