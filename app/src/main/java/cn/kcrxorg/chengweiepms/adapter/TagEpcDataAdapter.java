/*
 * Copyright (C) 2021 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cn.kcrxorg.chengweiepms.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.xuexiang.xui.widget.textview.supertextview.SuperButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.kcrxorg.chengweiepms.R;
import cn.kcrxorg.chengweiepms.bean.TagEpcData;
import cn.kcrxorg.chengweiepms.rfidtool.PervalueHelper;


public class TagEpcDataAdapter extends RecyclerView.Adapter<TagEpcDataAdapter.VH> {

    private List<TagEpcData> tagEpcDataList;
    Context context;
    public TagEpcDataAdapter(Context context, List<TagEpcData> tagEpcDataList)
    {
        this.context=context;
        this.tagEpcDataList=tagEpcDataList;
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_tedepcdata,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        try {
            holder.tv_tagid.setText(tagEpcDataList.get(position).getTagid() + "");
            holder.tv_tagpervalue.setText(PervalueHelper.getVal(tagEpcDataList.get(position).getPervalueid()));

            int val=tagEpcDataList.get(position).getPervalueid();
            switch (val)
            {
                case 1:
                    holder.btn_tagcolor.setBackgroundColor(context.getResources().getColor(R.color.cash100));
                    break;
                case 2:
                    holder.btn_tagcolor.setBackgroundColor(context.getResources().getColor(R.color.cash50));
                    break;
                case 3:
                    holder.btn_tagcolor.setBackgroundColor(context.getResources().getColor(R.color.cash20));
                    break;
                case 4:
                    holder.btn_tagcolor.setBackgroundColor(context.getResources().getColor(R.color.cash10));
                    break;
                case 5:
                    holder.btn_tagcolor.setBackgroundColor(context.getResources().getColor(R.color.cash5));
                    break;
                case 6:
                    holder.btn_tagcolor.setBackgroundColor(context.getResources().getColor(R.color.cash1));
                    break;
                default:
                    break;
            }
            String lockstate=tagEpcDataList.get(position).getLockstuts();
            if(lockstate.equals("Lock"))
            {
                holder.tv_lockstate.setText("关锁");
                holder.tv_lockstate.setTextColor(context.getResources().getColor(R.color.xui_btn_green_select_color));
            }else if(lockstate.equals("unLock"))
            {
                holder.tv_lockstate.setText("开锁");
                holder.tv_lockstate.setTextColor(context.getResources().getColor(R.color.xui_config_color_red));
            }else if(lockstate.equals("unKnown"))
            {
                holder.tv_lockstate.setText("非法");
                holder.tv_lockstate.setTextColor(context.getResources().getColor(R.color.xui_config_color_red));
            }else
            {
                holder.tv_lockstate.setText("未知");
                holder.tv_lockstate.setTextColor(context.getResources().getColor(R.color.xui_config_color_red));
            }

            


        }catch (Exception e)
        {
            Log.e("kcrx",e.toString(),e);
        }

    }
    @Override
    public int getItemCount() {
        return tagEpcDataList.size();
    }

     class VH extends RecyclerView.ViewHolder {
        SuperButton btn_tagcolor;
        TextView tv_tagid;
        TextView tv_tagpervalue;
        TextView tv_lockstate;
        public VH(@NonNull View itemView) {
            super(itemView);
            btn_tagcolor = itemView.findViewById(R.id.btn_tagcolor);
            tv_tagid = itemView.findViewById(R.id.tv_tagid);
            tv_tagpervalue = itemView.findViewById(R.id.tv_tagpervalue);
            tv_lockstate = itemView.findViewById(R.id.tv_lockstate);
        }
    }
}
