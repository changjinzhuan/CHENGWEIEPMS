package cn.kcrxorg.chengweiepms.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.kcrxorg.chengweiepms.R;
import cn.kcrxorg.chengweiepms.adapter.ViewCmdInfoAdapter;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;

public class CmdInfoListView extends ViewGroup {
    LinearLayout line_total;
    RecyclerView rv_cmdinfo;
    ViewCmdInfoAdapter viewCmdInfoAdapter;
    private View fatherViewGroup;
    public CmdInfoListView(Context context, List<ViewCmdInfo> viewCmdInfoList) {
        super(context);
        inflate(context, R.layout.viewcmdinfo_list,this);
      //  fatherViewGroup=getChildAt(0);
        line_total=findViewById(R.id.line_viewcmdinfo_totoal);
        for(ViewCmdInfo v:viewCmdInfoList)
        {
            if(v.getPaperTypeName().equals(""))
            {
                line_total.setVisibility(View.GONE);
                break;
            }
        }
        rv_cmdinfo=findViewById(R.id.rv_cmdinfo);
        viewCmdInfoAdapter=new ViewCmdInfoAdapter(context,viewCmdInfoList);
        rv_cmdinfo.setLayoutManager(new LinearLayoutManager(context));
        rv_cmdinfo.setAdapter(viewCmdInfoAdapter);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        //  int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        //控件的高度
        int  screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        //测量子控件大小，必须调用，否则不显示页面内容
        // 计算出所有的childView的宽和高，调用后，它所有的childView的宽和高的值就被确定，也即getMeasuredWidth（）有值了。
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        //由于这里child只有一个所以将整个长宽都设置给child
        View child = this.getChildAt(0);
        child.layout(childLeft, childTop, width - getPaddingRight(), height - getPaddingBottom());
    }
}
