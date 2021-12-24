package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

public class SuperScanData extends BaseData {
    public superScanPackinfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(superScanPackinfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    private superScanPackinfo[] packInfoList;
}
