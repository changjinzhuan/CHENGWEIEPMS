package cn.kcrxorg.chengweiepms;

import android.os.Bundle;
import android.view.Window;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xpage.core.PageOption;
import com.xuexiang.xpage.enums.CoreAnim;


public class PackageCheckActivity extends XPageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        PageOption.to(PackageCheckFragment.class).open(this);
    }
}