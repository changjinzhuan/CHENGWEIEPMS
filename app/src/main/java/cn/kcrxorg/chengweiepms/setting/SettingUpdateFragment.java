package cn.kcrxorg.chengweiepms.setting;

import android.widget.TextView;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xutil.app.AppUtils;

import butterknife.BindView;
import cn.kcrxorg.chengweiepms.R;


@Page(name = "版本升级",anim = CoreAnim.none)
public class SettingUpdateFragment extends XPageFragment {
    @BindView(R.id.tv_content)
    TextView tv_content;

    private String mUpdateUrl = "http://60.205.105.230:6999/chfs/shared/CHENGWEIEPMSRS/kcrxepmsrsupdate_api.json";
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting_update;
    }

    @Override
    protected void initViews() {
        tv_content.setText("当前版本号:V"+ AppUtils.getAppVersionName());

        XUpdate.newBuild(getContext())
                .updateUrl(mUpdateUrl)
                .promptThemeColor(ResUtils.getColor(R.color.colorPrimary))
                .promptButtonTextColor(ResUtils.getColor(R.color.TextWhite))
                .promptTopResId(R.drawable.update_bg_app_top)
                .promptWidthRatio(0.7F)
                .update();
    }

    @Override
    protected void initListeners() {

    }
}
