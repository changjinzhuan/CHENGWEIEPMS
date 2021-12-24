package cn.kcrxorg.chengweiepms.setting;

import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.tencent.mmkv.MMKV;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;

import butterknife.BindView;
import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.R;


@Page(name = "软件类型设置",anim = CoreAnim.fade)
public class SettingProductClassFragment extends XPageFragment {
    @BindView(R.id.rb_cbank)
    RadioButton rb_cbank;
    @BindView(R.id.rb_bbank)
    RadioButton rb_bbank;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting_produckclass;
    }

    @Override
    protected void initViews() {
          int productid= MMKV.defaultMMKV().getInt("produckid",MyApp.DEFAULT_PRODUCKCLASS);
          switch (productid)
          {
              case 0:
                  rb_cbank.setChecked(true);
                  break;
              case 1:
                  rb_bbank.setChecked(true);
                  break;
              default:
                  break;
          }
    }

    @Override
    protected void initListeners() {
        rb_cbank.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if(buttonView.getId()==R.id.rb_cbank&&isChecked)
                 {
                     MMKV.defaultMMKV().putInt("produckid",0);
                 }
            }
        });
        rb_bbank.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.getId()==R.id.rb_bbank&&isChecked)
                {
                    MMKV.defaultMMKV().putInt("produckid",1);
                }
            }
        });
    }
}
