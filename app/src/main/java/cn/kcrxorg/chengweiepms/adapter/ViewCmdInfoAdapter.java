package cn.kcrxorg.chengweiepms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xuexiang.xui.utils.ColorUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import cn.kcrxorg.chengweiepms.R;
import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.ViewCmdInfo;

public class ViewCmdInfoAdapter extends RecyclerView.Adapter<ViewCmdInfoAdapter.ViewCmdInfoHolder> {

     Context context;
     List<ViewCmdInfo> viewCmdInfoList;
     public ViewCmdInfoAdapter(Context context, List<ViewCmdInfo> viewCmdInfoList)
     {
         this.context=context;
         this.viewCmdInfoList=viewCmdInfoList;
     }

    @NonNull
    @Override
    public ViewCmdInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewCmdInfoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardviewcmdinfo_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewCmdInfoHolder holder, int position) {

        try {
            holder.tv_sackno.setText(viewCmdInfoList.get(position).getSackNo());
            holder.tv_varchername.setText(viewCmdInfoList.get(position).getVoucherTypeName());
            holder.tv_papername.setText(viewCmdInfoList.get(position).getPaperTypeName());
            holder.tv_val.setText(viewCmdInfoList.get(position).getVal());
            Float var=Float.parseFloat(viewCmdInfoList.get(position).getVal());
            if(var==100)
            {
               // holder.iv_packagelogo.setImageBitmap(R.drawable.cash100);
                holder.iv_packagelogo.setImageResource(R.drawable.cash100);
            }else if(var==50)
            {
                holder.iv_packagelogo.setImageResource(R.drawable.cash50);
            }else if(var==20)
            {
                holder.iv_packagelogo.setImageResource(R.drawable.cash20);
            }else if(var==10)
            {
                holder.iv_packagelogo.setImageResource(R.drawable.cash10);
            }else if(var==5)
            {
                holder.iv_packagelogo.setImageResource(R.drawable.cash5);
            }else if(var==1)
            {
                holder.iv_packagelogo.setImageResource(R.drawable.cash1);
            }else
            {
               holder.iv_packagelogo.setImageResource(R.drawable.cblogo);
            }

            if(viewCmdInfoList.get(position).isDone)
            {
                holder.card_view.setCardBackgroundColor(context.getResources().getColor(R.color.xui_btn_green_normal_color));

            }else
            {
                holder.card_view.setCardBackgroundColor(context.getResources().getColor(R.color.menu_red_pressed));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return viewCmdInfoList.size();
    }


    class ViewCmdInfoHolder extends RecyclerView.ViewHolder {
        CardView card_view;
        TextView tv_sackno;
        TextView tv_varchername;
        TextView tv_papername;
        TextView tv_val;
        ImageView iv_packagelogo;

        public ViewCmdInfoHolder(@NonNull View itemView) {
            super(itemView);
            tv_sackno=itemView.findViewById(R.id.tv_sackno);
            tv_varchername=itemView.findViewById(R.id.tv_varchername);
            tv_papername=itemView.findViewById(R.id.tv_papername);
            tv_val=itemView.findViewById(R.id.tv_val);
            iv_packagelogo=itemView.findViewById(R.id.iv_packagelogo);
            card_view=itemView.findViewById(R.id.card_view);
        }
    }
}
