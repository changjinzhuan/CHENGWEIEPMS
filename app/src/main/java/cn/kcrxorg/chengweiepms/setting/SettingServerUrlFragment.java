package cn.kcrxorg.chengweiepms.setting;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.tencent.mmkv.MMKV;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.textview.supertextview.SuperButton;

import butterknife.BindView;
import cn.kcrxorg.chengweiepms.MyApp;
import cn.kcrxorg.chengweiepms.R;
import cn.kcrxorg.chengweiepms.mbutil.XToastUtils;


@Page(name = "服务器地址设置",anim = CoreAnim.fade)
public class SettingServerUrlFragment extends XPageFragment {


    @BindView(R.id.btn_savesetting)
    SuperButton btn_savesetting;
    @BindView(R.id.et_serverurl)
    EditText et_serverurl;
    @BindView(R.id.et_serverport)
    EditText et_serverport;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting_serverurl;
    }

    @Override
    protected void initViews() {
        et_serverurl.setText(MMKV.defaultMMKV().decodeString("serverurl", MyApp.DEFAULT_SERVER_URL));
        et_serverport.setText(MMKV.defaultMMKV().decodeString("serverport", MyApp.DEFAULT_SERVER_PORT));
    }

    @Override
    protected void initListeners() {
        et_serverurl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_savesetting.setEnabled(true);

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_serverport.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_savesetting.setEnabled(true);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        btn_savesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MMKV.defaultMMKV().encode("serverurl",et_serverurl.getText().toString());
                MMKV.defaultMMKV().encode("serverport",et_serverport.getText().toString());
                XToastUtils.success("保存设置成功");
                btn_savesetting.setEnabled(false);
            }
        });
    }
}
