package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EnterCheckPackInfo;

public class EnterCheckData extends BaseData {
    EnterCheckPackInfo[] packInfoList;

    public EnterCheckPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(EnterCheckPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }
}
