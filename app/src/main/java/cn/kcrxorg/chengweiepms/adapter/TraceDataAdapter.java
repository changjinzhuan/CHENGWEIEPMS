package cn.kcrxorg.chengweiepms.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.kcrxorg.chengweiepms.R;
import cn.kcrxorg.chengweiepms.bean.UserTraceData;


public class TraceDataAdapter extends BaseAdapter {
    List<UserTraceData> userTraceDataList;
    private Context context ;

    public TraceDataAdapter(Context context , List<UserTraceData> userTraceDataList){
        this.context = context ;
        this.userTraceDataList = userTraceDataList ;
    }
    @Override
    public int getCount() {
        Log.e("kcrx","adapter刷新数据"+userTraceDataList.size()+"条");
        return userTraceDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return userTraceDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try
        {
            TraceDataViewHolder holder;
            if(convertView==null)
            {
                holder=new TraceDataViewHolder();
                convertView= LayoutInflater.from(context).inflate(R.layout.sacktrace_items,null);
                holder.tv_cmdid=convertView.findViewById(R.id.tv_cmdid);
                holder.tv_operinfoone=convertView.findViewById(R.id.tv_operinfoone);
                holder.tv_operinfotwo=convertView.findViewById(R.id.tv_operinfotwo);
                holder.tv_datatime=convertView.findViewById(R.id.tv_datatime);
                convertView.setTag(holder);
            }else
            {
                holder = (TraceDataViewHolder) convertView.getTag();
            }

            if(userTraceDataList!=null&&userTraceDataList.size()!=0)
            {
                Log.e("kcrx",userTraceDataList.get(position).getOpdatetime()+"操作时间已变化");
                holder.tv_cmdid.setText(userTraceDataList.get(position).getCommandid());
                holder.tv_operinfoone.setText(userTraceDataList.get(position).getOperator1());
                holder.tv_operinfotwo.setText(userTraceDataList.get(position).getOperator2());
                holder.tv_datatime.setText(userTraceDataList.get(position).getOpdatetime());
            }
        }catch (Exception e)
        {

        }
        return convertView;
    }

    private class TraceDataViewHolder {
        TextView tv_cmdid;
        TextView tv_operinfoone;
        TextView tv_operinfotwo;
        TextView tv_datatime;
    }
}
