package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

import cn.kcrxorg.chengweiepms.businessmodule.cmdinfo.EmptyEnterScanPackInfo;

public class EmptyEnterScanData extends BaseData {

    public EmptyEnterScanPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(EmptyEnterScanPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    private EmptyEnterScanPackInfo[] packInfoList;
}
