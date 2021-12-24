package cn.kcrxorg.chengweiepms.businessmodule.datainfo;

public class EnterScanData extends BaseData {
    public enterScanPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(enterScanPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    private enterScanPackInfo[] packInfoList;

}
